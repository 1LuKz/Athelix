package br.com.athletix.patrimonio.manutencao.service;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.patrimonio.entity.ItemPatrimonio;
import br.com.athletix.patrimonio.manutencao.dto.ManutencaoConclusaoRequest;
import br.com.athletix.patrimonio.manutencao.dto.ManutencaoPatrimonioCreateRequest;
import br.com.athletix.patrimonio.manutencao.dto.ManutencaoPatrimonioResponse;
import br.com.athletix.patrimonio.manutencao.entity.ManutencaoPatrimonio;
import br.com.athletix.patrimonio.manutencao.repository.ManutencaoPatrimonioRepository;
import br.com.athletix.patrimonio.repository.ItemPatrimonioRepository;
import br.com.athletix.patrimonio.service.PatrimonioAutorizacaoService;
import br.com.athletix.patrimonio.service.PatrimonioDisponibilidadeService;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ConcurrentModificationException;
import br.com.athletix.shared.exception.ResourceNotFoundException;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManutencaoPatrimonioService {
  private final ManutencaoPatrimonioRepository manutencoes;
  private final ItemPatrimonioRepository itens;
  private final AtleticaRepository atleticas;
  private final AtleticaResourceResolver resolver;
  private final PatrimonioAutorizacaoService autorizacao;
  private final PatrimonioDisponibilidadeService disponibilidade;
  private final AtividadeUsuarioService auditoria;

  public ManutencaoPatrimonioService(
      ManutencaoPatrimonioRepository manutencoes,
      ItemPatrimonioRepository itens,
      AtleticaRepository atleticas,
      AtleticaResourceResolver resolver,
      PatrimonioAutorizacaoService autorizacao,
      PatrimonioDisponibilidadeService disponibilidade,
      AtividadeUsuarioService auditoria) {
    this.manutencoes = manutencoes;
    this.itens = itens;
    this.atleticas = atleticas;
    this.resolver = resolver;
    this.autorizacao = autorizacao;
    this.disponibilidade = disponibilidade;
    this.auditoria = auditoria;
  }

  @Transactional(readOnly = true)
  public List<ManutencaoPatrimonioResponse> listar(Membro membro) {
    autorizacao.exigirConsulta(membro);
    return manutencoes
        .findAllByAtletica_IdOrderByIniciadaEmDesc(resolver.atleticaIdAtual())
        .stream()
        .map(ManutencaoPatrimonioResponse::from)
        .toList();
  }

  @Transactional
  public ManutencaoPatrimonioResponse criar(
      ManutencaoPatrimonioCreateRequest request, Membro autor) {
    ItemPatrimonio item = itemTravado(request.itemId());
    autorizacao.exigirGerenciamento(autor, item.getDepartamentoResponsavelId());
    return ManutencaoPatrimonioResponse.from(
        criarInterno(
            item, request.quantidade(), request.motivo(), request.previsaoConclusaoEm(), autor));
  }

  @Transactional
  public ManutencaoPatrimonioResponse concluir(
      Long id, ManutencaoConclusaoRequest request, Membro autor) {
    ManutencaoPatrimonio manutencao =
        manutencoes
            .findByIdAndAtletica_Id(id, resolver.atleticaIdAtual())
            .orElseThrow(ResourceNotFoundException::new);
    ItemPatrimonio item = itemTravado(manutencao.getItemId());
    autorizacao.exigirGerenciamento(autor, item.getDepartamentoResponsavelId());
    if (!manutencao.getVersion().equals(request.version()))
      throw new ConcurrentModificationException();
    if (!manutencao.isAberta())
      throw new BusinessRuleException("Esta manutenção já foi concluída.");
    manutencao.concluir(NomeNormalizado.displayValue(request.resultado()), Instant.now());
    auditoria.registrar(
        "MANUTENCAO_PATRIMONIO_CONCLUIDA",
        "MANUTENCAO_PATRIMONIO",
        manutencao.getId(),
        "Manutenção concluída.");
    return ManutencaoPatrimonioResponse.from(manutencao);
  }

  public ManutencaoPatrimonio criarInterno(
      ItemPatrimonio item, int quantidade, String motivo, Instant previsao, Membro autor) {
    if (!item.isAtivo())
      throw new BusinessRuleException("Não é possível movimentar um item de patrimônio inativo.");
    if (quantidade
        > disponibilidade
            .calcular(item.getId(), item.getAtleticaId(), item.getQuantidadeTotal())
            .quantidadeDisponivel()) {
      throw new BusinessRuleException("Não há unidades disponíveis suficientes para manutenção.");
    }
    Atletica atletica =
        atleticas
            .findByIdAndId(item.getAtleticaId(), item.getAtleticaId())
            .orElseThrow(ResourceNotFoundException::new);
    ManutencaoPatrimonio manutencao =
        manutencoes.save(
            new ManutencaoPatrimonio(
                atletica,
                item,
                quantidade,
                NomeNormalizado.displayValue(motivo),
                Instant.now(),
                previsao,
                autor.getId()));
    auditoria.registrar(
        "MANUTENCAO_PATRIMONIO_INICIADA",
        "MANUTENCAO_PATRIMONIO",
        manutencao.getId(),
        "Manutenção iniciada.");
    return manutencao;
  }

  private ItemPatrimonio itemTravado(Long id) {
    return itens
        .findByIdAndAtletica_IdForUpdate(id, resolver.atleticaIdAtual())
        .orElseThrow(ResourceNotFoundException::new);
  }
}
