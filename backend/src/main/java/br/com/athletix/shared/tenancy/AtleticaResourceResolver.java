package br.com.athletix.shared.tenancy;

import br.com.athletix.shared.exception.ResourceNotFoundException;
import java.util.Optional;
import java.util.function.BiFunction;
import org.springframework.stereotype.Component;

/**
 * Resolve recursos sempre pelo par (id do recurso, id da atlética atual). IDs recebidos do cliente
 * não concedem acesso: um recurso fora do escopo é indistinguível de um recurso inexistente.
 */
@Component
public class AtleticaResourceResolver {

  private final ContextoAtletica contextoAtletica;

  public AtleticaResourceResolver(ContextoAtletica contextoAtletica) {
    this.contextoAtletica = contextoAtletica;
  }

  public Long atleticaIdAtual() {
    return contextoAtletica.requireAtleticaId();
  }

  public <T> T requireByIdAndAtleticaId(
      Long resourceId, BiFunction<Long, Long, Optional<T>> scopedFinder) {
    return scopedFinder
        .apply(resourceId, atleticaIdAtual())
        .orElseThrow(ResourceNotFoundException::new);
  }

  public void requireBelongsToCurrentAtletica(AtleticaScoped resource) {
    if (!atleticaIdAtual().equals(resource.getAtleticaId())) {
      throw new ResourceNotFoundException();
    }
  }
}
