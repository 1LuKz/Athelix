package br.com.athletix.membro.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PendenciasResponsabilidadeMembroConfiguration {

    @Bean
    @ConditionalOnMissingBean(PendenciasResponsabilidadeMembroProvider.class)
    PendenciasResponsabilidadeMembroProvider pendenciasResponsabilidadeMembroProvider() {
        return (atleticaId, membroId) -> List.of();
    }
}
