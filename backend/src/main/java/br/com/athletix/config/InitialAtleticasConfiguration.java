package br.com.athletix.config;

import br.com.athletix.atletica.dto.AtleticaCreateRequest;
import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.entity.DominioEmailPermitido;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.atletica.repository.DominioEmailPermitidoRepository;
import br.com.athletix.atletica.service.AtleticaCadastroService;
import br.com.athletix.atletica.service.DominioEmail;
import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.cargo.repository.CargoRepository;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.departamento.repository.DepartamentoRepository;
import br.com.athletix.financeiro.entity.CategoriaFinanceira;
import br.com.athletix.financeiro.entity.TipoAplicavelCategoria;
import br.com.athletix.financeiro.repository.CategoriaFinanceiraRepository;
import br.com.athletix.gestao.entity.Gestao;
import br.com.athletix.gestao.entity.VinculoGestao;
import br.com.athletix.gestao.repository.GestaoRepository;
import br.com.athletix.gestao.repository.VinculoGestaoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.usuario.entity.Usuario;
import br.com.athletix.usuario.repository.UsuarioRepository;
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
            PasswordEncoder passwordEncoder
    ) {
        return args -> criarSeVazio(atleticaRepository, dominioRepository, cadastroService, cargoRepository,
                departamentoRepository, usuarioRepository, membroRepository, gestaoRepository,
                vinculoGestaoRepository, categoriaFinanceiraRepository, passwordEncoder);
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
            PasswordEncoder passwordEncoder
    ) {
        if (atleticaRepository.count() > 0) {
            return;
        }

        Atletica engenharia = cadastroService.criar(new AtleticaCreateRequest(
                "Atlética de Engenharia Exemplo", "AAE", "Universidade Exemplo", null,
                "America/Sao_Paulo", true
        ));
        Atletica saude = cadastroService.criar(new AtleticaCreateRequest(
                "Atlética de Saúde Exemplo", "AAS", "Universidade Exemplo", null,
                "America/Sao_Paulo", true
        ));

        dominioRepository.save(new DominioEmailPermitido(engenharia, DominioEmail.normalize("engenharia.exemplo.edu.br")));
        dominioRepository.save(new DominioEmailPermitido(saude, DominioEmail.normalize("saude.exemplo.edu.br")));

        Cargo administrador = cargoRepository.save(new Cargo(
                engenharia, "Presidência", NomeNormalizado.fromRequired("Presidência", "nome"),
                "Cargo administrativo inicial de desenvolvimento.", PerfilAcesso.ADMINISTRADOR, true
        ));
        Cargo financeiro = cargoRepository.save(new Cargo(
                engenharia, "Tesouraria", NomeNormalizado.fromRequired("Tesouraria", "nome"),
                "Cargo financeiro inicial de desenvolvimento.", PerfilAcesso.FINANCEIRO, true
        ));
        Departamento diretoria = departamentoRepository.save(new Departamento(
                engenharia, "Diretoria", NomeNormalizado.fromRequired("Diretoria", "nome"),
                "Departamento inicial de desenvolvimento."
        ));
        Usuario administradorUsuario = new Usuario("Administrador Athletix", "admin@athletix.local", null);
        administradorUsuario.definirSenhaHash(passwordEncoder.encode("Athletix@2026"));
        administradorUsuario = usuarioRepository.save(administradorUsuario);
        Membro administradorMembro = new Membro(engenharia, administradorUsuario, "Administração", null,
                administrador, diretoria, java.time.LocalDate.now());
        administradorMembro.ativar(null);
        administradorMembro = membroRepository.save(administradorMembro);
        diretoria.definirResponsavel(administradorMembro.getId());
        diretoria.ativar();
        departamentoRepository.save(diretoria);

        Gestao gestaoInicial = gestaoRepository.save(new Gestao(
                engenharia, "Gestão de Demonstração", java.time.LocalDate.now(), java.time.LocalDate.now().plusYears(1)
        ));
        gestaoInicial.ativar();
        gestaoRepository.save(gestaoInicial);
        vinculoGestaoRepository.save(new VinculoGestao(administradorMembro, gestaoInicial, administrador,
                diretoria, java.time.Instant.now()));

        Usuario financeiroUsuario = new Usuario("Financeiro Athletix", "financeiro@athletix.local", null);
        financeiroUsuario.definirSenhaHash(passwordEncoder.encode("Financeiro@2026"));
        financeiroUsuario = usuarioRepository.save(financeiroUsuario);
        Membro financeiroMembro = new Membro(engenharia, financeiroUsuario, "Administração", null,
                financeiro, diretoria, java.time.LocalDate.now());
        financeiroMembro.ativar(administradorMembro);
        financeiroMembro = membroRepository.save(financeiroMembro);
        vinculoGestaoRepository.save(new VinculoGestao(financeiroMembro, gestaoInicial, financeiro,
                diretoria, java.time.Instant.now()));

        criarCategoriasIniciais(categoriaFinanceiraRepository, engenharia);
        criarCategoriasIniciais(categoriaFinanceiraRepository, saude);
    }

    private void criarCategoriasIniciais(CategoriaFinanceiraRepository categorias, Atletica atletica) {
        categorias.save(new CategoriaFinanceira(atletica, "Eventos",
                NomeNormalizado.fromRequired("Eventos", "nome"), TipoAplicavelCategoria.AMBOS, true));
        categorias.save(new CategoriaFinanceira(atletica, "Esportivo",
                NomeNormalizado.fromRequired("Esportivo", "nome"), TipoAplicavelCategoria.DESPESA, true));
        categorias.save(new CategoriaFinanceira(atletica, "Patrocínio",
                NomeNormalizado.fromRequired("Patrocínio", "nome"), TipoAplicavelCategoria.RECEITA, true));
    }
}
