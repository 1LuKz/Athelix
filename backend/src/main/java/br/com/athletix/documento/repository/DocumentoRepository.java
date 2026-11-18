package br.com.athletix.documento.repository;

import br.com.athletix.documento.entity.Documento;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface DocumentoRepository
    extends JpaRepository<Documento, Long>, JpaSpecificationExecutor<Documento> {
  Optional<Documento> findByIdAndAtletica_Id(Long documentoId, Long atleticaId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select documento from Documento documento where documento.id = :documentoId "
          + "and documento.atletica.id = :atleticaId")
  Optional<Documento> findByIdAndAtletica_IdForUpdate(
      @Param("documentoId") Long documentoId, @Param("atleticaId") Long atleticaId);
}
