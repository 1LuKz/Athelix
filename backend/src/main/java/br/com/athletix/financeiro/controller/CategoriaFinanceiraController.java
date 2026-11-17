package br.com.athletix.financeiro.controller;

import br.com.athletix.financeiro.dto.CategoriaFinanceiraCreateRequest;
import br.com.athletix.financeiro.dto.CategoriaFinanceiraResponse;
import br.com.athletix.financeiro.service.CategoriaFinanceiraService;
import br.com.athletix.security.AutorizacaoAtualService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/financeiro/categorias")
public class CategoriaFinanceiraController {
  private final CategoriaFinanceiraService service;
  private final AutorizacaoAtualService autorizacao;

  public CategoriaFinanceiraController(
      CategoriaFinanceiraService service, AutorizacaoAtualService autorizacao) {
    this.service = service;
    this.autorizacao = autorizacao;
  }

  @GetMapping
  public List<CategoriaFinanceiraResponse> listar(
      Authentication authentication, HttpServletRequest request) {
    return service.listar(autorizacao.membroAtual(authentication, request)).stream()
        .map(CategoriaFinanceiraResponse::from)
        .toList();
  }

  @PostMapping
  public ResponseEntity<CategoriaFinanceiraResponse> criar(
      @Valid @RequestBody CategoriaFinanceiraCreateRequest body,
      Authentication authentication,
      HttpServletRequest request) {
    return ResponseEntity.status(201)
        .body(
            CategoriaFinanceiraResponse.from(
                service.criar(body, autorizacao.membroAtual(authentication, request))));
  }
}
