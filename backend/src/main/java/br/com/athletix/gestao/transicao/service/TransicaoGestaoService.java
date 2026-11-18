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
      TransicaoGestaoRepository transicoes,
      ItemTransicaoGestaoRepository itens,
      GestaoRepository gestoes,
      VinculoGestaoRepository vinculos,
      MembroRepository membros,
      CargoRepository cargos,
      DepartamentoRepository departamentos,
      AtleticaRepository atleticas,
      AtleticaResourceResolver resolver,
      InvalidacaoSessaoUsuarioService sessoes,
      AtividadeUsuarioService auditoria) {
    this.transicoes = transicoes;
    this.itens = itens;
    this.gestoes = gestoes;
    this.vinculos = vinculos;
    this.membros = membros;
    this.cargos = cargos;
    this.departamentos = departamentos;
    this.atleticas = atleticas;
    this.resolver = resolver;
    this.sessoes = sessoes;
    this.auditoria = auditoria;
  }

  @Transactional
  public TransicaoResponse criar(Long destinoId, Membro autor) {
    Long atleticaId = resolver.atleticaIdAtual();
    Atletica atletica =
        atleticas.findByIdForUpdate(atleticaId).orElseThrow(ResourceNotFoundException::new);
    Gestao origem =
        gestoes
            .findByAtletica_IdAndStatus(atleticaId, StatusGestao.ATIVA)
            .orElseThrow(() -> new BusinessRuleException("Não há gestão ativa para transferir."));
    Gestao destino =
        gestoes
            .findByIdAndAtletica_Id(destinoId, atleticaId)
            .orElseThrow(ResourceNotFoundException::new);
    if (destino.getStatus() != StatusGestao.PLANEJADA) {
      throw new BusinessRuleException("A gestão de destino deve estar planejada.");
    }
    TransicaoGestao transicao =
        transicoes.save(
            new TransicaoGestao(atletica, origem.getId(), destino.getId(), autor.getId()));
    membros
        .findAllByAtletica_IdAndStatusOrderByCursoAsc(atleticaId, StatusMembro.ATIVO)
        .forEach(
            membro ->
                itens.save(
                    new ItemTransicaoGestao(
                        transicao,
                        membro.getId(),
                        membro.getCargoId(),
                        membro.getDepartamentoId(),
                        membro.getCargoId(),
                        membro.getDepartamentoId(),
                        true,
                        null)));
    auditoria.registrar(
        "TRANSICAO_GESTAO_PREPARADA",
        "TRANSICAO_GESTAO",
        transicao.getId(),
        "Rascunho de transferência de gestão preparado.");
    return resposta(transicao);
  }

  @Transactional
  public TransicaoResponse atualizar(
      Long transicaoId, TransicaoUpdateRequest requisicao, Membro autor) {
    TransicaoGestao transicao = buscarTravada(transicaoId);
    validarAtualizacao(transicao, requisicao);
    atualizarItensTransicao(transicao, transicaoId, requisicao.itens());
    registrarAtualizacao(transicaoId);
    return resposta(transicao);
  }

  private void validarAtualizacao(TransicaoGestao transicao, TransicaoUpdateRequest requisicao) {
    if (transicao.getStatus() != StatusTransicaoGestao.RASCUNHO) {
      throw new BusinessRuleException("A transição já foi concluída.");
    }
    if (!transicao.getVersion().equals(requisicao.version())) {
      throw new br.com.athletix.shared.exception.ConcurrentModificationException();
    }
  }

  private void atualizarItensTransicao(
      TransicaoGestao transicao, Long transicaoId, List<ItemTransicaoRequest> itensRequisicao) {
    Set<Long> vistos = new HashSet<>();
    for (ItemTransicaoRequest itemRequisicao : itensRequisicao) {
      if (!vistos.add(itemRequisicao.membroId())) {
        throw new BusinessRuleException("Um membro não pode aparecer duas vezes na transição.");
      }
      atualizarItemTransicao(transicao, transicaoId, itemRequisicao);
    }
  }

  private void atualizarItemTransicao(
      TransicaoGestao transicao, Long transicaoId, ItemTransicaoRequest itemRequisicao) {
    Membro membro = buscarMembro(itemRequisicao.membroId());
    Cargo cargo = buscarCargo(itemRequisicao.cargoDestinoId());
    Departamento departamento = buscarDepartamento(itemRequisicao.departamentoDestinoId());
    validarResponsavelSubstituto(itemRequisicao.substituiResponsavelMembroId());
    itens
        .findByTransicao_IdAndMembroId(transicaoId, membro.getId())
        .ifPresentOrElse(
            itemTransicao ->
                itemTransicao.atualizar(
                    cargo.getId(),
                    departamento.getId(),
                    itemRequisicao.continuaAtivo(),
                    itemRequisicao.substituiResponsavelMembroId()),
            () -> salvarItemTransicao(transicao, membro, cargo, departamento, itemRequisicao));
  }

  private void salvarItemTransicao(
      TransicaoGestao transicao,
      Membro membro,
      Cargo cargo,
      Departamento departamento,
      ItemTransicaoRequest itemRequisicao) {
    itens.save(
        new ItemTransicaoGestao(
            transicao,
            membro.getId(),
            membro.getCargoId(),
            membro.getDepartamentoId(),
            cargo.getId(),
            departamento.getId(),
            itemRequisicao.continuaAtivo(),
            itemRequisicao.substituiResponsavelMembroId()));
  }

  private void validarResponsavelSubstituto(Long membroId) {
    if (membroId != null) {
      buscarMembro(membroId);
    }
  }

  private void registrarAtualizacao(Long transicaoId) {
    auditoria.registrar(
        "TRANSICAO_GESTAO_ATUALIZADA",
        "TRANSICAO_GESTAO",
        transicaoId,
        "Composição de destino atualizada.");
  }

  @Transactional(readOnly = true)
  public PreviaTransicaoResponse previa(Long transicaoId) {
    TransicaoGestao transicao = buscar(transicaoId);
    List<ItemTransicaoGestao> itensTransicao =
        itens.findAllByTransicao_IdOrderByMembroIdAsc(transicaoId);
    long acessosEntram =
        itensTransicao.stream().filter(ItemTransicaoGestao::isContinuaAtivo).count();
    long acessosSaem = itensTransicao.stream().filter(item -> !item.isContinuaAtivo()).count();
    long responsaveisSubstituidos =
        itensTransicao.stream()
            .filter(item -> item.getSubstituiResponsavelMembroId() != null)
            .count();
    Map<String, Long> pendencias = pendenciasIniciais();
    return new PreviaTransicaoResponse(
        resposta(transicao), acessosEntram, acessosSaem, responsaveisSubstituidos, pendencias);
  }

  @Transactional
  public TransicaoResponse concluir(Long transicaoId, Long version, Membro autor) {
    Long atleticaId = resolver.atleticaIdAtual();
    atleticas.findByIdForUpdate(atleticaId).orElseThrow(ResourceNotFoundException::new);
    TransicaoGestao transicao = buscarTravada(transicaoId);
    validarConclusao(transicao, version);
    Gestao origem = buscarGestao(transicao.getGestaoOrigemId(), atleticaId);
    Gestao destino = buscarGestao(transicao.getGestaoDestinoId(), atleticaId);
    validarSituacaoDasGestoes(origem, destino);
    List<ItemTransicaoGestao> itensTransicao =
        itens.findAllByTransicao_IdOrderByMembroIdAsc(transicaoId);
    validarAdministradorAtivo(itensTransicao, atleticaId);
    Instant agora = Instant.now();
    encerrarVinculosDaOrigem(origem, agora);
    aplicarItensDaTransicao(itensTransicao, destino, atleticaId, agora);
    concluirGestoes(transicao, origem, destino, autor, agora);
    registrarConclusao(transicaoId);
    return resposta(transicao);
  }

  private void validarConclusao(TransicaoGestao transicao, Long version) {
    if (!transicao.getVersion().equals(version)) {
      throw new br.com.athletix.shared.exception.ConcurrentModificationException();
    }
    if (transicao.getStatus() != StatusTransicaoGestao.RASCUNHO) {
      throw new BusinessRuleException("A transição já foi concluída.");
    }
  }

  private Gestao buscarGestao(Long gestaoId, Long atleticaId) {
    return gestoes
        .findByIdAndAtletica_Id(gestaoId, atleticaId)
        .orElseThrow(ResourceNotFoundException::new);
  }

  private void validarSituacaoDasGestoes(Gestao origem, Gestao destino) {
    if (origem.getStatus() != StatusGestao.ATIVA || destino.getStatus() != StatusGestao.PLANEJADA) {
      throw new BusinessRuleException(
          "As gestões não estão em situação válida para a transferência.");
    }
  }

  private void validarAdministradorAtivo(
      List<ItemTransicaoGestao> itensTransicao, Long atleticaId) {
    boolean possuiAdministrador =
        itensTransicao.stream()
            .anyMatch(
                itemTransicao ->
                    itemTransicao.isContinuaAtivo()
                        && cargos
                            .findByIdAndAtletica_Id(itemTransicao.getCargoDestinoId(), atleticaId)
                            .map(
                                cargoDestino ->
                                    cargoDestino.getPerfilAcesso() == PerfilAcesso.ADMINISTRADOR
                                        && cargoDestino.isAtivo())
                            .orElse(false));
    if (!possuiAdministrador) {
      throw new BusinessRuleException(
          "A composição de destino exige ao menos um administrador ativo.");
    }
  }

  private void encerrarVinculosDaOrigem(Gestao origem, Instant agora) {
    vinculos
        .findAllByGestao_IdAndFimVigenciaIsNull(origem.getId())
        .forEach(vinculo -> vinculo.encerrar(agora));
  }

  private void aplicarItensDaTransicao(
      List<ItemTransicaoGestao> itensTransicao, Gestao destino, Long atleticaId, Instant agora) {
    for (ItemTransicaoGestao itemTransicao : itensTransicao) {
      aplicarItemDaTransicao(itemTransicao, destino, atleticaId, agora);
    }
  }

  private void aplicarItemDaTransicao(
      ItemTransicaoGestao itemTransicao, Gestao destino, Long atleticaId, Instant agora) {
    Membro membro = buscarMembro(itemTransicao.getMembroId(), atleticaId);
    Cargo cargo = buscarCargo(itemTransicao.getCargoDestinoId(), atleticaId);
    Departamento departamento =
        buscarDepartamento(itemTransicao.getDepartamentoDestinoId(), atleticaId);
    boolean vinculoMudou =
        !membro.getCargoId().equals(cargo.getId())
            || !membro.getDepartamentoId().equals(departamento.getId())
            || !itemTransicao.isContinuaAtivo();
    aplicarVinculoDoMembro(itemTransicao, membro, cargo, departamento, destino, agora);
    invalidarSessoesQuandoVinculoMudar(membro, vinculoMudou);
  }

  private void aplicarVinculoDoMembro(
      ItemTransicaoGestao itemTransicao,
      Membro membro,
      Cargo cargo,
      Departamento departamento,
      Gestao destino,
      Instant agora) {
    if (itemTransicao.isContinuaAtivo()) {
      membro.atualizarVinculo(membro.getCurso(), membro.getPeriodoCurso(), cargo, departamento);
      vinculos.save(new VinculoGestao(membro, destino, cargo, departamento, agora));
    } else if (membro.getStatus() == StatusMembro.ATIVO) {
      membro.inativar();
    }
  }

  private void invalidarSessoesQuandoVinculoMudar(Membro membro, boolean vinculoMudou) {
    if (vinculoMudou) {
      sessoes.invalidarSessoesAnteriores(membro.getUsuarioId());
    }
  }

  private void concluirGestoes(
      TransicaoGestao transicao, Gestao origem, Gestao destino, Membro autor, Instant agora) {
    origem.encerrar(agora);
    destino.ativar();
    transicao.concluir(autor.getId());
  }

  private void registrarConclusao(Long transicaoId) {
    auditoria.registrar(
        "TRANSICAO_GESTAO_CONCLUIDA",
        "TRANSICAO_GESTAO",
        transicaoId,
        "Gestão encerrada e sucessora ativada com continuidade preservada.");
  }

  private Membro buscarMembro(Long membroId) {
    return buscarMembro(membroId, resolver.atleticaIdAtual());
  }

  private Membro buscarMembro(Long membroId, Long atleticaId) {
    return membros
        .findByIdAndAtletica_Id(membroId, atleticaId)
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Cargo buscarCargo(Long cargoId) {
    return buscarCargo(cargoId, resolver.atleticaIdAtual());
  }

  private Cargo buscarCargo(Long cargoId, Long atleticaId) {
    return cargos
        .findByIdAndAtletica_Id(cargoId, atleticaId)
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Departamento buscarDepartamento(Long departamentoId) {
    return buscarDepartamento(departamentoId, resolver.atleticaIdAtual());
  }

  private Departamento buscarDepartamento(Long departamentoId, Long atleticaId) {
    return departamentos
        .findByIdAndAtletica_Id(departamentoId, atleticaId)
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Map<String, Long> pendenciasIniciais() {
    Map<String, Long> pendencias = new LinkedHashMap<>();
    pendencias.put("contas", 0L);
    pendencias.put("eventos", 0L);
    pendencias.put("tarefas", 0L);
    pendencias.put("emprestimos", 0L);
    pendencias.put("manutencoes", 0L);
    return pendencias;
  }

  private TransicaoGestao buscar(Long transicaoId) {
    return transicoes
        .findByIdAndAtletica_Id(transicaoId, resolver.atleticaIdAtual())
        .orElseThrow(ResourceNotFoundException::new);
  }

  private TransicaoGestao buscarTravada(Long transicaoId) {
    return transicoes
        .findByIdAndAtletica_IdForUpdate(transicaoId, resolver.atleticaIdAtual())
        .orElseThrow(ResourceNotFoundException::new);
  }

  private TransicaoResponse resposta(TransicaoGestao transicao) {
    return TransicaoResponse.from(
        transicao, itens.findAllByTransicao_IdOrderByMembroIdAsc(transicao.getId()));
  }
}
