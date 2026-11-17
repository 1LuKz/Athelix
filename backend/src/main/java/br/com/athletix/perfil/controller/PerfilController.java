package br.com.athletix.perfil.controller;

import br.com.athletix.auditoria.dto.AtividadeUsuarioResponse;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.membro.dto.SolicitacaoAlteracaoVinculoCreateRequest;
import br.com.athletix.membro.dto.SolicitacaoAlteracaoVinculoResponse;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.service.SolicitacaoAlteracaoVinculoService;
import br.com.athletix.perfil.dto.PerfilResponse;
import br.com.athletix.perfil.dto.PerfilSenhaUpdateRequest;
import br.com.athletix.perfil.dto.PerfilUpdateRequest;
import br.com.athletix.perfil.service.PerfilService;
import br.com.athletix.security.AutorizacaoAtualService;
import br.com.athletix.shared.pagination.PageQuery;
import br.com.athletix.shared.pagination.PageResponse;
import br.com.athletix.shared.pagination.PageableFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/perfil")
public class PerfilController {
  private static final Map<String, String> ORDENACOES_ATIVIDADE =
      Map.of("ocorridaEm", "ocorridaEm", "acao", "acao", "tipoRecurso", "tipoRecurso");

  private final PerfilService perfilService;
  private final AtividadeUsuarioService atividadeService;
  private final AutorizacaoAtualService autorizacao;
  private final PageableFactory pageableFactory;
  private final SolicitacaoAlteracaoVinculoService solicitacaoService;

  public PerfilController(
      PerfilService perfilService,
      AtividadeUsuarioService atividadeService,
      AutorizacaoAtualService autorizacao,
      PageableFactory pageableFactory,
      SolicitacaoAlteracaoVinculoService solicitacaoService) {
    this.perfilService = perfilService;
    this.atividadeService = atividadeService;
    this.autorizacao = autorizacao;
    this.pageableFactory = pageableFactory;
    this.solicitacaoService = solicitacaoService;
  }

  @GetMapping
  public PerfilResponse consultar(Authentication authentication, HttpServletRequest request) {
    Membro membro = autorizacao.membroAtual(authentication, request);
    PerfilService.PerfilAtual perfil = perfilService.carregar(membro.getId());
    return PerfilResponse.from(perfil.usuario(), perfil.membro());
  }

  @PutMapping
  public PerfilResponse atualizar(
      @Valid @RequestBody PerfilUpdateRequest body,
      Authentication authentication,
      HttpServletRequest request) {
    Membro membro = autorizacao.membroAtual(authentication, request);
    PerfilService.PerfilAtual perfil = perfilService.atualizar(membro.getId(), body);
    return PerfilResponse.from(perfil.usuario(), perfil.membro());
  }

  @PutMapping("/senha")
  public ResponseEntity<Void> alterarSenha(
      @Valid @RequestBody PerfilSenhaUpdateRequest body,
      Authentication authentication,
      HttpServletRequest request) {
    Membro membro = autorizacao.membroAtual(authentication, request);
    perfilService.alterarSenha(
        membro.getId(),
        body,
        request.getSession(false) == null ? null : request.getSession(false).getId());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/solicitacoes-vinculo")
  public ResponseEntity<SolicitacaoAlteracaoVinculoResponse> solicitarAlteracaoVinculo(
      @Valid @RequestBody SolicitacaoAlteracaoVinculoCreateRequest body,
      Authentication authentication,
      HttpServletRequest request) {
    Membro membro = autorizacao.membroAtual(authentication, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(SolicitacaoAlteracaoVinculoResponse.from(solicitacaoService.criar(membro, body)));
  }

  @GetMapping("/atividades")
  public PageResponse<AtividadeUsuarioResponse> atividades(
      @Valid PageQuery pageQuery, Authentication authentication, HttpServletRequest request) {
    Membro membro = autorizacao.membroAtual(authentication, request);
    PageQuery query =
        pageQuery.sort() == null || pageQuery.sort().isEmpty()
            ? new PageQuery(pageQuery.page(), pageQuery.size(), List.of("ocorridaEm,desc"))
            : pageQuery;
    return PageResponse.from(
        atividadeService.listarDoUsuario(
            membro.getUsuarioId(),
            membro.getAtleticaId(),
            pageableFactory.create(query, ORDENACOES_ATIVIDADE)),
        AtividadeUsuarioResponse::from);
  }
}
