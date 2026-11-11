package br.com.athletix.financeiro.service;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.financeiro.dto.CategoriaFinanceiraCreateRequest;
import br.com.athletix.financeiro.entity.CategoriaFinanceira;
import br.com.athletix.financeiro.entity.TipoLancamentoFinanceiro;
import br.com.athletix.financeiro.repository.CategoriaFinanceiraRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ConflictException;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaFinanceiraService {
    private final CategoriaFinanceiraRepository categorias;
    private final AtleticaRepository atleticas;
    private final AtleticaResourceResolver resolver;
    private final FinanceiroAutorizacaoService autorizacao;
    private final AtividadeUsuarioService auditoria;

    public CategoriaFinanceiraService(CategoriaFinanceiraRepository categorias, AtleticaRepository atleticas,
                                      AtleticaResourceResolver resolver, FinanceiroAutorizacaoService autorizacao,
                                      AtividadeUsuarioService auditoria) {
        this.categorias = categorias;
        this.atleticas = atleticas;
        this.resolver = resolver;
        this.autorizacao = autorizacao;
        this.auditoria = auditoria;
    }

    @Transactional
    public CategoriaFinanceira criar(CategoriaFinanceiraCreateRequest request, Membro autor) {
        autorizacao.exigirGerenciamentoFinanceiro(autor);
        Long atleticaId = resolver.atleticaIdAtual();
        String nomeNormalizado = NomeNormalizado.fromRequired(request.nome(), "nome");
        if (categorias.existsByAtletica_IdAndNomeNormalizado(atleticaId, nomeNormalizado)) {
            throw new ConflictException("Já existe uma categoria financeira com este nome na atlética.");
        }
        Atletica atletica = resolver.requireByIdAndAtleticaId(atleticaId, atleticas::findByIdAndId);
        CategoriaFinanceira categoria = categorias.save(new CategoriaFinanceira(atletica, NomeNormalizado.displayValue(request.nome()),
                nomeNormalizado, request.tipoAplicavel(), request.ativa()));
        auditoria.registrar("CATEGORIA_FINANCEIRA_CRIADA", "CATEGORIA_FINANCEIRA", categoria.getId(),
                "Categoria financeira criada.");
        return categoria;
    }

    @Transactional(readOnly = true)
    public List<CategoriaFinanceira> listar(Membro membro) {
        autorizacao.exigirGerenciamentoFinanceiro(membro);
        return categorias.findAllByAtletica_IdOrderByNomeAsc(resolver.atleticaIdAtual());
    }

    @Transactional(readOnly = true)
    public CategoriaFinanceira exigirAtivaECompativel(Long categoriaId, TipoLancamentoFinanceiro tipo) {
        CategoriaFinanceira categoria = resolver.requireByIdAndAtleticaId(categoriaId, categorias::findByIdAndAtletica_Id);
        if (!categoria.isAtiva()) {
            throw new BusinessRuleException("A categoria financeira informada está inativa.");
        }
        if (!categoria.getTipoAplicavel().aceita(tipo)) {
            throw new BusinessRuleException("A categoria financeira não é aplicável ao tipo de lançamento informado.");
        }
        return categoria;
    }
}
