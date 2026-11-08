package br.com.athletix.departamento.service;

import br.com.athletix.shared.exception.BusinessRuleException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Impede transições que dependem de Membro até B05 fornecer validadores com integridade referencial.
 */
@Configuration
public class ValidadoresDepartamentoPendentesConfiguration {

    @Bean
    @ConditionalOnMissingBean(ResponsavelDepartamentoValidator.class)
    ResponsavelDepartamentoValidator responsavelDepartamentoValidatorPendente() {
        return (membroId, atleticaId, departamentoId) -> {
            throw new BusinessRuleException(
                    "A ativação exige validar o responsável no módulo de membros, disponível na B05."
            );
        };
    }

    @Bean
    @ConditionalOnMissingBean(InativacaoDepartamentoValidator.class)
    InativacaoDepartamentoValidator inativacaoDepartamentoValidatorPendente() {
        return (atleticaId, departamentoId) -> {
            throw new BusinessRuleException(
                    "A inativação exige transferir membros e substituir responsabilidades antes da B05."
            );
        };
    }
}
