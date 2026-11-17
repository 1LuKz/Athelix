package br.com.athletix.financeiro.repository;

import br.com.athletix.financeiro.entity.LancamentoFinanceiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LancamentoFinanceiroRepository extends JpaRepository<LancamentoFinanceiro, Long>, JpaSpecificationExecutor<LancamentoFinanceiro> {
    List<LancamentoFinanceiro> findByAtletica_IdAndStatusLiquidacaoAndDataLiquidacaoIsNotNullOrderByDataLiquidacaoDescIdDesc(
            Long atleticaId, br.com.athletix.financeiro.entity.StatusLiquidacao status, Pageable pageable);
    Optional<LancamentoFinanceiro> findByIdAndAtletica_Id(Long id, Long atleticaId);
    List<LancamentoFinanceiro> findAllByAtletica_Id(Long atleticaId);
    List<LancamentoFinanceiro> findAllByAtletica_IdAndEvento_Id(Long atleticaId, Long eventoId);
    Page<LancamentoFinanceiro> findAllByAtletica_IdAndStatusLiquidacao(Long atleticaId,
                                                                         br.com.athletix.financeiro.entity.StatusLiquidacao status,
                                                                         Pageable pageable);
    Page<LancamentoFinanceiro> findAllByAtletica_IdAndStatusLiquidacaoAndStatusAprovacaoNot(Long atleticaId,
            br.com.athletix.financeiro.entity.StatusLiquidacao statusLiquidacao,
            br.com.athletix.financeiro.entity.StatusAprovacao statusAprovacao, Pageable pageable);
}
