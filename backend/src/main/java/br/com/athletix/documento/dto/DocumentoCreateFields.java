package br.com.athletix.documento.dto;

import br.com.athletix.documento.entity.*;

/** Campos multipart da criação, validados pelo serviço porque são parâmetros de formulário. */
public record DocumentoCreateFields(String titulo, CategoriaDocumento categoria, String descricao,
                                   VisibilidadeDocumento visibilidade, Long gestaoOrigemId, String observacao) { }
