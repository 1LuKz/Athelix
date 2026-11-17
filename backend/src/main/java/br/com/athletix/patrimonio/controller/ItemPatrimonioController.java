package br.com.athletix.patrimonio.controller;

import br.com.athletix.patrimonio.dto.ItemPatrimonioCreateRequest;
import br.com.athletix.patrimonio.dto.ItemPatrimonioFiltro;
import br.com.athletix.patrimonio.dto.ItemPatrimonioResponse;
import br.com.athletix.patrimonio.dto.ItemPatrimonioUpdateRequest;
import br.com.athletix.patrimonio.dto.PatrimonioResumoResponse;
import br.com.athletix.patrimonio.entity.ConservacaoPatrimonio;
import br.com.athletix.patrimonio.entity.SituacaoOperacionalPatrimonio;
import br.com.athletix.patrimonio.service.ItemPatrimonioService;
import br.com.athletix.security.AutorizacaoAtualService;
import br.com.athletix.shared.pagination.PageQuery;
import br.com.athletix.shared.pagination.PageResponse;
import br.com.athletix.shared.pagination.PageableFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/patrimonio")
public class ItemPatrimonioController {
  private static final Map<String, String> ORDENACOES =
      Map.of(
          "nome",
          "nome",
          "conservacao",
          "conservacao",
          "localizacao",
          "localizacao",
          "valorEstimadoUnitario",
          "valorEstimadoUnitario",
          "createdAt",
          "createdAt");
  private final ItemPatrimonioService service;
  private final AutorizacaoAtualService autorizacao;
  private final PageableFactory pageableFactory;

  public ItemPatrimonioController(
      ItemPatrimonioService service,
      AutorizacaoAtualService autorizacao,
      PageableFactory pageableFactory) {
    this.service = service;
    this.autorizacao = autorizacao;
    this.pageableFactory = pageableFactory;
  }

  @GetMapping("/itens")
  public PageResponse<ItemPatrimonioResponse> listar(
      @RequestParam(required = false) String nome,
      @RequestParam(required = false) Long categoriaId,
      @RequestParam(required = false) ConservacaoPatrimonio conservacao,
      @RequestParam(required = false) SituacaoOperacionalPatrimonio situacao,
      @Valid PageQuery pageQuery,
      Authentication authentication,
      HttpServletRequest request) {
    return service.listar(
        new ItemPatrimonioFiltro(nome, categoriaId, conservacao, situacao),
        pageableFactory.create(pageQuery, ORDENACOES),
        autorizacao.membroAtual(authentication, request));
  }

  @PostMapping("/itens")
  public ResponseEntity<ItemPatrimonioResponse> criar(
      @Valid @RequestBody ItemPatrimonioCreateRequest body,
      Authentication authentication,
      HttpServletRequest request) {
    return ResponseEntity.status(201)
        .body(service.criar(body, autorizacao.membroAtual(authentication, request)));
  }

  @GetMapping("/itens/{id}")
  public ItemPatrimonioResponse buscar(
      @PathVariable Long id, Authentication authentication, HttpServletRequest request) {
    return service.buscar(id, autorizacao.membroAtual(authentication, request));
  }

  @PutMapping("/itens/{id}")
  public ItemPatrimonioResponse atualizar(
      @PathVariable Long id,
      @Valid @RequestBody ItemPatrimonioUpdateRequest body,
      Authentication authentication,
      HttpServletRequest request) {
    return service.atualizar(id, body, autorizacao.membroAtual(authentication, request));
  }

  @GetMapping("/resumo")
  public PatrimonioResumoResponse resumo(
      Authentication authentication, HttpServletRequest request) {
    return service.resumo(autorizacao.membroAtual(authentication, request));
  }
}
