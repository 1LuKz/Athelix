package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.*;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.repository.MembroRepository;
import java.time.LocalDate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class MembrosExtrasDemoSeeder {
  private final CargosEMembrosDemoSeeder pessoas;
  private final MembroRepository membroRepository;

  MembrosExtrasDemoSeeder(CargosEMembrosDemoSeeder pessoas, MembroRepository membroRepository) {
    this.pessoas = pessoas;
    this.membroRepository = membroRepository;
  }

  MembrosExtras criarMembrosExtras(
      Atletica engenharia,
      Cargo gestor,
      Cargo membro,
      Departamento esportes,
      Membro administradorMembro,
      LocalDate hoje) {
    Membro gestorMembro =
        pessoas.criarMembro(
            engenharia,
            gestor,
            esportes,
            CONTA_GESTORA,
            administradorMembro,
            hoje.minusMonths(TEMPO_GESTORA_MESES));
    Membro membroAtivo =
        pessoas.criarMembro(
            engenharia,
            membro,
            esportes,
            CONTA_ASSOCIADO,
            administradorMembro,
            hoje.minusMonths(TEMPO_ASSOCIADO_MESES));
    criarMembrosSemAcesso(engenharia, membro, esportes, administradorMembro, hoje);
    return new MembrosExtras(gestorMembro, membroAtivo);
  }

  private void criarMembrosSemAcesso(
      Atletica engenharia,
      Cargo membro,
      Departamento esportes,
      Membro administradorMembro,
      LocalDate hoje) {
    Membro pendente =
        new Membro(
            engenharia,
            pessoas.criarUsuario(CONTA_PENDENTE),
            ENGENHARIA,
            PERIODO_PENDENTE,
            membro,
            esportes,
            hoje);
    membroRepository.save(pendente);
    Membro inativo =
        pessoas.criarMembro(
            engenharia,
            membro,
            esportes,
            CONTA_INATIVO,
            administradorMembro,
            hoje.minusYears(TEMPO_INATIVO_ANOS));
    inativo.inativar();
    membroRepository.save(inativo);
  }

  record MembrosExtras(Membro gestor, Membro associado) {}
}
