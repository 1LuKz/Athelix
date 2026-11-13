package br.com.athletix.patrimonio.emprestimo.repository;
import br.com.athletix.patrimonio.emprestimo.entity.DevolucaoPatrimonio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DevolucaoPatrimonioRepository extends JpaRepository<DevolucaoPatrimonio, Long> {
    List<DevolucaoPatrimonio> findAllByEmprestimo_Id(Long emprestimoId);
}
