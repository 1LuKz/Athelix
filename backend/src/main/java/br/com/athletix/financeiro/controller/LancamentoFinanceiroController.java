package br.com.athletix.financeiro.controller;

import br.com.athletix.financeiro.dto.LancamentoFinanceiroCreateRequest;
import br.com.athletix.financeiro.dto.LancamentoAprovacaoRequest;
import br.com.athletix.financeiro.dto.LancamentoCancelamentoRequest;
import br.com.athletix.financeiro.dto.LancamentoLiquidacaoRequest;
import br.com.athletix.financeiro.dto.EstornoFinanceiroCreateRequest;
import br.com.athletix.financeiro.dto.EstornoFinanceiroResponse;
import br.com.athletix.financeiro.dto.LancamentoFinanceiroFiltro;
import br.com.athletix.financeiro.dto.LancamentoFinanceiroResponse;
import br.com.athletix.financeiro.dto.LancamentoFinanceiroUpdateRequest;
import br.com.athletix.financeiro.entity.ModoDataLancamento;
import br.com.athletix.financeiro.entity.SituacaoLancamento;
import br.com.athletix.financeiro.entity.StatusAprovacao;
import br.com.athletix.financeiro.entity.TipoLancamentoFinanceiro;
import br.com.athletix.financeiro.service.LancamentoFinanceiroService;
import br.com.athletix.security.AutorizacaoAtualService;
import br.com.athletix.shared.pagination.PageQuery;
import br.com.athletix.shared.pagination.PageResponse;
import br.com.athletix.shared.pagination.PageableFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/financeiro/lancamentos")
public class LancamentoFinanceiroController {
    private static final Map<String, String> ORDENACOES = Map.of(
            "dataCompetencia", "dataCompetencia", "dataVencimento", "dataVencimento",
            "dataLiquidacao", "dataLiquidacao", "descricao", "descricao", "valor", "valor",
            "createdAt", "createdAt");

    private final LancamentoFinanceiroService service;
    private final AutorizacaoAtualService autorizacao;
    private final PageableFactory pageableFactory;

    public LancamentoFinanceiroController(LancamentoFinanceiroService service, AutorizacaoAtualService autorizacao,
                                          PageableFactory pageableFactory) {
        this.service = service;
        this.autorizacao = autorizacao;
        this.pageableFactory = pageableFactory;
    }

    @GetMapping
    public PageResponse<LancamentoFinanceiroResponse> listar(
            @RequestParam(required = false) ModoDataLancamento modoData,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataAte,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) TipoLancamentoFinanceiro tipo,
            @RequestParam(required = false) SituacaoLancamento situacao,
            @RequestParam(required = false) StatusAprovacao statusAprovacao,
            @RequestParam(required = false) Long eventoId,
            @RequestParam(required = false) Long gestaoId,
            @Valid PageQuery pageQuery, Authentication authentication, HttpServletRequest request) {
        return service.listar(new LancamentoFinanceiroFiltro(modoData, dataDe, dataAte, categoriaId, tipo,
                        situacao, statusAprovacao, eventoId, gestaoId),
                pageableFactory.create(pageQuery, ORDENACOES), autorizacao.membroAtual(authentication, request));
    }

    @PostMapping
    public ResponseEntity<LancamentoFinanceiroResponse> criar(
            @Valid @RequestBody LancamentoFinanceiroCreateRequest body,
            Authentication authentication, HttpServletRequest request) {
        return ResponseEntity.status(201).body(service.criar(body, autorizacao.membroAtual(authentication, request)));
    }

    @GetMapping("/{id}")
    public LancamentoFinanceiroResponse buscar(@PathVariable Long id, Authentication authentication,
                                               HttpServletRequest request) {
        return service.buscar(id, autorizacao.membroAtual(authentication, request));
    }

    @PutMapping("/{id}")
    public LancamentoFinanceiroResponse atualizar(@PathVariable Long id,
                                                  @Valid @RequestBody LancamentoFinanceiroUpdateRequest body,
                                                  Authentication authentication, HttpServletRequest request) {
        return service.atualizar(id, body, autorizacao.membroAtual(authentication, request));
    }

    @PatchMapping("/{id}/aprovacao")
    public LancamentoFinanceiroResponse decidirAprovacao(@PathVariable Long id,
            @Valid @RequestBody LancamentoAprovacaoRequest body,
            Authentication authentication, HttpServletRequest request) {
        return service.decidirAprovacao(id, body, autorizacao.membroAtual(authentication, request));
    }

    @PostMapping("/{id}/liquidacao")
    public LancamentoFinanceiroResponse liquidar(@PathVariable Long id,
            @Valid @RequestBody LancamentoLiquidacaoRequest body,
            Authentication authentication, HttpServletRequest request) {
        return service.liquidar(id, body, autorizacao.membroAtual(authentication, request));
    }

    @PatchMapping("/{id}/cancelamento")
    public LancamentoFinanceiroResponse cancelar(@PathVariable Long id,
            @Valid @RequestBody LancamentoCancelamentoRequest body,
            Authentication authentication, HttpServletRequest request) {
        return service.cancelar(id, body, autorizacao.membroAtual(authentication, request));
    }

    @PostMapping("/{id}/estorno")
    public ResponseEntity<EstornoFinanceiroResponse> estornar(@PathVariable Long id,
            @Valid @RequestBody EstornoFinanceiroCreateRequest body,
            Authentication authentication, HttpServletRequest request) {
        return ResponseEntity.status(201).body(service.estornar(id, body,
                autorizacao.membroAtual(authentication, request)));
    }
}
