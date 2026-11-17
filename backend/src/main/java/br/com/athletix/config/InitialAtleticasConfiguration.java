package br.com.athletix.config;

import br.com.athletix.arquivo.entity.Arquivo;
import br.com.athletix.arquivo.repository.ArquivoRepository;
import br.com.athletix.atletica.dto.AtleticaCreateRequest;
import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.entity.DominioEmailPermitido;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.atletica.repository.DominioEmailPermitidoRepository;
import br.com.athletix.atletica.service.AtleticaCadastroService;
import br.com.athletix.atletica.service.DominioEmail;
import br.com.athletix.auditoria.entity.AtividadeUsuario;
import br.com.athletix.auditoria.repository.AtividadeUsuarioRepository;
import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.cargo.repository.CargoRepository;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.departamento.repository.DepartamentoRepository;
import br.com.athletix.documento.entity.*;
import br.com.athletix.documento.repository.DocumentoRepository;
import br.com.athletix.documento.repository.VersaoDocumentoRepository;
import br.com.athletix.evento.entity.*;
import br.com.athletix.evento.repository.EventoRepository;
import br.com.athletix.financeiro.entity.*;
import br.com.athletix.financeiro.entity.CategoriaFinanceira;
import br.com.athletix.financeiro.entity.TipoAplicavelCategoria;
import br.com.athletix.financeiro.repository.CategoriaFinanceiraRepository;
import br.com.athletix.financeiro.repository.LancamentoFinanceiroRepository;
import br.com.athletix.gestao.entity.Gestao;
import br.com.athletix.gestao.entity.VinculoGestao;
import br.com.athletix.gestao.repository.GestaoRepository;
import br.com.athletix.gestao.repository.VinculoGestaoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.notificacao.entity.*;
import br.com.athletix.notificacao.repository.NotificacaoRepository;
import br.com.athletix.patrimonio.conferencia.entity.*;
import br.com.athletix.patrimonio.conferencia.repository.*;
import br.com.athletix.patrimonio.emprestimo.entity.EmprestimoPatrimonio;
import br.com.athletix.patrimonio.emprestimo.repository.EmprestimoPatrimonioRepository;
import br.com.athletix.patrimonio.entity.*;
import br.com.athletix.patrimonio.entity.CategoriaPatrimonio;
import br.com.athletix.patrimonio.manutencao.entity.ManutencaoPatrimonio;
import br.com.athletix.patrimonio.manutencao.repository.ManutencaoPatrimonioRepository;
import br.com.athletix.patrimonio.repository.CategoriaPatrimonioRepository;
import br.com.athletix.patrimonio.repository.ItemPatrimonioRepository;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.tarefa.entity.*;
import br.com.athletix.tarefa.repository.TarefaRepository;
import br.com.athletix.usuario.entity.Usuario;
import br.com.athletix.usuario.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

/** Dados de protótipo, restritos ao perfil de desenvolvimento. */
@Configuration
@Profile("dev")
public class InitialAtleticasConfiguration {

  @Bean
  CommandLineRunner initialAtleticas(
      AtleticaRepository atleticaRepository,
      DominioEmailPermitidoRepository dominioRepository,
      AtleticaCadastroService cadastroService,
      CargoRepository cargoRepository,
      DepartamentoRepository departamentoRepository,
      UsuarioRepository usuarioRepository,
      MembroRepository membroRepository,
      GestaoRepository gestaoRepository,
      VinculoGestaoRepository vinculoGestaoRepository,
      CategoriaFinanceiraRepository categoriaFinanceiraRepository,
      CategoriaPatrimonioRepository categoriaPatrimonioRepository,
      EventoRepository eventoRepository,
      TarefaRepository tarefaRepository,
      LancamentoFinanceiroRepository lancamentoRepository,
      ItemPatrimonioRepository itemPatrimonioRepository,
      EmprestimoPatrimonioRepository emprestimoRepository,
      ManutencaoPatrimonioRepository manutencaoRepository,
      ConferenciaInventarioRepository conferenciaRepository,
      ItemConferenciaRepository itemConferenciaRepository,
      DocumentoRepository documentoRepository,
      VersaoDocumentoRepository versaoDocumentoRepository,
      ArquivoRepository arquivoRepository,
      NotificacaoRepository notificacaoRepository,
      AtividadeUsuarioRepository atividadeRepository,
      PasswordEncoder passwordEncoder,
      @Value("${athletix.demo.completa:false}") boolean demoCompleta) {
    return args ->
        criarSeVazio(
            atleticaRepository,
            dominioRepository,
            cadastroService,
            cargoRepository,
            departamentoRepository,
            usuarioRepository,
            membroRepository,
            gestaoRepository,
            vinculoGestaoRepository,
            categoriaFinanceiraRepository,
            categoriaPatrimonioRepository,
            eventoRepository,
            tarefaRepository,
            lancamentoRepository,
            itemPatrimonioRepository,
            emprestimoRepository,
            manutencaoRepository,
            conferenciaRepository,
            itemConferenciaRepository,
            documentoRepository,
            versaoDocumentoRepository,
            arquivoRepository,
            notificacaoRepository,
            atividadeRepository,
            passwordEncoder,
            demoCompleta);
  }

