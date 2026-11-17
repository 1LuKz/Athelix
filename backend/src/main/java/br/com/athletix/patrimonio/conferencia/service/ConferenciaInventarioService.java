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
      ConferenciaInventarioRepository c,
      ItemConferenciaRepository ic,
      ItemPatrimonioRepository i,
      AtleticaRepository a,
      GestaoRepository g,
      AtleticaResourceResolver r,
      PatrimonioAutorizacaoService h,
      AtividadeUsuarioService u) {
    conferencias = c;
    itensConferencia = ic;
    itens = i;
    atleticas = a;
    gestoes = g;
    resolver = r;
    auth = h;
    auditoria = u;
  }

  @Transactional(readOnly = true)
  public List<ConferenciaResponse> listar(Membro m) {
    auth.exigirConsulta(m);
    return conferencias
        .findAllByAtletica_IdOrderByIniciadaEmDesc(resolver.atleticaIdAtual())
        .stream()
        .map(ConferenciaResponse::from)
        .toList();
  }

  @Transactional
  public ConferenciaResponse criar(ConferenciaCreateRequest r, Membro m) {
    auth.exigirAdministracao(m);
    Long a = resolver.atleticaIdAtual();
    Atletica at = resolver.requireByIdAndAtleticaId(a, atleticas::findByIdAndId);
    ConferenciaInventario c =
        conferencias.save(
            new ConferenciaInventario(
                at,
                gestoes
                    .findByAtletica_IdAndStatus(a, StatusGestao.ATIVA)
                    .map(x -> x.getId())
                    .orElse(null),
                NomeNormalizado.displayValue(r.nome()),
                Instant.now(),
                m.getId()));
    for (ItemPatrimonio i :
        itens.findAll(
            (root, q, cb) ->
                cb.and(cb.equal(root.get("atletica").get("id"), a), cb.isTrue(root.get("ativo")))))
      itensConferencia.save(new ItemConferencia(c, i.getId(), i.getQuantidadeTotal()));
    auditoria.registrar(
        "CONFERENCIA_INVENTARIO_ABERTA",
        "CONFERENCIA_INVENTARIO",
        c.getId(),
        "Conferência de inventário aberta.");
    return ConferenciaResponse.from(c);
  }

  @Transactional(readOnly = true)
  public ConferenciaDetalheResponse detalhe(Long id, Membro m) {
    auth.exigirConsulta(m);
    ConferenciaInventario c = buscar(id);
    return detalheDe(c);
  }

  @Transactional
  public ItemConferenciaResponse conferir(
      Long id, Long itemId, ItemConferenciaUpdateRequest r, Membro m) {
    auth.exigirAdministracao(m);
    ConferenciaInventario c = travada(id);
    if (c.getStatus() != StatusConferenciaInventario.ABERTA)
      throw new BusinessRuleException("A conferência está encerrada.");
    ItemConferencia i =
        itensConferencia
            .findByConferencia_IdAndItemId(id, itemId)
            .orElseThrow(ResourceNotFoundException::new);
    if (!i.getVersion().equals(r.version()))
      throw new br.com.athletix.shared.exception.ConcurrentModificationException();
    if (r.quantidadeVerificada() != i.getQuantidadeEsperada()
        && NomeNormalizado.optionalDisplayValue(r.observacao()) == null)
      throw new BusinessRuleException("Registre a divergência na observação.");
    i.conferir(
        r.quantidadeVerificada(), NomeNormalizado.optionalDisplayValue(r.observacao()), m.getId());
    auditoria.registrar(
        "ITEM_INVENTARIO_CONFERIDO",
        "ITEM_CONFERENCIA",
        i.getId(),
        "Item de inventário conferido.");
    return ItemConferenciaResponse.from(i);
  }

  @Transactional
  public ConferenciaDetalheResponse encerrar(Long id, ConferenciaEncerramentoRequest r, Membro m) {
    auth.exigirAdministracao(m);
    ConferenciaInventario c = travada(id);
    if (!c.getVersion().equals(r.version()))
      throw new br.com.athletix.shared.exception.ConcurrentModificationException();
    if (c.getStatus() != StatusConferenciaInventario.ABERTA)
      throw new BusinessRuleException("A conferência já está encerrada.");
    c.encerrar();
    auditoria.registrar(
        "CONFERENCIA_INVENTARIO_ENCERRADA",
        "CONFERENCIA_INVENTARIO",
        id,
        "Conferência de inventário encerrada sem ajuste automático de patrimônio.");
    return detalheDe(c);
  }

  private ConferenciaInventario buscar(Long id) {
    return conferencias
        .findByIdAndAtletica_Id(id, resolver.atleticaIdAtual())
        .orElseThrow(ResourceNotFoundException::new);
  }

  private ConferenciaInventario travada(Long id) {
    return conferencias
        .findByIdAndAtletica_IdForUpdate(id, resolver.atleticaIdAtual())
        .orElseThrow(ResourceNotFoundException::new);
  }

  private ConferenciaDetalheResponse detalheDe(ConferenciaInventario c) {
    List<ItemConferenciaResponse> lista =
        itensConferencia.findAllByConferencia_IdOrderByItemIdAsc(c.getId()).stream()
            .map(ItemConferenciaResponse::from)
            .toList();
    long esperado = lista.stream().mapToLong(ItemConferenciaResponse::quantidadeEsperada).sum();
    long validas =
        lista.stream()
            .mapToLong(
                i ->
                    i.quantidadeVerificada() == null
                        ? 0
                        : Math.min(i.quantidadeVerificada(), i.quantidadeEsperada()))
            .sum();
    return new ConferenciaDetalheResponse(
        ConferenciaResponse.from(c),
        esperado,
        validas,
        esperado == 0 ? 0 : (int) Math.round(validas * 100.0 / esperado),
        lista);
  }
}
