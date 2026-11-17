package br.com.athletix.departamento.repository;

import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.departamento.entity.SituacaoDepartamento;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {

  boolean existsByAtletica_IdAndNomeNormalizado(Long atleticaId, String nomeNormalizado);

  Optional<Departamento> findByIdAndAtletica_Id(Long id, Long atleticaId);

  Optional<Departamento> findByAtletica_IdAndNomeNormalizado(
      Long atleticaId, String nomeNormalizado);

  List<Departamento> findAllByAtletica_IdAndSituacaoOrderByNomeAsc(
      Long atleticaId, SituacaoDepartamento situacao);

  List<Departamento> findAllByAtletica_IdOrderByNomeAsc(Long atleticaId);

  List<Departamento> findAllByAtletica_IdAndResponsavelMembroId(
      Long atleticaId, Long responsavelMembroId);
}
