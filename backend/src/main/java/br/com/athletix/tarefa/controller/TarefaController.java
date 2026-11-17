package br.com.athletix.tarefa.controller;

import br.com.athletix.security.AutorizacaoAtualService;
import br.com.athletix.shared.pagination.PageQuery;
import br.com.athletix.shared.pagination.PageResponse;
import br.com.athletix.shared.pagination.PageableFactory;
import br.com.athletix.tarefa.dto.TarefaFiltro;
import br.com.athletix.tarefa.dto.TarefaRequests;
import br.com.athletix.tarefa.dto.TarefaResponse;
import br.com.athletix.tarefa.entity.PrioridadeTarefa;
import br.com.athletix.tarefa.entity.StatusTarefa;
import br.com.athletix.tarefa.service.TarefaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tarefas")
public class TarefaController {
  private static final Map<String, String> ORDENACOES =
      Map.of("prazo", "prazo", "titulo", "titulo", "createdAt", "createdAt");

  private final TarefaService service;
  private final AutorizacaoAtualService autorizacao;
  private final PageableFactory pageableFactory;

  public TarefaController(
      TarefaService service, AutorizacaoAtualService autorizacao, PageableFactory pageableFactory) {
    this.service = service;
    this.autorizacao = autorizacao;
    this.pageableFactory = pageableFactory;
  }

  @GetMapping
  public PageResponse<TarefaResponse> listar(
      @RequestParam(required = false) Long eventoId,
      @RequestParam(required = false) Long responsavelMembroId,
      @RequestParam(required = false) Long departamentoId,
      @RequestParam(required = false) PrioridadeTarefa prioridade,
      @RequestParam(required = false) StatusTarefa status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate prazoDe,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate prazoAte,
      @RequestParam(required = false) Boolean atrasada,
      @Valid PageQuery pageQuery,
      Authentication authentication,
      HttpServletRequest request) {
    var filtro =
        new TarefaFiltro(
            eventoId,
            responsavelMembroId,
            departamentoId,
            prioridade,
            status,
            prazoDe,
            prazoAte,
            atrasada);
    return service.listar(
        filtro,
        pageableFactory.create(pageQuery, ORDENACOES),
        autorizacao.membroAtual(authentication, request));
  }

  @PostMapping
  public ResponseEntity<TarefaResponse> criar(
      @Valid @RequestBody TarefaRequests.Create body,
      Authentication authentication,
      HttpServletRequest request) {
    return ResponseEntity.status(201)
        .body(service.criar(body, autorizacao.membroAtual(authentication, request)));
  }

  @GetMapping("/{id}")
  public TarefaResponse buscar(
      @PathVariable Long id, Authentication authentication, HttpServletRequest request) {
    return service.buscar(id, autorizacao.membroAtual(authentication, request));
  }

  @PutMapping("/{id}")
  public TarefaResponse atualizar(
      @PathVariable Long id,
      @Valid @RequestBody TarefaRequests.Update body,
      Authentication authentication,
      HttpServletRequest request) {
    return service.alterar(id, body, autorizacao.membroAtual(authentication, request));
  }

  @PatchMapping("/{id}/status")
  public TarefaResponse alterarStatus(
      @PathVariable Long id,
      @Valid @RequestBody TarefaRequests.Status body,
      Authentication authentication,
      HttpServletRequest request) {
    return service.alterarStatus(id, body, autorizacao.membroAtual(authentication, request));
  }
}
