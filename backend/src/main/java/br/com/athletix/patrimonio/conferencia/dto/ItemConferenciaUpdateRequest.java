package br.com.athletix.patrimonio.conferencia.dto; import jakarta.validation.constraints.*;
public record ItemConferenciaUpdateRequest(@PositiveOrZero int quantidadeVerificada,@Size(max=1000) String observacao,@NotNull @PositiveOrZero Long version){}