  @Transactional
  void criarSeVazio(
      AtleticaRepository atleticaRepository,
      DominioEmailPermitidoRepository dominioRepository,
      AtleticaCadastroService cadastroService,
      CargoRepository cargoRepository,
      DepartamentoRepository departamentoRepository,
      UsuarioRepository usuarioRepository,
      MembroRepository membroRepository,
      GestaoRepository gestaoRepository,
      VinculoGestaoRepository vinculoGestaoRepository,
      CategoriaFinanceiraRepository categoriaFinanceiraRepository,
      CategoriaPatrimonioRepository categoriaPatrimonioRepository,
      EventoRepository eventoRepository,
      TarefaRepository tarefaRepository,
      LancamentoFinanceiroRepository lancamentoRepository,
      ItemPatrimonioRepository itemPatrimonioRepository,
      EmprestimoPatrimonioRepository emprestimoRepository,
      ManutencaoPatrimonioRepository manutencaoRepository,
      ConferenciaInventarioRepository conferenciaRepository,
      ItemConferenciaRepository itemConferenciaRepository,
      DocumentoRepository documentoRepository,
      VersaoDocumentoRepository versaoDocumentoRepository,
      ArquivoRepository arquivoRepository,
      NotificacaoRepository notificacaoRepository,
      AtividadeUsuarioRepository atividadeRepository,
      PasswordEncoder passwordEncoder,
      boolean demoCompleta) {
    if (atleticaRepository.count() > 0) {
      return;
    }

    Atletica engenharia =
        cadastroService.criar(
            new AtleticaCreateRequest(
                "Atlética de Engenharia Exemplo",
                "AAE",
                "Universidade Exemplo",
                null,
                "America/Sao_Paulo",
                true));
    Atletica saude =
        cadastroService.criar(
            new AtleticaCreateRequest(
                "Atlética de Saúde Exemplo",
                "AAS",
                "Universidade Exemplo",
                null,
                "America/Sao_Paulo",
                true));

    dominioRepository.save(
        new DominioEmailPermitido(engenharia, DominioEmail.normalize("engenharia.exemplo.edu.br")));
    dominioRepository.save(
        new DominioEmailPermitido(saude, DominioEmail.normalize("saude.exemplo.edu.br")));

    Cargo administrador =
        cargoRepository.save(
            new Cargo(
                engenharia,
                "Presidência",
                NomeNormalizado.fromRequired("Presidência", "nome"),
                "Cargo administrativo inicial de desenvolvimento.",
                PerfilAcesso.ADMINISTRADOR,
                true));
    Cargo financeiro =
        cargoRepository.save(
            new Cargo(
                engenharia,
                "Tesouraria",
                NomeNormalizado.fromRequired("Tesouraria", "nome"),
                "Cargo financeiro inicial de desenvolvimento.",
                PerfilAcesso.FINANCEIRO,
                true));
    Departamento diretoria =
        departamentoRepository.save(
            new Departamento(
                engenharia,
                "Diretoria",
                NomeNormalizado.fromRequired("Diretoria", "nome"),
                "Departamento inicial de desenvolvimento."));
    Usuario administradorUsuario =
        new Usuario("Administrador Athletix", "admin@athletix.local", null);
    administradorUsuario.definirSenhaHash(passwordEncoder.encode("Athletix@2026"));
    administradorUsuario = usuarioRepository.save(administradorUsuario);
    Membro administradorMembro =
        new Membro(
            engenharia,
            administradorUsuario,
            "Administração",
            null,
            administrador,
            diretoria,
            java.time.LocalDate.now());
    administradorMembro.ativar(null);
    administradorMembro = membroRepository.save(administradorMembro);
    diretoria.definirResponsavel(administradorMembro.getId());
    diretoria.ativar();
    departamentoRepository.save(diretoria);

    Gestao gestaoInicial =
        gestaoRepository.save(
            new Gestao(
                engenharia,
                "Gestão de Demonstração",
                java.time.LocalDate.now(),
                java.time.LocalDate.now().plusYears(1)));
    gestaoInicial.ativar();
    gestaoRepository.save(gestaoInicial);
    vinculoGestaoRepository.save(
        new VinculoGestao(
            administradorMembro, gestaoInicial, administrador, diretoria, java.time.Instant.now()));

    Usuario financeiroUsuario =
        new Usuario("Financeiro Athletix", "financeiro@athletix.local", null);
    financeiroUsuario.definirSenhaHash(passwordEncoder.encode("Financeiro@2026"));
    financeiroUsuario = usuarioRepository.save(financeiroUsuario);
    Membro financeiroMembro =
        new Membro(
            engenharia,
            financeiroUsuario,
            "Administração",
            null,
            financeiro,
            diretoria,
            java.time.LocalDate.now());
    financeiroMembro.ativar(administradorMembro);
    financeiroMembro = membroRepository.save(financeiroMembro);
    vinculoGestaoRepository.save(
        new VinculoGestao(
            financeiroMembro, gestaoInicial, financeiro, diretoria, java.time.Instant.now()));

    criarCategoriasIniciais(categoriaFinanceiraRepository, engenharia);
    criarCategoriasIniciais(categoriaFinanceiraRepository, saude);
    criarCategoriasPatrimonioIniciais(categoriaPatrimonioRepository, engenharia);
    criarCategoriasPatrimonioIniciais(categoriaPatrimonioRepository, saude);

    if (!demoCompleta) {
      return;
    }

    // A segunda atlética é intencionalmente pequena: ela existe para validar o isolamento de
    // escopo.
    Cargo adminSaude =
        cargoRepository.save(
            new Cargo(
                saude,
                "Coordenação",
                NomeNormalizado.fromRequired("Coordenação", "nome"),
                "Administração da atlética de saúde.",
                PerfilAcesso.ADMINISTRADOR,
                true));
    Departamento diretoriaSaude =
        departamentoRepository.save(
            new Departamento(
                saude,
                "Diretoria",
                NomeNormalizado.fromRequired("Diretoria", "nome"),
                "Departamento isolado de demonstração."));
    Usuario usuarioSaude =
        criarUsuario(
            usuarioRepository,
            "Administração Saúde",
            "admin@saude.athletix.local",
            "Saude@2026",
            passwordEncoder);
    Membro membroSaude =
        new Membro(saude, usuarioSaude, "Saúde", 2, adminSaude, diretoriaSaude, LocalDate.now());
    membroSaude.ativar(null);
    membroSaude = membroRepository.save(membroSaude);
    diretoriaSaude.definirResponsavel(membroSaude.getId());
    diretoriaSaude.ativar();
    departamentoRepository.save(diretoriaSaude);

    // Cenário demonstrável: tudo abaixo usa identidades sintéticas e datas relativas ao dia da
    // execução.
    LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
    Instant agora = Instant.now();
    Cargo gestor =
        cargoRepository.save(
            new Cargo(
                engenharia,
                "Coordenação de Eventos",
                NomeNormalizado.fromRequired("Coordenação de Eventos", "nome"),
                "Gestão de eventos.",
                PerfilAcesso.GESTOR,
                true));
    Cargo membro =
        cargoRepository.save(
            new Cargo(
                engenharia,
                "Associado",
                NomeNormalizado.fromRequired("Associado", "nome"),
                "Membro associado.",
                PerfilAcesso.MEMBRO,
                true));
    Departamento esportes =
        departamentoRepository.save(
            new Departamento(
                engenharia,
                "Esportes",
                NomeNormalizado.fromRequired("Esportes", "nome"),
                "Organização esportiva."));

    Membro gestorMembro =
        criarMembro(
            usuarioRepository,
            membroRepository,
            engenharia,
            gestor,
            esportes,
            "Gestora de Eventos",
            "gestor@athletix.local",
            "Gestor@2026",
            administradorMembro,
            passwordEncoder,
            hoje.minusMonths(5));
    Membro membroAtivo =
        criarMembro(
            usuarioRepository,
            membroRepository,
            engenharia,
            membro,
            esportes,
            "Membro Demonstração",
            "membro@athletix.local",
            "Membro@2026",
            administradorMembro,
            passwordEncoder,
            hoje.minusMonths(4));
    Membro pendente =
        new Membro(
            engenharia,
            criarUsuario(
                usuarioRepository,
                "Pendente Demonstração",
                "pendente@athletix.local",
                "Pendente@2026",
                passwordEncoder),
            "Engenharia",
            3,
            membro,
            esportes,
            hoje);
    membroRepository.save(pendente);
    Membro inativo =
        criarMembro(
            usuarioRepository,
            membroRepository,
            engenharia,
            membro,
            esportes,
            "Inativo Demonstração",
            "inativo@athletix.local",
            "Inativo@2026",
            administradorMembro,
            passwordEncoder,
            hoje.minusYears(1));
    inativo.inativar();
    membroRepository.save(inativo);
    esportes.definirResponsavel(gestorMembro.getId());
    esportes.ativar();
    departamentoRepository.save(esportes);

    Gestao anterior =
        new Gestao(engenharia, "Gestão anterior", hoje.minusYears(1), hoje.minusDays(1));
    anterior.ativar();
    anterior.encerrar(agora.minus(Duration.ofDays(1)));
    anterior = gestaoRepository.save(anterior);
    vinculoGestaoRepository.save(
        new VinculoGestao(
            gestorMembro, anterior, gestor, esportes, agora.minus(Duration.ofDays(300))));
    Gestao atual = gestaoInicial;
    vinculoGestaoRepository.save(
        new VinculoGestao(
            gestorMembro, atual, gestor, esportes, agora.minus(Duration.ofDays(120))));
    vinculoGestaoRepository.save(
        new VinculoGestao(membroAtivo, atual, membro, esportes, agora.minus(Duration.ofDays(100))));

    OffsetDateTime inicio = OffsetDateTime.now(ZoneId.of("America/Sao_Paulo"));
    Evento planejado =
        eventoRepository.save(
            evento(
                engenharia,
                atual,
                administradorMembro,
                "Torneio de Integração",
                inicio.plusDays(14),
                inicio.plusDays(15)));
    Evento inscricoes =
        eventoRepository.save(
            evento(
                engenharia,
                atual,
                gestorMembro,
                "Copa Aberta",
                inicio.plusDays(7),
                inicio.plusDays(8)));
    inscricoes.alterarStatus(StatusEvento.INSCRICOES_ABERTAS);
    eventoRepository.save(inscricoes);
    Evento andamento =
        eventoRepository.save(
            evento(
                engenharia,
                atual,
                gestorMembro,
                "Semana Esportiva",
                inicio.minusHours(2),
                inicio.plusDays(1)));
    andamento.alterarStatus(StatusEvento.EM_ANDAMENTO);
    eventoRepository.save(andamento);
    Evento concluido =
        eventoRepository.save(
            evento(
                engenharia,
                anterior,
                administradorMembro,
                "Jogos do Semestre",
                inicio.minusDays(30),
                inicio.minusDays(29)));
    concluido.alterarStatus(StatusEvento.CONCLUIDO);
    eventoRepository.save(concluido);

    Tarefa t1 =
        tarefaRepository.save(
            tarefa(
                engenharia,
                atual,
                planejado,
                gestorMembro,
                esportes,
                "Reservar quadra",
                PrioridadeTarefa.ALTA,
                hoje.plusDays(3)));
    Tarefa t2 =
        tarefaRepository.save(
            tarefa(
                engenharia,
                atual,
                inscricoes,
                membroAtivo,
                esportes,
                "Divulgar inscrições",
                PrioridadeTarefa.MEDIA,
                hoje.plusDays(1)));
    t2.status(StatusTarefa.EM_ANDAMENTO);
    tarefaRepository.save(t2);
    Tarefa t3 =
        tarefaRepository.save(
            tarefa(
                engenharia,
                atual,
                andamento,
                gestorMembro,
                esportes,
                "Conferir materiais",
                PrioridadeTarefa.MEDIA,
                hoje.minusDays(1)));
    t3.status(StatusTarefa.REVISAO);
    tarefaRepository.save(t3);
    Tarefa t4 =
        tarefaRepository.save(
            tarefa(
                engenharia,
                atual,
                concluido,
                membroAtivo,
                esportes,
                "Publicar resultados",
                PrioridadeTarefa.BAIXA,
                hoje.minusDays(5)));
    t4.status(StatusTarefa.CONCLUIDO);
    tarefaRepository.save(t4);
    notificacaoRepository.save(
        new Notificacao(
            engenharia.getId(),
            gestorMembro.getId(),
            t3.getId(),
            TipoNotificacao.ATRASADA,
            hoje,
            "Tarefa atrasada",
            "Conferir materiais está com prazo vencido."));

    CategoriaFinanceira eventos =
        categoriaFinanceiraRepository.findAllByAtletica_IdOrderByNomeAsc(engenharia.getId()).get(0);
    LancamentoFinanceiro receita =
        lancamentoRepository.save(
            lancamento(
                engenharia,
                atual,
                planejado,
                TipoLancamentoFinanceiro.RECEITA,
                "Patrocínio sintético",
                eventos,
                "1500.00",
                hoje.minusDays(20),
                hoje.minusDays(10),
                administradorMembro));
    receita.liquidar(hoje.minusDays(10), financeiroMembro.getId());
    lancamentoRepository.save(receita);
    LancamentoFinanceiro paga =
        lancamentoRepository.save(
            lancamento(
                engenharia,
                atual,
                planejado,
                TipoLancamentoFinanceiro.DESPESA,
                "Locação de quadra",
                eventos,
                "480.00",
                hoje.minusDays(15),
                hoje.minusDays(5),
                gestorMembro));
    paga.decidirAprovacao(
        StatusAprovacao.APROVADA,
        "Aprovada para demonstração",
        financeiroMembro.getId(),
        agora.minus(Duration.ofDays(14)));
    paga.liquidar(hoje.minusDays(4), financeiroMembro.getId());
    lancamentoRepository.save(paga);
    lancamentoRepository.save(
        lancamento(
            engenharia,
            atual,
            inscricoes,
            TipoLancamentoFinanceiro.DESPESA,
            "Material pendente",
            eventos,
            "220.00",
            hoje,
            hoje.plusDays(10),
            gestorMembro));
    LancamentoFinanceiro rejeitada =
        lancamentoRepository.save(
            lancamento(
                engenharia,
                atual,
                null,
                TipoLancamentoFinanceiro.DESPESA,
                "Despesa rejeitada",
                eventos,
                "90.00",
                hoje.minusDays(2),
                hoje.plusDays(2),
                gestorMembro));
    rejeitada.decidirAprovacao(
        StatusAprovacao.REJEITADA, "Não necessária", financeiroMembro.getId(), agora);
    lancamentoRepository.save(rejeitada);
    lancamentoRepository.save(
        lancamento(
            engenharia,
            atual,
            null,
            TipoLancamentoFinanceiro.DESPESA,
            "Conta vencida",
            eventos,
            "130.00",
            hoje.minusDays(20),
            hoje.minusDays(2),
            gestorMembro));
    for (int mesesAtras = 1; mesesAtras <= 5; mesesAtras++) {
      LocalDate competencia = hoje.minusMonths(mesesAtras).withDayOfMonth(10);
      LancamentoFinanceiro historico =
          lancamentoRepository.save(
              lancamento(
                  engenharia,
                  atual,
                  null,
                  TipoLancamentoFinanceiro.RECEITA,
                  "Receita histórica sintética " + mesesAtras,
                  eventos,
                  "300.00",
                  competencia,
                  competencia,
                  administradorMembro));
      historico.liquidar(competencia, financeiroMembro.getId());
      lancamentoRepository.save(historico);
    }

    CategoriaPatrimonio eletronicos =
        categoriaPatrimonioRepository.findAllByAtletica_IdOrderByNomeAsc(engenharia.getId()).get(0);
    ItemPatrimonio disponivel =
        itemPatrimonioRepository.save(
            new ItemPatrimonio(
                engenharia,
                atual.getId(),
                "Bolas oficiais",
                "Lote sintético disponível",
                eletronicos,
                12,
                ConservacaoPatrimonio.BOM,
                "Almoxarifado",
                esportes,
                new BigDecimal("80.00"),
                true));
    ItemPatrimonio emprestado =
        itemPatrimonioRepository.save(
            new ItemPatrimonio(
                engenharia,
                atual.getId(),
                "Coletes",
                "Lote demonstrativo",
                eletronicos,
                10,
                ConservacaoPatrimonio.OTIMO,
                "Almoxarifado",
                esportes,
                new BigDecimal("25.00"),
                true));
    ItemPatrimonio manutencao =
        itemPatrimonioRepository.save(
            new ItemPatrimonio(
                engenharia,
                atual.getId(),
                "Caixa de som",
                "Equipamento sintético",
                eletronicos,
                2,
                ConservacaoPatrimonio.REGULAR,
                "Sala de eventos",
                esportes,
                new BigDecimal("300.00"),
                true));
    emprestimoRepository.save(
        new EmprestimoPatrimonio(
            engenharia,
            emprestado,
            membroAtivo.getId(),
            3,
            agora.minus(Duration.ofDays(2)),
            agora.plus(Duration.ofDays(4)),
            "Empréstimo demonstrativo",
            gestorMembro.getId()));
    manutencaoRepository.save(
        new ManutencaoPatrimonio(
            engenharia,
            manutencao,
            1,
            "Revisão demonstrativa",
            agora.minus(Duration.ofDays(1)),
            agora.plus(Duration.ofDays(3)),
            gestorMembro.getId()));
    ConferenciaInventario conferencia =
        conferenciaRepository.save(
            new ConferenciaInventario(
                engenharia,
                atual.getId(),
                "Conferência parcial",
                agora.minus(Duration.ofDays(1)),
                administradorMembro.getId()));
    ItemConferencia linha =
        new ItemConferencia(conferencia, disponivel.getId(), disponivel.getQuantidadeTotal());
    linha.conferir(10, "Duas unidades em contagem", administradorMembro.getId());
    itemConferenciaRepository.save(linha);

    Documento documento =
        documentoRepository.save(
            new Documento(
                engenharia,
                "Regulamento de demonstração",
                CategoriaDocumento.REGIMENTO,
                "Documento sintético sem dados pessoais.",
                VisibilidadeDocumento.MEMBROS,
                atual.getId()));
    Arquivo arquivo1 =
        arquivoRepository.save(
            arquivo(
                engenharia,
                administradorMembro,
                "regulamento-v1.pdf",
                "Conteúdo demonstrativo v1"));
    Arquivo arquivo2 =
        arquivoRepository.save(
            arquivo(
                engenharia,
                administradorMembro,
                "regulamento-v2.pdf",
                "Conteúdo demonstrativo v2"));
    versaoDocumentoRepository.save(
        new VersaoDocumento(
            engenharia,
            documento,
            1,
            arquivo1.getId(),
            atual.getId(),
            "Versão inicial",
            administradorMembro.getId(),
            agora.minus(Duration.ofDays(5))));
    versaoDocumentoRepository.save(
        new VersaoDocumento(
            engenharia,
            documento,
            2,
            arquivo2.getId(),
            atual.getId(),
            "Atualização de demonstração",
            administradorMembro.getId(),
            agora));
    atividadeRepository.save(
        new AtividadeUsuario(
            engenharia.getId(),
            administradorUsuario.getId(),
            administradorMembro.getId(),
            "CARGA_DEV",
            "DOCUMENTO",
            documento.getId(),
            "Carga demonstrativa inicializada.",
            agora,
            atual.getId()));
  }

