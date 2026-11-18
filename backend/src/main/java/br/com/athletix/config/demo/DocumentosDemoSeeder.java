package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.*;

import br.com.athletix.arquivo.entity.Arquivo;
import br.com.athletix.arquivo.repository.ArquivoRepository;
import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.auditoria.entity.AtividadeUsuario;
import br.com.athletix.auditoria.repository.AtividadeUsuarioRepository;
import br.com.athletix.documento.entity.CategoriaDocumento;
import br.com.athletix.documento.entity.Documento;
import br.com.athletix.documento.entity.VersaoDocumento;
import br.com.athletix.documento.entity.VisibilidadeDocumento;
import br.com.athletix.documento.repository.DocumentoRepository;
import br.com.athletix.documento.repository.VersaoDocumentoRepository;
import br.com.athletix.gestao.entity.Gestao;
import br.com.athletix.membro.entity.Membro;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class DocumentosDemoSeeder {
  private final DocumentoRepository documentoRepository;
  private final VersaoDocumentoRepository versaoDocumentoRepository;
  private final ArquivoRepository arquivoRepository;
  private final AtividadeUsuarioRepository atividadeRepository;

  DocumentosDemoSeeder(
      DocumentoRepository documentoRepository,
      VersaoDocumentoRepository versaoDocumentoRepository,
      ArquivoRepository arquivoRepository,
      AtividadeUsuarioRepository atividadeRepository) {
    this.documentoRepository = documentoRepository;
    this.versaoDocumentoRepository = versaoDocumentoRepository;
    this.arquivoRepository = arquivoRepository;
    this.atividadeRepository = atividadeRepository;
  }

  void criar(Atletica engenharia, Gestao atual, Membro administradorMembro, Instant agora) {
    Documento documento =
        documentoRepository.save(
            new Documento(
                engenharia,
                REGULAMENTO_DE_DEMONSTRACAO,
                CategoriaDocumento.REGIMENTO,
                DOCUMENTO_SINTETICO_SEM_DADOS_PESSOAIS,
                VisibilidadeDocumento.MEMBROS,
                atual.getId()));
    Arquivo arquivoInicial =
        arquivoRepository.save(
            arquivo(
                engenharia, administradorMembro, REGULAMENTO_V1_PDF, CONTEUDO_DEMONSTRATIVO_V1));
    Arquivo arquivoAtualizado =
        arquivoRepository.save(
            arquivo(
                engenharia, administradorMembro, REGULAMENTO_V2_PDF, CONTEUDO_DEMONSTRATIVO_V2));
    versaoDocumentoRepository.save(
        new VersaoDocumento(
            engenharia,
            documento,
            NUMERO_VERSAO_INICIAL,
            arquivoInicial.getId(),
            atual.getId(),
            VERSAO_INICIAL,
            administradorMembro.getId(),
            agora.minus(Duration.ofDays(PUBLICACAO_VERSAO_INICIAL_DIAS))));
    versaoDocumentoRepository.save(
        new VersaoDocumento(
            engenharia,
            documento,
            NUMERO_VERSAO_ATUALIZADA,
            arquivoAtualizado.getId(),
            atual.getId(),
            ATUALIZACAO_DE_DEMONSTRACAO,
            administradorMembro.getId(),
            agora));
    atividadeRepository.save(
        new AtividadeUsuario(
            engenharia.getId(),
            administradorMembro.getUsuarioId(),
            administradorMembro.getId(),
            CARGA_DEV,
            DOCUMENTO,
            documento.getId(),
            CARGA_DEMONSTRATIVA_INICIALIZADA,
            agora,
            atual.getId()));
  }

  private Arquivo arquivo(Atletica atletica, Membro enviadoPor, String nome, String conteudo) {
    // PDF mínimo sintético: nenhuma informação pessoal é incluída na carga.
    byte[] bytes = (CABECALHO_PDF + conteudo + RODAPE_PDF).getBytes(StandardCharsets.UTF_8);
    return new Arquivo(
        atletica,
        nome,
        APPLICATION_PDF,
        bytes.length,
        sha256(bytes),
        enviadoPor.getId(),
        Instant.now(),
        bytes);
  }

  private String sha256(byte[] bytes) {
    try {
      return java.util.HexFormat.of()
          .formatHex(MessageDigest.getInstance(ALGORITMO_HASH_PDF).digest(bytes));
    } catch (Exception exception) {
      throw new IllegalStateException(
          "Não foi possível gerar hash da carga de demonstração", exception);
    }
  }
}
