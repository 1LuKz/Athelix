package br.com.athletix.gestao.repository;

import br.com.athletix.gestao.entity.VinculoGestao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VinculoGestaoRepository extends JpaRepository<VinculoGestao, Long> {

    Optional<VinculoGestao> findByMembro_IdAndGestao_IdAndFimVigenciaIsNull(Long membroId, Long gestaoId);

    List<VinculoGestao> findAllByMembro_IdAndGestao_IdOrderByInicioVigenciaAsc(Long membroId, Long gestaoId);
    List<VinculoGestao> findAllByGestao_IdAndFimVigenciaIsNull(Long gestaoId);
}
