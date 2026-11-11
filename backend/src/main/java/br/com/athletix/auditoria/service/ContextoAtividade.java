package br.com.athletix.auditoria.service;

import org.springframework.stereotype.Component;

/** Mantém a autoria confiável durante uma requisição autenticada. */
@Component
public class ContextoAtividade {
    private final ThreadLocal<Autor> atual = new ThreadLocal<>();

    public Escopo abrir(Long atleticaId, Long usuarioId, Long membroId) {
        Autor anterior = atual.get();
        atual.set(new Autor(atleticaId, usuarioId, membroId));
        return () -> {
            if (anterior == null) atual.remove(); else atual.set(anterior);
        };
    }

    public Autor atual() {
        return atual.get();
    }

    public record Autor(Long atleticaId, Long usuarioId, Long membroId) { }

    @FunctionalInterface
    public interface Escopo extends AutoCloseable {
        @Override
        void close();
    }
}
