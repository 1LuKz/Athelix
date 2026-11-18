package br.com.athletix.config.demo;

import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.financeiro.entity.TipoLancamentoFinanceiro;

/** Valores da carga de demonstração. Os deslocamentos são relativos à data de execução. */
final class DemoData {
  static final String ATLETICA_ENGENHARIA = "Atlética de Engenharia Exemplo";
  static final String SIGLA_ENGENHARIA = "AAE";
  static final String UNIVERSIDADE_EXEMPLO = "Universidade Exemplo";
  static final String FUSO_DEMONSTRACAO = "America/Sao_Paulo";
  static final String ATLETICA_SAUDE = "Atlética de Saúde Exemplo";
  static final String SIGLA_SAUDE = "AAS";
  static final String ENGENHARIA_EXEMPLO_EDU_BR = "engenharia.exemplo.edu.br";
  static final String SAUDE_EXEMPLO_EDU_BR = "saude.exemplo.edu.br";
  static final String DIRETORIA = "Diretoria";
  static final String CAMPO_NOME = "nome";
  static final String DEPARTAMENTO_INICIAL_DE_DESENVOLVIMENTO =
      "Departamento inicial de desenvolvimento.";
  static final String DEPARTAMENTO_ISOLADO_DE_DEMONSTRACAO =
      "Departamento isolado de demonstração.";
  static final String ESPORTES = "Esportes";
  static final String ORGANIZACAO_ESPORTIVA = "Organização esportiva.";
  static final String ADMINISTRACAO = "Administração";
  static final String SAUDE = "Saúde";
  static final int PERIODO_SAUDE = 2;
  static final int TEMPO_GESTORA_MESES = 5;
  static final int TEMPO_ASSOCIADO_MESES = 4;
  static final String ENGENHARIA = "Engenharia";
  static final int PERIODO_PENDENTE = 3;
  static final int TEMPO_INATIVO_ANOS = 1;
  static final int PERIODO_ENGENHARIA = 4;
  static final String GESTAO_DE_DEMONSTRACAO = "Gestão de Demonstração";
  static final int DURACAO_GESTAO_ANOS = 1;
  static final String GESTAO_ANTERIOR = "Gestão anterior";
  static final int INICIO_GESTAO_ANTERIOR_ANOS = 1;
  static final int FIM_GESTAO_ANTERIOR_DIAS = 1;
  static final int ENCERRAMENTO_GESTAO_ANTERIOR_DIAS = 1;
  static final int VINCULO_GESTORA_ANTERIOR_DIAS = 300;
  static final int VINCULO_GESTORA_ATUAL_DIAS = 120;
  static final int VINCULO_ASSOCIADO_ATUAL_DIAS = 100;
  static final String TORNEIO_DE_INTEGRACAO = "Torneio de Integração";
  static final int INICIO_TORNEIO_DIAS = 14;
  static final int FIM_TORNEIO_DIAS = 15;
  static final String COPA_ABERTA = "Copa Aberta";
  static final int INICIO_COPA_DIAS = 7;
  static final int FIM_COPA_DIAS = 8;
  static final String SEMANA_ESPORTIVA = "Semana Esportiva";
  static final int INICIO_SEMANA_ESPORTIVA_HORAS = 2;
  static final int FIM_SEMANA_ESPORTIVA_DIAS = 1;
  static final String JOGOS_DO_SEMESTRE = "Jogos do Semestre";
  static final int INICIO_JOGOS_DIAS = 30;
  static final int FIM_JOGOS_DIAS = 29;
  static final String EVENTO_SINTETICO_PARA_DEMONSTRACAO = "Evento sintético para demonstração.";
  static final String CAMPUS_EXEMPLO = "Campus Exemplo";
  static final String SAO_PAULO = "São Paulo";
  static final String SP = "SP";
  static final String ORCAMENTO_EVENTO = "2000.00";
  static final int PARTICIPANTES_ESPERADOS = 80;
  static final String RESERVAR_QUADRA = "Reservar quadra";
  static final int PRAZO_RESERVA_DIAS = 3;
  static final String DIVULGAR_INSCRICOES = "Divulgar inscrições";
  static final int PRAZO_DIVULGACAO_DIAS = 1;
  static final String CONFERIR_MATERIAIS = "Conferir materiais";
  static final int ATRASO_CONFERENCIA_MATERIAIS_DIAS = 1;
  static final String PUBLICAR_RESULTADOS = "Publicar resultados";
  static final int PRAZO_RESULTADOS_DIAS = 5;
  static final String TAREFA_ATRASADA = "Tarefa atrasada";
  static final String CONFERIR_MATERIAIS_ESTA_COM_PRAZO_VENCIDO =
      "Conferir materiais está com prazo vencido.";
  static final String TAREFA_SINTETICA_PARA_DEMONSTRACAO = "Tarefa sintética para demonstração.";
  static final int LIQUIDACAO_PATROCINIO_DIAS = 10;
  static final String APROVADA_PARA_DEMONSTRACAO = "Aprovada para demonstração";
  static final int APROVACAO_LOCACAO_DIAS = 14;
  static final int LIQUIDACAO_LOCACAO_DIAS = 4;
  static final String NAO_NECESSARIA = "Não necessária";
  static final int MESES_DE_HISTORICO = 5;
  static final int DIA_COMPETENCIA_HISTORICA = 10;
  static final String RECEITA_HISTORICA_SINTETICA = "Receita histórica sintética ";
  static final String VALOR_300_REAIS = "300.00";
  static final String EVENTOS = "Eventos";
  static final String ESPORTIVO = "Esportivo";
  static final String PATROCINIO = "Patrocínio";
  static final String BOLAS_OFICIAIS = "Bolas oficiais";
  static final String LOTE_SINTETICO_DISPONIVEL = "Lote sintético disponível";
  static final int QUANTIDADE_BOLAS = 12;
  static final String ALMOXARIFADO = "Almoxarifado";
  static final String VALOR_BOLA = "80.00";
  static final String COLETES = "Coletes";
  static final String LOTE_DEMONSTRATIVO = "Lote demonstrativo";
  static final int QUANTIDADE_COLETES = 10;
  static final String VALOR_COLETE = "25.00";
  static final String CAIXA_DE_SOM = "Caixa de som";
  static final String EQUIPAMENTO_SINTETICO = "Equipamento sintético";
  static final int QUANTIDADE_CAIXAS_DE_SOM = 2;
  static final String SALA_DE_EVENTOS = "Sala de eventos";
  static final int QUANTIDADE_EMPRESTADA = 3;
  static final int INICIO_EMPRESTIMO_DIAS = 2;
  static final int FIM_EMPRESTIMO_DIAS = 4;
  static final String EMPRESTIMO_DEMONSTRATIVO = "Empréstimo demonstrativo";
  static final int QUANTIDADE_EM_MANUTENCAO = 1;
  static final String REVISAO_DEMONSTRATIVA = "Revisão demonstrativa";
  static final int INICIO_MANUTENCAO_DIAS = 1;
  static final int FIM_MANUTENCAO_DIAS = 3;
  static final String CONFERENCIA_PARCIAL = "Conferência parcial";
  static final int INICIO_CONFERENCIA_DIAS = 1;
  static final int QUANTIDADE_CONFERIDA = 10;
  static final String DUAS_UNIDADES_EM_CONTAGEM = "Duas unidades em contagem";
  static final String ELETRONICOS = "Eletrônicos";
  static final String MATERIAL_ESPORTIVO = "Material esportivo";
  static final String REGULAMENTO_DE_DEMONSTRACAO = "Regulamento de demonstração";
  static final String DOCUMENTO_SINTETICO_SEM_DADOS_PESSOAIS =
      "Documento sintético sem dados pessoais.";
  static final String REGULAMENTO_V1_PDF = "regulamento-v1.pdf";
  static final String CONTEUDO_DEMONSTRATIVO_V1 = "Conteúdo demonstrativo v1";
  static final String REGULAMENTO_V2_PDF = "regulamento-v2.pdf";
  static final String CONTEUDO_DEMONSTRATIVO_V2 = "Conteúdo demonstrativo v2";
  static final int NUMERO_VERSAO_INICIAL = 1;
  static final String VERSAO_INICIAL = "Versão inicial";
  static final int PUBLICACAO_VERSAO_INICIAL_DIAS = 5;
  static final int NUMERO_VERSAO_ATUALIZADA = 2;
  static final String ATUALIZACAO_DE_DEMONSTRACAO = "Atualização de demonstração";
  static final String CARGA_DEV = "CARGA_DEV";
  static final String DOCUMENTO = "DOCUMENTO";
  static final String CARGA_DEMONSTRATIVA_INICIALIZADA = "Carga demonstrativa inicializada.";
  static final String CABECALHO_PDF = "%PDF-1.4\\n% Athletix demonstração\\n";
  static final String RODAPE_PDF = "\\n%%EOF";
  static final String APPLICATION_PDF = "application/pdf";
  static final String ALGORITMO_HASH_PDF = "SHA-256";

