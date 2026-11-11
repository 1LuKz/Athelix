package br.com.athletix.membro.service;

import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.cargo.repository.CargoRepository;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.departamento.entity.SituacaoDepartamento;
import br.com.athletix.departamento.repository.DepartamentoRepository;
import br.com.athletix.gestao.service.VinculoGestaoService;
import br.com.athletix.membro.dto.SolicitacaoAlteracaoVinculoCreateRequest;
import br.com.athletix.membro.dto.SolicitacaoAlteracaoVinculoDecisaoRequest;
import br.com.athletix.membro.entity.DecisaoSolicitacaoAlteracaoVinculo;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.SolicitacaoAlteracaoVinculo;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.membro.entity.StatusSolicitacaoAlteracaoVinculo;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.membro.repository.SolicitacaoAlteracaoVinculoRepository;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ConcurrentModificationException;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** Fluxo explícito para trocar cargo ou departamento somente após decisão administrativa. */
@Service
public class SolicitacaoAlteracaoVinculoService {
    private final SolicitacaoAlteracaoVinculoRepository solicitacaoRepository;
    private final MembroRepository membroRepository;
    private final CargoRepository cargoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final AtleticaResourceResolver resourceResolver;
    private final AtividadeUsuarioService atividadeService;
    private final VinculoGestaoService vinculoGestaoService;

    public SolicitacaoAlteracaoVinculoService(SolicitacaoAlteracaoVinculoRepository solicitacaoRepository,
                                              MembroRepository membroRepository, CargoRepository cargoRepository,
                                              DepartamentoRepository departamentoRepository,
                                              AtleticaResourceResolver resourceResolver,
                                              AtividadeUsuarioService activityService,
                                              VinculoGestaoService vinculoGestaoService) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.membroRepository = membroRepository;
        this.cargoRepository = cargoRepository;
        this.departamentoRepository = departamentoRepository;
        this.resourceResolver = resourceResolver;
        this.atividadeService = activityService;
        this.vinculoGestaoService = vinculoGestaoService;
    }

    @Transactional
    public SolicitacaoAlteracaoVinculo criar(Membro solicitante, SolicitacaoAlteracaoVinculoCreateRequest request) {
        if (solicitante.getStatus() != StatusMembro.ATIVO) {
            throw new BusinessRuleException("Somente membros ativos podem solicitar alteração de vínculo.");
        }
        Cargo cargo = cargoAtual(request.cargoPretendidoId());
        Departamento departamento = departamentoAtual(request.departamentoPretendidoId());
        validarDestino(cargo, departamento);
        if (solicitante.getCargoId().equals(cargo.getId())
                && solicitante.getDepartamentoId().equals(departamento.getId())) {
            throw new BusinessRuleException("O vínculo pretendido é igual ao vínculo atual.");
        }
        String motivo = NomeNormalizado.displayValue(request.motivo());
        SolicitacaoAlteracaoVinculo solicitacao = solicitacaoRepository.save(
                new SolicitacaoAlteracaoVinculo(solicitante, cargo, departamento, motivo)
        );
        atividadeService.registrar("SOLICITACAO_VINCULO_CRIADA", "SOLICITACAO_ALTERACAO_VINCULO",
                solicitacao.getId(), "Solicitação de alteração de vínculo criada.");
        return solicitacao;
    }

    @Transactional(readOnly = true)
    public Page<SolicitacaoAlteracaoVinculo> listar(StatusSolicitacaoAlteracaoVinculo status, Pageable pageable) {
        Long atleticaId = resourceResolver.atleticaIdAtual();
        return status == null
                ? solicitacaoRepository.findAllByMembro_Atletica_Id(atleticaId, pageable)
                : solicitacaoRepository.findAllByMembro_Atletica_IdAndStatus(atleticaId, status, pageable);
    }

    @Transactional
    public SolicitacaoAlteracaoVinculo decidir(Long solicitacaoId, Membro avaliador,
                                               SolicitacaoAlteracaoVinculoDecisaoRequest request) {
        SolicitacaoAlteracaoVinculo solicitacao = resourceResolver.requireByIdAndAtleticaId(
                solicitacaoId, solicitacaoRepository::findByIdAndMembro_Atletica_Id
        );
        if (!solicitacao.getVersion().equals(request.version())) {
            throw new ConcurrentModificationException();
        }
        if (solicitacao.getStatus() != StatusSolicitacaoAlteracaoVinculo.PENDENTE) {
            throw new BusinessRuleException("Esta solicitação já foi avaliada.");
        }
        Membro membro = resourceResolver.requireByIdAndAtleticaId(
                solicitacao.getMembroId(), membroRepository::findByIdAndAtletica_Id
        );
        if (!membro.getVersion().equals(request.membroVersion())) {
            throw new ConcurrentModificationException();
        }
        String observacao = NomeNormalizado.optionalDisplayValue(request.observacao());
        if (request.decisao() == DecisaoSolicitacaoAlteracaoVinculo.REJEITAR) {
            solicitacao.rejeitar(avaliador, observacao);
            atividadeService.registrar("SOLICITACAO_VINCULO_REJEITADA", "SOLICITACAO_ALTERACAO_VINCULO",
                    solicitacao.getId(), "Solicitação de alteração de vínculo rejeitada.");
            return solicitacao;
        }

        if (membro.getStatus() != StatusMembro.ATIVO
                || !membro.getCargoId().equals(solicitacao.getCargoAnteriorId())
                || !membro.getDepartamentoId().equals(solicitacao.getDepartamentoAnteriorId())) {
            throw new BusinessRuleException("O vínculo atual mudou; crie uma nova solicitação.");
        }
        Cargo cargo = cargoAtual(solicitacao.getCargoPretendidoId());
        Departamento departamento = departamentoAtual(solicitacao.getDepartamentoPretendidoId());
        validarDestino(cargo, departamento);
        membro.atualizarVinculo(membro.getCurso(), membro.getPeriodoCurso(), cargo, departamento);
        vinculoGestaoService.registrarMudancaDeAtribuicao(membro, cargo, departamento, Instant.now());
        solicitacao.aprovar(avaliador, observacao);
        atividadeService.registrar("SOLICITACAO_VINCULO_APROVADA", "SOLICITACAO_ALTERACAO_VINCULO",
                solicitacao.getId(), "Solicitação aprovada e vínculo atualizado.");
        return solicitacao;
    }

    private Cargo cargoAtual(Long cargoId) {
        return resourceResolver.requireByIdAndAtleticaId(cargoId, cargoRepository::findByIdAndAtletica_Id);
    }

    private Departamento departamentoAtual(Long departamentoId) {
        return resourceResolver.requireByIdAndAtleticaId(
                departamentoId, departamentoRepository::findByIdAndAtletica_Id
        );
    }

    private void validarDestino(Cargo cargo, Departamento departamento) {
        if (!cargo.isAtivo()) {
            throw new BusinessRuleException("O cargo pretendido está inativo.");
        }
        if (departamento.getSituacao() != SituacaoDepartamento.ATIVO) {
            throw new BusinessRuleException("O departamento pretendido deve estar ativo.");
        }
    }
}
