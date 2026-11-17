package br.com.athletix.patrimonio.manutencao.controller;

import br.com.athletix.patrimonio.manutencao.dto.*;
import br.com.athletix.patrimonio.manutencao.service.ManutencaoPatrimonioService;
import br.com.athletix.security.AutorizacaoAtualService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patrimonio/manutencoes")
public class ManutencaoPatrimonioController {
  private final ManutencaoPatrimonioService service;
  private final AutorizacaoAtualService autorizacao;

  public ManutencaoPatrimonioController(
      ManutencaoPatrimonioService service, AutorizacaoAtualService autorizacao) {
    this.service = service;
    this.autorizacao = autorizacao;
  }

  @GetMapping
  public List<ManutencaoPatrimonioResponse> listar(Authentication a, HttpServletRequest r) {
    return service.listar(autorizacao.membroAtual(a, r));
  }

  @PostMapping
  public ResponseEntity<ManutencaoPatrimonioResponse> criar(
      @Valid @RequestBody ManutencaoPatrimonioCreateRequest body,
      Authentication a,
      HttpServletRequest r) {
    return ResponseEntity.status(201).body(service.criar(body, autorizacao.membroAtual(a, r)));
  }

  @PatchMapping("/{id}/concluir")
  public ManutencaoPatrimonioResponse concluir(
      @PathVariable Long id,
      @Valid @RequestBody ManutencaoConclusaoRequest body,
      Authentication a,
      HttpServletRequest r) {
    return service.concluir(id, body, autorizacao.membroAtual(a, r));
  }
}
