package br.com.athletix.financeiro.controller;

import br.com.athletix.financeiro.dto.ContaAbertaResponse;
import br.com.athletix.financeiro.dto.EvolucaoMensalResponse;
import br.com.athletix.financeiro.dto.FinanceiroResumoResponse;
import br.com.athletix.financeiro.service.FinanceiroConsultaService;
import br.com.athletix.security.AutorizacaoAtualService;
import br.com.athletix.shared.pagination.PageQuery;
import br.com.athletix.shared.pagination.PageResponse;
import br.com.athletix.shared.pagination.PageableFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/financeiro")
public class FinanceiroConsultaController {
  private static final Map<String, String> ORDENACOES_CONTAS =
      Map.of(
          "dataVencimento",
          "dataVencimento",
          "descricao",
          "descricao",
          "valor",
          "valor",
          "createdAt",
          "createdAt");

  private final FinanceiroConsultaService service;
  private final AutorizacaoAtualService autorizacao;
  private final PageableFactory pageableFactory;

  public FinanceiroConsultaController(
      FinanceiroConsultaService service,
      AutorizacaoAtualService autorizacao,
      PageableFactory pageableFactory) {
    this.service = service;
    this.autorizacao = autorizacao;
    this.pageableFactory = pageableFactory;
  }

  @GetMapping("/resumo")
  public FinanceiroResumoResponse resumo(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate dataReferencia,
      Authentication authentication,
      HttpServletRequest request) {
    return service.resumo(dataReferencia, autorizacao.membroAtual(authentication, request));
  }

  @GetMapping("/evolucao-mensal")
  public List<EvolucaoMensalResponse> evolucaoMensal(
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth mesDe,
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth mesAte,
      Authentication authentication,
      HttpServletRequest request) {
    return service.evolucaoMensal(mesDe, mesAte, autorizacao.membroAtual(authentication, request));
  }

  @GetMapping("/contas")
  public PageResponse<ContaAbertaResponse> contas(
      @Valid PageQuery pageQuery, Authentication authentication, HttpServletRequest request) {
    return service.contas(
        pageableFactory.create(pageQuery, ORDENACOES_CONTAS),
        autorizacao.membroAtual(authentication, request));
  }

  @GetMapping("/relatorio")
  public ResponseEntity<byte[]> relatorio(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataDe,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataAte,
      Authentication authentication,
      HttpServletRequest request) {
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment().filename("relatorio-financeiro.csv").build().toString())
        .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
        .body(
            service.relatorioCsv(
                dataDe, dataAte, autorizacao.membroAtual(authentication, request)));
  }
}
