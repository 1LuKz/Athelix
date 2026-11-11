package br.com.athletix.membro.repository;

import br.com.athletix.membro.entity.SolicitacaoAlteracaoVinculo;
import br.com.athletix.membro.entity.StatusSolicitacaoAlteracaoVinculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SolicitacaoAlteracaoVinculoRepository extends JpaRepository<SolicitacaoAlteracaoVinculo, Long> {
    @EntityGraph(attributePaths = {"membro", "cargoPretendido", "departamentoPretendido", "avaliadaPorMembro"})
    Optional<SolicitacaoAlteracaoVinculo> findByIdAndMembro_Atletica_Id(Long id, Long atleticaId);

    @EntityGraph(attributePaths = {"membro", "cargoPretendido", "departamentoPretendido", "avaliadaPorMembro"})
    Page<SolicitacaoAlteracaoVinculo> findAllByMembro_Atletica_IdAndStatus(Long atleticaId,
                                                                            StatusSolicitacaoAlteracaoVinculo status,
                                                                            Pageable pageable);

    @EntityGraph(attributePaths = {"membro", "cargoPretendido", "departamentoPretendido", "avaliadaPorMembro"})
    Page<SolicitacaoAlteracaoVinculo> findAllByMembro_Atletica_Id(Long atleticaId, Pageable pageable);
}
