package br.com.athletix.evento.participacao.entity;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.membro.entity.Membro;
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
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/** Inscrição interna ou externa, preservada mesmo após cancelamento. */
@Entity
@Table(
    name = "participacoes_evento",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_participacao_evento_membro",
          columnNames = {"evento_id", "membro_id"}),
      @UniqueConstraint(
          name = "uk_participacao_evento_email",
          columnNames = {"evento_id", "email_participante"})
    })
public class ParticipacaoEvento extends AuditableEntity implements AtleticaScoped {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "atletica_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_participacao_evento_atletica"))
  private Atletica atletica;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "evento_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_participacao_evento_evento"))
  private Evento evento;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "membro_id", foreignKey = @ForeignKey(name = "fk_participacao_evento_membro"))
  private Membro membro;

  @Column(name = "nome_participante", length = 120)
  private String nomeParticipante;

  @Column(name = "email_participante", length = 254)
  private String emailParticipante;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 12)
  private StatusParticipacaoEvento status;

  @Column(name = "inscrito_em", nullable = false, updatable = false)
  private Instant inscritoEm;

  @Column(name = "confirmado_em")
  private Instant confirmadoEm;

  @Column(name = "presenca_em")
  private Instant presencaEm;

  protected ParticipacaoEvento() {}

  public ParticipacaoEvento(
      Atletica atletica,
      Evento evento,
      Membro membro,
      String nomeParticipante,
      String emailParticipante,
      Instant inscritoEm) {
    this.atletica = atletica;
    this.evento = evento;
    this.membro = membro;
    this.nomeParticipante = nomeParticipante;
    this.emailParticipante = emailParticipante;
    this.inscritoEm = inscritoEm;
    this.status = StatusParticipacaoEvento.INSCRITO;
  }

  @Override
  public Long getAtleticaId() {
    return atletica.getId();
  }

  public Long getEventoId() {
    return evento.getId();
  }

  public Long getMembroId() {
    return membro == null ? null : membro.getId();
  }

  public String getNomeParticipante() {
    return membro == null ? nomeParticipante : membro.getNomeUsuario();
  }

  public String getEmailParticipante() {
    return emailParticipante;
  }

  public StatusParticipacaoEvento getStatus() {
    return status;
  }

  public Instant getInscritoEm() {
    return inscritoEm;
  }

  public Instant getConfirmadoEm() {
    return confirmadoEm;
  }

  public Instant getPresencaEm() {
    return presencaEm;
  }

  public void confirmar(Instant instante) {
    this.status = StatusParticipacaoEvento.CONFIRMADO;
    this.confirmadoEm = instante;
  }

  public void registrarPresenca(Instant instante) {
    this.status = StatusParticipacaoEvento.PRESENTE;
    this.presencaEm = instante;
  }

  public void corrigirPresenca(Instant instante) {
    this.status = StatusParticipacaoEvento.CONFIRMADO;
    this.presencaEm = null;
    if (this.confirmadoEm == null) {
      this.confirmadoEm = instante;
    }
  }

  public void cancelar() {
    this.status = StatusParticipacaoEvento.CANCELADO;
  }
}
