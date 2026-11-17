package br.com.athletix.membro.service;

import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.shared.tenancy.ContextoAtletica;
import jakarta.persistence.criteria.JoinType;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consultas filtradas sempre incluem a atlética do contexto e jamais o id livre do navegador. */
@Service
public class MembroConsultaService {
  private final MembroRepository membroRepository;
  private final ContextoAtletica contextoAtletica;

  public MembroConsultaService(
      MembroRepository membroRepository, ContextoAtletica contextoAtletica) {
    this.membroRepository = membroRepository;
    this.contextoAtletica = contextoAtletica;
  }

  @Transactional(readOnly = true)
  public Page<Membro> listar(
      String busca, Long departamentoId, StatusMembro status, Pageable pageable) {
    return membroRepository.findAll(especificacao(busca, departamentoId, status), pageable);
  }

  @Transactional(readOnly = true)
  public List<Membro> exportar(String busca, Long departamentoId, StatusMembro status, Sort sort) {
    return membroRepository.findAll(especificacao(busca, departamentoId, status), sort);
  }

  private Specification<Membro> especificacao(
      String busca, Long departamentoId, StatusMembro status) {
    Long atleticaId = contextoAtletica.requireAtleticaId();
    return (root, query, criteriaBuilder) -> {
      var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
      predicates.add(criteriaBuilder.equal(root.get("atletica").get("id"), atleticaId));
      if (departamentoId != null) {
        predicates.add(criteriaBuilder.equal(root.get("departamento").get("id"), departamentoId));
      }
      if (status != null) {
        predicates.add(criteriaBuilder.equal(root.get("status"), status));
      }
      if (busca != null && !busca.isBlank()) {
        String termo = "%" + busca.trim().toLowerCase(Locale.ROOT) + "%";
        var usuario = root.join("usuario", JoinType.INNER);
        predicates.add(
            criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(usuario.get("nome")), termo),
                criteriaBuilder.like(criteriaBuilder.lower(usuario.get("email")), termo),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("curso")), termo)));
      }
      return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
    };
  }
}
