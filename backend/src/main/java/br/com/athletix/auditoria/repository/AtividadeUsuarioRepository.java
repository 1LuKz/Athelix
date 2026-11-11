package br.com.athletix.auditoria.repository;

import br.com.athletix.auditoria.entity.AtividadeUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtividadeUsuarioRepository extends JpaRepository<AtividadeUsuario, Long> {
    Page<AtividadeUsuario> findAllByUsuarioIdAndAtleticaId(Long usuarioId, Long atleticaId, Pageable pageable);
}