  private void criarCategoriasIniciais(
      CategoriaFinanceiraRepository categorias, Atletica atletica) {
    categorias.save(
        new CategoriaFinanceira(
            atletica,
            "Eventos",
            NomeNormalizado.fromRequired("Eventos", "nome"),
            TipoAplicavelCategoria.AMBOS,
            true));
    categorias.save(
        new CategoriaFinanceira(
            atletica,
            "Esportivo",
            NomeNormalizado.fromRequired("Esportivo", "nome"),
            TipoAplicavelCategoria.DESPESA,
            true));
    categorias.save(
        new CategoriaFinanceira(
            atletica,
            "Patrocínio",
            NomeNormalizado.fromRequired("Patrocínio", "nome"),
            TipoAplicavelCategoria.RECEITA,
            true));
  }

  private void criarCategoriasPatrimonioIniciais(
      CategoriaPatrimonioRepository categorias, Atletica atletica) {
    categorias.save(
        new CategoriaPatrimonio(
            atletica, "Eletrônicos", NomeNormalizado.fromRequired("Eletrônicos", "nome"), true));
    categorias.save(
        new CategoriaPatrimonio(
            atletica,
            "Material esportivo",
            NomeNormalizado.fromRequired("Material esportivo", "nome"),
            true));
  }

