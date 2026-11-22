package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.DURACAO_GESTAO_ANOS;
import static br.com.athletix.config.demo.DemoData.ENCERRAMENTO_GESTAO_ANTERIOR_DIAS;
import static br.com.athletix.config.demo.DemoData.FIM_GESTAO_ANTERIOR_DIAS;
import static br.com.athletix.config.demo.DemoData.GESTAO_ANTERIOR;
import static br.com.athletix.config.demo.DemoData.GESTAO_DE_DEMONSTRACAO;
import static br.com.athletix.config.demo.DemoData.INICIO_GESTAO_ANTERIOR_ANOS;
import static br.com.athletix.config.demo.DemoData.VINCULO_ASSOCIADO_ATUAL_DIAS;
import static br.com.athletix.config.demo.DemoData.VINCULO_GESTORA_ANTERIOR_DIAS;
import static br.com.athletix.config.demo.DemoData.VINCULO_GESTORA_ATUAL_DIAS;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.gestao.entity.Gestao;
import br.com.athletix.gestao.entity.VinculoGestao;
import br.com.athletix.gestao.repository.GestaoRepository;
import br.com.athletix.gestao.repository.VinculoGestaoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.shared.time.FusoHorarioAtletica;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
class GestoesDemoSeeder {
  private final GestaoRepository gestaoRepository;
  private final VinculoGestaoRepository vinculoGestaoRepository;

  GestoesDemoSeeder(
      GestaoRepository gestaoRepository, VinculoGestaoRepository vinculoGestaoRepository) {
    this.gestaoRepository = gestaoRepository;
    this.vinculoGestaoRepository = vinculoGestaoRepository;
  }

  Gestao criarInicial(Atletica engenharia) {
    Gestao gestaoInicial =
        gestaoRepository.save(
            new Gestao(
                engenharia,
                GESTAO_DE_DEMONSTRACAO,
                FusoHorarioAtletica.hoje(engenharia.getFusoHorario()),
                FusoHorarioAtletica.hoje(engenharia.getFusoHorario())
                    .plusYears(DURACAO_GESTAO_ANOS)));
    gestaoInicial.ativar();
    gestaoRepository.save(gestaoInicial);
    return gestaoInicial;
  }

  void criarVinculo(Membro membro, Gestao gestao, Cargo cargo, Departamento departamento) {
    vinculoGestaoRepository.save(
        new VinculoGestao(membro, gestao, cargo, departamento, Instant.now()));
  }

  Gestao criarHistorico(
      Atletica engenharia,
      Gestao atual,
      Membro gestorMembro,
      Cargo gestor,
      Membro membroAtivo,
      Cargo membro,
      Departamento esportes,
      LocalDate hoje,
      Instant agora) {
    Gestao anterior =
        new Gestao(
            engenharia,
            GESTAO_ANTERIOR,
            hoje.minusYears(INICIO_GESTAO_ANTERIOR_ANOS),
            hoje.minusDays(FIM_GESTAO_ANTERIOR_DIAS));
    anterior.ativar();
    anterior.encerrar(agora.minus(Duration.ofDays(ENCERRAMENTO_GESTAO_ANTERIOR_DIAS)));
    anterior = gestaoRepository.save(anterior);
    vinculoGestaoRepository.save(
        new VinculoGestao(
            gestorMembro,
            anterior,
            gestor,
            esportes,
            agora.minus(Duration.ofDays(VINCULO_GESTORA_ANTERIOR_DIAS))));
    vinculoGestaoRepository.save(
        new VinculoGestao(
            gestorMembro,
            atual,
            gestor,
            esportes,
            agora.minus(Duration.ofDays(VINCULO_GESTORA_ATUAL_DIAS))));
    vinculoGestaoRepository.save(
        new VinculoGestao(
            membroAtivo,
            atual,
            membro,
            esportes,
            agora.minus(Duration.ofDays(VINCULO_ASSOCIADO_ATUAL_DIAS))));
    return anterior;
  }
}
