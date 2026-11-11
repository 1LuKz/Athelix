package br.com.athletix.evento.cronograma.repository;

import br.com.athletix.evento.cronograma.entity.AtividadeCronograma;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AtividadeCronogramaRepository extends JpaRepository<AtividadeCronograma, Long> {

    @EntityGraph(attributePaths = "atletica")
    List<AtividadeCronograma> findAllByEvento_IdAndAtletica_IdOrderByInicioAscOrdemAscIdAsc(Long eventoId, Long atleticaId);

    @EntityGraph(attributePaths = "atletica")
    Optional<AtividadeCronograma> findByIdAndEvento_IdAndAtletica_Id(Long id, Long eventoId, Long atleticaId);
}
