package br.com.athletix.gestao.transicao.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

public record TransicaoUpdateRequest(
    @NotNull @PositiveOrZero Long version, @NotEmpty List<@Valid ItemTransicaoRequest> itens) {}
