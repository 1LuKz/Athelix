package br.com.athletix.documento.dto;

import br.com.athletix.documento.entity.VersaoDocumento;
import java.time.Instant;

public record VersaoDocumentoResponse(Long id, int numeroVersao, Long arquivoId, String nomeArquivo, Long gestaoId,
                                      String observacao, Long enviadoPorMembroId, Instant enviadoEm) {
    public static VersaoDocumentoResponse from(VersaoDocumento v, String nomeArquivo) {
        return new VersaoDocumentoResponse(v.getId(), v.getNumeroVersao(), v.getArquivoId(), nomeArquivo, v.getGestaoId(),
                v.getObservacao(), v.getEnviadoPorMembroId(), v.getEnviadoEm());
    }
}
