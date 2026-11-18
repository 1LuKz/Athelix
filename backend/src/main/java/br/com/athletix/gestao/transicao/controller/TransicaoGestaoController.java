package br.com.athletix.gestao.transicao.controller;

import br.com.athletix.gestao.transicao.dto.*;
import br.com.athletix.gestao.transicao.service.TransicaoGestaoService;
import br.com.athletix.security.AutorizacaoAtualService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gestoes")
public class TransicaoGestaoController {
  private final TransicaoGestaoService service;
  private final AutorizacaoAtualService auth;

  public TransicaoGestaoController(
      TransicaoGestaoService service, AutorizacaoAtualService autorizacaoAtualService) {
    this.service = service;
    this.auth = autorizacaoAtualService;
  }

  @PostMapping("/{id}/transicoes")
  public ResponseEntity<TransicaoResponse> criar(
      @PathVariable("id") Long gestaoDestinoId,
      Authentication authentication,
      HttpServletRequest request) {
    auth.exigirAdministrador(authentication, request);
    return ResponseEntity.status(201)
        .body(service.criar(gestaoDestinoId, auth.membroAtual(authentication, request)));
  }

  @PutMapping("/transicoes/{id}")
  public TransicaoResponse atualizar(
      @PathVariable("id") Long transicaoId,
      @Valid @RequestBody TransicaoUpdateRequest corpoDaRequisicao,
      Authentication authentication,
      HttpServletRequest request) {
    auth.exigirAdministrador(authentication, request);
    return service.atualizar(
        transicaoId, corpoDaRequisicao, auth.membroAtual(authentication, request));
  }

  @GetMapping("/transicoes/{id}/previa")
  public PreviaTransicaoResponse previa(
      @PathVariable("id") Long transicaoId,
      Authentication authentication,
      HttpServletRequest request) {
    auth.exigirAdministrador(authentication, request);
    return service.previa(transicaoId);
  }

  @PostMapping("/transicoes/{id}/concluir")
  public TransicaoResponse concluir(
      @PathVariable("id") Long transicaoId,
      @Valid @RequestBody ConcluirTransicaoRequest corpoDaRequisicao,
      Authentication authentication,
      HttpServletRequest request) {
    auth.exigirAdministrador(authentication, request);
    return service.concluir(
        transicaoId, corpoDaRequisicao.version(), auth.membroAtual(authentication, request));
  }
}
