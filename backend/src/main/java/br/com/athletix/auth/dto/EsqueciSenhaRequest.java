package br.com.athletix.auth.dto;

/** O formato aberto preserva a resposta genérica mesmo para e-mails ausentes ou inválidos. */
public record EsqueciSenhaRequest(String email) {
}
