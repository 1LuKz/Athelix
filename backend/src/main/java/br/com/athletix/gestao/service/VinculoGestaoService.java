package br.com.athletix.gestao.service;

import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.gestao.entity.Gestao;
import br.com.athletix.gestao.entity.StatusGestao;
import br.com.athletix.gestao.entity.VinculoGestao;
import br.com.athletix.gestao.repository.GestaoRepository;
import br.com.athletix.gestao.repository.VinculoGestaoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.membro.repository.MembroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** Mantém períodos históricos de cargo/departamento sem alterar registros passados. */
@Service
public class VinculoGestaoService {
    private final GestaoRepository gestaoRepository;
    private final VinculoGestaoRepository vinculoRepository;
    private final MembroRepository membroRepository;

    public VinculoGestaoService(GestaoRepository gestaoRepository, VinculoGestaoRepository vinculoRepository,
                                MembroRepository membroRepository) {
        this.gestaoRepository = gestaoRepository;
        this.vinculoRepository = vinculoRepository;
        this.membroRepository = membroRepository;
    }

    @Transactional
    public void abrirParaMembrosAtivos(Gestao gestao, Instant instante) {
        membroRepository.findAllByAtletica_IdAndStatusOrderByCursoAsc(gestao.getAtleticaId(), StatusMembro.ATIVO)
                .forEach(membro -> abrirNaGestao(membro, gestao, instante));
    }

    @Transactional
    public void abrirParaMembroAtivo(Membro membro, Instant instante) {
        if (membro.getStatus() != StatusMembro.ATIVO) {
            return;
        }
        gestaoRepository.findByAtletica_IdAndStatus(membro.getAtleticaId(), StatusGestao.ATIVA)
                .ifPresent(gestao -> abrirNaGestao(membro, gestao, instante));
    }

    @Transactional
    public void registrarMudancaDeAtribuicao(Membro membro, Cargo novoCargo,
                                              Departamento novoDepartamento, Instant instante) {
        if (membro.getStatus() != StatusMembro.ATIVO) {
            return;
        }
        gestaoRepository.findByAtletica_IdAndStatus(membro.getAtleticaId(), StatusGestao.ATIVA)
                .ifPresent(gestao -> vinculoRepository
                        .findByMembro_IdAndGestao_IdAndFimVigenciaIsNull(membro.getId(), gestao.getId())
                        .ifPresentOrElse(atual -> {
                            if (atual.getCargoId().equals(novoCargo.getId())
                                    && atual.getDepartamentoId().equals(novoDepartamento.getId())) {
                                return;
                            }
                            atual.encerrar(instante);
                            vinculoRepository.save(new VinculoGestao(membro, gestao, novoCargo, novoDepartamento, instante));
                        }, () -> abrirNaGestao(membro, gestao, instante)));
    }

    @Transactional
    public void encerrarParaMembro(Membro membro, Instant instante) {
        gestaoRepository.findByAtletica_IdAndStatus(membro.getAtleticaId(), StatusGestao.ATIVA)
                .flatMap(gestao -> vinculoRepository.findByMembro_IdAndGestao_IdAndFimVigenciaIsNull(
                        membro.getId(), gestao.getId()))
                .ifPresent(vinculo -> vinculo.encerrar(instante));
    }

    private void abrirNaGestao(Membro membro, Gestao gestao, Instant instante) {
        if (vinculoRepository.findByMembro_IdAndGestao_IdAndFimVigenciaIsNull(membro.getId(), gestao.getId()).isEmpty()) {
            vinculoRepository.save(new VinculoGestao(membro, gestao, membro.getCargo(), membro.getDepartamento(), instante));
        }
    }
}
