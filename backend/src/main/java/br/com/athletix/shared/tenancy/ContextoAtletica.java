package br.com.athletix.shared.tenancy;

import br.com.athletix.shared.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Contexto da atlética da requisição ou execução agendada atual. B06 preencherá este valor a partir
 * da sessão autenticada; até lá ele só pode ser definido por fluxos internos controlados.
 */
@Component
public class ContextoAtletica {

    private final ThreadLocal<Long> atleticaIdAtual = new ThreadLocal<>();

    public Optional<Long> getAtleticaId() {
        return Optional.ofNullable(atleticaIdAtual.get());
    }

    public Long requireAtleticaId() {
        return getAtleticaId().orElseThrow(
                () -> new BusinessRuleException("O contexto da atlética é obrigatório para esta operação.")
        );
    }

    public Escopo abrir(Long atleticaId) {
        if (atleticaId == null || atleticaId <= 0) {
            throw new IllegalArgumentException("O identificador da atlética deve ser informado.");
        }
        Long anterior = atleticaIdAtual.get();
        atleticaIdAtual.set(atleticaId);
        return () -> restaurar(anterior);
    }

    public void limpar() {
        atleticaIdAtual.remove();
    }

    private void restaurar(Long anterior) {
        if (anterior == null) {
            limpar();
        } else {
            atleticaIdAtual.set(anterior);
        }
    }

    @FunctionalInterface
    public interface Escopo extends AutoCloseable {
        @Override
        void close();
    }
}
