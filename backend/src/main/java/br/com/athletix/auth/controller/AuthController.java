package br.com.athletix.auth.controller;

import br.com.athletix.auth.dto.AuthStateResponse;
import br.com.athletix.auth.dto.CsrfResponse;
import br.com.athletix.auth.dto.EsqueciSenhaRequest;
import br.com.athletix.auth.dto.LoginRequest;
import br.com.athletix.auth.dto.MensagemResponse;
import br.com.athletix.auth.dto.RedefinirSenhaRequest;
import br.com.athletix.auth.dto.SelecionarAtleticaRequest;
import br.com.athletix.auth.service.LoginAttemptService;
import br.com.athletix.auth.service.SessaoAtleticaService;
import br.com.athletix.auth.service.TokenCredencialService;
import br.com.athletix.security.AuthenticatedUserPrincipal;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.usuario.service.EmailNormalizado;
import br.com.athletix.usuario.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final AuthenticationManager authenticationManager;
  private final SecurityContextRepository securityContextRepository;
  private final SessaoAtleticaService sessaoAtleticaService;
  private final LoginAttemptService loginAttemptService;
  private final UsuarioService usuarioService;
  private final TokenCredencialService tokenCredencialService;
  private final SessionRegistry sessionRegistry;

  public AuthController(
      AuthenticationManager authenticationManager,
      SecurityContextRepository securityContextRepository,
      SessaoAtleticaService sessaoAtleticaService,
      LoginAttemptService loginAttemptService,
      UsuarioService usuarioService,
      TokenCredencialService tokenCredencialService,
      SessionRegistry sessionRegistry) {
    this.authenticationManager = authenticationManager;
    this.securityContextRepository = securityContextRepository;
    this.sessaoAtleticaService = sessaoAtleticaService;
    this.loginAttemptService = loginAttemptService;
    this.usuarioService = usuarioService;
    this.tokenCredencialService = tokenCredencialService;
    this.sessionRegistry = sessionRegistry;
  }

  @GetMapping("/csrf")
  public CsrfResponse csrf(CsrfToken csrfToken) {
    return new CsrfResponse(
        csrfToken.getToken(), csrfToken.getHeaderName(), csrfToken.getParameterName());
  }

  @PostMapping("/login")
  public ResponseEntity<AuthStateResponse> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {
    String email = EmailNormalizado.normalize(request.email());
    String origem = servletRequest.getRemoteAddr();
    if (!loginAttemptService.permitido(email, origem)) {
      throw new BadCredentialsException("Credenciais inválidas.");
    }
    try {
      Authentication authentication =
          authenticationManager.authenticate(
              UsernamePasswordAuthenticationToken.unauthenticated(email, request.senha()));
      // O repositório CSRF em cookie não precisa criar sessão. Garanta uma antes de trocar o id
      // para manter a proteção contra fixação tanto no primeiro login quanto em reautenticações.
      String sessionIdAnterior = servletRequest.getSession().getId();
      servletRequest.changeSessionId();
      SecurityContext context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(authentication);
      SecurityContextHolder.setContext(context);
      securityContextRepository.saveContext(context, servletRequest, servletResponse);
      AuthenticatedUserPrincipal principal =
          (AuthenticatedUserPrincipal) authentication.getPrincipal();
      sessionRegistry.removeSessionInformation(sessionIdAnterior);
      sessionRegistry.registerNewSession(servletRequest.getSession().getId(), principal);
      usuarioService.registrarAcesso(principal.usuarioId());
      AuthStateResponse state = sessaoAtleticaService.estado(principal.usuarioId(), null);
      if (state.membroSelecionadoId() != null) {
        servletRequest
            .getSession()
            .setAttribute(
                SessaoAtleticaService.MEMBRO_SELECIONADO_SESSION_ATTRIBUTE,
                state.membroSelecionadoId());
      }
      loginAttemptService.limpar(email, origem);
      return ResponseEntity.ok(state);
    } catch (org.springframework.security.core.AuthenticationException exception) {
      loginAttemptService.registrarFalha(email, origem);
      throw new BadCredentialsException("Credenciais inválidas.");
    }
  }

  @PostMapping("/esqueci-senha")
  public MensagemResponse esqueciSenha(@RequestBody(required = false) EsqueciSenhaRequest request) {
    tokenCredencialService.solicitarRecuperacao(request == null ? null : request.email());
    return mensagemRecuperacao();
  }

  @PostMapping("/redefinir-senha")
  public MensagemResponse redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
    tokenCredencialService.redefinirSenha(request.token(), request.novaSenha());
    return new MensagemResponse("Senha redefinida com sucesso.");
  }

  @PostMapping("/definir-senha")
  public MensagemResponse definirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
    tokenCredencialService.definirSenha(request.token(), request.novaSenha());
    return new MensagemResponse("Senha definida com sucesso.");
  }

  @GetMapping("/me")
  public AuthStateResponse me(Authentication authentication, HttpServletRequest request) {
    AuthenticatedUserPrincipal principal = principal(authentication);
    HttpSession session = request.getSession(false);
    Long membroId =
        session == null
            ? null
            : (Long)
                session.getAttribute(SessaoAtleticaService.MEMBRO_SELECIONADO_SESSION_ATTRIBUTE);
    AuthStateResponse state = sessaoAtleticaService.estado(principal.usuarioId(), membroId);
    if (membroId == null && state.membroSelecionadoId() != null) {
      request
          .getSession()
          .setAttribute(
              SessaoAtleticaService.MEMBRO_SELECIONADO_SESSION_ATTRIBUTE,
              state.membroSelecionadoId());
    }
    return state;
  }

  @PostMapping("/selecionar-atletica")
  public AuthStateResponse selecionarAtletica(
      @Valid @RequestBody SelecionarAtleticaRequest request,
      Authentication authentication,
      HttpServletRequest servletRequest) {
    AuthenticatedUserPrincipal principal = principal(authentication);
    Long membroId = sessaoAtleticaService.selecionar(principal.usuarioId(), request.membroId());
    servletRequest
        .getSession()
        .setAttribute(SessaoAtleticaService.MEMBRO_SELECIONADO_SESSION_ATTRIBUTE, membroId);
    return sessaoAtleticaService.estado(principal.usuarioId(), membroId);
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    new SecurityContextLogoutHandler().logout(request, response, authentication);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  private AuthenticatedUserPrincipal principal(Authentication authentication) {
    if (authentication == null
        || !(authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal)) {
      throw new BusinessRuleException("A autenticação da sessão é inválida.");
    }
    return principal;
  }

  private MensagemResponse mensagemRecuperacao() {
    return new MensagemResponse(
        "Se houver uma conta elegível, instruções de recuperação poderão ser enviadas.");
  }
}
