package br.com.athletix.patrimonio.conferencia.controller;

import br.com.athletix.patrimonio.conferencia.dto.*;
import br.com.athletix.patrimonio.conferencia.service.ConferenciaInventarioService;
import br.com.athletix.security.AutorizacaoAtualService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patrimonio/conferencias")
public class ConferenciaInventarioController {
  private final ConferenciaInventarioService service;
  private final AutorizacaoAtualService auth;

  public ConferenciaInventarioController(
      ConferenciaInventarioService service, AutorizacaoAtualService auth) {
    this.service = service;
    this.auth = auth;
  }

  @GetMapping
  public List<ConferenciaResponse> listar(
      Authentication authentication, HttpServletRequest request) {
    return service.listar(auth.membroAtual(authentication, request));
  }

  @PostMapping
  public ResponseEntity<ConferenciaResponse> criar(
      @Valid @RequestBody ConferenciaCreateRequest body,
      Authentication authentication,
      HttpServletRequest request) {
    return ResponseEntity.status(201)
        .body(service.criar(body, auth.membroAtual(authentication, request)));
  }

  @GetMapping("/{id}")
  public ConferenciaDetalheResponse detalhe(
      @PathVariable("id") Long conferenciaId,
      Authentication authentication,
      HttpServletRequest request) {
    return service.detalhe(conferenciaId, auth.membroAtual(authentication, request));
  }

  @PutMapping("/{id}/itens/{itemId}")
  public ItemConferenciaResponse conferir(
      @PathVariable("id") Long conferenciaId,
      @PathVariable Long itemId,
      @Valid @RequestBody ItemConferenciaUpdateRequest body,
      Authentication authentication,
      HttpServletRequest request) {
    return service.conferir(conferenciaId, itemId, body, auth.membroAtual(authentication, request));
  }

  @PatchMapping("/{id}/encerrar")
  public ConferenciaDetalheResponse encerrar(
      @PathVariable("id") Long conferenciaId,
      @Valid @RequestBody ConferenciaEncerramentoRequest body,
      Authentication authentication,
      HttpServletRequest request) {
    return service.encerrar(conferenciaId, body, auth.membroAtual(authentication, request));
  }
}
