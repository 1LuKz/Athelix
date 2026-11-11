package br.com.athletix.evento.repository;

import br.com.athletix.evento.entity.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long>, JpaSpecificationExecutor<Evento> {

    @EntityGraph(attributePaths = "atletica")
    Optional<Evento> findByIdAndAtletica_Id(Long id, Long atleticaId);

    @Override
    @EntityGraph(attributePaths = "atletica")
    Page<Evento> findAll(Specification<Evento> specification, Pageable pageable);
}
