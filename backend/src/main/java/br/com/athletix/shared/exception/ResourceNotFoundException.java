package br.com.athletix.shared.exception;

/** Também usada quando um recurso pertence a outra atlética, sem revelar sua existência. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
        super("Recurso não encontrado.");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
