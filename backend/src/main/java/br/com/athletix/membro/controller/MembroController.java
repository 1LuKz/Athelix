package br.com.athletix.membro.controller;

import br.com.athletix.membro.dto.MembroActivationRequest;
import br.com.athletix.membro.dto.MembroCadastroRequest;
import br.com.athletix.membro.dto.MembroInactivationRequest;
import br.com.athletix.membro.dto.MembroResumoResponse;
import br.com.athletix.membro.dto.MembroUpdateRequest;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.membro.service.MembroConsultaService;
import br.com.athletix.membro.service.MembroService;
import br.com.athletix.security.AutorizacaoAtualService;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.shared.pagination.PageQuery;
import br.com.athletix.shared.pagination.PageResponse;
import br.com.athletix.shared.pagination.PageableFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/membros")
public class MembroController {
    private static final Map<String, String> ORDENACOES = Map.of(
            "nome", "usuario.nome", "email", "usuario.email", "curso", "curso", "status", "status",
            "cargo", "cargo.nome", "departamento", "departamento.nome", "membroDesde", "membroDesde"
    );
    private static final String[] CABECALHO_CSV = {
            "Nome", "E-mail", "Telefone", "Curso", "Período", "Cargo", "Departamento", "Status", "Membro desde"
    };

    private final MembroService membroService;
    private final MembroConsultaService consultaService;
    private final AutorizacaoAtualService autorizacao;
    private final PageableFactory pageableFactory;

    public MembroController(MembroService membroService, MembroConsultaService consultaService,
                            AutorizacaoAtualService autorizacao, PageableFactory pageableFactory) {
        this.membroService = membroService;
        this.consultaService = consultaService;
        this.autorizacao = autorizacao;
        this.pageableFactory = pageableFactory;
    }

    @GetMapping
    public PageResponse<MembroResumoResponse> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Long departamentoId,
            @RequestParam(required = false) StatusMembro status,
            @Valid PageQuery pageQuery,
            Authentication authentication,
            HttpServletRequest request
    ) {
        boolean incluirContato = podeConsultarContato(authentication, request);
        return PageResponse.from(
                consultaService.listar(busca, departamentoId, status, pageableFactory.create(pageQuery, ORDENACOES)),
                membro -> MembroResumoResponse.from(membro, incluirContato)
        );
    }

    @GetMapping("/{id}")
    public MembroResumoResponse buscar(@PathVariable Long id, Authentication authentication, HttpServletRequest request) {
        return MembroResumoResponse.from(membroService.buscarDaAtleticaAtual(id), podeConsultarContato(authentication, request));
    }

    @PostMapping
    public ResponseEntity<MembroResumoResponse> criar(@Valid @RequestBody MembroCadastroRequest request,
                                                       Authentication authentication, HttpServletRequest servletRequest) {
        autorizacao.exigirAdministrador(authentication, servletRequest);
        Membro membro = membroService.cadastrar(request);
        return ResponseEntity.status(201).body(MembroResumoResponse.from(membro, true));
    }

    @PutMapping("/{id}")
    public MembroResumoResponse atualizar(@PathVariable Long id, @Valid @RequestBody MembroUpdateRequest request,
                                          Authentication authentication, HttpServletRequest servletRequest) {
        autorizacao.exigirAdministrador(authentication, servletRequest);
        return MembroResumoResponse.from(membroService.atualizar(id, request), true);
    }

    @PatchMapping("/{id}/aprovar")
    public MembroResumoResponse aprovar(@PathVariable Long id, @Valid @RequestBody MembroActivationRequest request,
                                        Authentication authentication, HttpServletRequest servletRequest) {
        autorizacao.exigirAdministrador(authentication, servletRequest);
        return MembroResumoResponse.from(membroService.ativar(id, request), true);
    }

    @PatchMapping("/{id}/inativar")
    public MembroResumoResponse inativar(@PathVariable Long id, @Valid @RequestBody MembroInactivationRequest request,
                                         Authentication authentication, HttpServletRequest servletRequest) {
        autorizacao.exigirAdministrador(authentication, servletRequest);
        return MembroResumoResponse.from(membroService.inativar(id, request), true);
    }

    @GetMapping("/exportacao")
    public ResponseEntity<byte[]> exportar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Long departamentoId,
            @RequestParam(required = false) StatusMembro status,
            @RequestParam(required = false) List<String> sort,
            Authentication authentication,
            HttpServletRequest request
    ) {
        autorizacao.exigirAdministrador(authentication, request);
        List<Membro> membros = consultaService.exportar(busca, departamentoId, status,
                pageableFactory.create(new PageQuery(null, null, sort), ORDENACOES).getSort());
        byte[] csv = gerarCsv(membros).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("membros.csv").build().toString())
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(csv);
    }

    private boolean podeConsultarContato(Authentication authentication, HttpServletRequest request) {
        return autorizacao.perfil(authentication, request) == PerfilAcesso.ADMINISTRADOR;
    }

    private String gerarCsv(List<Membro> membros) {
        StringBuilder csv = new StringBuilder(String.join(",", CABECALHO_CSV)).append("\r\n");
        for (Membro membro : membros) {
            csv.append(String.join(",",
                    celula(membro.getNomeUsuario()), celula(membro.getEmailUsuario()), celula(membro.getTelefoneUsuario()),
                    celula(membro.getCurso()), celula(membro.getPeriodoCurso()), celula(membro.getNomeCargo()),
                    celula(membro.getNomeDepartamento()), celula(membro.getStatus()), celula(membro.getMembroDesde())
            )).append("\r\n");
        }
        return csv.toString();
    }

    private String celula(Object valor) {
        String texto = valor == null ? "" : valor.toString();
        if (texto.matches("^[\\s]*[=+\\-@].*")) {
            texto = "'" + texto;
        }
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }
}
