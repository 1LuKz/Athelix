package br.com.athletix.cargo.repository;

import br.com.athletix.cargo.entity.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CargoRepository extends JpaRepository<Cargo, Long> {

    boolean existsByAtletica_IdAndNomeNormalizado(Long atleticaId, String nomeNormalizado);

    Optional<Cargo> findByIdAndAtletica_Id(Long id, Long atleticaId);

    Optional<Cargo> findByAtletica_IdAndNomeNormalizado(Long atleticaId, String nomeNormalizado);

    List<Cargo> findAllByAtletica_IdOrderByNomeAsc(Long atleticaId);
}
