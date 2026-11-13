package br.com.athletix.evento.financeiro.controller;

import br.com.athletix.evento.financeiro.dto.EventoFinanceiroResumoResponse;
import br.com.athletix.evento.financeiro.service.EventoFinanceiroResumoService;
import br.com.athletix.security.AutorizacaoAtualService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/eventos/{eventoId}/financeiro")
public class EventoFinanceiroController {
    private final EventoFinanceiroResumoService resumoService;
    private final AutorizacaoAtualService autorizacao;

    public EventoFinanceiroController(EventoFinanceiroResumoService resumoService,
                                     AutorizacaoAtualService autorizacao) {
        this.resumoService = resumoService;
        this.autorizacao = autorizacao;
    }

    @GetMapping("/resumo")
    public EventoFinanceiroResumoResponse resumo(@PathVariable Long eventoId,
            @RequestParam(required = false) LocalDate dataReferencia,
            Authentication authentication, HttpServletRequest request) {
        return resumoService.resumir(eventoId, dataReferencia, autorizacao.membroAtual(authentication, request));
    }
}
