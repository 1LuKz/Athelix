package br.com.athletix.patrimonio.manutencao.repository;
import br.com.athletix.patrimonio.manutencao.entity.ManutencaoPatrimonio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.Optional;
public interface ManutencaoPatrimonioRepository extends JpaRepository<ManutencaoPatrimonio, Long> {
    Optional<ManutencaoPatrimonio> findByIdAndAtletica_Id(Long id, Long atleticaId);
    List<ManutencaoPatrimonio> findAllByItem_IdAndAtletica_IdAndConcluidaEmIsNull(Long itemId, Long atleticaId);
    List<ManutencaoPatrimonio> findAllByAtletica_IdOrderByIniciadaEmDesc(Long atleticaId);
}
