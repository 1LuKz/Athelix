package br.com.athletix.shared.time;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/** "Hoje" sempre no fuso da atlética, nunca no do servidor. */
public final class FusoHorarioAtletica {
    public static final ZoneId PADRAO = ZoneId.of("America/Sao_Paulo");

    private FusoHorarioAtletica() {
    }

    public static ZoneId zona(String fusoHorario) {
        try {
            return fusoHorario == null || fusoHorario.isBlank() ? PADRAO : ZoneId.of(fusoHorario.trim());
        } catch (DateTimeException e) {
            return PADRAO;
        }
    }

    public static LocalDate hoje(String fusoHorario) {
        return LocalDate.now(zona(fusoHorario));
    }

    public static LocalDate dataDe(Instant instante, String fusoHorario) {
        return instante.atZone(zona(fusoHorario)).toLocalDate();
    }
}