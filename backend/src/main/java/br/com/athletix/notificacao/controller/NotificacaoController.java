package br.com.athletix.notificacao.controller;

import br.com.athletix.notificacao.dto.NotificacaoResponse;
import br.com.athletix.notificacao.service.NotificacaoService;
import br.com.athletix.security.AutorizacaoAtualService;
import br.com.athletix.shared.pagination.PageQuery;
import br.com.athletix.shared.pagination.PageResponse;
import br.com.athletix.shared.pagination.PageableFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {
  private static final Map<String, String> ORDENACOES =
      Map.of("criadaEm", "createdAt", "lidaEm", "lidaEm");

  private final NotificacaoService service;
  private final AutorizacaoAtualService autorizacao;
  private final PageableFactory pageableFactory;

  public NotificacaoController(
      NotificacaoService service,
      AutorizacaoAtualService autorizacao,
      PageableFactory pageableFactory) {
    this.service = service;
    this.autorizacao = autorizacao;
    this.pageableFactory = pageableFactory;
  }

  @GetMapping
  public PageResponse<NotificacaoResponse> listar(
      @RequestParam(defaultValue = "false") boolean apenasNaoLidas,
      @Valid PageQuery pageQuery,
      Authentication authentication,
      HttpServletRequest request) {
    PageQuery consulta =
        pageQuery.sort() == null || pageQuery.sort().isEmpty()
            ? new PageQuery(pageQuery.page(), pageQuery.size(), List.of("criadaEm,desc"))
            : pageQuery;
    return service.listar(
        autorizacao.membroAtual(authentication, request).getId(),
        apenasNaoLidas,
        pageableFactory.create(consulta, ORDENACOES));
  }

  @GetMapping("/resumo")
  public NotificacaoResponse.Resumo resumo(
      Authentication authentication, HttpServletRequest request) {
    return service.resumo(autorizacao.membroAtual(authentication, request).getId());
  }

  @PatchMapping("/{id}/ler")
  public NotificacaoResponse ler(
      @PathVariable Long id, Authentication authentication, HttpServletRequest request) {
    return service.ler(id, autorizacao.membroAtual(authentication, request).getId());
  }
}
