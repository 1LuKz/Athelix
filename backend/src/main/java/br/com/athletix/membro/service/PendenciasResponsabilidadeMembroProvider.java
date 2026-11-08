package br.com.athletix.membro.service;

import java.util.List;

/** Extensão para módulos como Evento informarem responsabilidades abertas antes da inativação. */
public interface PendenciasResponsabilidadeMembroProvider {

    List<String> listarPendencias(Long atleticaId, Long membroId);
}
