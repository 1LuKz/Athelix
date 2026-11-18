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
      Authentication authentication,
      HttpServletRequest request) {
    return service.listar(
        busca, categoria, gestaoId, incluirArquivados, auth.membroAtual(authentication, request));
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
      Authentication authentication,
      HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            service.criar(
                new DocumentoCreateFields(
                    titulo, categoria, descricao, visibilidade, gestaoOrigemId, observacao),
                arquivo,
                auth.membroAtual(authentication, request)));
  }

  @GetMapping("/{id}")
  public DocumentoResponse obter(
      @PathVariable("id") Long documentoId,
      Authentication authentication,
      HttpServletRequest request) {
    return service.obter(documentoId, auth.membroAtual(authentication, request));
  }

  @PutMapping("/{id}")
  public DocumentoResponse atualizar(
      @PathVariable("id") Long documentoId,
      @Valid @RequestBody DocumentoUpdateRequest corpo,
      Authentication authentication,
      HttpServletRequest request) {
    return service.atualizar(documentoId, corpo, auth.membroAtual(authentication, request));
  }

  @GetMapping("/{id}/versoes")
  public List<VersaoDocumentoResponse> listarVersoes(
      @PathVariable("id") Long documentoId,
      Authentication authentication,
      HttpServletRequest request) {
    return service.listarVersoes(documentoId, auth.membroAtual(authentication, request));
  }

  @PostMapping(value = "/{id}/versoes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<VersaoDocumentoResponse> novaVersao(
      @PathVariable("id") Long documentoId,
      @RequestParam("arquivo") MultipartFile arquivo,
      @RequestParam(required = false) Long gestaoId,
      @RequestParam(required = false) String observacao,
      Authentication authentication,
      HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            service.novaVersao(
                documentoId,
                new VersaoDocumentoCreateFields(gestaoId, observacao),
                arquivo,
                auth.membroAtual(authentication, request)));
  }

  @GetMapping("/{id}/versoes/{versaoId}/conteudo")
  public ResponseEntity<byte[]> conteudo(
      @PathVariable("id") Long documentoId,
      @PathVariable Long versaoId,
      Authentication authentication,
      HttpServletRequest request) {
    var membro = auth.membroAtual(authentication, request);
    var arquivo = leitura.abrir(service.arquivoDaVersao(documentoId, versaoId, membro), membro);
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
