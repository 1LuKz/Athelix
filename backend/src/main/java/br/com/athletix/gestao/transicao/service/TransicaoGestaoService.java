package br.com.athletix.gestao.transicao.service;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.auth.service.InvalidacaoSessaoUsuarioService;
import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.cargo.repository.CargoRepository;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.departamento.repository.DepartamentoRepository;
import br.com.athletix.gestao.entity.Gestao;
import br.com.athletix.gestao.entity.StatusGestao;
import br.com.athletix.gestao.entity.VinculoGestao;
import br.com.athletix.gestao.repository.GestaoRepository;
import br.com.athletix.gestao.repository.VinculoGestaoRepository;
import br.com.athletix.gestao.transicao.dto.ItemTransicaoRequest;
import br.com.athletix.gestao.transicao.dto.PreviaTransicaoResponse;
import br.com.athletix.gestao.transicao.dto.TransicaoResponse;
import br.com.athletix.gestao.transicao.dto.TransicaoUpdateRequest;
import br.com.athletix.gestao.transicao.entity.ItemTransicaoGestao;
import br.com.athletix.gestao.transicao.entity.StatusTransicaoGestao;
import br.com.athletix.gestao.transicao.entity.TransicaoGestao;
import br.com.athletix.gestao.transicao.repository.ItemTransicaoGestaoRepository;
import br.com.athletix.gestao.transicao.repository.TransicaoGestaoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ResourceNotFoundException;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransicaoGestaoService {

  private final TransicaoGestaoRepository transicoes;
  private final ItemTransicaoGestaoRepository itens;
  private final GestaoRepository gestoes;
  private final VinculoGestaoRepository vinculos;
  private final MembroRepository membros;
  private final CargoRepository cargos;
  private final DepartamentoRepository departamentos;
  private final AtleticaRepository atleticas;
  private final AtleticaResourceResolver resolver;
  private final InvalidacaoSessaoUsuarioService sessoes;
  private final AtividadeUsuarioService auditoria;

  public TransicaoGestaoService(
      TransicaoGestaoRepository t,
      ItemTransicaoGestaoRepository i,
      GestaoRepository g,
      VinculoGestaoRepository v,
      MembroRepository m,
      CargoRepository c,
      DepartamentoRepository d,
      AtleticaRepository a,
      AtleticaResourceResolver r,
      InvalidacaoSessaoUsuarioService s,
      AtividadeUsuarioService u) {
    transicoes = t;
    itens = i;
    gestoes = g;
    vinculos = v;
    membros = m;
    cargos = c;
    departamentos = d;
    atleticas = a;
    resolver = r;
    sessoes = s;
    auditoria = u;
  }

  @Transactional
  public TransicaoResponse criar(Long destinoId, Membro autor) {
    Long a = resolver.atleticaIdAtual();
    Atletica atletica = atleticas.findByIdForUpdate(a).orElseThrow(ResourceNotFoundException::new);
    Gestao origem =
        gestoes
            .findByAtletica_IdAndStatus(a, StatusGestao.ATIVA)
            .orElseThrow(() -> new BusinessRuleException("Não há gestão ativa para transferir."));
    Gestao destino =
        gestoes.findByIdAndAtletica_Id(destinoId, a).orElseThrow(ResourceNotFoundException::new);
    if (destino.getStatus() != StatusGestao.PLANEJADA) {
      throw new BusinessRuleException("A gestão de destino deve estar planejada.");
    }
    TransicaoGestao t =
        transicoes.save(
            new TransicaoGestao(atletica, origem.getId(), destino.getId(), autor.getId()));
    membros
        .findAllByAtletica_IdAndStatusOrderByCursoAsc(a, StatusMembro.ATIVO)
        .forEach(
            m ->
                itens.save(
                    new ItemTransicaoGestao(
                        t,
                        m.getId(),
                        m.getCargoId(),
                        m.getDepartamentoId(),
                        m.getCargoId(),
                        m.getDepartamentoId(),
                        true,
                        null)));
    auditoria.registrar(
        "TRANSICAO_GESTAO_PREPARADA",
        "TRANSICAO_GESTAO",
        t.getId(),
        "Rascunho de transferência de gestão preparado.");
    return resposta(t);
  }

  @Transactional
  public TransicaoResponse atualizar(Long id, TransicaoUpdateRequest r, Membro autor) {
    TransicaoGestao t = buscarTravada(id);
    if (t.getStatus() != StatusTransicaoGestao.RASCUNHO) {
      throw new BusinessRuleException("A transição já foi concluída.");
    }
    if (!t.getVersion().equals(r.version())) {
      throw new br.com.athletix.shared.exception.ConcurrentModificationException();
    }
    Set<Long> vistos = new HashSet<>();
    for (ItemTransicaoRequest x : r.itens()) {
      if (!vistos.add(x.membroId())) {
        throw new BusinessRuleException("Um membro não pode aparecer duas vezes na transição.");
      }
      Membro m =
          membros
              .findByIdAndAtletica_Id(x.membroId(), resolver.atleticaIdAtual())
              .orElseThrow(ResourceNotFoundException::new);
      Cargo c =
          cargos
              .findByIdAndAtletica_Id(x.cargoDestinoId(), resolver.atleticaIdAtual())
              .orElseThrow(ResourceNotFoundException::new);
      Departamento d =
          departamentos
              .findByIdAndAtletica_Id(x.departamentoDestinoId(), resolver.atleticaIdAtual())
              .orElseThrow(ResourceNotFoundException::new);
      if (x.substituiResponsavelMembroId() != null) {
        membros
            .findByIdAndAtletica_Id(x.substituiResponsavelMembroId(), resolver.atleticaIdAtual())
            .orElseThrow(ResourceNotFoundException::new);
      }
      itens
          .findByTransicao_IdAndMembroId(id, m.getId())
          .ifPresentOrElse(
              i ->
                  i.atualizar(
                      c.getId(), d.getId(), x.continuaAtivo(), x.substituiResponsavelMembroId()),
              () ->
                  itens.save(
                      new ItemTransicaoGestao(
                          t,
                          m.getId(),
                          m.getCargoId(),
                          m.getDepartamentoId(),
                          c.getId(),
                          d.getId(),
                          x.continuaAtivo(),
                          x.substituiResponsavelMembroId())));
    }
    auditoria.registrar(
        "TRANSICAO_GESTAO_ATUALIZADA", "TRANSICAO_GESTAO", id, "Composição de destino atualizada.");
    return resposta(t);
  }

  @Transactional(readOnly = true)
  public PreviaTransicaoResponse previa(Long id) {
    TransicaoGestao t = buscar(id);
    List<ItemTransicaoGestao> xs = itens.findAllByTransicao_IdOrderByMembroIdAsc(id);
    long entra = xs.stream().filter(ItemTransicaoGestao::isContinuaAtivo).count(),
        sai = xs.stream().filter(x -> !x.isContinuaAtivo()).count(),
        subs = xs.stream().filter(x -> x.getSubstituiResponsavelMembroId() != null).count();
    Map<String, Long> p = new LinkedHashMap<>();
    p.put("contas", 0L);
    p.put("eventos", 0L);
    p.put("tarefas", 0L);
    p.put("emprestimos", 0L);
    p.put("manutencoes", 0L);
    return new PreviaTransicaoResponse(resposta(t), entra, sai, subs, p);
  }

  @Transactional
  public TransicaoResponse concluir(Long id, Long version, Membro autor) {
    Long a = resolver.atleticaIdAtual();
    atleticas.findByIdForUpdate(a).orElseThrow(ResourceNotFoundException::new);
    TransicaoGestao t = buscarTravada(id);
    if (!t.getVersion().equals(version)) {
      throw new br.com.athletix.shared.exception.ConcurrentModificationException();
    }
    if (t.getStatus() != StatusTransicaoGestao.RASCUNHO) {
      throw new BusinessRuleException("A transição já foi concluída.");
    }
    Gestao origem =
        gestoes
            .findByIdAndAtletica_Id(t.getGestaoOrigemId(), a)
            .orElseThrow(ResourceNotFoundException::new);
    Gestao destino =
        gestoes
            .findByIdAndAtletica_Id(t.getGestaoDestinoId(), a)
            .orElseThrow(ResourceNotFoundException::new);
    if (origem.getStatus() != StatusGestao.ATIVA || destino.getStatus() != StatusGestao.PLANEJADA) {
      throw new BusinessRuleException(
          "As gestões não estão em situação válida para a transferência.");
    }
    List<ItemTransicaoGestao> xs = itens.findAllByTransicao_IdOrderByMembroIdAsc(id);
    boolean admin =
        xs.stream()
            .anyMatch(
                x ->
                    x.isContinuaAtivo()
                        && cargos
                            .findByIdAndAtletica_Id(x.getCargoDestinoId(), a)
                            .map(
                                c ->
                                    c.getPerfilAcesso() == PerfilAcesso.ADMINISTRADOR
                                        && c.isAtivo())
                            .orElse(false));
    if (!admin) {
      throw new BusinessRuleException(
          "A composição de destino exige ao menos um administrador ativo.");
    }
    Instant agora = Instant.now();
    vinculos.findAllByGestao_IdAndFimVigenciaIsNull(origem.getId()).forEach(v -> v.encerrar(agora));
    for (ItemTransicaoGestao x : xs) {
      Membro m =
          membros
              .findByIdAndAtletica_Id(x.getMembroId(), a)
              .orElseThrow(ResourceNotFoundException::new);
      Cargo c =
          cargos
              .findByIdAndAtletica_Id(x.getCargoDestinoId(), a)
              .orElseThrow(ResourceNotFoundException::new);
      Departamento d =
          departamentos
              .findByIdAndAtletica_Id(x.getDepartamentoDestinoId(), a)
              .orElseThrow(ResourceNotFoundException::new);
      boolean mudou =
          !m.getCargoId().equals(c.getId())
              || !m.getDepartamentoId().equals(d.getId())
              || !x.isContinuaAtivo();
      if (x.isContinuaAtivo()) {
        m.atualizarVinculo(m.getCurso(), m.getPeriodoCurso(), c, d);
        vinculos.save(new VinculoGestao(m, destino, c, d, agora));
      } else if (m.getStatus() == StatusMembro.ATIVO) {
        m.inativar();
      }
      if (mudou) {
        sessoes.invalidarSessoesAnteriores(m.getUsuarioId());
      }
    }
    origem.encerrar(agora);
    destino.ativar();
    t.concluir(autor.getId());
    auditoria.registrar(
        "TRANSICAO_GESTAO_CONCLUIDA",
        "TRANSICAO_GESTAO",
        id,
        "Gestão encerrada e sucessora ativada com continuidade preservada.");
    return resposta(t);
  }

  private TransicaoGestao buscar(Long id) {
    return transicoes
        .findByIdAndAtletica_Id(id, resolver.atleticaIdAtual())
        .orElseThrow(ResourceNotFoundException::new);
  }

  private TransicaoGestao buscarTravada(Long id) {
    return transicoes
        .findByIdAndAtletica_IdForUpdate(id, resolver.atleticaIdAtual())
        .orElseThrow(ResourceNotFoundException::new);
  }

  private TransicaoResponse resposta(TransicaoGestao t) {
    return TransicaoResponse.from(t, itens.findAllByTransicao_IdOrderByMembroIdAsc(t.getId()));
  }
}
