package br.com.athletix.departamento.service;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.departamento.dto.DepartamentoCreateRequest;
import br.com.athletix.departamento.dto.DepartamentoTransitionRequest;
import br.com.athletix.departamento.dto.DepartamentoUpdateRequest;
import br.com.athletix.departamento.dto.ResponsavelDepartamentoUpdateRequest;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.departamento.entity.SituacaoDepartamento;
import br.com.athletix.departamento.repository.DepartamentoRepository;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ConcurrentModificationException;
import br.com.athletix.shared.exception.ConflictException;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Ciclo de vida específico de departamentos, sem CRUD genérico. */
@Service
public class DepartamentoService {

    private final DepartamentoRepository departamentoRepository;
    private final AtleticaRepository atleticaRepository;
    private final AtleticaResourceResolver resourceResolver;
    private final ResponsavelDepartamentoValidator responsavelValidator;
    private final InativacaoDepartamentoValidator inativacaoValidator;

    public DepartamentoService(
            DepartamentoRepository departamentoRepository,
            AtleticaRepository atleticaRepository,
            AtleticaResourceResolver resourceResolver,
            ResponsavelDepartamentoValidator responsavelValidator,
            InativacaoDepartamentoValidator inativacaoValidator
    ) {
        this.departamentoRepository = departamentoRepository;
        this.atleticaRepository = atleticaRepository;
        this.resourceResolver = resourceResolver;
        this.responsavelValidator = responsavelValidator;
        this.inativacaoValidator = inativacaoValidator;
    }

    @Transactional
    public Departamento criarEmConfiguracao(DepartamentoCreateRequest request) {
        Long atleticaId = resourceResolver.atleticaIdAtual();
        String nomeNormalizado = NomeNormalizado.fromRequired(request.nome(), "nome");
        ensureNameAvailable(atleticaId, nomeNormalizado, null);
        Atletica atletica = resourceResolver.requireByIdAndAtleticaId(atleticaId, atleticaRepository::findByIdAndId);

        return departamentoRepository.save(new Departamento(
                atletica,
                NomeNormalizado.displayValue(request.nome()),
                nomeNormalizado,
                NomeNormalizado.optionalDisplayValue(request.descricao())
        ));
    }

    @Transactional
    public Departamento atualizarDadosBasicos(Long departamentoId, DepartamentoUpdateRequest request) {
        Departamento departamento = buscarDaAtleticaAtual(departamentoId);
        assertVersion(departamento, request.version());
        if (departamento.getSituacao() == SituacaoDepartamento.INATIVO) {
            throw new BusinessRuleException("Um departamento inativo não pode ser alterado.");
        }

        String nomeNormalizado = NomeNormalizado.fromRequired(request.nome(), "nome");
        ensureNameAvailable(departamento.getAtleticaId(), nomeNormalizado, departamento.getId());
        departamento.atualizarDadosBasicos(
                NomeNormalizado.displayValue(request.nome()),
                nomeNormalizado,
                NomeNormalizado.optionalDisplayValue(request.descricao())
        );
        return departamento;
    }

    @Transactional
    public Departamento designarResponsavel(Long departamentoId, ResponsavelDepartamentoUpdateRequest request) {
        Departamento departamento = buscarDaAtleticaAtual(departamentoId);
        assertVersion(departamento, request.version());
        if (departamento.getSituacao() == SituacaoDepartamento.INATIVO) {
            throw new BusinessRuleException("Não é possível designar responsável em departamento inativo.");
        }

        // B05 garante que o membro é ativo, da mesma atlética e do próprio departamento.
        responsavelValidator.validarMembroAtivoDoDepartamento(
                request.responsavelMembroId(), departamento.getAtleticaId(), departamento.getId()
        );
        departamento.definirResponsavel(request.responsavelMembroId());
        return departamento;
    }

    @Transactional
    public Departamento ativar(Long departamentoId, DepartamentoTransitionRequest request) {
        Departamento departamento = buscarDaAtleticaAtual(departamentoId);
        assertVersion(departamento, request.version());
        if (departamento.getSituacao() != SituacaoDepartamento.EM_CONFIGURACAO) {
            throw new BusinessRuleException("Somente departamentos em configuração podem ser ativados.");
        }
        if (departamento.getResponsavelMembroId() == null) {
            throw new BusinessRuleException("A ativação exige um responsável ativo do próprio departamento.");
        }

        responsavelValidator.validarMembroAtivoDoDepartamento(
                departamento.getResponsavelMembroId(), departamento.getAtleticaId(), departamento.getId()
        );
        departamento.ativar();
        return departamento;
    }

    @Transactional
    public Departamento inativar(Long departamentoId, DepartamentoTransitionRequest request) {
        Departamento departamento = buscarDaAtleticaAtual(departamentoId);
        assertVersion(departamento, request.version());
        if (departamento.getSituacao() != SituacaoDepartamento.ATIVO) {
            throw new BusinessRuleException("Somente departamentos ativos podem ser inativados.");
        }

        inativacaoValidator.validarMembrosTransferidosEResponsabilidadesSubstituidas(
                departamento.getAtleticaId(), departamento.getId()
        );
        departamento.inativar();
        return departamento;
    }

    @Transactional(readOnly = true)
    public Departamento buscarDaAtleticaAtual(Long departamentoId) {
        return resourceResolver.requireByIdAndAtleticaId(
                departamentoId, departamentoRepository::findByIdAndAtletica_Id
        );
    }

    /** Usado pelas telas comuns; departamentos em configuração não integram a estrutura ativa. */
    @Transactional(readOnly = true)
    public List<Departamento> listarEstruturaAtivaDaAtleticaAtual() {
        return departamentoRepository.findAllByAtletica_IdAndSituacaoOrderByNomeAsc(
                resourceResolver.atleticaIdAtual(), SituacaoDepartamento.ATIVO
        );
    }

    /** A lista administrativa inclui configurações para concluir o fluxo do primeiro membro. */
    @Transactional(readOnly = true)
    public List<Departamento> listarParaAdministracaoDaAtleticaAtual(PerfilAcesso perfilSolicitante) {
        if (perfilSolicitante != PerfilAcesso.ADMINISTRADOR) {
            throw new AccessDeniedException("Apenas a administração pode consultar departamentos em configuração.");
        }
        return departamentoRepository.findAllByAtletica_IdOrderByNomeAsc(resourceResolver.atleticaIdAtual());
    }

    private void assertVersion(Departamento departamento, Long version) {
        if (!departamento.getVersion().equals(version)) {
            throw new ConcurrentModificationException();
        }
    }

    private void ensureNameAvailable(Long atleticaId, String nomeNormalizado, Long currentDepartamentoId) {
        if (!departamentoRepository.existsByAtletica_IdAndNomeNormalizado(atleticaId, nomeNormalizado)) {
            return;
        }
        departamentoRepository.findByAtletica_IdAndNomeNormalizado(atleticaId, nomeNormalizado)
                .filter(departamento -> !departamento.getId().equals(currentDepartamentoId))
                .ifPresent(departamento -> {
                    throw new ConflictException("Já existe um departamento com este nome na atlética.");
                });
    }
}
