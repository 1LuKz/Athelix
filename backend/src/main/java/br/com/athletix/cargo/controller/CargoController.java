package br.com.athletix.cargo.controller;

import br.com.athletix.cargo.dto.CargoCreateRequest;
import br.com.athletix.cargo.dto.CargoResponse;
import br.com.athletix.cargo.dto.CargoUpdateRequest;
import br.com.athletix.cargo.service.CargoService;
import br.com.athletix.security.AutorizacaoAtualService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cargos")
public class CargoController {
    private final CargoService cargoService;
    private final AutorizacaoAtualService autorizacao;

    public CargoController(CargoService cargoService, AutorizacaoAtualService autorizacao) {
        this.cargoService = cargoService;
        this.autorizacao = autorizacao;
    }

    @GetMapping
    public List<CargoResponse> listar(Authentication authentication, HttpServletRequest request) {
        autorizacao.exigirAdministrador(authentication, request);
        return cargoService.listarDaAtleticaAtual().stream().map(CargoResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<CargoResponse> criar(@Valid @RequestBody CargoCreateRequest request,
                                                Authentication authentication, HttpServletRequest servletRequest) {
        autorizacao.exigirAdministrador(authentication, servletRequest);
        return ResponseEntity.status(201).body(CargoResponse.from(
                cargoService.criar(request, autorizacao.perfil(authentication, servletRequest))
        ));
    }

    @PutMapping("/{id}")
    public CargoResponse atualizar(@PathVariable Long id, @Valid @RequestBody CargoUpdateRequest request,
                                   Authentication authentication, HttpServletRequest servletRequest) {
        autorizacao.exigirAdministrador(authentication, servletRequest);
        return CargoResponse.from(cargoService.atualizar(id, request, autorizacao.perfil(authentication, servletRequest)));
    }
}
