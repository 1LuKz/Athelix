package br.com.athletix.shared.tenancy;

import br.com.athletix.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AtleticaResourceResolverTest {

    private final ContextoAtletica contexto = new ContextoAtletica();
    private final AtleticaResourceResolver resolver = new AtleticaResourceResolver(contexto);

    @Test
    void resolvesUsingTheResourceAndCurrentAtleticaIds() {
        AtomicLong scopedAtleticaId = new AtomicLong();

        try (ContextoAtletica.Escopo ignored = contexto.abrir(7L)) {
            String resource = resolver.requireByIdAndAtleticaId(15L, (resourceId, atleticaId) -> {
                scopedAtleticaId.set(atleticaId);
                return resourceId.equals(15L) && atleticaId.equals(7L)
                        ? Optional.of("recurso")
                        : Optional.empty();
            });

            assertEquals("recurso", resource);
            assertEquals(7L, scopedAtleticaId.get());
        }
    }

    @Test
    void hidesResourcesOutsideTheCurrentAtletica() {
        try (ContextoAtletica.Escopo ignored = contexto.abrir(7L)) {
            assertThrows(ResourceNotFoundException.class,
                    () -> resolver.requireByIdAndAtleticaId(15L, (resourceId, atleticaId) -> Optional.empty()));
        }
    }
}
