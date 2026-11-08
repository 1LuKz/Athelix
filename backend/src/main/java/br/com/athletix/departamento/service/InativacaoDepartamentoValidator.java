package br.com.athletix.departamento.service;

/** Porta implementada pelo módulo de membros na B05 antes de permitir a inativação. */
public interface InativacaoDepartamentoValidator {

    void validarMembrosTransferidosEResponsabilidadesSubstituidas(Long atleticaId, Long departamentoId);
}
