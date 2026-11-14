package br.com.athletix.documento.service;

import br.com.athletix.arquivo.dto.ArquivoMetadata;
import br.com.athletix.arquivo.service.ArquivoAcessoAoConteudo;
import br.com.athletix.documento.entity.Documento;
import br.com.athletix.documento.entity.VersaoDocumento;
import br.com.athletix.documento.repository.DocumentoRepository;
import br.com.athletix.documento.repository.VersaoDocumentoRepository;
import br.com.athletix.membro.entity.Membro;
import org.springframework.stereotype.Service;
import java.util.List;

/** Garante que o BLOB de qualquer versão siga a visibilidade do documento lógico. */
@Service
public class DocumentoArquivoAcessoAoConteudo implements ArquivoAcessoAoConteudo {
    private final VersaoDocumentoRepository versoes;
    private final DocumentoRepository documentos;
    private final DocumentoService documentosService;
    public DocumentoArquivoAcessoAoConteudo(VersaoDocumentoRepository versoes, DocumentoRepository documentos, DocumentoService documentosService) {
        this.versoes=versoes; this.documentos=documentos; this.documentosService=documentosService;
    }
    @Override public DecisaoLeitura decidirLeitura(ArquivoMetadata arquivo, Membro membro) {
        List<VersaoDocumento> vinculos=versoes.findAllByArquivoIdAndAtletica_Id(arquivo.id(),arquivo.atleticaId());
        if(vinculos.isEmpty()) return DecisaoLeitura.NAO_SE_APLICA;
        if(membro==null || !arquivo.atleticaId().equals(membro.getAtleticaId())) return DecisaoLeitura.NEGAR;
        for(VersaoDocumento versao:vinculos) {
            Documento documento=documentos.findByIdAndAtletica_Id(versao.getDocumentoId(),arquivo.atleticaId()).orElse(null);
            if(documento!=null && documentosService.podeLer(documento,membro)) return DecisaoLeitura.PERMITIR;
        }
        return DecisaoLeitura.NEGAR;
    }
}
