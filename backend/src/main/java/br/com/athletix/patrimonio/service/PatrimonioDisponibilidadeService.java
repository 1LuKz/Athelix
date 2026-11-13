package br.com.athletix.patrimonio.service;

import br.com.athletix.patrimonio.emprestimo.entity.EmprestimoPatrimonio;
import br.com.athletix.patrimonio.emprestimo.repository.DevolucaoPatrimonioRepository;
import br.com.athletix.patrimonio.emprestimo.repository.EmprestimoPatrimonioRepository;
import br.com.athletix.patrimonio.manutencao.repository.ManutencaoPatrimonioRepository;
import org.springframework.stereotype.Service;

/** Fonte única da disponibilidade; nenhum contador é gravado no item. */
@Service
public class PatrimonioDisponibilidadeService {
    private final EmprestimoPatrimonioRepository emprestimos;
    private final DevolucaoPatrimonioRepository devolucoes;
    private final ManutencaoPatrimonioRepository manutencoes;
    public PatrimonioDisponibilidadeService(EmprestimoPatrimonioRepository emprestimos, DevolucaoPatrimonioRepository devolucoes,
            ManutencaoPatrimonioRepository manutencoes) {
        this.emprestimos = emprestimos; this.devolucoes = devolucoes; this.manutencoes = manutencoes;
    }
    public Disponibilidade calcular(Long itemId, Long atleticaId, int quantidadeTotal) {
        int emprestada = 0;
        for (EmprestimoPatrimonio emprestimo : emprestimos.findAllByItem_IdAndAtletica_Id(itemId, atleticaId)) {
            int devolvida = devolucoes.findAllByEmprestimo_Id(emprestimo.getId()).stream().mapToInt(d -> d.getQuantidade()).sum();
            emprestada += emprestimo.getQuantidade() - devolvida;
        }
        int manutencao = manutencoes.findAllByItem_IdAndAtletica_IdAndConcluidaEmIsNull(itemId, atleticaId)
                .stream().mapToInt(m -> m.getQuantidade()).sum();
        return new Disponibilidade(emprestada, manutencao, quantidadeTotal - emprestada - manutencao);
    }
    public record Disponibilidade(int quantidadeEmprestada, int quantidadeEmManutencao, int quantidadeDisponivel) { }
}
