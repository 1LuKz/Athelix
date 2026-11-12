package br.com.athletix.financeiro.repository;

import br.com.athletix.financeiro.entity.EstornoFinanceiro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstornoFinanceiroRepository extends JpaRepository<EstornoFinanceiro, Long> {
    boolean existsByLancamento_IdAndAtletica_Id(Long lancamentoId, Long atleticaId);
    Optional<EstornoFinanceiro> findByLancamento_IdAndAtletica_Id(Long lancamentoId, Long atleticaId);
    List<EstornoFinanceiro> findAllByAtletica_Id(Long atleticaId);
}
