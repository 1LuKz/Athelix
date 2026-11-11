package br.com.athletix.gestao.service;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.gestao.dto.GestaoCreateRequest;
import br.com.athletix.gestao.entity.Gestao;
import br.com.athletix.gestao.entity.StatusGestao;
import br.com.athletix.gestao.repository.GestaoRepository;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ConcurrentModificationException;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** Gestão possui criação e consulta próprias; a transição HTTP será introduzida no fluxo B24. */
@Service
public class GestaoService {
    private final GestaoRepository gestaoRepository;
    private final AtleticaRepository atleticaRepository;
    private final AtleticaResourceResolver resourceResolver;
    private final VinculoGestaoService vinculoGestaoService;
    private final AtividadeUsuarioService atividadeService;

    public GestaoService(GestaoRepository gestaoRepository, AtleticaRepository atleticaRepository,
                         AtleticaResourceResolver resourceResolver, VinculoGestaoService vinculoGestaoService,
                         AtividadeUsuarioService atividadeService) {
        this.gestaoRepository = gestaoRepository;
        this.atleticaRepository = atleticaRepository;
        this.resourceResolver = resourceResolver;
        this.vinculoGestaoService = vinculoGestaoService;
        this.atividadeService = atividadeService;
    }

    @Transactional
    public Gestao criar(GestaoCreateRequest request) {
        Long atleticaId = resourceResolver.atleticaIdAtual();
        Atletica atletica = resourceResolver.requireByIdAndAtleticaId(atleticaId, atleticaRepository::findByIdAndId);
        Gestao gestao = gestaoRepository.save(new Gestao(atletica, NomeNormalizado.displayValue(request.nome()),
                request.dataInicio(), request.dataFimPrevista()));
        atividadeService.registrar("GESTAO_CRIADA", "GESTAO", gestao.getId(), "Gestão planejada criada.");
        return gestao;
    }

    @Transactional(readOnly = true)
    public List<Gestao> listarDaAtleticaAtual() {
        return gestaoRepository.findAllByAtletica_IdOrderByDataInicioDesc(resourceResolver.atleticaIdAtual());
    }

    @Transactional(readOnly = true)
    public Gestao buscarDaAtleticaAtual(Long gestaoId) {
        return resourceResolver.requireByIdAndAtleticaId(gestaoId, gestaoRepository::findByIdAndAtletica_Id);
    }

    /** Base transacional para B24: bloqueia a atlética, garantindo no máximo uma gestão ativa. */
    @Transactional
    public Gestao ativar(Long gestaoId, Long version) {
        Long atleticaId = resourceResolver.atleticaIdAtual();
        atleticaRepository.findByIdForUpdate(atleticaId).orElseThrow();
        Gestao gestao = buscarDaAtleticaAtual(gestaoId);
        if (!gestao.getVersion().equals(version)) {
            throw new ConcurrentModificationException();
        }
        if (gestao.getStatus() != StatusGestao.PLANEJADA) {
            throw new BusinessRuleException("Somente uma gestão planejada pode ser ativada.");
        }
        if (gestaoRepository.findByAtletica_IdAndStatus(atleticaId, StatusGestao.ATIVA).isPresent()) {
            throw new BusinessRuleException("Já existe uma gestão ativa nesta atlética.");
        }
        Instant agora = Instant.now();
        gestao.ativar();
        vinculoGestaoService.abrirParaMembrosAtivos(gestao, agora);
        atividadeService.registrar("GESTAO_ATIVADA", "GESTAO", gestao.getId(), "Gestão ativada.");
        return gestao;
    }
}
