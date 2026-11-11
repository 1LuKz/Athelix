package br.com.athletix.tarefa.repository;
import br.com.athletix.tarefa.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.domain.*; import org.springframework.data.jpa.domain.Specification; import java.util.*;
public interface TarefaRepository extends JpaRepository<Tarefa,Long>,JpaSpecificationExecutor<Tarefa>{ Optional<Tarefa> findByIdAndAtletica_Id(Long id,Long atleticaId); List<Tarefa> findAllByEvento_IdAndAtletica_IdOrderByPrazoAscIdAsc(Long eventoId,Long atleticaId); List<Tarefa> findAllByPrazoIsNotNull(); long countByEvento_IdAndAtletica_IdAndStatus(Long e,Long a,StatusTarefa s); @Override Page<Tarefa> findAll(Specification<Tarefa> s,Pageable p);
}
