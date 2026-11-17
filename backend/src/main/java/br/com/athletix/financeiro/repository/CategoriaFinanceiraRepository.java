package br.com.athletix.financeiro.repository;

import br.com.athletix.financeiro.entity.CategoriaFinanceira;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaFinanceiraRepository extends JpaRepository<CategoriaFinanceira, Long> {
  boolean existsByAtletica_IdAndNomeNormalizado(Long atleticaId, String nomeNormalizado);

  Optional<CategoriaFinanceira> findByIdAndAtletica_Id(Long id, Long atleticaId);

  List<CategoriaFinanceira> findAllByAtletica_IdOrderByNomeAsc(Long atleticaId);
}
