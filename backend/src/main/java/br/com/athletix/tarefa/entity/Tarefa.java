package br.com.athletix.tarefa.entity;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.shared.persistence.AuditableEntity;
import br.com.athletix.shared.tenancy.AtleticaScoped;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "tarefas")
public class Tarefa extends AuditableEntity implements AtleticaScoped {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "atletica_id", nullable = false)
  private Atletica atletica;

  @Column(name = "gestao_origem_id")
  private Long gestaoOrigemId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "evento_id", nullable = false)
  private Evento evento;

  @Column(nullable = false, length = 160)
  private String titulo;

  @Column(length = 3000)
  private String descricao;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "responsavel_membro_id")
  private Membro responsavel;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "departamento_id")
  private Departamento departamento;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 8)
  private PrioridadeTarefa prioridade;

  private LocalDate prazo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private StatusTarefa status;

  @Column(name = "concluida_em")
  private Instant concluidaEm;

  @Column(name = "criada_por_membro_id", nullable = false)
  private Long criadaPorMembroId;

  protected Tarefa() {}

  public Tarefa(
      Atletica atletica,
      Long gestaoOrigemId,
      Evento evento,
      String titulo,
      String descricao,
      Membro responsavel,
      Departamento departamento,
      PrioridadeTarefa prioridade,
      LocalDate prazo,
      Long criadorMembroId) {
    this.atletica = atletica;
    this.gestaoOrigemId = gestaoOrigemId;
    this.evento = evento;
    this.titulo = titulo;
    this.descricao = descricao;
    this.responsavel = responsavel;
    this.departamento = departamento;
    this.prioridade = prioridade;
    this.prazo = prazo;
    criadaPorMembroId = criadorMembroId;
    status = StatusTarefa.A_FAZER;
  }

  public Long getAtleticaId() {
    return atletica.getId();
  }

  public Long getEventoId() {
    return evento.getId();
  }

  public Long getResponsavelMembroId() {
    return responsavel == null ? null : responsavel.getId();
  }

  public Long getDepartamentoId() {
    return departamento == null ? null : departamento.getId();
  }

  public String getTitulo() {
    return titulo;
  }

  public String getDescricao() {
    return descricao;
  }

  public PrioridadeTarefa getPrioridade() {
    return prioridade;
  }

  public LocalDate getPrazo() {
    return prazo;
  }

  public StatusTarefa getStatus() {
    return status;
  }

  public Instant getConcluidaEm() {
    return concluidaEm;
  }

  public Long getGestaoOrigemId() {
    return gestaoOrigemId;
  }

  public Long getCriadaPorMembroId() {
    return criadaPorMembroId;
  }

  public void atualizar(
      String titulo,
      String descricao,
      Membro responsavel,
      Departamento departamento,
      PrioridadeTarefa prioridade,
      LocalDate prazo) {
    this.titulo = titulo;
    this.descricao = descricao;
    this.responsavel = responsavel;
    this.departamento = departamento;
    this.prioridade = prioridade;
    this.prazo = prazo;
  }

  public void status(StatusTarefa status) {
    this.status = status;
    concluidaEm = status == StatusTarefa.CONCLUIDO ? Instant.now() : null;
  }

  public Evento getEvento() {
    return evento;
  }

  public Membro getResponsavel() {
    return responsavel;
  }

  public Departamento getDepartamento() {
    return departamento;
  }

  public boolean atrasada(LocalDate hoje) {
    return prazo != null && prazo.isBefore(hoje) && status != StatusTarefa.CONCLUIDO;
  }
}
