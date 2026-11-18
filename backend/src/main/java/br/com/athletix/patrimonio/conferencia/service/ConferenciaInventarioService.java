package br.com.athletix.patrimonio.conferencia.service;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.gestao.entity.StatusGestao;
import br.com.athletix.gestao.repository.GestaoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.patrimonio.conferencia.dto.*;
import br.com.athletix.patrimonio.conferencia.entity.*;
import br.com.athletix.patrimonio.conferencia.repository.*;
import br.com.athletix.patrimonio.entity.ItemPatrimonio;
import br.com.athletix.patrimonio.repository.ItemPatrimonioRepository;
import br.com.athletix.patrimonio.service.PatrimonioAutorizacaoService;
import br.com.athletix.shared.exception.*;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConferenciaInventarioService {
  private final ConferenciaInventarioRepository conferencias;
  private final ItemConferenciaRepository itensConferencia;
  private final ItemPatrimonioRepository itens;
  private final AtleticaRepository atleticas;
  private final GestaoRepository gestoes;
  private final AtleticaResourceResolver resolver;
  private final PatrimonioAutorizacaoService auth;
  private final AtividadeUsuarioService auditoria;

  public ConferenciaInventarioService(
      ConferenciaInventarioRepository conferencias,
      ItemConferenciaRepository itensConferencia,
      ItemPatrimonioRepository itens,
      AtleticaRepository atleticas,
      GestaoRepository gestoes,
      AtleticaResourceResolver resolver,
      PatrimonioAutorizacaoService auth,
      AtividadeUsuarioService auditoria) {
    this.conferencias = conferencias;
    this.itensConferencia = itensConferencia;
    this.itens = itens;
    this.atleticas = atleticas;
    this.gestoes = gestoes;
    this.resolver = resolver;
    this.auth = auth;
    this.auditoria = auditoria;
  }

  @Transactional(readOnly = true)
  public List<ConferenciaResponse> listar(Membro membro) {
    auth.exigirConsulta(membro);
    return conferencias
        .findAllByAtletica_IdOrderByIniciadaEmDesc(resolver.atleticaIdAtual())
        .stream()
        .map(ConferenciaResponse::from)
        .toList();
  }

  @Transactional
  public ConferenciaResponse criar(ConferenciaCreateRequest requisicao, Membro membro) {
    auth.exigirAdministracao(membro);
    Long atleticaId = resolver.atleticaIdAtual();
    Atletica atletica = resolver.requireByIdAndAtleticaId(atleticaId, atleticas::findByIdAndId);
    ConferenciaInventario conferencia =
        conferencias.save(
            new ConferenciaInventario(
                atletica,
                gestoes
                    .findByAtletica_IdAndStatus(atleticaId, StatusGestao.ATIVA)
                    .map(gestaoAtiva -> gestaoAtiva.getId())
                    .orElse(null),
                NomeNormalizado.displayValue(requisicao.nome()),
                Instant.now(),
                membro.getId()));
    for (ItemPatrimonio itemPatrimonio :
        itens.findAll(
            (raizItem, consulta, criteriaBuilder) ->
                criteriaBuilder.and(
                    criteriaBuilder.equal(raizItem.get("atletica").get("id"), atleticaId),
                    criteriaBuilder.isTrue(raizItem.get("ativo"))))) {
      itensConferencia.save(
          new ItemConferencia(
              conferencia, itemPatrimonio.getId(), itemPatrimonio.getQuantidadeTotal()));
    }
    auditoria.registrar(
        "CONFERENCIA_INVENTARIO_ABERTA",
        "CONFERENCIA_INVENTARIO",
        conferencia.getId(),
        "Conferência de inventário aberta.");
    return ConferenciaResponse.from(conferencia);
  }

  @Transactional(readOnly = true)
  public ConferenciaDetalheResponse detalhe(Long conferenciaId, Membro membro) {
    auth.exigirConsulta(membro);
    ConferenciaInventario conferencia = buscar(conferenciaId);
    return detalheDe(conferencia);
  }

  @Transactional
  public ItemConferenciaResponse conferir(
      Long conferenciaId, Long itemId, ItemConferenciaUpdateRequest requisicao, Membro membro) {
    auth.exigirAdministracao(membro);
    ConferenciaInventario conferencia = travada(conferenciaId);
    if (conferencia.getStatus() != StatusConferenciaInventario.ABERTA)
      throw new BusinessRuleException("A conferência está encerrada.");
    ItemConferencia itemConferencia =
        itensConferencia
            .findByConferencia_IdAndItemId(conferenciaId, itemId)
            .orElseThrow(ResourceNotFoundException::new);
    if (!itemConferencia.getVersion().equals(requisicao.version()))
      throw new br.com.athletix.shared.exception.ConcurrentModificationException();
    if (requisicao.quantidadeVerificada() != itemConferencia.getQuantidadeEsperada()
        && NomeNormalizado.optionalDisplayValue(requisicao.observacao()) == null)
      throw new BusinessRuleException("Registre a divergência na observação.");
    itemConferencia.conferir(
        requisicao.quantidadeVerificada(),
        NomeNormalizado.optionalDisplayValue(requisicao.observacao()),
        membro.getId());
    auditoria.registrar(
        "ITEM_INVENTARIO_CONFERIDO",
        "ITEM_CONFERENCIA",
        itemConferencia.getId(),
        "Item de inventário conferido.");
    return ItemConferenciaResponse.from(itemConferencia);
  }

  @Transactional
  public ConferenciaDetalheResponse encerrar(
      Long conferenciaId, ConferenciaEncerramentoRequest requisicao, Membro membro) {
    auth.exigirAdministracao(membro);
    ConferenciaInventario conferencia = travada(conferenciaId);
    if (!conferencia.getVersion().equals(requisicao.version()))
      throw new br.com.athletix.shared.exception.ConcurrentModificationException();
    if (conferencia.getStatus() != StatusConferenciaInventario.ABERTA)
      throw new BusinessRuleException("A conferência já está encerrada.");
    conferencia.encerrar();
    auditoria.registrar(
        "CONFERENCIA_INVENTARIO_ENCERRADA",
        "CONFERENCIA_INVENTARIO",
        conferenciaId,
        "Conferência de inventário encerrada sem ajuste automático de patrimônio.");
    return detalheDe(conferencia);
  }

  private ConferenciaInventario buscar(Long conferenciaId) {
    return conferencias
        .findByIdAndAtletica_Id(conferenciaId, resolver.atleticaIdAtual())
        .orElseThrow(ResourceNotFoundException::new);
  }

  private ConferenciaInventario travada(Long conferenciaId) {
    return conferencias
        .findByIdAndAtletica_IdForUpdate(conferenciaId, resolver.atleticaIdAtual())
        .orElseThrow(ResourceNotFoundException::new);
  }

  private ConferenciaDetalheResponse detalheDe(ConferenciaInventario conferencia) {
    List<ItemConferenciaResponse> itensDaConferencia =
        itensConferencia.findAllByConferencia_IdOrderByItemIdAsc(conferencia.getId()).stream()
            .map(ItemConferenciaResponse::from)
            .toList();
    long unidadesEsperadas =
        itensDaConferencia.stream().mapToLong(ItemConferenciaResponse::quantidadeEsperada).sum();
    long unidadesVerificadasValidas =
        itensDaConferencia.stream()
            .mapToLong(
                itemConferencia ->
                    itemConferencia.quantidadeVerificada() == null
                        ? 0
                        : Math.min(
                            itemConferencia.quantidadeVerificada(),
                            itemConferencia.quantidadeEsperada()))
            .sum();
    return new ConferenciaDetalheResponse(
        ConferenciaResponse.from(conferencia),
        unidadesEsperadas,
        unidadesVerificadasValidas,
        unidadesEsperadas == 0
            ? 0
            : (int) Math.round(unidadesVerificadasValidas * 100.0 / unidadesEsperadas),
        itensDaConferencia);
  }
}
