package br.com.athletix.gestao.repository;

import br.com.athletix.gestao.entity.Gestao;
import br.com.athletix.gestao.entity.StatusGestao;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GestaoRepository extends JpaRepository<Gestao, Long> {

  Optional<Gestao> findByIdAndAtletica_Id(Long id, Long atleticaId);

  List<Gestao> findAllByAtletica_IdOrderByDataInicioDesc(Long atleticaId);

  Optional<Gestao> findByAtletica_IdAndStatus(Long atleticaId, StatusGestao status);
}
