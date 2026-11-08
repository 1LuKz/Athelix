package br.com.athletix.usuario.service;

import br.com.athletix.shared.exception.ConflictException;
import br.com.athletix.shared.exception.ResourceNotFoundException;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.usuario.dto.SenhaDefinicaoRequest;
import br.com.athletix.usuario.dto.UsuarioConviteCreateRequest;
import br.com.athletix.usuario.entity.Usuario;
import br.com.athletix.usuario.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Regras da identidade global; a associação com atléticas pertence ao módulo Membro. */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario criarPorConvite(UsuarioConviteCreateRequest request) {
        String email = EmailNormalizado.normalize(request.email());
        NomeNormalizado.fromRequired(request.nome(), "nome");
        String nome = NomeNormalizado.displayValue(request.nome());
        if (nome.length() < 3 || nome.length() > 120) {
            throw new br.com.athletix.shared.exception.BusinessRuleException(
                    "O nome deve ter entre 3 e 120 caracteres."
            );
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflictException("Já existe uma conta com este e-mail.");
        }
        return usuarioRepository.save(new Usuario(
                nome,
                email,
                TelefoneNormalizado.normalize(request.telefone())
        ));
    }

    @Transactional
    public void definirSenha(Long usuarioId, SenhaDefinicaoRequest request) {
        PasswordPolicy.validate(request.senha());
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow(ResourceNotFoundException::new);
        usuario.definirSenhaHash(passwordEncoder.encode(request.senha()));
    }

    @Transactional
    public void registrarAcesso(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow(ResourceNotFoundException::new);
        usuario.registrarAcesso();
    }
}
