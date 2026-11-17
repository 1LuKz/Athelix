package br.com.athletix.tarefa.service;

import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.departamento.entity.SituacaoDepartamento;
import br.com.athletix.departamento.repository.DepartamentoRepository;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.evento.service.EventoService;
import br.com.athletix.gestao.entity.StatusGestao;
import br.com.athletix.gestao.repository.GestaoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ConcurrentModificationException;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.pagination.PageResponse;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import br.com.athletix.shared.time.FusoHorarioAtletica;
import br.com.athletix.tarefa.dto.ChecklistEventoResponse;
import br.com.athletix.tarefa.dto.TarefaFiltro;
import br.com.athletix.tarefa.dto.TarefaRequests;
import br.com.athletix.tarefa.dto.TarefaResponse;
import br.com.athletix.tarefa.entity.StatusTarefa;
import br.com.athletix.tarefa.entity.Tarefa;
import br.com.athletix.tarefa.repository.TarefaRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TarefaService {
  private final TarefaRepository tarefas;
  private final EventoService eventos;
  private final AtleticaRepository atleticas;
  private final MembroRepository membros;
  private final DepartamentoRepository departamentos;
  private final GestaoRepository gestoes;
  private final AtleticaResourceResolver resolver;
  private final AtividadeUsuarioService auditoria;
  private final TarefaAutorizacaoService autorizacao;

  public TarefaService(
      TarefaRepository tarefas,
      EventoService eventos,
      AtleticaRepository atleticas,
      MembroRepository membros,
      DepartamentoRepository departamentos,
      GestaoRepository gestoes,
      AtleticaResourceResolver resolver,
      AtividadeUsuarioService auditoria,
      TarefaAutorizacaoService autorizacao) {
    this.tarefas = tarefas;
    this.eventos = eventos;
    this.atleticas = atleticas;
    this.membros = membros;
    this.departamentos = departamentos;
    this.gestoes = gestoes;
    this.resolver = resolver;
    this.auditoria = auditoria;
    this.autorizacao = autorizacao;
  }

  @Transactional
  public TarefaResponse criar(TarefaRequests.Create q, Membro autor) {
    Long atleticaId = resolver.atleticaIdAtual();
    Evento evento = eventos.buscarDaAtleticaAtual(q.eventoId());
    autorizacao.exigirGerenciar(autor, evento, q.departamentoId());
    var atletica = resolver.requireByIdAndAtleticaId(atleticaId, atleticas::findByIdAndId);
    Long gestaoId =
        gestoes
            .findByAtletica_IdAndStatus(atleticaId, StatusGestao.ATIVA)
            .map(g -> g.getId())
            .orElse(null);
    Tarefa t =
        tarefas.save(
            new Tarefa(
                atletica,
                gestaoId,
                evento,
                NomeNormalizado.displayValue(q.titulo()),
                NomeNormalizado.optionalDisplayValue(q.descricao()),
                resolverResponsavel(q.responsavelMembroId()),
                resolverDepartamento(q.departamentoId()),
                q.prioridade(),
                q.prazo(),
                autor.getId()));
    auditoria.registrar("TAREFA_CRIADA", "TAREFA", t.getId(), "Tarefa criada.");
    return TarefaResponse.from(t, hoje());
  }

  @Transactional(readOnly = true)
  public TarefaResponse buscar(Long id, Membro membro) {
    Tarefa t = carregar(id);
    if (!autorizacao.veTodas(membro) && !autorizacao.ehResponsavel(membro, t)) {
      throw new AccessDeniedException("Você só pode consultar as tarefas atribuídas a você.");
    }
    return TarefaResponse.from(t, hoje());
  }

  @Transactional(readOnly = true)
  public PageResponse<TarefaResponse> listar(TarefaFiltro f, Pageable pageable, Membro membro) {
    if (f.prazoDe() != null && f.prazoAte() != null && f.prazoAte().isBefore(f.prazoDe())) {
      throw new BusinessRuleException(
          "O fim do intervalo de prazo não pode ser anterior ao início.");
    }
    Long responsavelId = f.responsavelMembroId();
    if (!autorizacao.veTodas(membro)) {
      if (responsavelId != null && !responsavelId.equals(membro.getId())) {
        throw new AccessDeniedException("Você só pode consultar as tarefas atribuídas a você.");
      }
      responsavelId = membro.getId();
    }
    LocalDate hoje = hoje();
    return PageResponse.from(
        tarefas.findAll(especificacao(f, responsavelId, hoje), pageable),
        t -> TarefaResponse.from(t, hoje));
  }

  @Transactional
  public TarefaResponse alterar(Long id, TarefaRequests.Update q, Membro autor) {
    Tarefa t = carregar(id);
    autorizacao.exigirGerenciar(autor, t.getEvento(), t.getDepartamentoId());
    exigirVersao(t, q.version());

    boolean trocouResponsavel =
        !Objects.equals(q.responsavelMembroId(), t.getResponsavelMembroId());
    // o "ativo" só é exigido de quem está sendo designado agora
    Membro responsavel =
        trocouResponsavel ? resolverResponsavel(q.responsavelMembroId()) : t.getResponsavel();

    boolean trocouDepartamento = !Objects.equals(q.departamentoId(), t.getDepartamentoId());
    Departamento departamento = t.getDepartamento();
    if (trocouDepartamento) {
      autorizacao.exigirGerenciar(autor, t.getEvento(), q.departamentoId());
      departamento = resolverDepartamento(q.departamentoId());
    }

    t.atualizar(
        NomeNormalizado.displayValue(q.titulo()),
        NomeNormalizado.optionalDisplayValue(q.descricao()),
        responsavel,
        departamento,
        q.prioridade(),
        q.prazo());
    tarefas.saveAndFlush(t);
    auditoria.registrar(
        trocouResponsavel ? "TAREFA_RESPONSAVEL_ALTERADO" : "TAREFA_ATUALIZADA",
        "TAREFA",
        id,
        trocouResponsavel ? "Responsável da tarefa alterado." : "Tarefa atualizada.");
    return TarefaResponse.from(t, hoje());
  }

  @Transactional
  public TarefaResponse alterarStatus(Long id, TarefaRequests.Status q, Membro autor) {
    Tarefa t = carregar(id);
    if (!autorizacao.podeAtualizarStatus(autor, t)) {
      throw new AccessDeniedException(
          "Somente o responsável pela tarefa ou a coordenação autorizada pode alterar o status.");
    }
    exigirVersao(t, q.version());
    String motivo = NomeNormalizado.optionalDisplayValue(q.motivo());
    StatusTarefa anterior = t.getStatus();
    TarefaTransicoes.validar(anterior, q.status(), motivo);
    t.status(q.status());
    tarefas.saveAndFlush(t);
    auditoria.registrar(
        q.status() == StatusTarefa.CONCLUIDO ? "TAREFA_CONCLUIDA" : "TAREFA_STATUS_ATUALIZADO",
        "TAREFA",
        id,
        resumoStatus(anterior, q.status(), motivo));
    return TarefaResponse.from(t, hoje());
  }

  @Transactional(readOnly = true)
  public ChecklistEventoResponse checklist(Long eventoId, Membro membro) {
    eventos.buscarDaAtleticaAtual(eventoId); // 404 se o evento for de outra atlética
    List<Tarefa> lista =
        tarefas.findAllByEvento_IdAndAtletica_IdOrderByPrazoAscIdAsc(
            eventoId, resolver.atleticaIdAtual());
    if (!autorizacao.veTodas(membro)) {
      lista = lista.stream().filter(t -> autorizacao.ehResponsavel(membro, t)).toList();
    }
    return ChecklistEventoResponse.from(eventoId, lista, hoje());
  }

  // ---------- auxiliares ----------

  private Tarefa carregar(Long id) {
    return resolver.requireByIdAndAtleticaId(id, tarefas::findByIdAndAtletica_Id);
  }

  private void exigirVersao(Tarefa t, Long version) {
    if (!t.getVersion().equals(version)) {
      throw new ConcurrentModificationException();
    }
  }

  private Membro resolverResponsavel(Long id) {
    if (id == null) return null;
    Membro m = resolver.requireByIdAndAtleticaId(id, membros::findByIdAndAtletica_Id);
    if (m.getStatus() != StatusMembro.ATIVO) {
      throw new BusinessRuleException("O responsável deve ser um membro ativo da atlética.");
    }
    return m;
  }

  private Departamento resolverDepartamento(Long id) {
    if (id == null) return null;
    Departamento d = resolver.requireByIdAndAtleticaId(id, departamentos::findByIdAndAtletica_Id);
    if (d.getSituacao() == SituacaoDepartamento.INATIVO) {
      throw new BusinessRuleException("O departamento informado está inativo.");
    }
    return d;
  }

  private LocalDate hoje() {
    Long id = resolver.atleticaIdAtual();
    return atleticas
        .findByIdAndId(id, id)
        .map(a -> FusoHorarioAtletica.hoje(a.getFusoHorario()))
        .orElseGet(() -> FusoHorarioAtletica.hoje(null));
  }

  private static String resumoStatus(StatusTarefa de, StatusTarefa para, String motivo) {
    String texto =
        "Status: " + de + " → " + para + "." + (motivo == null ? "" : " Motivo: " + motivo);
    return texto.length() <= 500 ? texto : texto.substring(0, 497) + "...";
  }

  private Specification<Tarefa> especificacao(TarefaFiltro f, Long responsavelId, LocalDate hoje) {
    Long atleticaId = resolver.atleticaIdAtual();
    return (root, query, cb) -> {
      List<Predicate> p = new ArrayList<>();
      p.add(cb.equal(root.get("atletica").get("id"), atleticaId));
      if (f.eventoId() != null) p.add(cb.equal(root.get("evento").get("id"), f.eventoId()));
      if (responsavelId != null) p.add(cb.equal(root.get("responsavel").get("id"), responsavelId));
      if (f.departamentoId() != null)
        p.add(cb.equal(root.get("departamento").get("id"), f.departamentoId()));
      if (f.prioridade() != null) p.add(cb.equal(root.get("prioridade"), f.prioridade()));
      if (f.status() != null) p.add(cb.equal(root.get("status"), f.status()));
      if (f.prazoDe() != null)
        p.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("prazo"), f.prazoDe()));
      if (f.prazoAte() != null)
        p.add(cb.lessThanOrEqualTo(root.<LocalDate>get("prazo"), f.prazoAte()));
      if (f.atrasada() != null) {
        Predicate atrasada =
            cb.and(
                cb.isNotNull(root.get("prazo")),
                cb.lessThan(root.<LocalDate>get("prazo"), hoje),
                cb.notEqual(root.get("status"), StatusTarefa.CONCLUIDO));
        p.add(f.atrasada() ? atrasada : cb.not(atrasada));
      }
      return cb.and(p.toArray(Predicate[]::new));
    };
  }
}
