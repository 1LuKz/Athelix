package br.com.athletix.auth.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/** Limitador local de demonstração por conta normalizada e origem. Não persiste senhas nem tokens. */
@Service
public class LoginAttemptService {
    static final int MAX_TENTATIVAS = 5;
    static final Duration JANELA = Duration.ofMinutes(15);
    static final Duration BLOQUEIO = Duration.ofMinutes(15);
    private final ConcurrentHashMap<String, Tentativas> tentativas = new ConcurrentHashMap<>();

    public boolean permitido(String email, String origem) {
        Tentativas atual = tentativas.get(chave(email, origem));
        return atual == null || !atual.bloqueadoAte().isAfter(Instant.now());
    }

    public void registrarFalha(String email, String origem) {
        tentativas.compute(chave(email, origem), (chave, atual) -> {
            Instant agora = Instant.now();
            if (atual != null && atual.bloqueadoAte().isAfter(agora)) {
                return atual;
            }
            if (atual == null || !atual.inicioJanela().plus(JANELA).isAfter(agora)) {
                return new Tentativas(1, agora, Instant.EPOCH);
            }
            int quantidade = atual.quantidade() + 1;
            return quantidade >= MAX_TENTATIVAS
                    ? new Tentativas(quantidade, atual.inicioJanela(), agora.plus(BLOQUEIO))
                    : new Tentativas(quantidade, atual.inicioJanela(), Instant.EPOCH);
        });
    }

    public void limpar(String email, String origem) {
        tentativas.remove(chave(email, origem));
    }

    private String chave(String email, String origem) {
        return email + "|" + origem;
    }

    private record Tentativas(int quantidade, Instant inicioJanela, Instant bloqueadoAte) {
    }
}
