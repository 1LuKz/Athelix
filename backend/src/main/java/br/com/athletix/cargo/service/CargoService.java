package br.com.athletix.cargo.service;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.cargo.dto.CargoCreateRequest;
import br.com.athletix.cargo.dto.CargoUpdateRequest;
import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.cargo.repository.CargoRepository;
import br.com.athletix.shared.exception.ConcurrentModificationException;
import br.com.athletix.shared.exception.ConflictException;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import br.com.athletix.membro.service.AdministradorAtivoGuard;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Operações de cargos exigem um perfil administrador; B08 ligará o perfil à sessão autenticada. */
@Service
public class CargoService {

    private final CargoRepository cargoRepository;
    private final AtleticaRepository atleticaRepository;
    private final AtleticaResourceResolver resourceResolver;
    private final AdministradorAtivoGuard administradorAtivoGuard;
    private final AtividadeUsuarioService atividadeService;

    public CargoService(
            CargoRepository cargoRepository,
            AtleticaRepository atleticaRepository,
            AtleticaResourceResolver resourceResolver,
            AdministradorAtivoGuard administradorAtivoGuard,
            AtividadeUsuarioService atividadeService
    ) {
        this.cargoRepository = cargoRepository;
        this.atleticaRepository = atleticaRepository;
        this.resourceResolver = resourceResolver;
        this.administradorAtivoGuard = administradorAtivoGuard;
        this.atividadeService = atividadeService;
    }

    @Transactional
    public Cargo criar(CargoCreateRequest request, PerfilAcesso perfilSolicitante) {
        requireAdministrator(perfilSolicitante);
        Long atleticaId = resourceResolver.atleticaIdAtual();
        String nomeNormalizado = NomeNormalizado.fromRequired(request.nome(), "nome");
        ensureNameAvailable(atleticaId, nomeNormalizado, null);
        Atletica atletica = resourceResolver.requireByIdAndAtleticaId(atleticaId, atleticaRepository::findByIdAndId);

        Cargo cargo = cargoRepository.save(new Cargo(
                atletica,
                NomeNormalizado.displayValue(request.nome()),
                nomeNormalizado,
                NomeNormalizado.optionalDisplayValue(request.descricao()),
                request.perfilAcesso(),
                request.ativo()
        ));
        atividadeService.registrar("CARGO_CRIADO", "CARGO", cargo.getId(), "Cargo criado.");
        return cargo;
    }

    @Transactional
    public Cargo atualizar(Long cargoId, CargoUpdateRequest request, PerfilAcesso perfilSolicitante) {
        requireAdministrator(perfilSolicitante);
        Cargo cargo = buscarDaAtleticaAtual(cargoId);
        if (!cargo.getVersion().equals(request.version())) {
            throw new ConcurrentModificationException();
        }

        String nomeNormalizado = NomeNormalizado.fromRequired(request.nome(), "nome");
        ensureNameAvailable(cargo.getAtleticaId(), nomeNormalizado, cargo.getId());
        administradorAtivoGuard.validarAlteracaoCargo(cargo, request.perfilAcesso(), request.ativo());
        cargo.atualizar(
                NomeNormalizado.displayValue(request.nome()),
                nomeNormalizado,
                NomeNormalizado.optionalDisplayValue(request.descricao()),
                request.perfilAcesso(),
                request.ativo()
        );
        atividadeService.registrar("CARGO_ATUALIZADO", "CARGO", cargo.getId(), "Cargo e permissões atualizados.");
        return cargo;
    }

    @Transactional(readOnly = true)
    public Cargo buscarDaAtleticaAtual(Long cargoId) {
        return resourceResolver.requireByIdAndAtleticaId(cargoId, cargoRepository::findByIdAndAtletica_Id);
    }

    @Transactional(readOnly = true)
    public List<Cargo> listarDaAtleticaAtual() {
        return cargoRepository.findAllByAtletica_IdOrderByNomeAsc(resourceResolver.atleticaIdAtual());
    }

    private void ensureNameAvailable(Long atleticaId, String nomeNormalizado, Long currentCargoId) {
        if (!cargoRepository.existsByAtletica_IdAndNomeNormalizado(atleticaId, nomeNormalizado)) {
            return;
        }
        cargoRepository.findByAtletica_IdAndNomeNormalizado(atleticaId, nomeNormalizado)
                .filter(cargo -> !cargo.getId().equals(currentCargoId))
                .ifPresent(cargo -> {
                    throw new ConflictException("Já existe um cargo com este nome na atlética.");
                });
    }

    private void requireAdministrator(PerfilAcesso perfilSolicitante) {
        if (perfilSolicitante != PerfilAcesso.ADMINISTRADOR) {
            throw new AccessDeniedException(
                    "Somente administradores podem atribuir perfis de acesso."
            );
        }
    }
}
