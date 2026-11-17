package br.com.athletix.shared.exception;

public class ConcurrentModificationException extends RuntimeException {

  public ConcurrentModificationException() {
    super("O recurso foi alterado por outra operação. Atualize os dados e tente novamente.");
  }
}
