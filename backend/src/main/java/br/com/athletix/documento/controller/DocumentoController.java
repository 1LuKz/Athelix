package br.com.athletix.documento.controller;

import br.com.athletix.arquivo.service.ArquivoLeituraProtegidaService;
import br.com.athletix.documento.dto.*;
import br.com.athletix.documento.entity.*;
import br.com.athletix.documento.service.DocumentoService;
import br.com.athletix.security.AutorizacaoAtualService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/documentos")
public class DocumentoController {
  private final DocumentoService service;
  private final AutorizacaoAtualService auth;
  private final ArquivoLeituraProtegidaService leitura;

  public DocumentoController(
      DocumentoService service,
      AutorizacaoAtualService auth,
      ArquivoLeituraProtegidaService leitura) {
    this.service = service;
    this.auth = auth;
    this.leitura = leitura;
  }

  @GetMapping
  public List<DocumentoResponse> listar(
      @RequestParam(required = false) String busca,
      @RequestParam(required = false) CategoriaDocumento categoria,
      @RequestParam(required = false) Long gestaoId,
      @RequestParam(defaultValue = "false") boolean incluirArquivados,
      Authentication a,
      HttpServletRequest r) {
    return service.listar(busca, categoria, gestaoId, incluirArquivados, auth.membroAtual(a, r));
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<DocumentoResponse> criar(
      @RequestParam String titulo,
      @RequestParam CategoriaDocumento categoria,
      @RequestParam(required = false) String descricao,
      @RequestParam VisibilidadeDocumento visibilidade,
      @RequestParam(required = false) Long gestaoOrigemId,
      @RequestParam(required = false) String observacao,
      @RequestParam("arquivo") MultipartFile arquivo,
      Authentication a,
      HttpServletRequest r) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            service.criar(
                new DocumentoCreateFields(
                    titulo, categoria, descricao, visibilidade, gestaoOrigemId, observacao),
                arquivo,
                auth.membroAtual(a, r)));
  }

  @GetMapping("/{id}")
  public DocumentoResponse obter(@PathVariable Long id, Authentication a, HttpServletRequest r) {
    return service.obter(id, auth.membroAtual(a, r));
  }

  @PutMapping("/{id}")
  public DocumentoResponse atualizar(
      @PathVariable Long id,
      @Valid @RequestBody DocumentoUpdateRequest corpo,
      Authentication a,
      HttpServletRequest r) {
    return service.atualizar(id, corpo, auth.membroAtual(a, r));
  }

  @GetMapping("/{id}/versoes")
  public List<VersaoDocumentoResponse> listarVersoes(
      @PathVariable Long id, Authentication a, HttpServletRequest r) {
    return service.listarVersoes(id, auth.membroAtual(a, r));
  }

  @PostMapping(value = "/{id}/versoes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<VersaoDocumentoResponse> novaVersao(
      @PathVariable Long id,
      @RequestParam("arquivo") MultipartFile arquivo,
      @RequestParam(required = false) Long gestaoId,
      @RequestParam(required = false) String observacao,
      Authentication a,
      HttpServletRequest r) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            service.novaVersao(
                id,
                new VersaoDocumentoCreateFields(gestaoId, observacao),
                arquivo,
                auth.membroAtual(a, r)));
  }

  @GetMapping("/{id}/versoes/{versaoId}/conteudo")
  public ResponseEntity<byte[]> conteudo(
      @PathVariable Long id, @PathVariable Long versaoId, Authentication a, HttpServletRequest r) {
    var membro = auth.membroAtual(a, r);
    var arquivo = leitura.abrir(service.arquivoDaVersao(id, versaoId, membro), membro);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(arquivo.metadata().mimeType()))
        .contentLength(arquivo.conteudo().length)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.inline()
                .filename(arquivo.metadata().nomeOriginal(), StandardCharsets.UTF_8)
                .build()
                .toString())
        .header("X-Content-Type-Options", "nosniff")
        .cacheControl(CacheControl.noStore().cachePrivate())
        .body(arquivo.conteudo());
  }
}
