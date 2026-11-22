package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.*;

import br.com.athletix.documento.dto.DocumentoCreateFields;
import br.com.athletix.documento.dto.DocumentoResponse;
import br.com.athletix.documento.dto.VersaoDocumentoCreateFields;
import br.com.athletix.documento.entity.VisibilidadeDocumento;
import br.com.athletix.documento.service.DocumentoService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class DocumentosDemoSeeder {
  private final DocumentoService documentos;

  DocumentosDemoSeeder(DocumentoService documentos) {
    this.documentos = documentos;
  }

  void criar(CenarioDemo cenario) {
    for (DemoData.DocumentoDemo dados : DOCUMENTOS_DEMO) {
      DocumentoResponse documento =
          documentos.criar(
              new DocumentoCreateFields(
                  dados.titulo(),
                  dados.categoria(),
                  DOCUMENTO_SINTETICO_SEM_DADOS_PESSOAIS,
                  VisibilidadeDocumento.MEMBROS,
                  cenario.gestao().getId(),
                  VERSAO_INICIAL),
              ArquivoSintetico.pdf(dados.nomeArquivo() + "-v1.pdf", CONTEUDO_DEMONSTRATIVO_V1),
              cenario.administrador());
      if (dados.duasVersoes()) {
        documentos.novaVersao(
            documento.id(),
            new VersaoDocumentoCreateFields(cenario.gestao().getId(), ATUALIZACAO_DE_DEMONSTRACAO),
            ArquivoSintetico.pdf(dados.nomeArquivo() + "-v2.pdf", CONTEUDO_DEMONSTRATIVO_V2),
            cenario.administrador());
      }
    }
  }
}
