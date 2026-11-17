package br.com.athletix.patrimonio.service;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.patrimonio.dto.CategoriaPatrimonioCreateRequest;
import br.com.athletix.patrimonio.entity.CategoriaPatrimonio;
import br.com.athletix.patrimonio.repository.CategoriaPatrimonioRepository;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ConflictException;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaPatrimonioService {
  private final CategoriaPatrimonioRepository categorias;
  private final AtleticaRepository atleticas;
  private final AtleticaResourceResolver resolver;
  private final PatrimonioAutorizacaoService autorizacao;
  private final AtividadeUsuarioService auditoria;

  public CategoriaPatrimonioService(
      CategoriaPatrimonioRepository categorias,
      AtleticaRepository atleticas,
      AtleticaResourceResolver resolver,
      PatrimonioAutorizacaoService autorizacao,
      AtividadeUsuarioService auditoria) {
    this.categorias = categorias;
    this.atleticas = atleticas;
    this.resolver = resolver;
    this.autorizacao = autorizacao;
    this.auditoria = auditoria;
  }

  @Transactional
  public CategoriaPatrimonio criar(CategoriaPatrimonioCreateRequest request, Membro autor) {
    autorizacao.exigirAdministracao(autor);
    Long atleticaId = resolver.atleticaIdAtual();
    String normalizado = NomeNormalizado.fromRequired(request.nome(), "nome");
    if (categorias.existsByAtletica_IdAndNomeNormalizado(atleticaId, normalizado)) {
      throw new ConflictException(
          "Já existe uma categoria de patrimônio com este nome na atlética.");
    }
    Atletica atletica = resolver.requireByIdAndAtleticaId(atleticaId, atleticas::findByIdAndId);
    CategoriaPatrimonio categoria =
        categorias.save(
            new CategoriaPatrimonio(
                atletica,
                NomeNormalizado.displayValue(request.nome()),
                normalizado,
                request.ativa()));
    auditoria.registrar(
        "CATEGORIA_PATRIMONIO_CRIADA",
        "CATEGORIA_PATRIMONIO",
        categoria.getId(),
        "Categoria de patrimônio criada.");
    return categoria;
  }

  @Transactional(readOnly = true)
  public List<CategoriaPatrimonio> listar(Membro membro) {
    autorizacao.exigirConsulta(membro);
    return categorias.findAllByAtletica_IdOrderByNomeAsc(resolver.atleticaIdAtual());
  }

  @Transactional(readOnly = true)
  public CategoriaPatrimonio exigirAtiva(Long id) {
    CategoriaPatrimonio categoria =
        resolver.requireByIdAndAtleticaId(id, categorias::findByIdAndAtletica_Id);
    if (!categoria.isAtiva())
      throw new BusinessRuleException("A categoria de patrimônio informada está inativa.");
    return categoria;
  }
}
