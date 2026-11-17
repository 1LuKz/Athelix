package br.com.athletix.auth.repository;

import br.com.athletix.auth.entity.FinalidadeTokenCredencial;
import br.com.athletix.auth.entity.TokenCredencial;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TokenCredencialRepository extends JpaRepository<TokenCredencial, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select token from TokenCredencial token where token.tokenHash = :tokenHash")
  Optional<TokenCredencial> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

  @Modifying
  @Query(
      """
      update TokenCredencial token set token.consumidoEm = :instante
      where token.usuario.id = :usuarioId and token.finalidade = :finalidade
        and token.consumidoEm is null and token.id <> :tokenId
      """)
  void consumirOutrosDisponiveis(
      @Param("usuarioId") Long usuarioId,
      @Param("finalidade") FinalidadeTokenCredencial finalidade,
      @Param("tokenId") Long tokenId,
      @Param("instante") Instant instante);
}
