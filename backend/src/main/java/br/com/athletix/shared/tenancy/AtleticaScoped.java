package br.com.athletix.shared.tenancy;

/** Implementado por recursos que pertencem a uma única atlética. */
public interface AtleticaScoped {

  Long getAtleticaId();
}
