package br.com.athletix.departamento.controller;

import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.departamento.dto.DepartamentoCreateRequest;
import br.com.athletix.departamento.dto.DepartamentoResponse;
import br.com.athletix.departamento.dto.DepartamentoTransitionRequest;
import br.com.athletix.departamento.dto.DepartamentoUpdateRequest;
import br.com.athletix.departamento.dto.ResponsavelDepartamentoUpdateRequest;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.departamento.entity.SituacaoDepartamento;
import br.com.athletix.departamento.service.DepartamentoService;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.security.AutorizacaoAtualService;
import br.com.athletix.shared.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/departamentos")
public class DepartamentoController {
    private final DepartamentoService departamentoService;
    private final MembroRepository membroRepository;
    private final AutorizacaoAtualService autorizacao;

    public DepartamentoController(DepartamentoService departamentoService, MembroRepository membroRepository,
                                  AutorizacaoAtualService autorizacao) {
        this.departamentoService = departamentoService;
        this.membroRepository = membroRepository;
        this.autorizacao = autorizacao;
    }

    @GetMapping
    public List<DepartamentoResponse> listar(Authentication authentication, HttpServletRequest request) {
        PerfilAcesso perfil = autorizacao.perfil(authentication, request);
        List<Departamento> departamentos = perfil == PerfilAcesso.ADMINISTRADOR
                ? departamentoService.listarParaAdministracaoDaAtleticaAtual(perfil)
                : departamentoService.listarEstruturaAtivaDaAtleticaAtual();
        return departamentos.stream().map(this::resposta).toList();
    }

    @GetMapping("/{id}")
    public DepartamentoResponse buscar(@PathVariable Long id, Authentication authentication, HttpServletRequest request) {
        Departamento departamento = departamentoService.buscarDaAtleticaAtual(id);
        if (autorizacao.perfil(authentication, request) != PerfilAcesso.ADMINISTRADOR
                && departamento.getSituacao() != SituacaoDepartamento.ATIVO) {
            throw new ResourceNotFoundException();
        }
        return resposta(departamento);
    }

    @PostMapping
    public ResponseEntity<DepartamentoResponse> criar(@Valid @RequestBody DepartamentoCreateRequest request,
                                                       Authentication authentication, HttpServletRequest servletRequest) {
        autorizacao.exigirAdministrador(authentication, servletRequest);
        return ResponseEntity.status(201).body(resposta(departamentoService.criarEmConfiguracao(request)));
    }

    @PutMapping("/{id}")
    public DepartamentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody DepartamentoUpdateRequest request,
                                          Authentication authentication, HttpServletRequest servletRequest) {
        autorizacao.exigirAdministrador(authentication, servletRequest);
        return resposta(departamentoService.atualizarDadosBasicos(id, request));
    }

    @PatchMapping("/{id}/responsavel")
    public DepartamentoResponse designarResponsavel(@PathVariable Long id,
                                                     @Valid @RequestBody ResponsavelDepartamentoUpdateRequest request,
                                                     Authentication authentication, HttpServletRequest servletRequest) {
        autorizacao.exigirAdministrador(authentication, servletRequest);
        return resposta(departamentoService.designarResponsavel(id, request));
    }

    @PatchMapping("/{id}/ativar")
    public DepartamentoResponse ativar(@PathVariable Long id, @Valid @RequestBody DepartamentoTransitionRequest request,
                                       Authentication authentication, HttpServletRequest servletRequest) {
        autorizacao.exigirAdministrador(authentication, servletRequest);
        return resposta(departamentoService.ativar(id, request));
    }

    @PatchMapping("/{id}/inativar")
    public DepartamentoResponse inativar(@PathVariable Long id, @Valid @RequestBody DepartamentoTransitionRequest request,
                                         Authentication authentication, HttpServletRequest servletRequest) {
        autorizacao.exigirAdministrador(authentication, servletRequest);
        return resposta(departamentoService.inativar(id, request));
    }

    private DepartamentoResponse resposta(Departamento departamento) {
        String responsavelNome = departamento.getResponsavelMembroId() == null ? null
                : membroRepository.findNomeUsuarioByIdAndAtleticaId(
                        departamento.getResponsavelMembroId(), departamento.getAtleticaId()
                ).orElse(null);
        long quantidadeMembros = membroRepository.countByDepartamento_IdAndStatus(departamento.getId(), StatusMembro.ATIVO);
        return DepartamentoResponse.from(departamento, responsavelNome, quantidadeMembros);
    }
}
