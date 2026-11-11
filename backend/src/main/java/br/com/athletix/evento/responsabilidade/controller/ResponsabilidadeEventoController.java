package br.com.athletix.evento.responsabilidade.controller;

import br.com.athletix.evento.responsabilidade.dto.ResponsabilidadeEventoCreateRequest;
import br.com.athletix.evento.responsabilidade.dto.ResponsabilidadeEventoResponse;
import br.com.athletix.evento.responsabilidade.dto.ResponsabilidadeEventoUpdateRequest;
import br.com.athletix.evento.responsabilidade.service.ResponsabilidadeEventoService;
import br.com.athletix.evento.service.OrganizacaoEventoAuthorizationService;
import br.com.athletix.security.AutorizacaoAtualService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/eventos/{eventoId}/responsaveis")
public class ResponsabilidadeEventoController {
    private final ResponsabilidadeEventoService responsabilidadeService;
    private final OrganizacaoEventoAuthorizationService autorizacaoEvento;
    private final AutorizacaoAtualService autorizacaoAtual;

    public ResponsabilidadeEventoController(ResponsabilidadeEventoService responsabilidadeService,
                                            OrganizacaoEventoAuthorizationService autorizacaoEvento,
                                            AutorizacaoAtualService autorizacaoAtual) {
        this.responsabilidadeService = responsabilidadeService;
        this.autorizacaoEvento = autorizacaoEvento;
        this.autorizacaoAtual = autorizacaoAtual;
    }

    @GetMapping
    public List<ResponsabilidadeEventoResponse> listar(@PathVariable Long eventoId,
            @RequestParam(defaultValue = "false") boolean incluirHistorico,
            Authentication authentication, HttpServletRequest request) {
        autorizacaoEvento.exigirOrganizadorOuAdministrador(eventoId, authentication, request);
        return responsabilidadeService.listar(eventoId, incluirHistorico).stream()
                .map(ResponsabilidadeEventoResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<ResponsabilidadeEventoResponse> criar(@PathVariable Long eventoId,
            @Valid @RequestBody ResponsabilidadeEventoCreateRequest body,
            Authentication authentication, HttpServletRequest request) {
        autorizacaoEvento.exigirOrganizadorOuAdministrador(eventoId, authentication, request);
        return ResponseEntity.status(201).body(ResponsabilidadeEventoResponse.from(
                responsabilidadeService.criar(eventoId, body)));
    }

    @PutMapping("/{responsabilidadeId}")
    public ResponsabilidadeEventoResponse substituir(@PathVariable Long eventoId, @PathVariable Long responsabilidadeId,
            @Valid @RequestBody ResponsabilidadeEventoUpdateRequest body,
            Authentication authentication, HttpServletRequest request) {
        autorizacaoEvento.exigirOrganizadorOuAdministrador(eventoId, authentication, request);
        return ResponsabilidadeEventoResponse.from(responsabilidadeService.substituir(eventoId, responsabilidadeId,
                body, autorizacaoAtual.membroAtual(authentication, request)));
    }

    @DeleteMapping("/{responsabilidadeId}")
    public ResponseEntity<Void> remover(@PathVariable Long eventoId, @PathVariable Long responsabilidadeId,
            @RequestParam @PositiveOrZero Long version, Authentication authentication, HttpServletRequest request) {
        autorizacaoEvento.exigirOrganizadorOuAdministrador(eventoId, authentication, request);
        responsabilidadeService.remover(eventoId, responsabilidadeId, version,
                autorizacaoAtual.membroAtual(authentication, request));
        return ResponseEntity.noContent().build();
    }
}
