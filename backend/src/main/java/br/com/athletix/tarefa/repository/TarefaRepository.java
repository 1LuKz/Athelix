package br.com.athletix.tarefa.repository;

import br.com.athletix.tarefa.entity.Tarefa;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface TarefaRepository
    extends JpaRepository<Tarefa, Long>, JpaSpecificationExecutor<Tarefa> {

  Optional<Tarefa> findByIdAndAtletica_Id(Long id, Long atleticaId);

  List<Tarefa> findAllByEvento_IdAndAtletica_IdOrderByPrazoAscIdAsc(Long eventoId, Long atleticaId);

  @Query(
      """
      select tarefa from Tarefa tarefa
      where tarefa.atletica.id = :atleticaId
        and tarefa.status <> br.com.athletix.tarefa.entity.StatusTarefa.CONCLUIDO
      order by case when tarefa.prazo is null then 1 else 0 end, tarefa.prazo asc, tarefa.id asc
      """)
  List<Tarefa> findPendentesByAtleticaId(Long atleticaId, Pageable pageable);

  @Query(
      """
      select tarefa from Tarefa tarefa
      where tarefa.atletica.id = :atleticaId
        and tarefa.responsavel.id = :membroId
        and tarefa.status <> br.com.athletix.tarefa.entity.StatusTarefa.CONCLUIDO
      order by case when tarefa.prazo is null then 1 else 0 end, tarefa.prazo asc, tarefa.id asc
      """)
  List<Tarefa> findPendentesByAtleticaIdAndResponsavelMembroId(
      Long atleticaId, Long membroId, Pageable pageable);

  /** B15: abertas, com prazo até {@code limite} e responsável ATIVO, de uma única atlética. */
  @Query(
      """
      select t from Tarefa t
      join fetch t.responsavel r
      join fetch t.evento e
      where t.atletica.id = :atleticaId
        and r.atletica.id = :atleticaId
        and t.prazo is not null
        and t.prazo <= :limite
        and t.status <> br.com.athletix.tarefa.entity.StatusTarefa.CONCLUIDO
        and r.status = br.com.athletix.membro.entity.StatusMembro.ATIVO
      order by t.id
      """)
  List<Tarefa> findNotificaveis(Long atleticaId, LocalDate limite);
}
