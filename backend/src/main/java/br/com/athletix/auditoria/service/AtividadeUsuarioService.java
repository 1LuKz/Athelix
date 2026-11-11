package br.com.athletix.auditoria.service;

import br.com.athletix.auditoria.entity.AtividadeUsuario;
import br.com.athletix.auditoria.repository.AtividadeUsuarioRepository;
import br.com.athletix.gestao.entity.StatusGestao;
import br.com.athletix.gestao.repository.GestaoRepository;
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
    private final GestaoRepository gestaoRepository;

    public AtividadeUsuarioService(AtividadeUsuarioRepository repository, ContextoAtividade contextoAtividade,
                                   GestaoRepository gestaoRepository) {
        this.repository = repository;
        this.contextoAtividade = contextoAtividade;
        this.gestaoRepository = gestaoRepository;
    }

    @Transactional
    public void registrar(String acao, String tipoRecurso, Long recursoId, String resumo) {
        ContextoAtividade.Autor autor = contextoAtividade.atual();
        if (autor == null) {
            return;
        }
        repository.save(new AtividadeUsuario(
                autor.atleticaId(), autor.usuarioId(), autor.membroId(), acao, tipoRecurso, recursoId,
                resumo, Instant.now(), gestaoRepository
                        .findByAtletica_IdAndStatus(autor.atleticaId(), StatusGestao.ATIVA)
                        .map(gestao -> gestao.getId())
                        .orElse(null)
        ));
    }

    @Transactional(readOnly = true)
    public Page<AtividadeUsuario> listarDoUsuario(Long usuarioId, Long atleticaId, Pageable pageable) {
        return repository.findAllByUsuarioIdAndAtleticaId(usuarioId, atleticaId, pageable);
    }
}
