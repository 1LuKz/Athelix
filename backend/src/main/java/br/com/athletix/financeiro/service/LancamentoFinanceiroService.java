package br.com.athletix.financeiro.service;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.evento.service.EventoService;
import br.com.athletix.financeiro.dto.LancamentoFinanceiroCreateRequest;
import br.com.athletix.financeiro.dto.LancamentoFinanceiroFiltro;
import br.com.athletix.financeiro.dto.LancamentoFinanceiroResponse;
import br.com.athletix.financeiro.dto.LancamentoFinanceiroUpdateRequest;
import br.com.athletix.financeiro.entity.LancamentoFinanceiro;
import br.com.athletix.financeiro.entity.ModoDataLancamento;
import br.com.athletix.financeiro.entity.SituacaoLancamento;
import br.com.athletix.financeiro.entity.StatusLiquidacao;
import br.com.athletix.financeiro.repository.LancamentoFinanceiroRepository;
import br.com.athletix.gestao.entity.StatusGestao;
import br.com.athletix.gestao.repository.GestaoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ConcurrentModificationException;
import br.com.athletix.shared.money.MoneyValues;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.pagination.PageResponse;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import br.com.athletix.shared.time.FusoHorarioAtletica;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LancamentoFinanceiroService {
    private final LancamentoFinanceiroRepository lancamentos;
    private final AtleticaRepository atleticas;
    private final EventoService eventos;
    private final GestaoRepository gestoes;
    private final CategoriaFinanceiraService categorias;
    private final AtleticaResourceResolver resolver;
    private final FinanceiroAutorizacaoService autorizacao;
    private final AtividadeUsuarioService auditoria;

    public LancamentoFinanceiroService(LancamentoFinanceiroRepository lancamentos, AtleticaRepository atleticas,
                                       EventoService eventos, GestaoRepository gestoes,
                                       CategoriaFinanceiraService categorias, AtleticaResourceResolver resolver,
                                       FinanceiroAutorizacaoService autorizacao, AtividadeUsuarioService auditoria) {
        this.lancamentos = lancamentos;
        this.atleticas = atleticas;
        this.eventos = eventos;
        this.gestoes = gestoes;
        this.categorias = categorias;
        this.resolver = resolver;
        this.autorizacao = autorizacao;
        this.auditoria = auditoria;
    }

    @Transactional
    public LancamentoFinanceiroResponse criar(LancamentoFinanceiroCreateRequest request, Membro autor) {
        autorizacao.exigirGerenciamentoFinanceiro(autor);
        Long atleticaId = resolver.atleticaIdAtual();
        LocalDate vencimento = exigirVencimentoAberto(request.dataVencimento());
        BigDecimal valor = exigirValorPositivo(request.valor());
        Atletica atletica = resolver.requireByIdAndAtleticaId(atleticaId, atleticas::findByIdAndId);
        Evento evento = request.eventoId() == null ? null : eventos.buscarDaAtleticaAtual(request.eventoId());
        LancamentoFinanceiro lancamento = lancamentos.save(new LancamentoFinanceiro(atletica,
                gestoes.findByAtletica_IdAndStatus(atleticaId, StatusGestao.ATIVA).map(g -> g.getId()).orElse(null),
                evento, request.tipo(), NomeNormalizado.displayValue(request.descricao()),
                categorias.exigirAtivaECompativel(request.categoriaId(), request.tipo()), valor,
                request.dataCompetencia(), vencimento, autor.getId()));
        auditoria.registrar("LANCAMENTO_FINANCEIRO_CRIADO", "LANCAMENTO_FINANCEIRO", lancamento.getId(),
                "Lançamento financeiro criado.");
        return LancamentoFinanceiroResponse.from(lancamento, hoje());
    }

    @Transactional(readOnly = true)
    public LancamentoFinanceiroResponse buscar(Long id, Membro membro) {
        autorizacao.exigirGerenciamentoFinanceiro(membro);
        return LancamentoFinanceiroResponse.from(carregar(id), hoje());
    }

    @Transactional(readOnly = true)
    public PageResponse<LancamentoFinanceiroResponse> listar(LancamentoFinanceiroFiltro filtro, Pageable pageable,
                                                               Membro membro) {
        autorizacao.exigirGerenciamentoFinanceiro(membro);
        validarIntervalo(filtro.dataDe(), filtro.dataAte());
        LocalDate hoje = hoje();
        return PageResponse.from(lancamentos.findAll(especificacao(filtro, hoje), pageable),
                lancamento -> LancamentoFinanceiroResponse.from(lancamento, hoje));
    }

    @Transactional
    public LancamentoFinanceiroResponse atualizar(Long id, LancamentoFinanceiroUpdateRequest request, Membro autor) {
        autorizacao.exigirGerenciamentoFinanceiro(autor);
        LancamentoFinanceiro lancamento = carregar(id);
        if (!lancamento.getVersion().equals(request.version())) {
            throw new ConcurrentModificationException();
        }
        if (lancamento.getStatusLiquidacao() != StatusLiquidacao.ABERTO) {
            throw new BusinessRuleException("Somente lançamentos em aberto podem ser alterados.");
        }
        Evento evento = request.eventoId() == null ? null : eventos.buscarDaAtleticaAtual(request.eventoId());
        lancamento.atualizar(NomeNormalizado.displayValue(request.descricao()),
                categorias.exigirAtivaECompativel(request.categoriaId(), lancamento.getTipo()),
                exigirValorPositivo(request.valor()), evento, request.dataCompetencia(),
                exigirVencimentoAberto(request.dataVencimento()));
        lancamentos.saveAndFlush(lancamento);
        auditoria.registrar("LANCAMENTO_FINANCEIRO_ATUALIZADO", "LANCAMENTO_FINANCEIRO", lancamento.getId(),
                "Lançamento financeiro atualizado.");
        return LancamentoFinanceiroResponse.from(lancamento, hoje());
    }

    private LancamentoFinanceiro carregar(Long id) {
        return resolver.requireByIdAndAtleticaId(id, lancamentos::findByIdAndAtletica_Id);
    }

    private LocalDate exigirVencimentoAberto(LocalDate dataVencimento) {
        if (dataVencimento == null) {
            throw new BusinessRuleException("A data de vencimento é obrigatória para lançamentos em aberto.");
        }
        return dataVencimento;
    }

    private BigDecimal exigirValorPositivo(String valor) {
        BigDecimal normalizado = MoneyValues.parse(valor);
        if (normalizado.signum() <= 0) {
            throw new BusinessRuleException("O valor do lançamento deve ser maior que zero.");
        }
        return normalizado;
    }

    private void validarIntervalo(LocalDate dataDe, LocalDate dataAte) {
        if (dataDe != null && dataAte != null && dataAte.isBefore(dataDe)) {
            throw new BusinessRuleException("O fim do intervalo não pode ser anterior ao início.");
        }
    }

    private LocalDate hoje() {
        Long atleticaId = resolver.atleticaIdAtual();
        return atleticas.findByIdAndId(atleticaId, atleticaId)
                .map(atletica -> FusoHorarioAtletica.hoje(atletica.getFusoHorario()))
                .orElseGet(() -> FusoHorarioAtletica.hoje(null));
    }

    private Specification<LancamentoFinanceiro> especificacao(LancamentoFinanceiroFiltro filtro, LocalDate hoje) {
        Long atleticaId = resolver.atleticaIdAtual();
        ModoDataLancamento modo = filtro.modoData() == null ? ModoDataLancamento.COMPETENCIA : filtro.modoData();
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            predicados.add(cb.equal(root.get("atletica").get("id"), atleticaId));
            if (filtro.categoriaId() != null) predicados.add(cb.equal(root.get("categoria").get("id"), filtro.categoriaId()));
            if (filtro.tipo() != null) predicados.add(cb.equal(root.get("tipo"), filtro.tipo()));
            if (filtro.statusAprovacao() != null) predicados.add(cb.equal(root.get("statusAprovacao"), filtro.statusAprovacao()));
            if (filtro.eventoId() != null) predicados.add(cb.equal(root.get("evento").get("id"), filtro.eventoId()));
            if (filtro.gestaoId() != null) predicados.add(cb.equal(root.get("gestaoOrigemId"), filtro.gestaoId()));

            String campoData = switch (modo) {
                case COMPETENCIA -> "dataCompetencia";
                case VENCIMENTO -> "dataVencimento";
                case LIQUIDACAO -> "dataLiquidacao";
            };
            if (filtro.dataDe() != null) predicados.add(cb.greaterThanOrEqualTo(root.get(campoData), filtro.dataDe()));
            if (filtro.dataAte() != null) predicados.add(cb.lessThanOrEqualTo(root.get(campoData), filtro.dataAte()));
            adicionarSituacao(filtro.situacao(), hoje, root, cb, predicados);
            return cb.and(predicados.toArray(Predicate[]::new));
        };
    }

    private void adicionarSituacao(SituacaoLancamento situacao, LocalDate hoje,
                                   jakarta.persistence.criteria.Root<LancamentoFinanceiro> root,
                                   jakarta.persistence.criteria.CriteriaBuilder cb, List<Predicate> predicados) {
        if (situacao == null) return;
        if (situacao == SituacaoLancamento.VENCIDA) {
            predicados.add(cb.equal(root.get("statusLiquidacao"), StatusLiquidacao.ABERTO));
            predicados.add(cb.isNotNull(root.get("dataVencimento")));
            predicados.add(cb.lessThan(root.get("dataVencimento"), hoje));
            return;
        }
        predicados.add(cb.equal(root.get("statusLiquidacao"), StatusLiquidacao.valueOf(situacao.name())));
    }
}
