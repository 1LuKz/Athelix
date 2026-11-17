package br.com.athletix.membro.service;

import br.com.athletix.departamento.repository.DepartamentoRepository;
import br.com.athletix.departamento.service.InativacaoDepartamentoValidator;
import br.com.athletix.departamento.service.ResponsavelDepartamentoValidator;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

/** Integra B04 e B05 sem criar dependência circular na carga inicial. */
@Component
public class MembroDepartamentoValidators
    implements ResponsavelDepartamentoValidator, InativacaoDepartamentoValidator {

  private final MembroRepository membroRepository;
  private final DepartamentoRepository departamentoRepository;

  public MembroDepartamentoValidators(
      MembroRepository membroRepository, DepartamentoRepository departamentoRepository) {
    this.membroRepository = membroRepository;
    this.departamentoRepository = departamentoRepository;
  }

  @Override
  public void validarMembroAtivoDoDepartamento(
      Long membroId, Long atleticaId, Long departamentoId) {
    Membro membro =
        membroRepository
            .findByIdAndAtletica_Id(membroId, atleticaId)
            .orElseThrow(ResourceNotFoundException::new);
    if (membro.getStatus() != StatusMembro.ATIVO
        || !membro.getDepartamentoId().equals(departamentoId)) {
      throw new BusinessRuleException(
          "O responsável deve ser membro ativo do próprio departamento.");
    }
  }

  @Override
  public void validarMembrosTransferidosEResponsabilidadesSubstituidas(
      Long atleticaId, Long departamentoId) {
    if (membroRepository.existsByDepartamento_IdAndStatus(departamentoId, StatusMembro.ATIVO)) {
      throw new BusinessRuleException(
          "Existem membros ativos no departamento. Transfira-os antes de inativar.");
    }
  }

  public void validarSemResponsabilidadesDeDepartamento(Long atleticaId, Long membroId) {
    if (!departamentoRepository
        .findAllByAtletica_IdAndResponsavelMembroId(atleticaId, membroId)
        .isEmpty()) {
      throw new BusinessRuleException(
          "O membro é responsável por departamento(s). Defina substitutos antes de inativá-lo.");
    }
  }
}
