package br.com.athletix.evento.cronograma.entity;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.shared.persistence.AuditableEntity;
import br.com.athletix.shared.tenancy.AtleticaScoped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Item agendado com data completa para suportar eventos em vários dias. */
@Entity
@Table(name = "atividades_cronograma")
public class AtividadeCronograma extends AuditableEntity implements AtleticaScoped {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "atletica_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_cronograma_atletica"))
  private Atletica atletica;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "evento_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_cronograma_evento"))
  private Evento evento;

  @Column(nullable = false, length = 160)
  private String titulo;

  @Column(length = 1000)
  private String descricao;

  @Column(nullable = false)
  private OffsetDateTime inicio;

  @Column private OffsetDateTime fim;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private StatusAtividadeCronograma status;

  @Column(nullable = false)
  private Integer ordem;

  protected AtividadeCronograma() {}

  public AtividadeCronograma(
      Atletica atletica,
      Evento evento,
      String titulo,
      String descricao,
      OffsetDateTime inicio,
      OffsetDateTime fim,
      StatusAtividadeCronograma status,
      Integer ordem) {
    this.atletica = atletica;
    this.evento = evento;
    this.titulo = titulo;
    this.descricao = descricao;
    this.inicio = inicio;
    this.fim = fim;
    this.status = status;
    this.ordem = ordem;
  }

  @Override
  public Long getAtleticaId() {
    return atletica.getId();
  }

  public String getFusoHorarioAtletica() {
    return atletica.getFusoHorario();
  }

  public Long getEventoId() {
    return evento.getId();
  }

  public String getTitulo() {
    return titulo;
  }

  public String getDescricao() {
    return descricao;
  }

  public OffsetDateTime getInicio() {
    return inicio;
  }

  public OffsetDateTime getFim() {
    return fim;
  }

  public StatusAtividadeCronograma getStatus() {
    return status;
  }

  public Integer getOrdem() {
    return ordem;
  }

  public void atualizar(
      String titulo,
      String descricao,
      OffsetDateTime inicio,
      OffsetDateTime fim,
      StatusAtividadeCronograma status,
      Integer ordem) {
    this.titulo = titulo;
    this.descricao = descricao;
    this.inicio = inicio;
    this.fim = fim;
    this.status = status;
    this.ordem = ordem;
  }
}
