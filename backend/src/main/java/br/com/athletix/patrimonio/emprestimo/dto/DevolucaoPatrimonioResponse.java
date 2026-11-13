package br.com.athletix.patrimonio.emprestimo.dto;
import br.com.athletix.patrimonio.emprestimo.entity.DevolucaoPatrimonio;
import java.time.Instant;
public record DevolucaoPatrimonioResponse(Long id, Long emprestimoId, int quantidade, Instant devolvidaEm,
        br.com.athletix.patrimonio.entity.ConservacaoPatrimonio condicaoRetorno, String observacao, Long registradoPorMembroId) {
    public static DevolucaoPatrimonioResponse from(DevolucaoPatrimonio d) { return new DevolucaoPatrimonioResponse(d.getId(), d.getEmprestimoId(),
            d.getQuantidade(), d.getDevolvidaEm(), d.getCondicaoRetorno(), d.getObservacao(), d.getRegistradoPorMembroId()); }
}
