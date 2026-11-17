package br.com.athletix.shared.exception;

public class InvalidSortException extends RuntimeException {

  public InvalidSortException(String sort) {
    super("Ordenação não permitida: " + sort + ".");
  }
}
