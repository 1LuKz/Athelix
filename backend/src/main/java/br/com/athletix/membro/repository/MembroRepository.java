package br.com.athletix.membro.repository;

import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MembroRepository extends JpaRepository<Membro, Long>, JpaSpecificationExecutor<Membro> {

    boolean existsByUsuario_IdAndAtletica_Id(Long usuarioId, Long atleticaId);

    @EntityGraph(attributePaths = {"usuario", "cargo", "departamento", "atletica"})
    Optional<Membro> findByIdAndAtletica_Id(Long id, Long atleticaId);

    Optional<Membro> findByUsuario_IdAndAtletica_Id(Long usuarioId, Long atleticaId);

    @EntityGraph(attributePaths = {"usuario", "cargo", "departamento", "atletica"})
    Optional<Membro> findByIdAndUsuario_Id(Long id, Long usuarioId);

    List<Membro> findAllByUsuario_IdAndStatusOrderByAtletica_NomeAsc(Long usuarioId, StatusMembro status);

    long countByAtletica_IdAndStatusAndCargo_PerfilAcessoAndCargo_AtivoTrue(
            Long atleticaId, StatusMembro status, br.com.athletix.cargo.entity.PerfilAcesso perfilAcesso
    );

    boolean existsByDepartamento_IdAndStatus(Long departamentoId, StatusMembro status);

    boolean existsByCargo_IdAndStatus(Long cargoId, StatusMembro status);

    long countByDepartamento_IdAndStatus(Long departamentoId, StatusMembro status);

    @Query("select membro.usuario.nome from Membro membro where membro.id = :membroId and membro.atletica.id = :atleticaId")
    Optional<String> findNomeUsuarioByIdAndAtleticaId(Long membroId, Long atleticaId);

    @Override
    @EntityGraph(attributePaths = {"usuario", "cargo", "departamento", "atletica"})
    Page<Membro> findAll(Specification<Membro> specification, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"usuario", "cargo", "departamento", "atletica"})
    List<Membro> findAll(Specification<Membro> specification, Sort sort);

    List<Membro> findAllByAtletica_IdAndStatusOrderByCursoAsc(Long atleticaId, StatusMembro status);
}
