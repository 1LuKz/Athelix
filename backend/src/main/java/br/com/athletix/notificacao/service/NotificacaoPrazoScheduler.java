package br.com.athletix.notificacao.service;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class NotificacaoPrazoScheduler {
  private static final Logger log = LoggerFactory.getLogger(NotificacaoPrazoScheduler.class);

  private final AtleticaRepository atleticas;
  private final NotificacaoPrazoService service;

  public NotificacaoPrazoScheduler(AtleticaRepository atleticas, NotificacaoPrazoService service) {
    this.atleticas = atleticas;
    this.service = service;
  }

  @Scheduled(
      fixedDelayString = "${athletix.notificacoes.intervalo-ms:3600000}",
      initialDelayString = "${athletix.notificacoes.atraso-inicial-ms:60000}")
  public void executar() {
    Instant agora = Instant.now();
    for (Atletica atletica : atleticas.findAllByAtivaTrue()) {
      try {
        int criadas = service.gerarParaAtletica(atletica.getId(), agora);
        if (criadas > 0) {
          log.info("Atlética {}: {} aviso(s) de prazo gerado(s).", atletica.getId(), criadas);
        }
      } catch (RuntimeException excecao) {
        // uma atlética com erro não impede as demais; a próxima execução tenta de novo
        log.error("Falha ao gerar avisos de prazo da atlética {}.", atletica.getId(), excecao);
      }
    }
  }
}
