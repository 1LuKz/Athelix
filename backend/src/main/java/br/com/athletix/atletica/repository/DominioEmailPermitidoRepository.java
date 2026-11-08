package br.com.athletix.atletica.repository;

import br.com.athletix.atletica.entity.DominioEmailPermitido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DominioEmailPermitidoRepository extends JpaRepository<DominioEmailPermitido, Long> {

    boolean existsByAtletica_IdAndDominio(Long atleticaId, String dominio);

    Optional<DominioEmailPermitido> findByIdAndAtletica_Id(Long id, Long atleticaId);

    List<DominioEmailPermitido> findAllByAtletica_IdOrderByDominioAsc(Long atleticaId);
}
