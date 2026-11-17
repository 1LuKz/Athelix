package br.com.athletix.tarefa.controller;

import br.com.athletix.security.AutorizacaoAtualService;
import br.com.athletix.tarefa.dto.ChecklistEventoResponse;
import br.com.athletix.tarefa.service.TarefaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/eventos")
public class EventoChecklistController {
  private final TarefaService service;
  private final AutorizacaoAtualService autorizacao;

  public EventoChecklistController(TarefaService service, AutorizacaoAtualService autorizacao) {
    this.service = service;
    this.autorizacao = autorizacao;
  }

  @GetMapping("/{eventoId}/checklist")
  public ChecklistEventoResponse checklist(
      @PathVariable Long eventoId, Authentication authentication, HttpServletRequest request) {
    return service.checklist(eventoId, autorizacao.membroAtual(authentication, request));
  }
}
