package br.com.athletix.dashboard.controller;

import br.com.athletix.dashboard.dto.DashboardResponses.*;
import br.com.athletix.dashboard.service.DashboardService;
import br.com.athletix.security.AutorizacaoAtualService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
  private final DashboardService service;
  private final AutorizacaoAtualService auth;

  public DashboardController(
      DashboardService service, AutorizacaoAtualService autorizacaoAtualService) {
    this.service = service;
    this.auth = autorizacaoAtualService;
  }

  private br.com.athletix.membro.entity.Membro membroAtual(
      Authentication authentication, HttpServletRequest request) {
    return auth.membroAtual(authentication, request);
  }

  @GetMapping("/resumo")
  public Resumo resumo(Authentication authentication, HttpServletRequest request) {
    return service.resumo(membroAtual(authentication, request));
  }

  @GetMapping("/financeiro")
  public Financeiro financeiro(Authentication authentication, HttpServletRequest request) {
    return service.financeiro(membroAtual(authentication, request));
  }

  @GetMapping("/departamentos")
  public List<Departamento> departamentos() {
    return service.departamentos();
  }

  @GetMapping("/proximos-eventos")
  public List<Evento> eventos() {
    return service.proximos();
  }

  @GetMapping("/tarefas-pendentes")
  public List<Tarefa> tarefas(Authentication authentication, HttpServletRequest request) {
    return service.pendentes(membroAtual(authentication, request));
  }

  @GetMapping("/ultimas-movimentacoes")
  public List<Movimento> movimentacoes(Authentication authentication, HttpServletRequest request) {
    return service.movimentacoes(membroAtual(authentication, request));
  }
}
