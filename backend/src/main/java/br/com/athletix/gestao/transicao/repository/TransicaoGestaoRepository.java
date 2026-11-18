package br.com.athletix.gestao.transicao.repository;

import br.com.athletix.gestao.transicao.entity.TransicaoGestao;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface TransicaoGestaoRepository extends JpaRepository<TransicaoGestao, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select transicao from TransicaoGestao transicao where transicao.id = :transicaoId "
          + "and transicao.atletica.id = :atleticaId")
  Optional<TransicaoGestao> findByIdAndAtletica_IdForUpdate(Long transicaoId, Long atleticaId);

  Optional<TransicaoGestao> findByIdAndAtletica_Id(Long transicaoId, Long atleticaId);
}
