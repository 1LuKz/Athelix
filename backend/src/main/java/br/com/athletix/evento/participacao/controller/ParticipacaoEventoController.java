package br.com.athletix.evento.participacao.controller;

import br.com.athletix.evento.participacao.dto.ParticipacaoEventoCreateRequest;
import br.com.athletix.evento.participacao.dto.ParticipacaoEventoResponse;
import br.com.athletix.evento.participacao.dto.ParticipacaoEventoResumoResponse;
import br.com.athletix.evento.participacao.dto.ParticipacaoEventoStatusUpdateRequest;
import br.com.athletix.evento.participacao.service.ParticipacaoEventoService;
import br.com.athletix.evento.service.OrganizacaoEventoAuthorizationService;
import br.com.athletix.shared.pagination.PageQuery;
import br.com.athletix.shared.pagination.PageResponse;
import br.com.athletix.shared.pagination.PageableFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/eventos/{eventoId}/participacoes")
public class ParticipacaoEventoController {
    private static final Map<String, String> ORDENACOES = Map.of(
            "status", "status", "inscritoEm", "inscritoEm"
    );

    private final ParticipacaoEventoService participacaoService;
    private final OrganizacaoEventoAuthorizationService autorizacaoEvento;
    private final PageableFactory pageableFactory;

    public ParticipacaoEventoController(ParticipacaoEventoService participacaoService,
                                        OrganizacaoEventoAuthorizationService autorizacaoEvento,
                                        PageableFactory pageableFactory) {
        this.participacaoService = participacaoService;
        this.autorizacaoEvento = autorizacaoEvento;
        this.pageableFactory = pageableFactory;
    }

    @GetMapping
    public PageResponse<ParticipacaoEventoResponse> listar(@PathVariable Long eventoId, @Valid PageQuery pageQuery,
                                                            Authentication authentication, HttpServletRequest request) {
        autorizacaoEvento.exigirOrganizadorOuAdministrador(eventoId, authentication, request);
        return PageResponse.from(participacaoService.listar(eventoId, pageableFactory.create(pageQuery, ORDENACOES)),
                ParticipacaoEventoResponse::from);
    }

    @PostMapping
    public ResponseEntity<ParticipacaoEventoResponse> criar(@PathVariable Long eventoId,
            @Valid @RequestBody ParticipacaoEventoCreateRequest body,
            Authentication authentication, HttpServletRequest request) {
        autorizacaoEvento.exigirOrganizadorOuAdministrador(eventoId, authentication, request);
        return ResponseEntity.status(201).body(ParticipacaoEventoResponse.from(participacaoService.criar(eventoId, body)));
    }

    @PatchMapping("/{participacaoId}/status")
    public ParticipacaoEventoResponse alterarStatus(@PathVariable Long eventoId, @PathVariable Long participacaoId,
            @Valid @RequestBody ParticipacaoEventoStatusUpdateRequest body,
            Authentication authentication, HttpServletRequest request) {
        autorizacaoEvento.exigirOrganizadorOuAdministrador(eventoId, authentication, request);
        return ParticipacaoEventoResponse.from(participacaoService.alterarStatus(eventoId, participacaoId, body));
    }

    @GetMapping("/resumo")
    public ParticipacaoEventoResumoResponse resumo(@PathVariable Long eventoId,
                                                    Authentication authentication, HttpServletRequest request) {
        autorizacaoEvento.exigirOrganizadorOuAdministrador(eventoId, authentication, request);
        return participacaoService.resumo(eventoId);
    }
}
