package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.*;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.auditoria.service.ContextoAtividade;
import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.config.demo.AtleticasDemoSeeder.Atleticas;
import br.com.athletix.config.demo.MembrosExtrasDemoSeeder.MembrosExtras;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.gestao.entity.Gestao;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.notificacao.service.NotificacaoPrazoService;
import br.com.athletix.shared.tenancy.ContextoAtletica;
import br.com.athletix.shared.time.FusoHorarioAtletica;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
class DemonstracaoCompletaSeeder {
  private final DepartamentosDemoSeeder departamentos;
  private final CargosEMembrosDemoSeeder pessoas;
  private final GestoesDemoSeeder gestoes;
  private final MembrosExtrasDemoSeeder membrosExtras;
  private final EventosDemoSeeder eventos;
  private final OrganizacaoEventosDemoSeeder organizacao;
  private final ParticipacoesDemoSeeder participacoes;
  private final TarefasDemoSeeder tarefas;
  private final FinanceiroDemoSeeder financeiro;
  private final HistoricoFinanceiroDemoSeeder historicoFinanceiro;
  private final PatrimonioDemoSeeder patrimonio;
  private final DocumentosDemoSeeder documentos;
  private final NotificacaoPrazoService notificacoes;
  private final ContextoAtletica contextoAtletica;
  private final ContextoAtividade contextoAtividade;

  DemonstracaoCompletaSeeder(
      DepartamentosDemoSeeder departamentos,
      CargosEMembrosDemoSeeder pessoas,
      GestoesDemoSeeder gestoes,
      MembrosExtrasDemoSeeder membrosExtras,
      EventosDemoSeeder eventos,
      OrganizacaoEventosDemoSeeder organizacao,
      ParticipacoesDemoSeeder participacoes,
      TarefasDemoSeeder tarefas,
      FinanceiroDemoSeeder financeiro,
      HistoricoFinanceiroDemoSeeder historicoFinanceiro,
      PatrimonioDemoSeeder patrimonio,
      DocumentosDemoSeeder documentos,
      NotificacaoPrazoService notificacoes,
      ContextoAtletica contextoAtletica,
      ContextoAtividade contextoAtividade) {
    this.departamentos = departamentos;
    this.pessoas = pessoas;
    this.gestoes = gestoes;
    this.membrosExtras = membrosExtras;
    this.eventos = eventos;
    this.organizacao = organizacao;
    this.participacoes = participacoes;
    this.tarefas = tarefas;
    this.financeiro = financeiro;
    this.historicoFinanceiro = historicoFinanceiro;
    this.patrimonio = patrimonio;
    this.documentos = documentos;
    this.notificacoes = notificacoes;
    this.contextoAtletica = contextoAtletica;
    this.contextoAtividade = contextoAtividade;
  }

  @Transactional
  public void criar(Atleticas base, Membro administrador, Membro tesoureiro, Gestao atual) {
    criarAcessoSaude(base.saude());
    CenarioDemo cenario = prepararCenario(base.engenharia(), administrador, tesoureiro, atual);
    try (var escopoAtletica = contextoAtletica.abrir(base.engenharia().getId());
        var escopoAtividade =
            contextoAtividade.abrir(
                base.engenharia().getId(), administrador.getUsuarioId(), administrador.getId())) {
      criarConteudo(cenario);
    }
  }

  private void criarConteudo(CenarioDemo cenario) {
    EventosDemoSeeder.Eventos eventosCriados = eventos.criar(cenario);
    organizacao.criar(cenario, eventosCriados);
    participacoes.criar(cenario, eventosCriados);
    tarefas.criar(cenario, eventosCriados);
    financeiro.criar(cenario, eventosCriados);
    historicoFinanceiro.criar(cenario);
    patrimonio.criar(cenario);
    documentos.criar(cenario);
    notificacoes.gerarParaAtletica(cenario.atletica().getId(), cenario.agora());
  }

  private void criarAcessoSaude(Atletica saude) {
    Cargo cargo = pessoas.criarCargo(saude, COORDENACAO_SAUDE);
    Departamento diretoria = departamentos.criarDiretoriaSaude(saude);
    Membro administrador = pessoas.criarAdministradorSaude(saude, cargo, diretoria);
    departamentos.ativar(diretoria, administrador);
  }

  private CenarioDemo prepararCenario(
      Atletica engenharia, Membro administrador, Membro tesoureiro, Gestao atual) {
    Instant agora = Instant.now();
    LocalDate hoje = FusoHorarioAtletica.dataDe(agora, engenharia.getFusoHorario());
    Cargo cargoGestora = pessoas.criarCargo(engenharia, COORDENACAO_EVENTOS);
    Cargo cargoAssociado = pessoas.criarCargo(engenharia, CARGO_ASSOCIADO);
    Departamento esportes = departamentos.criarEsportes(engenharia);
    MembrosExtras membros =
        membrosExtras.criarMembrosExtras(
            engenharia, cargoGestora, cargoAssociado, esportes, administrador, hoje);
    departamentos.ativar(esportes, membros.gestor());
    gestoes.criarHistorico(
        engenharia,
        atual,
        membros.gestor(),
        cargoGestora,
        membros.associado(),
        cargoAssociado,
        esportes,
        hoje,
        agora);
    return new CenarioDemo(
        engenharia,
        atual,
        administrador,
        tesoureiro,
        membros.gestor(),
        membros.associado(),
        esportes,
        hoje,
        agora);
  }
}