  private Usuario criarUsuario(
      UsuarioRepository usuarios,
      String nome,
      String email,
      String senha,
      PasswordEncoder encoder) {
    Usuario usuario = new Usuario(nome, email, null);
    usuario.definirSenhaHash(encoder.encode(senha));
    return usuarios.save(usuario);
  }

  private Membro criarMembro(
      UsuarioRepository usuarios,
      MembroRepository membros,
      Atletica atletica,
      Cargo cargo,
      Departamento departamento,
      String nome,
      String email,
      String senha,
      Membro validador,
      PasswordEncoder encoder,
      LocalDate desde) {
    Membro membro =
        new Membro(
            atletica,
            criarUsuario(usuarios, nome, email, senha, encoder),
            "Engenharia",
            4,
            cargo,
            departamento,
            desde);
    membro.ativar(validador);
    return membros.save(membro);
  }

  private Evento evento(
      Atletica atletica,
      Gestao gestao,
      Membro criador,
      String nome,
      OffsetDateTime inicio,
      OffsetDateTime fim) {
    return new Evento(
        atletica,
        gestao.getId(),
        nome,
        "Evento sintético para demonstração.",
        inicio,
        fim,
        "Campus Exemplo",
        "São Paulo",
        "SP",
        new BigDecimal("2000.00"),
        80,
        criador.getId());
  }

