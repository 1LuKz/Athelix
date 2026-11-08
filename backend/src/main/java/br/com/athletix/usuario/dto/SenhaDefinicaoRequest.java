package br.com.athletix.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Entrada transitória: a senha nunca é incluída em respostas nem persistida em texto puro. */
public record SenhaDefinicaoRequest(
        @NotBlank(message = "A senha é obrigatória.")
        @Size(max = 128, message = "A senha deve ter no máximo 128 caracteres.") String senha
) {
}
