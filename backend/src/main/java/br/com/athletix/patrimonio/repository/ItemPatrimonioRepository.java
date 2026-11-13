package br.com.athletix.patrimonio.repository;

import br.com.athletix.patrimonio.entity.ItemPatrimonio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface ItemPatrimonioRepository extends JpaRepository<ItemPatrimonio, Long>, JpaSpecificationExecutor<ItemPatrimonio> {
    @EntityGraph(attributePaths = {"atletica", "categoria", "departamentoResponsavel"})
    Optional<ItemPatrimonio> findByIdAndAtletica_Id(Long id, Long atleticaId);
    @Override @EntityGraph(attributePaths = {"categoria", "departamentoResponsavel"})
    Page<ItemPatrimonio> findAll(Specification<ItemPatrimonio> specification, Pageable pageable);
}