  private Tarefa tarefa(
      Atletica atletica,
      Gestao gestao,
      Evento evento,
      Membro responsavel,
      Departamento departamento,
      String titulo,
      PrioridadeTarefa prioridade,
      LocalDate prazo) {
    return new Tarefa(
        atletica,
        gestao.getId(),
        evento,
        titulo,
        "Tarefa sintética para demonstração.",
        responsavel,
        departamento,
        prioridade,
        prazo,
        responsavel.getId());
  }

  private LancamentoFinanceiro lancamento(
      Atletica atletica,
      Gestao gestao,
      Evento evento,
      TipoLancamentoFinanceiro tipo,
      String descricao,
      CategoriaFinanceira categoria,
      String valor,
      LocalDate competencia,
      LocalDate vencimento,
      Membro criador) {
    return new LancamentoFinanceiro(
        atletica,
        gestao.getId(),
        evento,
        tipo,
        descricao,
        categoria,
        new BigDecimal(valor),
        competencia,
        vencimento,
        criador.getId());
  }

  private Arquivo arquivo(Atletica atletica, Membro enviadoPor, String nome, String conteudo) {
    // PDF mínimo sintético: nenhuma informação pessoal é incluída na carga.
    byte[] bytes =
        ("%PDF-1.4\\n% Athletix demonstração\\n" + conteudo + "\\n%%EOF")
            .getBytes(StandardCharsets.UTF_8);
    return new Arquivo(
        atletica,
        nome,
        "application/pdf",
        bytes.length,
        sha256(bytes),
        enviadoPor.getId(),
        Instant.now(),
        bytes);
  }

  private String sha256(byte[] bytes) {
    try {
      return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    } catch (Exception exception) {
      throw new IllegalStateException(
          "Não foi possível gerar hash da carga de demonstração", exception);
    }
  }
}
