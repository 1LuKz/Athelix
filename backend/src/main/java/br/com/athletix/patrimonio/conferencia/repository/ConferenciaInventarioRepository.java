package br.com.athletix.patrimonio.conferencia.repository;

import br.com.athletix.patrimonio.conferencia.entity.ConferenciaInventario;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface ConferenciaInventarioRepository
    extends JpaRepository<ConferenciaInventario, Long> {
  List<ConferenciaInventario> findAllByAtletica_IdOrderByIniciadaEmDesc(Long a);

  Optional<ConferenciaInventario> findByIdAndAtletica_Id(Long i, Long a);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from ConferenciaInventario c where c.id=:id and c.atletica.id=:atleticaId")
  Optional<ConferenciaInventario> findByIdAndAtletica_IdForUpdate(Long id, Long atleticaId);
}
