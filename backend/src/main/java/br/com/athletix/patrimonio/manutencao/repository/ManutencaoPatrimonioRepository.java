package br.com.athletix.patrimonio.manutencao.repository;

import br.com.athletix.patrimonio.manutencao.entity.ManutencaoPatrimonio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManutencaoPatrimonioRepository extends JpaRepository<ManutencaoPatrimonio, Long> {
  Optional<ManutencaoPatrimonio> findByIdAndAtletica_Id(Long id, Long atleticaId);

  List<ManutencaoPatrimonio> findAllByItem_IdAndAtletica_IdAndConcluidaEmIsNull(
      Long itemId, Long atleticaId);

  List<ManutencaoPatrimonio> findAllByAtletica_IdOrderByIniciadaEmDesc(Long atleticaId);
}
