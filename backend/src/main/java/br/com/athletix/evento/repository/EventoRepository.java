package br.com.athletix.evento.repository;

import br.com.athletix.evento.entity.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long>, JpaSpecificationExecutor<Evento> {

    @EntityGraph(attributePaths = "atletica")
    Optional<Evento> findByIdAndAtletica_Id(Long id, Long atleticaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select evento from Evento evento where evento.id = :eventoId and evento.atletica.id = :atleticaId")
    Optional<Evento> findByIdAndAtletica_IdForUpdate(Long eventoId, Long atleticaId);

    @Override
    @EntityGraph(attributePaths = "atletica")
    Page<Evento> findAll(Specification<Evento> specification, Pageable pageable);
}
