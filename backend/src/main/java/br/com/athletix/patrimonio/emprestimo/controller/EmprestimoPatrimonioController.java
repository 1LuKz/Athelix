package br.com.athletix.patrimonio.emprestimo.controller;

import br.com.athletix.patrimonio.emprestimo.dto.*;
import br.com.athletix.patrimonio.emprestimo.service.EmprestimoPatrimonioService;
import br.com.athletix.security.AutorizacaoAtualService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patrimonio/emprestimos")
public class EmprestimoPatrimonioController {
  private final EmprestimoPatrimonioService service;
  private final AutorizacaoAtualService autorizacao;

  public EmprestimoPatrimonioController(
      EmprestimoPatrimonioService service, AutorizacaoAtualService autorizacao) {
    this.service = service;
    this.autorizacao = autorizacao;
  }

  @GetMapping
  public List<EmprestimoPatrimonioResponse> listar(Authentication a, HttpServletRequest r) {
    return service.listar(autorizacao.membroAtual(a, r));
  }

  @PostMapping
  public ResponseEntity<EmprestimoPatrimonioResponse> criar(
      @Valid @RequestBody EmprestimoPatrimonioCreateRequest body,
      Authentication a,
      HttpServletRequest r) {
    return ResponseEntity.status(201).body(service.criar(body, autorizacao.membroAtual(a, r)));
  }

  @PostMapping("/{id}/devolucoes")
  public ResponseEntity<DevolucaoPatrimonioResponse> devolver(
      @PathVariable Long id,
      @Valid @RequestBody DevolucaoPatrimonioCreateRequest body,
      Authentication a,
      HttpServletRequest r) {
    return ResponseEntity.status(201)
        .body(service.devolver(id, body, autorizacao.membroAtual(a, r)));
  }
}
