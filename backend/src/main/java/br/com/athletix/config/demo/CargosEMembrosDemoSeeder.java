package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.*;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.cargo.repository.CargoRepository;
import br.com.athletix.config.demo.DemoData.CargoDemo;
import br.com.athletix.config.demo.DemoData.ContaDemo;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.time.FusoHorarioAtletica;
import br.com.athletix.usuario.entity.Usuario;
import br.com.athletix.usuario.repository.UsuarioRepository;
import java.time.LocalDate;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class CargosEMembrosDemoSeeder {
  private final CargoRepository cargoRepository;
  private final UsuarioRepository usuarioRepository;
  private final MembroRepository membroRepository;
  private final PasswordEncoder passwordEncoder;

  CargosEMembrosDemoSeeder(
      CargoRepository cargoRepository,
      UsuarioRepository usuarioRepository,
      MembroRepository membroRepository,
      PasswordEncoder passwordEncoder) {
    this.cargoRepository = cargoRepository;
    this.usuarioRepository = usuarioRepository;
    this.membroRepository = membroRepository;
    this.passwordEncoder = passwordEncoder;
  }

  Cargo criarCargo(Atletica atletica, CargoDemo cargo) {
    return cargoRepository.save(
        new Cargo(
            atletica,
            cargo.nome(),
            NomeNormalizado.fromRequired(cargo.nome(), CAMPO_NOME),
            cargo.descricao(),
            cargo.perfil(),
            true));
  }

  Membro criarMembroDiretoria(
      Atletica atletica, Cargo cargo, Departamento diretoria, ContaDemo conta, Membro validador) {
    Usuario usuario = criarUsuario(conta);
    return ativarMembro(
        atletica,
        usuario,
        ADMINISTRACAO,
        null,
        cargo,
        diretoria,
        FusoHorarioAtletica.hoje(atletica.getFusoHorario()),
        validador);
  }

  Membro criarAdministradorSaude(Atletica saude, Cargo adminSaude, Departamento diretoriaSaude) {
    Usuario usuario = criarUsuario(CONTA_SAUDE);
    return ativarMembro(
        saude,
        usuario,
        SAUDE,
        PERIODO_SAUDE,
        adminSaude,
        diretoriaSaude,
        FusoHorarioAtletica.hoje(saude.getFusoHorario()),
        null);
  }

  Usuario criarUsuario(ContaDemo conta) {
    Usuario usuario = new Usuario(conta.nome(), conta.email(), null);
    usuario.definirSenhaHash(passwordEncoder.encode(conta.senha()));
    return usuarioRepository.save(usuario);
  }

  Membro criarMembro(
      Atletica atletica,
      Cargo cargo,
      Departamento departamento,
      ContaDemo conta,
      Membro validador,
      LocalDate desde) {
    Usuario usuario = criarUsuario(conta);
    return ativarMembro(
        atletica, usuario, ENGENHARIA, PERIODO_ENGENHARIA, cargo, departamento, desde, validador);
  }

  private Membro ativarMembro(
      Atletica atletica,
      Usuario usuario,
      String curso,
      Integer periodo,
      Cargo cargo,
      Departamento departamento,
      LocalDate desde,
      Membro validador) {
    Membro membro = new Membro(atletica, usuario, curso, periodo, cargo, departamento, desde);
    membro.ativar(validador);
    return membroRepository.save(membro);
  }
}
