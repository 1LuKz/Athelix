package br.com.athletix.membro.service;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PendenciasResponsabilidadeMembroConfiguration {

  @Bean
  @ConditionalOnMissingBean(PendenciasResponsabilidadeMembroProvider.class)
  PendenciasResponsabilidadeMembroProvider pendenciasResponsabilidadeMembroProvider() {
    return (atleticaId, membroId) -> List.of();
  }
}
