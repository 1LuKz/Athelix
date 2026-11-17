package br.com.athletix.patrimonio.emprestimo.service;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.patrimonio.emprestimo.dto.*;
import br.com.athletix.patrimonio.emprestimo.entity.DevolucaoPatrimonio;
import br.com.athletix.patrimonio.emprestimo.entity.EmprestimoPatrimonio;
import br.com.athletix.patrimonio.emprestimo.repository.DevolucaoPatrimonioRepository;
import br.com.athletix.patrimonio.emprestimo.repository.EmprestimoPatrimonioRepository;
import br.com.athletix.patrimonio.entity.ItemPatrimonio;
import br.com.athletix.patrimonio.manutencao.service.ManutencaoPatrimonioService;
import br.com.athletix.patrimonio.repository.ItemPatrimonioRepository;
import br.com.athletix.patrimonio.service.PatrimonioAutorizacaoService;
import br.com.athletix.patrimonio.service.PatrimonioDisponibilidadeService;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ResourceNotFoundException;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmprestimoPatrimonioService {
  private final EmprestimoPatrimonioRepository emprestimos;
  private final DevolucaoPatrimonioRepository devolucoes;
  private final ItemPatrimonioRepository itens;
  private final MembroRepository membros;
  private final AtleticaRepository atleticas;
  private final AtleticaResourceResolver resolver;
  private final PatrimonioAutorizacaoService autorizacao;
  private final PatrimonioDisponibilidadeService disponibilidade;
  private final ManutencaoPatrimonioService manutencoes;
  private final AtividadeUsuarioService auditoria;

  public EmprestimoPatrimonioService(
      EmprestimoPatrimonioRepository emprestimos,
      DevolucaoPatrimonioRepository devolucoes,
      ItemPatrimonioRepository itens,
      MembroRepository membros,
      AtleticaRepository atleticas,
      AtleticaResourceResolver resolver,
      PatrimonioAutorizacaoService autorizacao,
      PatrimonioDisponibilidadeService disponibilidade,
      ManutencaoPatrimonioService manutencoes,
      AtividadeUsuarioService auditoria) {
    this.emprestimos = emprestimos;
    this.devolucoes = devolucoes;
    this.itens = itens;
    this.membros = membros;
    this.atleticas = atleticas;
    this.resolver = resolver;
    this.autorizacao = autorizacao;
    this.disponibilidade = disponibilidade;
    this.manutencoes = manutencoes;
    this.auditoria = auditoria;
  }

  @Transactional(readOnly = true)
  public List<EmprestimoPatrimonioResponse> listar(Membro membro) {
    autorizacao.exigirConsulta(membro);
    return emprestimos
        .findAllByAtletica_IdOrderByRetiradoEmDesc(resolver.atleticaIdAtual())
        .stream()
        .map(this::resposta)
        .toList();
  }

  @Transactional
  public EmprestimoPatrimonioResponse criar(
      EmprestimoPatrimonioCreateRequest request, Membro autor) {
    ItemPatrimonio item = itemTravado(request.itemId());
    autorizacao.exigirGerenciamento(autor, item.getDepartamentoResponsavelId());
    if (!item.isAtivo()) throw new BusinessRuleException("Não é possível emprestar item inativo.");
    Membro responsavel =
        resolver.requireByIdAndAtleticaId(
            request.membroResponsavelId(), membros::findByIdAndAtletica_Id);
    if (responsavel.getStatus() != StatusMembro.ATIVO)
      throw new BusinessRuleException("O responsável pelo empréstimo deve ser membro ativo.");
    if (request.quantidade()
        > disponibilidade
            .calcular(item.getId(), item.getAtleticaId(), item.getQuantidadeTotal())
            .quantidadeDisponivel())
      throw new BusinessRuleException("Não há unidades disponíveis suficientes para empréstimo.");
    Atletica atletica =
        atleticas
            .findByIdAndId(item.getAtleticaId(), item.getAtleticaId())
            .orElseThrow(ResourceNotFoundException::new);
    EmprestimoPatrimonio emprestimo =
        emprestimos.save(
            new EmprestimoPatrimonio(
                atletica,
                item,
                responsavel.getId(),
                request.quantidade(),
                Instant.now(),
                request.devolucaoPrevistaEm(),
                NomeNormalizado.optionalDisplayValue(request.observacao()),
                autor.getId()));
    auditoria.registrar(
        "EMPRESTIMO_PATRIMONIO_REGISTRADO",
        "EMPRESTIMO_PATRIMONIO",
        emprestimo.getId(),
        "Empréstimo de patrimônio registrado.");
    return resposta(emprestimo);
  }

  @Transactional
  public DevolucaoPatrimonioResponse devolver(
      Long emprestimoId, DevolucaoPatrimonioCreateRequest request, Membro autor) {
    EmprestimoPatrimonio emprestimo =
        emprestimos
            .findByIdAndAtletica_Id(emprestimoId, resolver.atleticaIdAtual())
            .orElseThrow(ResourceNotFoundException::new);
    ItemPatrimonio item = itemTravado(emprestimo.getItemId());
    autorizacao.exigirGerenciamento(autor, item.getDepartamentoResponsavelId());
    int devolvida = quantidadeDevolvida(emprestimo);
    if (request.quantidade() > emprestimo.getQuantidade() - devolvida)
      throw new BusinessRuleException("A devolução não pode superar o saldo emprestado.");
    DevolucaoPatrimonio devolucao =
        devolucoes.save(
            new DevolucaoPatrimonio(
                emprestimo,
                request.quantidade(),
                request.devolvidaEm() == null ? Instant.now() : request.devolvidaEm(),
                request.condicaoRetorno(),
                NomeNormalizado.optionalDisplayValue(request.observacao()),
                autor.getId()));
    if (request.enviarParaManutencao()) {
      if (NomeNormalizado.optionalDisplayValue(request.motivoManutencao()) == null)
        throw new BusinessRuleException("Informe o motivo da manutenção.");
      manutencoes.criarInterno(
          item,
          request.quantidade(),
          request.motivoManutencao(),
          request.previsaoConclusaoManutencao(),
          autor);
    }
    auditoria.registrar(
        "DEVOLUCAO_PATRIMONIO_REGISTRADA",
        "DEVOLUCAO_PATRIMONIO",
        devolucao.getId(),
        "Devolução de patrimônio registrada.");
    return DevolucaoPatrimonioResponse.from(devolucao);
  }

  private EmprestimoPatrimonioResponse resposta(EmprestimoPatrimonio emprestimo) {
    return EmprestimoPatrimonioResponse.from(emprestimo, quantidadeDevolvida(emprestimo));
  }

  private int quantidadeDevolvida(EmprestimoPatrimonio emprestimo) {
    return devolucoes.findAllByEmprestimo_Id(emprestimo.getId()).stream()
        .mapToInt(DevolucaoPatrimonio::getQuantidade)
        .sum();
  }

  private ItemPatrimonio itemTravado(Long id) {
    return itens
        .findByIdAndAtletica_IdForUpdate(id, resolver.atleticaIdAtual())
        .orElseThrow(ResourceNotFoundException::new);
  }
}
