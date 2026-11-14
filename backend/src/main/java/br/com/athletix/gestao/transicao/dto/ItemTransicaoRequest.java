package br.com.athletix.gestao.transicao.dto; import jakarta.validation.constraints.*;
public record ItemTransicaoRequest(@NotNull @Positive Long membroId,@NotNull @Positive Long cargoDestinoId,@NotNull @Positive Long departamentoDestinoId,boolean continuaAtivo,@Positive Long substituiResponsavelMembroId){}
