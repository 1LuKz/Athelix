package br.com.athletix.documento.repository;

import br.com.athletix.documento.entity.Documento;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface DocumentoRepository
    extends JpaRepository<Documento, Long>, JpaSpecificationExecutor<Documento> {
  Optional<Documento> findByIdAndAtletica_Id(Long id, Long atleticaId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select d from Documento d where d.id = :id and d.atletica.id = :atleticaId")
  Optional<Documento> findByIdAndAtletica_IdForUpdate(Long id, Long atleticaId);
}
