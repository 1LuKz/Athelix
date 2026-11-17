package br.com.athletix.patrimonio.emprestimo.repository;

import br.com.athletix.patrimonio.emprestimo.entity.EmprestimoPatrimonio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmprestimoPatrimonioRepository extends JpaRepository<EmprestimoPatrimonio, Long> {
  Optional<EmprestimoPatrimonio> findByIdAndAtletica_Id(Long id, Long atleticaId);

  List<EmprestimoPatrimonio> findAllByItem_IdAndAtletica_Id(Long itemId, Long atleticaId);

  List<EmprestimoPatrimonio> findAllByAtletica_IdOrderByRetiradoEmDesc(Long atleticaId);
}
