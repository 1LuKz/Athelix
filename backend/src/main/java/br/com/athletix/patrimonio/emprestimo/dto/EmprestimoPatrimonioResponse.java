package br.com.athletix.patrimonio.emprestimo.dto;
import br.com.athletix.patrimonio.emprestimo.entity.EmprestimoPatrimonio;
import java.time.Instant;
public record EmprestimoPatrimonioResponse(Long id, Long itemId, Long membroResponsavelId, int quantidade,
        int quantidadeDevolvida, int saldoAberto, Instant retiradoEm, Instant devolucaoPrevistaEm, String observacao,
        Long registradoPorMembroId, Instant createdAt, Instant updatedAt, Long version) {
    public static EmprestimoPatrimonioResponse from(EmprestimoPatrimonio e, int devolvida) { return new EmprestimoPatrimonioResponse(e.getId(),
            e.getItemId(), e.getMembroResponsavelId(), e.getQuantidade(), devolvida, e.getQuantidade() - devolvida,
            e.getRetiradoEm(), e.getDevolucaoPrevistaEm(), e.getObservacao(), e.getRegistradoPorMembroId(), e.getCreatedAt(), e.getUpdatedAt(), e.getVersion()); }
}
