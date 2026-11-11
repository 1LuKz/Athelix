package br.com.athletix.evento.service;

import br.com.athletix.evento.entity.Evento;
import br.com.athletix.evento.entity.GrupoStatusEvento;
import br.com.athletix.evento.entity.StatusEvento;
import br.com.athletix.evento.repository.EventoRepository;
import br.com.athletix.gestao.repository.GestaoRepository;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/** Consultas de eventos sempre partem da atlética da sessão e reutilizam a mesma busca nas abas. */
@Service
public class EventoConsultaService {
    private final EventoRepository eventoRepository;
    private final GestaoRepository gestaoRepository;
    private final AtleticaResourceResolver resourceResolver;

    public EventoConsultaService(EventoRepository eventoRepository, GestaoRepository gestaoRepository,
                                AtleticaResourceResolver resourceResolver) {
        this.eventoRepository = eventoRepository;
        this.gestaoRepository = gestaoRepository;
        this.resourceResolver = resourceResolver;
    }

    @Transactional(readOnly = true)
    public Resultado listar(String busca, OffsetDateTime inicioDe, OffsetDateTime inicioAte, StatusEvento status,
                            GrupoStatusEvento grupoStatus, Long gestaoId, Pageable pageable) {
        if (inicioDe != null && inicioAte != null && inicioAte.isBefore(inicioDe)) {
            throw new BusinessRuleException("O fim do intervalo não pode ser anterior ao início.");
        }
        if (gestaoId != null) {
            resourceResolver.requireByIdAndAtleticaId(gestaoId, gestaoRepository::findByIdAndAtletica_Id);
        }
        Specification<Evento> base = especificacaoBase(busca, inicioDe, inicioAte, gestaoId);
        Page<Evento> pagina = eventoRepository.findAll(base.and(filtroStatus(status, grupoStatus)), pageable);
        Map<StatusEvento, Long> contagens = new EnumMap<>(StatusEvento.class);
        for (StatusEvento item : StatusEvento.values()) {
            contagens.put(item, eventoRepository.count(base.and(filtroStatus(item, null))));
        }
        long totalAtivos = contagens.get(StatusEvento.INSCRICOES_ABERTAS) + contagens.get(StatusEvento.EM_ANDAMENTO);
        return new Resultado(pagina, contagens, totalAtivos);
    }

    private Specification<Evento> especificacaoBase(String busca, OffsetDateTime inicioDe, OffsetDateTime inicioAte,
                                                     Long gestaoId) {
        Long atleticaId = resourceResolver.atleticaIdAtual();
        return (root, query, criteriaBuilder) -> {
            java.util.List<Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("atletica").get("id"), atleticaId));
            if (busca != null && !busca.isBlank()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nome")),
                        "%" + busca.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (inicioDe != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fim"), inicioDe));
            }
            if (inicioAte != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("inicio"), inicioAte));
            }
            if (gestaoId != null) {
                predicates.add(criteriaBuilder.equal(root.get("gestaoOrigemId"), gestaoId));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<Evento> filtroStatus(StatusEvento status, GrupoStatusEvento grupoStatus) {
        return (root, query, criteriaBuilder) -> {
            if (status != null) {
                return criteriaBuilder.equal(root.get("status"), status);
            }
            if (grupoStatus == GrupoStatusEvento.ATIVOS) {
                return root.get("status").in(StatusEvento.INSCRICOES_ABERTAS, StatusEvento.EM_ANDAMENTO);
            }
            return criteriaBuilder.conjunction();
        };
    }

    public record Resultado(Page<Evento> pagina, Map<StatusEvento, Long> contagensPorStatus, long totalAtivos) {
    }
}
