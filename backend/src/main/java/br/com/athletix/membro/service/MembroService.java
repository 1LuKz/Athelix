package br.com.athletix.membro.service;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.cargo.repository.CargoRepository;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.departamento.entity.SituacaoDepartamento;
import br.com.athletix.departamento.repository.DepartamentoRepository;
import br.com.athletix.membro.dto.MembroActivationRequest;
import br.com.athletix.membro.dto.MembroCreateRequest;
import br.com.athletix.membro.dto.MembroInactivationRequest;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ConcurrentModificationException;
import br.com.athletix.shared.exception.ConflictException;
import br.com.athletix.shared.exception.ResourceNotFoundException;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import br.com.athletix.usuario.entity.Usuario;
import br.com.athletix.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** Regras do vínculo, incluindo elegibilidade de acesso e preservação de histórico na inativação. */
@Service
public class MembroService {

    private final MembroRepository membroRepository;
    private final AtleticaRepository atleticaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CargoRepository cargoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final AtleticaResourceResolver resourceResolver;
    private final MembroDepartamentoValidators departamentoValidators;
    private final PendenciasResponsabilidadeMembroProvider pendenciasProvider;

    public MembroService(
            MembroRepository membroRepository,
            AtleticaRepository atleticaRepository,
            UsuarioRepository usuarioRepository,
            CargoRepository cargoRepository,
            DepartamentoRepository departamentoRepository,
            AtleticaResourceResolver resourceResolver,
            MembroDepartamentoValidators departamentoValidators,
            PendenciasResponsabilidadeMembroProvider pendenciasProvider
    ) {
        this.membroRepository = membroRepository;
        this.atleticaRepository = atleticaRepository;
        this.usuarioRepository = usuarioRepository;
        this.cargoRepository = cargoRepository;
        this.departamentoRepository = departamentoRepository;
        this.resourceResolver = resourceResolver;
        this.departamentoValidators = departamentoValidators;
        this.pendenciasProvider = pendenciasProvider;
    }

    @Transactional
    public Membro criarPendente(MembroCreateRequest request) {
        Long atleticaId = resourceResolver.atleticaIdAtual();
        Atletica atletica = resourceResolver.requireByIdAndAtleticaId(atleticaId, atleticaRepository::findByIdAndId);
        if (!atletica.isAtiva()) {
            throw new BusinessRuleException("Não é possível criar vínculo em uma atlética inativa.");
        }
        if (membroRepository.existsByUsuario_IdAndAtletica_Id(request.usuarioId(), atleticaId)) {
            throw new ConflictException("O usuário já possui vínculo com esta atlética.");
        }

        Usuario usuario = usuarioRepository.findById(request.usuarioId()).orElseThrow(ResourceNotFoundException::new);
        Cargo cargo = resourceResolver.requireByIdAndAtleticaId(request.cargoId(), cargoRepository::findByIdAndAtletica_Id);
        Departamento departamento = resourceResolver.requireByIdAndAtleticaId(
                request.departamentoId(), departamentoRepository::findByIdAndAtletica_Id
        );
        validarAtribuicoes(cargo, departamento);
        NomeNormalizado.fromRequired(request.curso(), "curso");
        String curso = NomeNormalizado.displayValue(request.curso());
        if (curso.length() > 160) {
            throw new BusinessRuleException("O curso deve ter no máximo 160 caracteres.");
        }
        if (request.periodoCurso() != null && (request.periodoCurso() < 1 || request.periodoCurso() > 30)) {
            throw new BusinessRuleException("O período do curso deve estar entre 1 e 30.");
        }

        return membroRepository.save(new Membro(
                atletica,
                usuario,
                curso,
                request.periodoCurso(),
                cargo,
                departamento,
                request.membroDesde() == null ? LocalDate.now() : request.membroDesde()
        ));
    }

