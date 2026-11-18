package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.*;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.config.demo.AtleticasDemoSeeder.Atleticas;
import br.com.athletix.config.demo.CargosEMembrosDemoSeeder.MembrosExtras;
import br.com.athletix.config.demo.EventosDemoSeeder.Eventos;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.gestao.entity.Gestao;
import br.com.athletix.membro.entity.Membro;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Profile("dev")
public class DemoDataInitializer {
  private final AtleticasDemoSeeder atleticas;
  private final DepartamentosDemoSeeder departamentos;
  private final CargosEMembrosDemoSeeder pessoas;
  private final GestoesDemoSeeder gestoes;
  private final EventosDemoSeeder eventos;
  private final TarefasDemoSeeder tarefas;
  private final FinanceiroDemoSeeder financeiro;
  private final PatrimonioDemoSeeder patrimonio;
  private final DocumentosDemoSeeder documentos;

  DemoDataInitializer(
      AtleticasDemoSeeder atleticas,
      DepartamentosDemoSeeder departamentos,
      CargosEMembrosDemoSeeder pessoas,
      GestoesDemoSeeder gestoes,
      EventosDemoSeeder eventos,
      TarefasDemoSeeder tarefas,
      FinanceiroDemoSeeder financeiro,
      PatrimonioDemoSeeder patrimonio,
      DocumentosDemoSeeder documentos) {
    this.atleticas = atleticas;
    this.departamentos = departamentos;
    this.pessoas = pessoas;
    this.gestoes = gestoes;
    this.eventos = eventos;
    this.tarefas = tarefas;
    this.financeiro = financeiro;
    this.patrimonio = patrimonio;
    this.documentos = documentos;
  }

  @Bean
  CommandLineRunner initialAtleticas(
      @Value("${athletix.demo.completa:false}") boolean demoCompleta) {
    return argumentos -> criarSeVazio(demoCompleta);
  }

  @Transactional
  void criarSeVazio(boolean demoCompleta) {
    if (atleticas.jaInicializado()) {
      return;
    }

    // Primeiro a estrutura mínima: atléticas, cargos, diretoria, contas, gestão e categorias.
    Atleticas base = atleticas.criar();
    EstruturaMinima estrutura = criarEstruturaMinima(base.engenharia());
    financeiro.criarCategorias(base.engenharia());
    financeiro.criarCategorias(base.saude());
    patrimonio.criarCategorias(base.engenharia());
    patrimonio.criarCategorias(base.saude());

    if (demoCompleta) {
      criarDemonstracaoCompleta(base, estrutura);
    }
  }

  private EstruturaMinima criarEstruturaMinima(Atletica engenharia) {
    Cargo cargoAdministrador = pessoas.criarCargo(engenharia, PRESIDENCIA_DEMO);
    Cargo cargoFinanceiro = pessoas.criarCargo(engenharia, TESOURARIA_DEMO);
    Departamento diretoria = departamentos.criarDiretoria(engenharia);
    Membro administrador =
        pessoas.criarMembroDiretoria(
            engenharia, cargoAdministrador, diretoria, CONTA_ADMINISTRADOR, null);
    departamentos.ativar(diretoria, administrador);
    Gestao atual = gestoes.criarInicial(engenharia);
    gestoes.criarVinculo(administrador, atual, cargoAdministrador, diretoria);
    Membro tesoureiro =
        pessoas.criarMembroDiretoria(
            engenharia, cargoFinanceiro, diretoria, CONTA_FINANCEIRO, administrador);
    gestoes.criarVinculo(tesoureiro, atual, cargoFinanceiro, diretoria);
    return new EstruturaMinima(administrador, tesoureiro, atual);
  }

  private void criarDemonstracaoCompleta(Atleticas base, EstruturaMinima estrutura) {
    // A conta isolada de Saúde vem antes do cenário completo de Engenharia.
    criarAcessoSaude(base.saude());
    LocalDate hoje = LocalDate.now(ZoneId.of(FUSO_DEMONSTRACAO));
    Instant agora = Instant.now();
    Atletica engenharia = base.engenharia();
    EstruturaComplementar complemento =
        criarEstruturaComplementar(engenharia, estrutura, hoje, agora);

    // Tarefas e lançamentos usam os eventos já criados. Patrimônio e documentos usam a estrutura
    // inicial.
    Membro gestora = complemento.membros().gestor();
    Membro associado = complemento.membros().associado();
    Gestao atual = estrutura.gestao();
    Membro administrador = estrutura.administrador();
    Eventos eventosCriados =
        eventos.criar(engenharia, atual, complemento.gestaoAnterior(), administrador, gestora);
    tarefas.criar(
        engenharia, atual, eventosCriados, gestora, associado, complemento.esportes(), hoje);
    financeiro.criar(
        engenharia,
        atual,
        eventosCriados,
        administrador,
        estrutura.tesoureiro(),
        gestora,
        hoje,
        agora);
    patrimonio.criar(
        engenharia, atual, complemento.esportes(), associado, gestora, administrador, agora);
    documentos.criar(engenharia, atual, administrador, agora);
  }

  private void criarAcessoSaude(Atletica saude) {
    Cargo cargo = pessoas.criarCargo(saude, COORDENACAO_SAUDE);
    Departamento diretoria = departamentos.criarDiretoriaSaude(saude);
    Membro administrador = pessoas.criarAdministradorSaude(saude, cargo, diretoria);
    departamentos.ativar(diretoria, administrador);
  }

  private EstruturaComplementar criarEstruturaComplementar(
      Atletica engenharia, EstruturaMinima estrutura, LocalDate hoje, Instant agora) {
    Cargo cargoGestora = pessoas.criarCargo(engenharia, COORDENACAO_EVENTOS);
    Cargo cargoAssociado = pessoas.criarCargo(engenharia, CARGO_ASSOCIADO);
    Departamento esportes = departamentos.criarEsportes(engenharia);
    MembrosExtras membros =
        pessoas.criarMembrosExtras(
            engenharia, cargoGestora, cargoAssociado, esportes, estrutura.administrador(), hoje);
    departamentos.ativar(esportes, membros.gestor());
    Gestao anterior =
        gestoes.criarHistorico(
            engenharia,
            estrutura.gestao(),
            membros.gestor(),
            cargoGestora,
            membros.associado(),
            cargoAssociado,
            esportes,
            hoje,
            agora);
    return new EstruturaComplementar(esportes, membros, anterior);
  }

  private record EstruturaMinima(Membro administrador, Membro tesoureiro, Gestao gestao) {}

  private record EstruturaComplementar(
      Departamento esportes, MembrosExtras membros, Gestao gestaoAnterior) {}
}
