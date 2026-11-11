package br.com.athletix.evento.service;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.evento.dto.EventoCreateRequest;
import br.com.athletix.evento.dto.EventoStatusUpdateRequest;
import br.com.athletix.evento.dto.EventoUpdateRequest;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.evento.entity.StatusEvento;
import br.com.athletix.evento.repository.EventoRepository;
import br.com.athletix.gestao.entity.StatusGestao;
import br.com.athletix.gestao.repository.GestaoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ConcurrentModificationException;
import br.com.athletix.shared.money.MoneyValues;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Locale;

/** Regras específicas de eventos, incluindo fluxo de status sem um CRUD genérico. */
@Service
public class EventoService {
    private final EventoRepository eventoRepository;
    private final AtleticaRepository atleticaRepository;
    private final GestaoRepository gestaoRepository;
    private final AtleticaResourceResolver resourceResolver;
    private final AtividadeUsuarioService atividadeService;

    public EventoService(EventoRepository eventoRepository, AtleticaRepository atleticaRepository,
                         GestaoRepository gestaoRepository, AtleticaResourceResolver resourceResolver,
                         AtividadeUsuarioService atividadeService) {
        this.eventoRepository = eventoRepository;
        this.atleticaRepository = atleticaRepository;
        this.gestaoRepository = gestaoRepository;
        this.resourceResolver = resourceResolver;
        this.atividadeService = atividadeService;
    }

    @Transactional
    public Evento criar(EventoCreateRequest request, Membro criador) {
        Long atleticaId = resourceResolver.atleticaIdAtual();
        Atletica atletica = resourceResolver.requireByIdAndAtleticaId(atleticaId, atleticaRepository::findByIdAndId);
        validarPeriodo(request.inicio(), request.fim());
        BigDecimal orcamento = validarOrcamento(request.orcamentoPrevisto());
        Evento evento = eventoRepository.save(new Evento(atletica,
                gestaoRepository.findByAtletica_IdAndStatus(atleticaId, StatusGestao.ATIVA)
                        .map(gestao -> gestao.getId()).orElse(null),
                NomeNormalizado.displayValue(request.nome()), NomeNormalizado.optionalDisplayValue(request.descricao()),
                request.inicio(), request.fim(), NomeNormalizado.displayValue(request.local()),
                NomeNormalizado.optionalDisplayValue(request.cidade()), normalizarUf(request.uf()), orcamento,
                request.participantesEsperados(), criador.getId()));
        atividadeService.registrar("EVENTO_CRIADO", "EVENTO", evento.getId(), "Evento criado.");
        return evento;
    }

    @Transactional(readOnly = true)
    public Evento buscarDaAtleticaAtual(Long eventoId) {
        return resourceResolver.requireByIdAndAtleticaId(eventoId, eventoRepository::findByIdAndAtletica_Id);
    }

    @Transactional
    public Evento atualizar(Long eventoId, EventoUpdateRequest request) {
        Evento evento = buscarDaAtleticaAtual(eventoId);
        assertVersion(evento, request.version());
        validarPeriodo(request.inicio(), request.fim());
        evento.atualizarDados(NomeNormalizado.displayValue(request.nome()),
                NomeNormalizado.optionalDisplayValue(request.descricao()), request.inicio(), request.fim(),
                NomeNormalizado.displayValue(request.local()), NomeNormalizado.optionalDisplayValue(request.cidade()),
                normalizarUf(request.uf()), validarOrcamento(request.orcamentoPrevisto()), request.participantesEsperados());
        atividadeService.registrar("EVENTO_ATUALIZADO", "EVENTO", evento.getId(), "Evento atualizado.");
        return evento;
    }

    @Transactional
    public Evento alterarStatus(Long eventoId, EventoStatusUpdateRequest request, PerfilAcesso perfil) {
        Evento evento = buscarDaAtleticaAtual(eventoId);
        assertVersion(evento, request.version());
        StatusEvento anterior = evento.getStatus();
        if (anterior == request.status()) {
            throw new BusinessRuleException("O evento já está neste status.");
        }
        if (request.status().ordinal() < anterior.ordinal()) {
            if (perfil != PerfilAcesso.ADMINISTRADOR) {
                throw new BusinessRuleException("A correção para um status anterior exige administrador.");
            }
            String motivo = NomeNormalizado.optionalDisplayValue(request.motivoCorrecao());
            if (motivo == null) {
                throw new BusinessRuleException("Informe o motivo da correção de status.");
            }
            evento.alterarStatus(request.status());
            atividadeService.registrar("EVENTO_STATUS_CORRIGIDO", "EVENTO", evento.getId(),
                    resumoCorrecao(anterior, request.status(), motivo));
            return evento;
        }
        if (!proximos(anterior).contains(request.status())) {
            throw new BusinessRuleException("Transição de status inválida para este evento.");
        }
        evento.alterarStatus(request.status());
        atividadeService.registrar("EVENTO_STATUS_ATUALIZADO", "EVENTO", evento.getId(),
                "Status do evento atualizado: " + anterior + " para " + request.status() + ".");
        return evento;
    }

    private EnumSet<StatusEvento> proximos(StatusEvento status) {
        return switch (status) {
            case PLANEJADO -> EnumSet.of(StatusEvento.INSCRICOES_ABERTAS, StatusEvento.EM_ANDAMENTO);
            case INSCRICOES_ABERTAS -> EnumSet.of(StatusEvento.EM_ANDAMENTO);
            case EM_ANDAMENTO -> EnumSet.of(StatusEvento.CONCLUIDO);
            case CONCLUIDO -> EnumSet.noneOf(StatusEvento.class);
        };
    }

    private BigDecimal validarOrcamento(String valor) {
        BigDecimal orcamento = MoneyValues.parse(valor);
        if (orcamento.signum() < 0) {
            throw new BusinessRuleException("O orçamento previsto não pode ser negativo.");
        }
        return orcamento;
    }

    private void validarPeriodo(OffsetDateTime inicio, OffsetDateTime fim) {
        if (inicio == null || fim == null || fim.isBefore(inicio)) {
            throw new BusinessRuleException("O fim do evento deve ser igual ou posterior ao início.");
        }
    }

    private String normalizarUf(String uf) {
        String valor = NomeNormalizado.optionalDisplayValue(uf);
        return valor == null ? null : valor.toUpperCase(Locale.ROOT);
    }

    private void assertVersion(Evento evento, Long version) {
        if (!evento.getVersion().equals(version)) {
            throw new ConcurrentModificationException();
        }
    }

    private String resumoCorrecao(StatusEvento anterior, StatusEvento novoStatus, String motivo) {
        String resumo = "Status do evento corrigido: " + anterior + " para " + novoStatus + ". Motivo: " + motivo;
        return resumo.length() <= 500 ? resumo : resumo.substring(0, 500);
    }
}
