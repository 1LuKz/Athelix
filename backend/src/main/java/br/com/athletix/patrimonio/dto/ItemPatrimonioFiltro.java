package br.com.athletix.patrimonio.dto;

import br.com.athletix.patrimonio.entity.ConservacaoPatrimonio;
import br.com.athletix.patrimonio.entity.SituacaoOperacionalPatrimonio;

public record ItemPatrimonioFiltro(String nome, Long categoriaId, ConservacaoPatrimonio conservacao,
                                   SituacaoOperacionalPatrimonio situacao) { }
