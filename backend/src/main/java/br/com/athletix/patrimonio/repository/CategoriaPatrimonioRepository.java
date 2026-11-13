package br.com.athletix.patrimonio.repository;

import br.com.athletix.patrimonio.entity.CategoriaPatrimonio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoriaPatrimonioRepository extends JpaRepository<CategoriaPatrimonio, Long> {
    boolean existsByAtletica_IdAndNomeNormalizado(Long atleticaId, String nomeNormalizado);
    Optional<CategoriaPatrimonio> findByIdAndAtletica_Id(Long id, Long atleticaId);
    List<CategoriaPatrimonio> findAllByAtletica_IdOrderByNomeAsc(Long atleticaId);
}
