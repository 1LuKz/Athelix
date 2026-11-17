package br.com.athletix.patrimonio.service;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.departamento.entity.SituacaoDepartamento;
import br.com.athletix.departamento.repository.DepartamentoRepository;
import br.com.athletix.gestao.entity.StatusGestao;
import br.com.athletix.gestao.repository.GestaoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.patrimonio.dto.ItemPatrimonioCreateRequest;
import br.com.athletix.patrimonio.dto.ItemPatrimonioFiltro;
import br.com.athletix.patrimonio.dto.ItemPatrimonioResponse;
import br.com.athletix.patrimonio.dto.ItemPatrimonioUpdateRequest;
import br.com.athletix.patrimonio.dto.PatrimonioResumoResponse;
import br.com.athletix.patrimonio.entity.CategoriaPatrimonio;
import br.com.athletix.patrimonio.entity.ItemPatrimonio;
import br.com.athletix.patrimonio.entity.SituacaoOperacionalPatrimonio;
import br.com.athletix.patrimonio.repository.ItemPatrimonioRepository;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ConcurrentModificationException;
import br.com.athletix.shared.exception.ResourceNotFoundException;
import br.com.athletix.shared.money.MoneyValues;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.pagination.PageResponse;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemPatrimonioService {
  private final ItemPatrimonioRepository itens;
  private final CategoriaPatrimonioService categorias;
  private final DepartamentoRepository departamentos;
  private final AtleticaRepository atleticas;
  private final GestaoRepository gestoes;
  private final AtleticaResourceResolver resolver;
  private final PatrimonioAutorizacaoService autorizacao;
  private final AtividadeUsuarioService auditoria;
  private final PatrimonioDisponibilidadeService disponibilidade;

  public ItemPatrimonioService(
      ItemPatrimonioRepository itens,
      CategoriaPatrimonioService categorias,
      DepartamentoRepository departamentos,
      AtleticaRepository atleticas,
      GestaoRepository gestoes,
      AtleticaResourceResolver resolver,
      PatrimonioAutorizacaoService autorizacao,
      AtividadeUsuarioService auditoria,
      PatrimonioDisponibilidadeService disponibilidade) {
    this.itens = itens;
    this.categorias = categorias;
    this.departamentos = departamentos;
    this.atleticas = atleticas;
    this.gestoes = gestoes;
    this.resolver = resolver;
    this.autorizacao = autorizacao;
    this.auditoria = auditoria;
    this.disponibilidade = disponibilidade;
  }

  @Transactional(readOnly = true)
  public PageResponse<ItemPatrimonioResponse> listar(
      ItemPatrimonioFiltro filtro, Pageable pageable, Membro membro) {
    autorizacao.exigirConsulta(membro);
    if (filtro.categoriaId() != null) categorias.exigirAtiva(filtro.categoriaId());
    Long atleticaId = resolver.atleticaIdAtual();
    Specification<ItemPatrimonio> spec =
        (root, query, cb) -> {
          ArrayList<Predicate> predicates = new ArrayList<>();
          predicates.add(cb.equal(root.get("atletica").get("id"), atleticaId));
          if (filtro.nome() != null && !filtro.nome().isBlank())
            predicates.add(
                cb.like(
                    cb.lower(root.get("nome")),
                    "%" + filtro.nome().trim().toLowerCase(java.util.Locale.ROOT) + "%"));
          if (filtro.categoriaId() != null)
            predicates.add(cb.equal(root.get("categoria").get("id"), filtro.categoriaId()));
          if (filtro.conservacao() != null)
            predicates.add(cb.equal(root.get("conservacao"), filtro.conservacao()));
          return cb.and(predicates.toArray(Predicate[]::new));
        };
    java.util.List<ItemPatrimonioResponse> todos =
        itens.findAll(spec, pageable.getSort()).stream()
            .map(item -> resposta(item, filtro.situacao()))
            .filter(java.util.Objects::nonNull)
            .toList();
    int inicio = Math.min((int) pageable.getOffset(), todos.size());
    int fim = Math.min(inicio + pageable.getPageSize(), todos.size());
    int totalPaginas =
        todos.isEmpty() ? 0 : (int) Math.ceil((double) todos.size() / pageable.getPageSize());
    return new PageResponse<>(
        todos.subList(inicio, fim),
        pageable.getPageNumber(),
        pageable.getPageSize(),
        todos.size(),
        totalPaginas);
  }

  @Transactional
  public ItemPatrimonioResponse criar(ItemPatrimonioCreateRequest request, Membro autor) {
    Departamento departamento = departamentoAtivo(request.departamentoResponsavelId());
    autorizacao.exigirGerenciamento(autor, departamento.getId());
    CategoriaPatrimonio categoria = categorias.exigirAtiva(request.categoriaId());
    Long atleticaId = resolver.atleticaIdAtual();
    Atletica atletica = resolver.requireByIdAndAtleticaId(atleticaId, atleticas::findByIdAndId);
    ItemPatrimonio item =
        itens.save(
            new ItemPatrimonio(
                atletica,
                gestoes
                    .findByAtletica_IdAndStatus(atleticaId, StatusGestao.ATIVA)
                    .map(g -> g.getId())
                    .orElse(null),
                NomeNormalizado.displayValue(request.nome()),
                NomeNormalizado.optionalDisplayValue(request.descricao()),
                categoria,
                request.quantidadeTotal(),
                request.conservacao(),
                NomeNormalizado.displayValue(request.localizacao()),
                departamento,
                valorNaoNegativo(request.valorEstimadoUnitario()),
                request.ativo()));
    auditoria.registrar(
        "ITEM_PATRIMONIO_CRIADO", "ITEM_PATRIMONIO", item.getId(), "Item de patrimônio criado.");
    return resposta(item, null);
  }

  @Transactional(readOnly = true)
  public ItemPatrimonioResponse buscar(Long id, Membro membro) {
    autorizacao.exigirConsulta(membro);
    return resposta(buscarEntidade(id), null);
  }

  @Transactional
  public ItemPatrimonioResponse atualizar(
      Long id, ItemPatrimonioUpdateRequest request, Membro autor) {
    ItemPatrimonio item = buscarEntidadeTravada(id);
    if (!item.getVersion().equals(request.version())) throw new ConcurrentModificationException();
    Departamento novoDepartamento = departamentoAtivo(request.departamentoResponsavelId());
    autorizacao.exigirGerenciamento(autor, item.getDepartamentoResponsavelId());
    autorizacao.exigirGerenciamento(autor, novoDepartamento.getId());
    if (request.quantidadeTotal() < item.getQuantidadeTotal()) {
      String justificativa = NomeNormalizado.optionalDisplayValue(request.justificativaReducao());
      if (justificativa == null)
        throw new BusinessRuleException("A redução de quantidade exige justificativa.");
      var saldo =
          disponibilidade.calcular(item.getId(), item.getAtleticaId(), item.getQuantidadeTotal());
      if (request.quantidadeTotal()
          < saldo.quantidadeEmprestada() + saldo.quantidadeEmManutencao()) {
        throw new BusinessRuleException(
            "A redução não pode retirar unidades emprestadas ou em manutenção.");
      }
    }
    if (item.isAtivo() && !request.ativo()) {
      var saldo =
          disponibilidade.calcular(item.getId(), item.getAtleticaId(), item.getQuantidadeTotal());
      if (saldo.quantidadeEmprestada() > 0 || saldo.quantidadeEmManutencao() > 0) {
        throw new BusinessRuleException(
            "Não é possível inativar item com empréstimo ou manutenção em aberto.");
      }
    }
    item.atualizar(
        NomeNormalizado.displayValue(request.nome()),
        NomeNormalizado.optionalDisplayValue(request.descricao()),
        categorias.exigirAtiva(request.categoriaId()),
        request.quantidadeTotal(),
        request.conservacao(),
        NomeNormalizado.displayValue(request.localizacao()),
        novoDepartamento,
        valorNaoNegativo(request.valorEstimadoUnitario()),
        request.ativo());
    auditoria.registrar(
        "ITEM_PATRIMONIO_ATUALIZADO",
        "ITEM_PATRIMONIO",
        item.getId(),
        "Item de patrimônio atualizado.");
    return resposta(item, null);
  }

  @Transactional(readOnly = true)
  public PatrimonioResumoResponse resumo(Membro membro) {
    autorizacao.exigirConsulta(membro);
    long registros = 0, quantidade = 0, disponivel = 0, emprestada = 0, manutencao = 0;
    BigDecimal valor = BigDecimal.ZERO.setScale(2);
    Long atleticaId = resolver.atleticaIdAtual();
    for (ItemPatrimonio item :
        itens.findAll(
            (root, query, cb) ->
                cb.and(
                    cb.equal(root.get("atletica").get("id"), atleticaId),
                    cb.isTrue(root.get("ativo"))))) {
      registros++;
      quantidade += item.getQuantidadeTotal();
      var saldo = disponibilidade.calcular(item.getId(), atleticaId, item.getQuantidadeTotal());
      disponivel += saldo.quantidadeDisponivel();
      emprestada += saldo.quantidadeEmprestada();
      manutencao += saldo.quantidadeEmManutencao();
      valor =
          valor.add(
              item.getValorEstimadoUnitario()
                  .multiply(BigDecimal.valueOf(item.getQuantidadeTotal())));
    }
    return new PatrimonioResumoResponse(
        registros, quantidade, disponivel, emprestada, manutencao, MoneyValues.format(valor));
  }

  private ItemPatrimonio buscarEntidade(Long id) {
    return itens
        .findByIdAndAtletica_Id(id, resolver.atleticaIdAtual())
        .orElseThrow(ResourceNotFoundException::new);
  }

  private ItemPatrimonio buscarEntidadeTravada(Long id) {
    return itens
        .findByIdAndAtletica_IdForUpdate(id, resolver.atleticaIdAtual())
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Departamento departamentoAtivo(Long id) {
    Departamento departamento =
        resolver.requireByIdAndAtleticaId(id, departamentos::findByIdAndAtletica_Id);
    if (departamento.getSituacao() != SituacaoDepartamento.ATIVO)
      throw new BusinessRuleException("O departamento responsável deve estar ativo.");
    return departamento;
  }

  private BigDecimal valorNaoNegativo(String valor) {
    BigDecimal convertido = MoneyValues.parse(valor);
    if (convertido.signum() < 0)
      throw new BusinessRuleException("O valor estimado unitário não pode ser negativo.");
    return convertido;
  }

  private ItemPatrimonioResponse resposta(
      ItemPatrimonio item, SituacaoOperacionalPatrimonio situacaoFiltro) {
    var saldo =
        disponibilidade.calcular(item.getId(), item.getAtleticaId(), item.getQuantidadeTotal());
    ItemPatrimonioResponse resposta = ItemPatrimonioResponse.from(item, saldo);
    if (situacaoFiltro != null && resposta.situacaoOperacional() != situacaoFiltro) return null;
    return resposta;
  }
}
