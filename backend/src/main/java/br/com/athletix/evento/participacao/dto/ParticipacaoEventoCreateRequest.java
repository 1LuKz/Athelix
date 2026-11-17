package br.com.athletix.evento.participacao.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ParticipacaoEventoCreateRequest(
    @Positive Long membroId,
    @Size(max = 120) String nomeParticipante,
    @Email @Size(max = 254) String emailParticipante) {
  @AssertTrue(message = "Informe um membro interno ou nome e e-mail de participante externo.")
  public boolean isParticipanteValido() {
    boolean externoInformado =
        nomeParticipante != null
            && !nomeParticipante.isBlank()
            && emailParticipante != null
            && !emailParticipante.isBlank();
    boolean dadosExternosAusentes =
        (nomeParticipante == null || nomeParticipante.isBlank())
            && (emailParticipante == null || emailParticipante.isBlank());
    return membroId == null ? externoInformado : dadosExternosAusentes;
  }
}
