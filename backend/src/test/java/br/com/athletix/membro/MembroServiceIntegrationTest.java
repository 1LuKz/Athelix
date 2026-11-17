package br.com.athletix.membro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.cargo.dto.CargoCreateRequest;
import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.cargo.service.CargoService;
import br.com.athletix.departamento.dto.DepartamentoCreateRequest;
import br.com.athletix.departamento.dto.DepartamentoTransitionRequest;
import br.com.athletix.departamento.dto.ResponsavelDepartamentoUpdateRequest;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.departamento.entity.SituacaoDepartamento;
import br.com.athletix.departamento.service.DepartamentoService;
import br.com.athletix.membro.dto.MembroActivationRequest;
import br.com.athletix.membro.dto.MembroCreateRequest;
import br.com.athletix.membro.dto.MembroInactivationRequest;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.membro.service.MembroService;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.tenancy.ContextoAtletica;
import br.com.athletix.usuario.dto.SenhaDefinicaoRequest;
import br.com.athletix.usuario.dto.UsuarioConviteCreateRequest;
import br.com.athletix.usuario.entity.Usuario;
import br.com.athletix.usuario.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
class MembroServiceIntegrationTest {

  @Autowired private AtleticaRepository atleticaRepository;

  @Autowired private ContextoAtletica contextoAtletica;

  @Autowired private CargoService cargoService;

  @Autowired private DepartamentoService departamentoService;

  @Autowired private UsuarioService usuarioService;

  @Autowired private MembroService membroService;

  @Test
  void createsPendingMemberAndAllowsFirstMemberActivationInConfiguration() {
    Long atleticaId = atleticaRepository.findAll().get(0).getId();
    Usuario usuario =
        usuarioService.criarPorConvite(
            new UsuarioConviteCreateRequest(
                "Maria da Silva", "Maria.Silva@Exemplo.edu.br", "(11) 99999-9999"));

    try (ContextoAtletica.Escopo ignored = contextoAtletica.abrir(atleticaId)) {
      Cargo cargo =
          cargoService.criar(
              new CargoCreateRequest("Membro B05", null, PerfilAcesso.MEMBRO, true),
              PerfilAcesso.ADMINISTRADOR);
      Departamento departamento =
          departamentoService.criarEmConfiguracao(
              new DepartamentoCreateRequest("Recepção B05", null));
      Membro pendente =
          membroService.criarPendente(
              new MembroCreateRequest(
                  usuario.getId(),
                  "Engenharia de Software",
                  4,
                  cargo.getId(),
                  departamento.getId(),
                  null));

      assertEquals(StatusMembro.PENDENTE, pendente.getStatus());
      assertFalse(membroService.possuiAcessoAtivo(usuario.getId()));
      assertThrows(
          BusinessRuleException.class,
          () ->
              membroService.ativar(
                  pendente.getId(), new MembroActivationRequest(null, pendente.getVersion())));

      usuarioService.definirSenha(usuario.getId(), new SenhaDefinicaoRequest("senha-de-teste"));
      Membro ativo =
          membroService.ativar(
              pendente.getId(), new MembroActivationRequest(null, pendente.getVersion()));

      assertEquals(StatusMembro.ATIVO, ativo.getStatus());
      assertTrue(membroService.possuiAcessoAtivo(usuario.getId()));

      Departamento comResponsavel =
          departamentoService.designarResponsavel(
              departamento.getId(),
              new ResponsavelDepartamentoUpdateRequest(ativo.getId(), departamento.getVersion()));
      assertEquals(ativo.getId(), comResponsavel.getResponsavelMembroId());
      Departamento departamentoAtivo =
          departamentoService.ativar(
              comResponsavel.getId(),
              new DepartamentoTransitionRequest(comResponsavel.getVersion()));
      assertEquals(SituacaoDepartamento.ATIVO, departamentoAtivo.getSituacao());
      assertThrows(
          BusinessRuleException.class,
          () ->
              membroService.inativar(
                  ativo.getId(), new MembroInactivationRequest(ativo.getVersion())));
    }
  }
}
