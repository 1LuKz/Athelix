package br.com.athletix.financeiro.repository;

import br.com.athletix.financeiro.entity.LancamentoFinanceiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface LancamentoFinanceiroRepository extends JpaRepository<LancamentoFinanceiro, Long>, JpaSpecificationExecutor<LancamentoFinanceiro> {
    Optional<LancamentoFinanceiro> findByIdAndAtletica_Id(Long id, Long atleticaId);
}
