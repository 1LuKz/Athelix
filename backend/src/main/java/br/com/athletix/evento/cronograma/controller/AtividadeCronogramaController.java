package br.com.athletix.evento.cronograma.controller;

import br.com.athletix.evento.cronograma.dto.AtividadeCronogramaCreateRequest;
import br.com.athletix.evento.cronograma.dto.AtividadeCronogramaResponse;
import br.com.athletix.evento.cronograma.dto.AtividadeCronogramaUpdateRequest;
import br.com.athletix.evento.cronograma.service.AtividadeCronogramaService;
import br.com.athletix.evento.service.OrganizacaoEventoAuthorizationService;
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
@RequestMapping("/eventos/{eventoId}/cronograma")
public class AtividadeCronogramaController {
    private final AtividadeCronogramaService cronogramaService;
    private final OrganizacaoEventoAuthorizationService autorizacaoEvento;

    public AtividadeCronogramaController(AtividadeCronogramaService cronogramaService,
                                         OrganizacaoEventoAuthorizationService autorizacaoEvento) {
        this.cronogramaService = cronogramaService;
        this.autorizacaoEvento = autorizacaoEvento;
    }

    @GetMapping
    public List<AtividadeCronogramaResponse> listar(@PathVariable Long eventoId, Authentication authentication,
                                                     HttpServletRequest request) {
        autorizacaoEvento.exigirOrganizadorOuAdministrador(eventoId, authentication, request);
        return cronogramaService.listar(eventoId).stream().map(AtividadeCronogramaResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<AtividadeCronogramaResponse> criar(@PathVariable Long eventoId,
            @Valid @RequestBody AtividadeCronogramaCreateRequest body, Authentication authentication,
            HttpServletRequest request) {
        autorizacaoEvento.exigirOrganizadorOuAdministrador(eventoId, authentication, request);
        return ResponseEntity.status(201).body(AtividadeCronogramaResponse.from(cronogramaService.criar(eventoId, body)));
    }

    @PutMapping("/{atividadeId}")
    public AtividadeCronogramaResponse atualizar(@PathVariable Long eventoId, @PathVariable Long atividadeId,
            @Valid @RequestBody AtividadeCronogramaUpdateRequest body, Authentication authentication,
            HttpServletRequest request) {
        autorizacaoEvento.exigirOrganizadorOuAdministrador(eventoId, authentication, request);
        return AtividadeCronogramaResponse.from(cronogramaService.atualizar(eventoId, atividadeId, body));
    }

    @DeleteMapping("/{atividadeId}")
    public ResponseEntity<Void> remover(@PathVariable Long eventoId, @PathVariable Long atividadeId,
            @RequestParam @PositiveOrZero Long version, Authentication authentication, HttpServletRequest request) {
        autorizacaoEvento.exigirOrganizadorOuAdministrador(eventoId, authentication, request);
        cronogramaService.remover(eventoId, atividadeId, version);
        return ResponseEntity.noContent().build();
    }
}
