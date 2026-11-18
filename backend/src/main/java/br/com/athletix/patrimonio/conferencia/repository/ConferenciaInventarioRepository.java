package br.com.athletix.patrimonio.conferencia.repository;

import br.com.athletix.patrimonio.conferencia.entity.ConferenciaInventario;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface ConferenciaInventarioRepository
    extends JpaRepository<ConferenciaInventario, Long> {
  List<ConferenciaInventario> findAllByAtletica_IdOrderByIniciadaEmDesc(Long atleticaId);

  Optional<ConferenciaInventario> findByIdAndAtletica_Id(Long conferenciaId, Long atleticaId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select conferencia from ConferenciaInventario conferencia where conferencia.id = :conferenciaId "
          + "and conferencia.atletica.id = :atleticaId")
  Optional<ConferenciaInventario> findByIdAndAtletica_IdForUpdate(
      Long conferenciaId, Long atleticaId);
}
