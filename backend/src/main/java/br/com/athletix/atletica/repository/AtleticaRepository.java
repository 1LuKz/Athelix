package br.com.athletix.atletica.repository;

import br.com.athletix.atletica.entity.Atletica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface AtleticaRepository extends JpaRepository<Atletica, Long> {

    boolean existsByCnpj(String cnpj);

    Optional<Atletica> findByCnpj(String cnpj);

    Optional<Atletica> findByIdAndId(Long id, Long atleticaId);

    /** Serializa a ativação de gestões de uma mesma atlética. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select atletica from Atletica atletica where atletica.id = :atleticaId")
    Optional<Atletica> findByIdForUpdate(Long atleticaId);

    java.util.List<Atletica> findAllByAtivaTrue();
}
