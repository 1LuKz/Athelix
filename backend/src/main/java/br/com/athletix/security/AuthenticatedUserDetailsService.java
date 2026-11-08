package br.com.athletix.security;

import br.com.athletix.usuario.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;
    public AuthenticatedUserDetailsService(UsuarioRepository usuarioRepository) { this.usuarioRepository = usuarioRepository; }
    @Override public UserDetails loadUserByUsername(String email) {
        return usuarioRepository.findByEmail(email)
                .filter(usuario -> usuario.possuiCredencialDefinida() && usuario.isCredencialAtiva())
                .map(usuario -> new AuthenticatedUserPrincipal(usuario.getId(), usuario.getEmail(),
                        usuario.senhaHashParaAutenticacao(), true))
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas."));
    }
}
