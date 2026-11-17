package br.com.athletix.arquivo.controller;

import br.com.athletix.arquivo.service.ArquivoLeituraProtegidaService;
import br.com.athletix.security.AutorizacaoAtualService;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/arquivos")
public class ArquivoController {
  private final ArquivoLeituraProtegidaService leituraProtegida;
  private final AutorizacaoAtualService autorizacao;

  public ArquivoController(
      ArquivoLeituraProtegidaService leituraProtegida, AutorizacaoAtualService autorizacao) {
    this.leituraProtegida = leituraProtegida;
    this.autorizacao = autorizacao;
  }

  @GetMapping("/{id}/conteudo")
  public ResponseEntity<byte[]> conteudo(
      @PathVariable Long id, Authentication authentication, HttpServletRequest request) {
    ArquivoLeituraProtegidaService.ArquivoConteudo arquivo =
        leituraProtegida.abrir(id, autorizacao.membroAtual(authentication, request));
    boolean pdf = "application/pdf".equals(arquivo.metadata().mimeType());
    ContentDisposition disposition =
        (pdf ? ContentDisposition.inline() : ContentDisposition.attachment())
            .filename(arquivo.metadata().nomeOriginal(), StandardCharsets.UTF_8)
            .build();
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(arquivo.metadata().mimeType()))
        .contentLength(arquivo.conteudo().length)
        .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
        .header("X-Content-Type-Options", "nosniff")
        .cacheControl(CacheControl.noStore().cachePrivate())
        .body(arquivo.conteudo());
  }
}
