package br.com.athletix.gestao.controller;

import br.com.athletix.gestao.dto.GestaoCreateRequest;
import br.com.athletix.gestao.dto.GestaoResponse;
import br.com.athletix.gestao.service.GestaoService;
import br.com.athletix.security.AutorizacaoAtualService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gestoes")
public class GestaoController {
  private final GestaoService gestaoService;
  private final AutorizacaoAtualService autorizacao;

  public GestaoController(GestaoService gestaoService, AutorizacaoAtualService autorizacao) {
    this.gestaoService = gestaoService;
    this.autorizacao = autorizacao;
  }

  @GetMapping
  public List<GestaoResponse> listar(Authentication authentication, HttpServletRequest request) {
    autorizacao.exigirAdministrador(authentication, request);
    return gestaoService.listarDaAtleticaAtual().stream().map(GestaoResponse::from).toList();
  }

  @PostMapping
  public ResponseEntity<GestaoResponse> criar(
      @Valid @RequestBody GestaoCreateRequest body,
      Authentication authentication,
      HttpServletRequest request) {
    autorizacao.exigirAdministrador(authentication, request);
    return ResponseEntity.status(201).body(GestaoResponse.from(gestaoService.criar(body)));
  }

  @GetMapping("/{id}")
  public GestaoResponse buscar(
      @PathVariable Long id, Authentication authentication, HttpServletRequest request) {
    autorizacao.exigirAdministrador(authentication, request);
    return GestaoResponse.from(gestaoService.buscarDaAtleticaAtual(id));
  }
}
