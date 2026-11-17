package br.com.athletix.evento.anexo.controller;

import br.com.athletix.evento.anexo.dto.AnexoEventoResponse;
import br.com.athletix.evento.anexo.entity.TipoMaterialEvento;
import br.com.athletix.evento.anexo.service.AnexoEventoService;
import br.com.athletix.evento.service.OrganizacaoEventoAuthorizationService;
import br.com.athletix.security.AutorizacaoAtualService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
@RequestMapping("/eventos/{eventoId}/anexos")
public class AnexoEventoController {
  private final AnexoEventoService anexos;
  private final OrganizacaoEventoAuthorizationService autorizacaoEvento;
  private final AutorizacaoAtualService autorizacaoAtual;

  public AnexoEventoController(
      AnexoEventoService anexos,
      OrganizacaoEventoAuthorizationService autorizacaoEvento,
      AutorizacaoAtualService autorizacaoAtual) {
    this.anexos = anexos;
    this.autorizacaoEvento = autorizacaoEvento;
    this.autorizacaoAtual = autorizacaoAtual;
  }

  @GetMapping
  public List<AnexoEventoResponse> listar(
      @PathVariable Long eventoId, Authentication authentication, HttpServletRequest request) {
    autorizacaoEvento.exigirOrganizadorOuAdministrador(eventoId, authentication, request);
    return anexos.listar(eventoId).stream().map(AnexoEventoResponse::from).toList();
  }

  @PostMapping(consumes = "multipart/form-data")
  public ResponseEntity<AnexoEventoResponse> criar(
      @PathVariable Long eventoId,
      @RequestParam("arquivo") MultipartFile arquivo,
      @RequestParam @NotBlank @Size(max = 160) String titulo,
      @RequestParam(required = false) @Size(max = 1000) String descricao,
      @RequestParam TipoMaterialEvento tipoMaterial,
      Authentication authentication,
      HttpServletRequest request) {
    autorizacaoEvento.exigirOrganizadorOuAdministrador(eventoId, authentication, request);
    return ResponseEntity.status(201)
        .body(
            AnexoEventoResponse.from(
                anexos.criar(
                    eventoId,
                    arquivo,
                    titulo,
                    descricao,
                    tipoMaterial,
                    autorizacaoAtual.membroAtual(authentication, request))));
  }

  @DeleteMapping("/{anexoId}")
  public ResponseEntity<Void> remover(
      @PathVariable Long eventoId,
      @PathVariable Long anexoId,
      @RequestParam @PositiveOrZero Long version,
      Authentication authentication,
      HttpServletRequest request) {
    autorizacaoEvento.exigirOrganizadorOuAdministrador(eventoId, authentication, request);
    anexos.remover(eventoId, anexoId, version);
    return ResponseEntity.noContent().build();
  }
}
