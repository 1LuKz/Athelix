package br.com.athletix.arquivo.service;

import br.com.athletix.arquivo.dto.ArquivoMetadata;
import br.com.athletix.membro.entity.Membro;

/** Implementado pelos módulos que possuem o recurso pai (documento, divulgação, evento etc.). */
public interface ArquivoAcessoAoConteudo {
    DecisaoLeitura decidirLeitura(ArquivoMetadata arquivo, Membro membro);

    enum DecisaoLeitura {
        NAO_SE_APLICA,
        PERMITIR,
        NEGAR
    }
}
