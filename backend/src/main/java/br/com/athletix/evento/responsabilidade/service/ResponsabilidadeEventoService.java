package br.com.athletix.evento.responsabilidade.service;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.evento.repository.EventoRepository;
import br.com.athletix.evento.responsabilidade.dto.ResponsabilidadeEventoCreateRequest;
import br.com.athletix.evento.responsabilidade.dto.ResponsabilidadeEventoUpdateRequest;
import br.com.athletix.evento.responsabilidade.entity.AreaResponsabilidadeEvento;
import br.com.athletix.evento.responsabilidade.entity.ResponsabilidadeEvento;
import br.com.athletix.evento.responsabilidade.repository.ResponsabilidadeEventoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ConcurrentModificationException;
import br.com.athletix.shared.exception.ConflictException;
import br.com.athletix.shared.exception.ResourceNotFoundException;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** Mantém uma designação ativa por área sem apagar as substituídas ou removidas. */
@Service
public class ResponsabilidadeEventoService {
    private final ResponsabilidadeEventoRepository responsabilidadeRepository;
    private final EventoRepository eventoRepository;
    private final AtleticaRepository atleticaRepository;
    private final MembroRepository membroRepository;
    private final AtleticaResourceResolver resourceResolver;
    private final AtividadeUsuarioService atividadeService;

    public ResponsabilidadeEventoService(ResponsabilidadeEventoRepository responsabilidadeRepository,
                                         EventoRepository eventoRepository, AtleticaRepository atleticaRepository,
                                         MembroRepository membroRepository, AtleticaResourceResolver resourceResolver,
                                         AtividadeUsuarioService atividadeService) {
        this.responsabilidadeRepository = responsabilidadeRepository;
        this.eventoRepository = eventoRepository;
        this.atleticaRepository = atleticaRepository;
        this.membroRepository = membroRepository;
        this.resourceResolver = resourceResolver;
        this.atividadeService = atividadeService;
    }

    @Transactional(readOnly = true)
    public List<ResponsabilidadeEvento> listar(Long eventoId, boolean incluirHistorico) {
        validarEvento(eventoId);
        Long atleticaId = resourceResolver.atleticaIdAtual();
        return incluirHistorico
                ? responsabilidadeRepository.findAllByEvento_IdAndAtletica_IdOrderByCreatedAtAsc(eventoId, atleticaId)
                : responsabilidadeRepository.findAllByEvento_IdAndAtletica_IdAndAtivaOrderByAreaAsc(eventoId, atleticaId, true);
    }

    @Transactional
    public ResponsabilidadeEvento criar(Long eventoId, ResponsabilidadeEventoCreateRequest request) {
        Evento evento = bloquearEvento(eventoId);
        Long atleticaId = resourceResolver.atleticaIdAtual();
        if (responsabilidadeRepository.findByEvento_IdAndAtletica_IdAndAreaAndAtiva(
                eventoId, atleticaId, request.area(), true).isPresent()) {
            throw new ConflictException("Já existe responsável ativo para esta área.");
        }
        Membro membro = membroAtivo(request.membroId());
        Atletica atletica = resourceResolver.requireByIdAndAtleticaId(atleticaId, atleticaRepository::findByIdAndId);
        ResponsabilidadeEvento responsabilidade = responsabilidadeRepository.save(
                new ResponsabilidadeEvento(atletica, evento, membro, request.area())
        );
        atividadeService.registrar("RESPONSABILIDADE_EVENTO_ATRIBUIDA", "RESPONSABILIDADE_EVENTO",
                responsabilidade.getId(), "Responsável de evento atribuído para " + request.area() + ".");
        return responsabilidade;
    }

    @Transactional
    public ResponsabilidadeEvento substituir(Long eventoId, Long responsabilidadeId,
                                              ResponsabilidadeEventoUpdateRequest request, Membro autor) {
        bloquearEvento(eventoId);
        ResponsabilidadeEvento atual = buscar(eventoId, responsabilidadeId);
        assertVersion(atual, request.version());
        if (!atual.isAtiva()) {
            throw new BusinessRuleException("Somente uma responsabilidade ativa pode ser substituída.");
        }
        Membro novoResponsavel = membroAtivo(request.membroId());
        if (atual.getMembroId().equals(novoResponsavel.getId())) {
            throw new BusinessRuleException("O membro informado já é responsável por esta área.");
        }
        Instant agora = Instant.now();
        atual.encerrar(autor, agora);
        Atletica atletica = resourceResolver.requireByIdAndAtleticaId(resourceResolver.atleticaIdAtual(), atleticaRepository::findByIdAndId);
        Evento evento = validarEvento(eventoId);
        ResponsabilidadeEvento nova = responsabilidadeRepository.save(
                new ResponsabilidadeEvento(atletica, evento, novoResponsavel, atual.getArea())
        );
        atividadeService.registrar("RESPONSABILIDADE_EVENTO_SUBSTITUIDA", "RESPONSABILIDADE_EVENTO", nova.getId(),
                "Responsável de evento substituído na área " + atual.getArea() + ".");
        return nova;
    }

    @Transactional
    public void remover(Long eventoId, Long responsabilidadeId, Long version, Membro autor) {
        bloquearEvento(eventoId);
        ResponsabilidadeEvento atual = buscar(eventoId, responsabilidadeId);
        assertVersion(atual, version);
        if (!atual.isAtiva()) {
            throw new BusinessRuleException("Esta responsabilidade já foi encerrada.");
        }
        atual.encerrar(autor, Instant.now());
        atividadeService.registrar("RESPONSABILIDADE_EVENTO_REMOVIDA", "RESPONSABILIDADE_EVENTO", atual.getId(),
                "Responsabilidade de evento removida.");
    }

    private Evento validarEvento(Long eventoId) {
        return resourceResolver.requireByIdAndAtleticaId(eventoId, eventoRepository::findByIdAndAtletica_Id);
    }

    private Evento bloquearEvento(Long eventoId) {
        return eventoRepository.findByIdAndAtletica_IdForUpdate(eventoId, resourceResolver.atleticaIdAtual())
                .orElseThrow(ResourceNotFoundException::new);
    }

    private ResponsabilidadeEvento buscar(Long eventoId, Long responsabilidadeId) {
        return responsabilidadeRepository.findByIdAndEvento_IdAndAtletica_Id(responsabilidadeId, eventoId,
                resourceResolver.atleticaIdAtual()).orElseThrow(ResourceNotFoundException::new);
    }

    private Membro membroAtivo(Long membroId) {
        Membro membro = resourceResolver.requireByIdAndAtleticaId(membroId, membroRepository::findByIdAndAtletica_Id);
        if (membro.getStatus() != StatusMembro.ATIVO) {
            throw new BusinessRuleException("O responsável deve ser um membro ativo da atlética.");
        }
        return membro;
    }

    private void assertVersion(ResponsabilidadeEvento responsabilidade, Long version) {
        if (!responsabilidade.getVersion().equals(version)) {
            throw new ConcurrentModificationException();
        }
    }
}