    @Transactional
    public Membro ativar(Long membroId, MembroActivationRequest request) {
        Membro membro = buscarDaAtleticaAtual(membroId);
        assertVersion(membro, request.version());
        if (membro.getStatus() != StatusMembro.PENDENTE) {
            throw new BusinessRuleException("Somente membros pendentes podem ser ativados.");
        }

        Long atleticaId = membro.getAtleticaId();
        Atletica atletica = resourceResolver.requireByIdAndAtleticaId(atleticaId, atleticaRepository::findByIdAndId);
        Usuario usuario = usuarioRepository.findById(membro.getUsuarioId()).orElseThrow(ResourceNotFoundException::new);
        Cargo cargo = resourceResolver.requireByIdAndAtleticaId(membro.getCargoId(), cargoRepository::findByIdAndAtletica_Id);
        Departamento departamento = resourceResolver.requireByIdAndAtleticaId(
                membro.getDepartamentoId(), departamentoRepository::findByIdAndAtletica_Id
        );
        validarAtribuicoes(cargo, departamento);
        if (!atletica.isAtiva() || !usuario.isCredencialAtiva() || !usuario.possuiCredencialDefinida()) {
            throw new BusinessRuleException("A ativação exige atlética ativa e credencial definida.");
        }

        Membro validador = validarValidador(request.validadoPorMembroId(), departamento);
        membro.ativar(validador);
        return membro;
    }

    @Transactional
    public Membro inativar(Long membroId, MembroInactivationRequest request) {
        Membro membro = buscarDaAtleticaAtual(membroId);
        assertVersion(membro, request.version());
        if (membro.getStatus() != StatusMembro.ATIVO) {
            throw new BusinessRuleException("Somente membros ativos podem ser inativados.");
        }

        departamentoValidators.validarSemResponsabilidadesDeDepartamento(membro.getAtleticaId(), membro.getId());
        List<String> pendencias = pendenciasProvider.listarPendencias(membro.getAtleticaId(), membro.getId());
        if (!pendencias.isEmpty()) {
            throw new BusinessRuleException("O membro possui pendências: " + String.join("; ", pendencias));
        }
        membro.inativar();
        return membro;
    }

    @Transactional(readOnly = true)
    public boolean possuiAcessoAtivo(Long usuarioId) {
        Long atleticaId = resourceResolver.atleticaIdAtual();
        Atletica atletica = resourceResolver.requireByIdAndAtleticaId(atleticaId, atleticaRepository::findByIdAndId);
        return atletica.isAtiva()
                && membroRepository.findByUsuario_IdAndAtletica_Id(usuarioId, atleticaId)
                .filter(membro -> membro.getStatus() == StatusMembro.ATIVO)
                .map(membro -> usuarioRepository.findById(membro.getUsuarioId()))
                .flatMap(optionalUsuario -> optionalUsuario)
                .map(usuario -> usuario.isCredencialAtiva() && usuario.possuiCredencialDefinida())
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Membro buscarDaAtleticaAtual(Long membroId) {
        return resourceResolver.requireByIdAndAtleticaId(membroId, membroRepository::findByIdAndAtletica_Id);
    }

    @Transactional(readOnly = true)
    public List<Membro> listarAtivosDaAtleticaAtual() {
        return membroRepository.findAllByAtletica_IdAndStatusOrderByCursoAsc(
                resourceResolver.atleticaIdAtual(), StatusMembro.ATIVO
        );
    }

    private void validarAtribuicoes(Cargo cargo, Departamento departamento) {
        if (!cargo.isAtivo()) {
            throw new BusinessRuleException("O cargo informado está inativo.");
        }
        if (departamento.getSituacao() == SituacaoDepartamento.INATIVO) {
            throw new BusinessRuleException("O departamento informado está inativo.");
        }
    }

    private Membro validarValidador(Long validadoPorMembroId, Departamento departamento) {
        if (validadoPorMembroId == null) {
            if (departamento.getSituacao() == SituacaoDepartamento.EM_CONFIGURACAO) {
                return null;
            }
            throw new BusinessRuleException("A ativação exige um membro validador ativo.");
        }
        Membro validador = resourceResolver.requireByIdAndAtleticaId(
                validadoPorMembroId, membroRepository::findByIdAndAtletica_Id
        );
        if (validador.getStatus() != StatusMembro.ATIVO) {
            throw new BusinessRuleException("O membro validador deve estar ativo.");
        }
        return validador;
    }

    private void assertVersion(Membro membro, Long version) {
        if (!membro.getVersion().equals(version)) {
            throw new ConcurrentModificationException();
        }
    }
}
