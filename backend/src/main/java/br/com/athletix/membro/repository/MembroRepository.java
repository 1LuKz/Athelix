package br.com.athletix.membro.repository;

import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembroRepository extends JpaRepository<Membro, Long> {

    boolean existsByUsuario_IdAndAtletica_Id(Long usuarioId, Long atleticaId);

    Optional<Membro> findByIdAndAtletica_Id(Long id, Long atleticaId);

    Optional<Membro> findByUsuario_IdAndAtletica_Id(Long usuarioId, Long atleticaId);

    Optional<Membro> findByIdAndUsuario_Id(Long id, Long usuarioId);

    List<Membro> findAllByUsuario_IdAndStatusOrderByAtletica_NomeAsc(Long usuarioId, StatusMembro status);

    long countByAtletica_IdAndStatusAndCargo_PerfilAcessoAndCargo_AtivoTrue(
            Long atleticaId, StatusMembro status, br.com.athletix.cargo.entity.PerfilAcesso perfilAcesso
    );

    boolean existsByDepartamento_IdAndStatus(Long departamentoId, StatusMembro status);

    boolean existsByCargo_IdAndStatus(Long cargoId, StatusMembro status);

    List<Membro> findAllByAtletica_IdAndStatusOrderByCursoAsc(Long atleticaId, StatusMembro status);
}
