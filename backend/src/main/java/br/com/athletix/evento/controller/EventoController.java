package br.com.athletix.evento.controller;

import br.com.athletix.evento.dto.EventoCreateRequest;
import br.com.athletix.evento.dto.EventoPageResponse;
import br.com.athletix.evento.dto.EventoResponse;
import br.com.athletix.evento.dto.EventoStatusUpdateRequest;
import br.com.athletix.evento.dto.EventoUpdateRequest;
import br.com.athletix.evento.entity.GrupoStatusEvento;
import br.com.athletix.evento.entity.StatusEvento;
import br.com.athletix.evento.service.EventoConsultaService;
import br.com.athletix.evento.service.EventoService;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.security.AutorizacaoAtualService;
import br.com.athletix.shared.pagination.PageQuery;
import br.com.athletix.shared.pagination.PageableFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/eventos")
public class EventoController {
    private static final Map<String, String> ORDENACOES = Map.of(
            "nome", "nome", "inicio", "inicio", "fim", "fim", "status", "status",
            "orcamentoPrevisto", "orcamentoPrevisto", "createdAt", "createdAt"
    );

    private final EventoService eventoService;
    private final EventoConsultaService consultaService;
    private final AutorizacaoAtualService autorizacao;
    private final PageableFactory pageableFactory;

    public EventoController(EventoService eventoService, EventoConsultaService consultaService,
                            AutorizacaoAtualService autorizacao, PageableFactory pageableFactory) {
        this.eventoService = eventoService;
        this.consultaService = consultaService;
        this.autorizacao = autorizacao;
        this.pageableFactory = pageableFactory;
    }

    @GetMapping
    public EventoPageResponse listar(@RequestParam(required = false) String busca,
                                      @RequestParam(required = false) OffsetDateTime inicioDe,
                                      @RequestParam(required = false) OffsetDateTime inicioAte,
                                      @RequestParam(required = false) StatusEvento status,
                                      @RequestParam(required = false) GrupoStatusEvento grupoStatus,
                                      @RequestParam(required = false) Long gestaoId,
                                      @Valid PageQuery pageQuery,
                                      Authentication authentication, HttpServletRequest request) {
        autorizacao.membroAtual(authentication, request);
        EventoConsultaService.Resultado resultado = consultaService.listar(busca, inicioDe, inicioAte, status,
                grupoStatus, gestaoId, pageableFactory.create(pageQuery, ORDENACOES));
        return EventoPageResponse.from(resultado.pagina(), resultado.contagensPorStatus(), resultado.totalAtivos());
    }

    @GetMapping("/{id}")
    public EventoResponse buscar(@PathVariable Long id, Authentication authentication, HttpServletRequest request) {
        autorizacao.membroAtual(authentication, request);
        return EventoResponse.from(eventoService.buscarDaAtleticaAtual(id));
    }

    @PostMapping
    public ResponseEntity<EventoResponse> criar(@Valid @RequestBody EventoCreateRequest body,
                                                 Authentication authentication, HttpServletRequest request) {
        autorizacao.exigirAdministrador(authentication, request);
        Membro criador = autorizacao.membroAtual(authentication, request);
        return ResponseEntity.status(201).body(EventoResponse.from(eventoService.criar(body, criador)));
    }

    @PutMapping("/{id}")
    public EventoResponse atualizar(@PathVariable Long id, @Valid @RequestBody EventoUpdateRequest body,
                                    Authentication authentication, HttpServletRequest request) {
        autorizacao.exigirAdministrador(authentication, request);
        return EventoResponse.from(eventoService.atualizar(id, body));
    }

    @PatchMapping("/{id}/status")
    public EventoResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody EventoStatusUpdateRequest body,
                                        Authentication authentication, HttpServletRequest request) {
        autorizacao.exigirAdministrador(authentication, request);
        return EventoResponse.from(eventoService.alterarStatus(id, body, autorizacao.perfil(authentication, request)));
    }
}
