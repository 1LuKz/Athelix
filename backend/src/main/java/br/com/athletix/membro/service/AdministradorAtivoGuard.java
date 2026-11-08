package br.com.athletix.membro.service;

import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.shared.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

/** Evita que uma atlética fique sem administrador ativo por inativação de membro ou cargo. */
@Component
public class AdministradorAtivoGuard {
    private final MembroRepository membroRepository;

    public AdministradorAtivoGuard(MembroRepository membroRepository) {
        this.membroRepository = membroRepository;
    }

    public void validarInativacaoMembro(Membro membro) {
        if (membro.getPerfilAcesso() == PerfilAcesso.ADMINISTRADOR
                && administradoresAtivos(membro.getAtleticaId()) <= 1) {
            throw new BusinessRuleException("Não é possível inativar o último administrador ativo da atlética.");
        }
    }

    public void validarAlteracaoCargo(Cargo cargo, PerfilAcesso proximoPerfil, boolean proximoAtivo) {
        boolean removeAdministracao = cargo.getPerfilAcesso() == PerfilAcesso.ADMINISTRADOR
                && (!proximoAtivo || proximoPerfil != PerfilAcesso.ADMINISTRADOR);
        if (removeAdministracao && membroRepository.existsByCargo_IdAndStatus(cargo.getId(), StatusMembro.ATIVO)
                && administradoresAtivos(cargo.getAtleticaId()) <= 1) {
            throw new BusinessRuleException("Não é possível remover o último administrador ativo da atlética.");
        }
    }

    private long administradoresAtivos(Long atleticaId) {
        return membroRepository.countByAtletica_IdAndStatusAndCargo_PerfilAcessoAndCargo_AtivoTrue(
                atleticaId, StatusMembro.ATIVO, PerfilAcesso.ADMINISTRADOR
        );
    }
}
