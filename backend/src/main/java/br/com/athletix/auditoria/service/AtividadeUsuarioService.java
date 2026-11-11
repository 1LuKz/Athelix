package br.com.athletix.auditoria.service;

import br.com.athletix.auditoria.entity.AtividadeUsuario;
import br.com.athletix.auditoria.repository.AtividadeUsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** Ponto único para módulos registrarem fatos de negócio sem expor autoria ao cliente. */
@Service
public class AtividadeUsuarioService {
    private final AtividadeUsuarioRepository repository;
    private final ContextoAtividade contextoAtividade;

    public AtividadeUsuarioService(AtividadeUsuarioRepository repository, ContextoAtividade contextoAtividade) {
        this.repository = repository;
        this.contextoAtividade = contextoAtividade;
    }

    @Transactional
    public void registrar(String acao, String tipoRecurso, Long recursoId, String resumo) {
        ContextoAtividade.Autor autor = contextoAtividade.atual();
        if (autor == null) {
            return;
        }
        repository.save(new AtividadeUsuario(
                autor.atleticaId(), autor.usuarioId(), autor.membroId(), acao, tipoRecurso, recursoId,
                resumo, Instant.now(), null
        ));
    }

    @Transactional(readOnly = true)
    public Page<AtividadeUsuario> listarDoUsuario(Long usuarioId, Long atleticaId, Pageable pageable) {
        return repository.findAllByUsuarioIdAndAtleticaId(usuarioId, atleticaId, pageable);
    }
}
