package br.com.athletix.departamento.repository;

import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.departamento.entity.SituacaoDepartamento;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {

  interface ResumoDashboard {
    Long getId();

    String getNome();

    long getMembrosAtivos();
  }

  boolean existsByAtletica_IdAndNomeNormalizado(Long atleticaId, String nomeNormalizado);

  Optional<Departamento> findByIdAndAtletica_Id(Long id, Long atleticaId);

  Optional<Departamento> findByAtletica_IdAndNomeNormalizado(
      Long atleticaId, String nomeNormalizado);

  List<Departamento> findAllByAtletica_IdAndSituacaoOrderByNomeAsc(
      Long atleticaId, SituacaoDepartamento situacao);

  List<Departamento> findAllByAtletica_IdOrderByNomeAsc(Long atleticaId);

  List<Departamento> findAllByAtletica_IdAndResponsavelMembroId(
      Long atleticaId, Long responsavelMembroId);

  @Query(
      """
      select departamento.id as id,
             departamento.nome as nome,
             count(membro) as membrosAtivos
      from Departamento departamento
      left join Membro membro on membro.departamento = departamento
        and membro.status = br.com.athletix.membro.entity.StatusMembro.ATIVO
      where departamento.atletica.id = :atleticaId
      group by departamento.id, departamento.nome
      order by departamento.nome asc
      """)
  List<ResumoDashboard> findResumoDashboardByAtleticaId(Long atleticaId);
}
