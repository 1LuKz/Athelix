package br.com.athletix.evento.participacao.repository;

import br.com.athletix.evento.participacao.entity.ParticipacaoEvento;
import br.com.athletix.evento.participacao.entity.StatusParticipacaoEvento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface ParticipacaoEventoRepository extends JpaRepository<ParticipacaoEvento, Long> {

    @EntityGraph(attributePaths = {"membro", "membro.usuario"})
    Page<ParticipacaoEvento> findAllByEvento_IdAndAtletica_Id(Long eventoId, Long atleticaId, Pageable pageable);

    @EntityGraph(attributePaths = {"membro", "membro.usuario"})
    Optional<ParticipacaoEvento> findByIdAndEvento_IdAndAtletica_Id(Long id, Long eventoId, Long atleticaId);

    boolean existsByEvento_IdAndMembro_Id(Long eventoId, Long membroId);

    boolean existsByEvento_IdAndEmailParticipante(Long eventoId, String emailParticipante);

    long countByEvento_IdAndStatusIn(Long eventoId, Collection<StatusParticipacaoEvento> statuses);
}
