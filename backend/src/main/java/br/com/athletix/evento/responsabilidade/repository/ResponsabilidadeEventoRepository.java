package br.com.athletix.evento.responsabilidade.repository;

import br.com.athletix.evento.responsabilidade.entity.AreaResponsabilidadeEvento;
import br.com.athletix.evento.responsabilidade.entity.ResponsabilidadeEvento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResponsabilidadeEventoRepository extends JpaRepository<ResponsabilidadeEvento, Long> {

    @EntityGraph(attributePaths = {"membro", "membro.usuario"})
    List<ResponsabilidadeEvento> findAllByEvento_IdAndAtletica_IdAndAtivaOrderByAreaAsc(Long eventoId, Long atleticaId,
                                                                                          boolean ativa);

    @EntityGraph(attributePaths = {"membro", "membro.usuario"})
    List<ResponsabilidadeEvento> findAllByEvento_IdAndAtletica_IdOrderByCreatedAtAsc(Long eventoId, Long atleticaId);

    @EntityGraph(attributePaths = {"membro", "membro.usuario"})
    Optional<ResponsabilidadeEvento> findByIdAndEvento_IdAndAtletica_Id(Long id, Long eventoId, Long atleticaId);

    Optional<ResponsabilidadeEvento> findByEvento_IdAndAtletica_IdAndAreaAndAtiva(Long eventoId, Long atleticaId,
                                                                                     AreaResponsabilidadeEvento area,
                                                                                     boolean ativa);

    boolean existsByEvento_IdAndAtletica_IdAndMembro_IdAndAtiva(Long eventoId, Long atleticaId, Long membroId,
                                                                  boolean ativa);
}
