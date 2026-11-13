package br.com.athletix.patrimonio.controller;

import br.com.athletix.patrimonio.dto.CategoriaPatrimonioCreateRequest;
import br.com.athletix.patrimonio.dto.CategoriaPatrimonioResponse;
import br.com.athletix.patrimonio.service.CategoriaPatrimonioService;
import br.com.athletix.security.AutorizacaoAtualService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/patrimonio/categorias")
public class CategoriaPatrimonioController {
    private final CategoriaPatrimonioService service; private final AutorizacaoAtualService autorizacao;
    public CategoriaPatrimonioController(CategoriaPatrimonioService service, AutorizacaoAtualService autorizacao) {
        this.service = service; this.autorizacao = autorizacao;
    }
    @GetMapping public List<CategoriaPatrimonioResponse> listar(Authentication authentication, HttpServletRequest request) {
        return service.listar(autorizacao.membroAtual(authentication, request)).stream().map(CategoriaPatrimonioResponse::from).toList();
    }
    @PostMapping public ResponseEntity<CategoriaPatrimonioResponse> criar(@Valid @RequestBody CategoriaPatrimonioCreateRequest body,
            Authentication authentication, HttpServletRequest request) {
        return ResponseEntity.status(201).body(CategoriaPatrimonioResponse.from(
                service.criar(body, autorizacao.membroAtual(authentication, request))));
    }
}
