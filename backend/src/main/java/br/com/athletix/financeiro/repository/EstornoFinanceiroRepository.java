package br.com.athletix.financeiro.repository;

import br.com.athletix.financeiro.entity.EstornoFinanceiro;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstornoFinanceiroRepository extends JpaRepository<EstornoFinanceiro, Long> {
  @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "lancamento")
  List<EstornoFinanceiro> findByAtletica_IdOrderByDataEstornoDescIdDesc(
      Long atleticaId, org.springframework.data.domain.Pageable pageable);

  boolean existsByLancamento_IdAndAtletica_Id(Long lancamentoId, Long atleticaId);

  Optional<EstornoFinanceiro> findByLancamento_IdAndAtletica_Id(Long lancamentoId, Long atleticaId);

  List<EstornoFinanceiro> findAllByAtletica_Id(Long atleticaId);
}
