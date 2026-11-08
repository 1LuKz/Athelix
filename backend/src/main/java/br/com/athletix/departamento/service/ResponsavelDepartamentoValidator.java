package br.com.athletix.departamento.service;

/** Porta implementada pelo módulo de membros na B05. */
public interface ResponsavelDepartamentoValidator {

    void validarMembroAtivoDoDepartamento(Long membroId, Long atleticaId, Long departamentoId);
}