  static final CargoDemo PRESIDENCIA_DEMO =
      new CargoDemo(
          "Presidência",
          "Cargo administrativo inicial de desenvolvimento.",
          PerfilAcesso.ADMINISTRADOR);
  static final CargoDemo TESOURARIA_DEMO =
      new CargoDemo(
          "Tesouraria", "Cargo financeiro inicial de desenvolvimento.", PerfilAcesso.FINANCEIRO);
  static final CargoDemo COORDENACAO_SAUDE =
      new CargoDemo(
          "Coordenação", "Administração da atlética de saúde.", PerfilAcesso.ADMINISTRADOR);
  static final CargoDemo COORDENACAO_EVENTOS =
      new CargoDemo("Coordenação de Eventos", "Gestão de eventos.", PerfilAcesso.GESTOR);
  static final CargoDemo CARGO_ASSOCIADO =
      new CargoDemo("Associado", "Membro associado.", PerfilAcesso.MEMBRO);
  static final ContaDemo CONTA_ADMINISTRADOR =
      new ContaDemo("Administrador Athletix", "admin@athletix.local", "Athletix@2026");
  static final ContaDemo CONTA_FINANCEIRO =
      new ContaDemo("Financeiro Athletix", "financeiro@athletix.local", "Financeiro@2026");
  static final ContaDemo CONTA_SAUDE =
      new ContaDemo("Administração Saúde", "admin@saude.athletix.local", "Saude@2026");
  static final ContaDemo CONTA_GESTORA =
      new ContaDemo("Gestora de Eventos", "gestor@athletix.local", "Gestor@2026");
  static final ContaDemo CONTA_ASSOCIADO =
      new ContaDemo("Membro Demonstração", "membro@athletix.local", "Membro@2026");
  static final ContaDemo CONTA_PENDENTE =
      new ContaDemo("Pendente Demonstração", "pendente@athletix.local", "Pendente@2026");
  static final ContaDemo CONTA_INATIVO =
      new ContaDemo("Inativo Demonstração", "inativo@athletix.local", "Inativo@2026");
  static final LancamentoDemo RECEITA_PATROCINIO =
      new LancamentoDemo(
          TipoLancamentoFinanceiro.RECEITA, "Patrocínio sintético", "1500.00", -20, -10);
  static final LancamentoDemo DESPESA_LOCACAO =
      new LancamentoDemo(TipoLancamentoFinanceiro.DESPESA, "Locação de quadra", "480.00", -15, -5);
  static final LancamentoDemo DESPESA_MATERIAL =
      new LancamentoDemo(TipoLancamentoFinanceiro.DESPESA, "Material pendente", "220.00", 0, 10);
  static final LancamentoDemo DESPESA_NAO_APROVADA =
      new LancamentoDemo(TipoLancamentoFinanceiro.DESPESA, "Despesa rejeitada", "90.00", -2, 2);
  static final LancamentoDemo DESPESA_VENCIDA =
      new LancamentoDemo(TipoLancamentoFinanceiro.DESPESA, "Conta vencida", "130.00", -20, -2);

  record CargoDemo(String nome, String descricao, PerfilAcesso perfil) {}

  record ContaDemo(String nome, String email, String senha) {}

  record LancamentoDemo(
      TipoLancamentoFinanceiro tipo,
      String descricao,
      String valor,
      int competencia,
      int vencimento) {}

  private DemoData() {}
}
