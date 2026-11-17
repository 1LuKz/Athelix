package br.com.athletix.evento.anexo.service;

import br.com.athletix.arquivo.entity.FinalidadeArquivo;
import br.com.athletix.arquivo.service.ArquivoArmazenamentoService;
import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.evento.anexo.entity.AnexoEvento;
import br.com.athletix.evento.anexo.entity.TipoMaterialEvento;
import br.com.athletix.evento.anexo.repository.AnexoEventoRepository;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.evento.entity.StatusEvento;
import br.com.athletix.evento.repository.EventoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ConcurrentModificationException;
import br.com.athletix.shared.exception.ResourceNotFoundException;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Mantém o vínculo do anexo no evento, sem apagar o arquivo protegido subjacente. */
@Service
public class AnexoEventoService {
  private final AnexoEventoRepository anexos;
  private final EventoRepository eventos;
  private final AtleticaRepository atleticas;
  private final ArquivoArmazenamentoService arquivos;
  private final AtleticaResourceResolver resolver;
  private final AtividadeUsuarioService atividadeService;

  public AnexoEventoService(
      AnexoEventoRepository anexos,
      EventoRepository eventos,
      AtleticaRepository atleticas,
      ArquivoArmazenamentoService arquivos,
      AtleticaResourceResolver resolver,
      AtividadeUsuarioService atividadeService) {
    this.anexos = anexos;
    this.eventos = eventos;
    this.atleticas = atleticas;
    this.arquivos = arquivos;
    this.resolver = resolver;
    this.atividadeService = atividadeService;
  }

  @Transactional(readOnly = true)
  public List<AnexoEvento> listar(Long eventoId) {
    validarEvento(eventoId);
    return anexos.findAllByEvento_IdAndAtletica_IdOrderByCreatedAtAsc(
        eventoId, resolver.atleticaIdAtual());
  }

  @Transactional
  public AnexoEvento criar(
      Long eventoId,
      MultipartFile arquivo,
      String titulo,
      String descricao,
      TipoMaterialEvento tipoMaterial,
      Membro autor) {
    Evento evento = validarEvento(eventoId);
    if (!autor.getAtleticaId().equals(evento.getAtleticaId())) {
      throw new ResourceNotFoundException();
    }
    String tituloNormalizado = NomeNormalizado.displayValue(titulo);
    String descricaoNormalizada = NomeNormalizado.optionalDisplayValue(descricao);
    Atletica atletica =
        resolver.requireByIdAndAtleticaId(resolver.atleticaIdAtual(), atleticas::findByIdAndId);
    Long arquivoId = arquivos.armazenar(arquivo, finalidadePara(tipoMaterial), autor).id();
    AnexoEvento anexo =
        anexos.save(
            new AnexoEvento(
                atletica,
                evento,
                arquivoId,
                tituloNormalizado,
                descricaoNormalizada,
                tipoMaterial));
    atividadeService.registrar(
        "EVENTO_ANEXO_CRIADO", "ANEXO_EVENTO", anexo.getId(), "Anexo adicionado ao evento.");
    return anexo;
  }

  @Transactional
  public void remover(Long eventoId, Long anexoId, Long version) {
    Evento evento =
        eventos
            .findByIdAndAtletica_IdForUpdate(eventoId, resolver.atleticaIdAtual())
            .orElseThrow(ResourceNotFoundException::new);
    if (evento.getStatus() == StatusEvento.CONCLUIDO) {
      throw new BusinessRuleException(
          "Anexos não podem ser removidos após o encerramento do evento.");
    }
    AnexoEvento anexo =
        anexos
            .findByIdAndEvento_IdAndAtletica_Id(anexoId, eventoId, resolver.atleticaIdAtual())
            .orElseThrow(ResourceNotFoundException::new);
    if (!anexo.getVersion().equals(version)) {
      throw new ConcurrentModificationException();
    }
    anexos.delete(anexo);
    atividadeService.registrar(
        "EVENTO_ANEXO_REMOVIDO",
        "ANEXO_EVENTO",
        anexoId,
        "Vínculo de anexo removido do evento; o arquivo foi preservado.");
  }

  private Evento validarEvento(Long eventoId) {
    return eventos
        .findByIdAndAtletica_Id(eventoId, resolver.atleticaIdAtual())
        .orElseThrow(ResourceNotFoundException::new);
  }

  private FinalidadeArquivo finalidadePara(TipoMaterialEvento tipoMaterial) {
    if (tipoMaterial == null) {
      throw new BusinessRuleException("O tipo do material é obrigatório.");
    }
    return tipoMaterial == TipoMaterialEvento.REGULAMENTO
        ? FinalidadeArquivo.DOCUMENTO
        : FinalidadeArquivo.DIVULGACAO;
  }
}
