package br.com.athletix.documento.service;

import br.com.athletix.arquivo.dto.ArquivoMetadata;
import br.com.athletix.arquivo.entity.FinalidadeArquivo;
import br.com.athletix.arquivo.repository.ArquivoRepository;
import br.com.athletix.arquivo.service.ArquivoArmazenamentoService;
import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.documento.dto.*;
import br.com.athletix.documento.entity.*;
import br.com.athletix.documento.repository.*;
import br.com.athletix.gestao.entity.StatusGestao;
import br.com.athletix.gestao.repository.GestaoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.shared.exception.*;
import br.com.athletix.shared.normalization.NomeNormalizado;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import java.time.Instant;
import java.util.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentoService {
  private final DocumentoRepository documentos;
  private final VersaoDocumentoRepository versoes;
  private final ArquivoRepository arquivosMetadata;
  private final ArquivoArmazenamentoService armazenamento;
  private final AtleticaRepository atleticas;
  private final GestaoRepository gestoes;
  private final AtleticaResourceResolver resolver;
  private final AtividadeUsuarioService auditoria;

  public DocumentoService(
      DocumentoRepository documentos,
      VersaoDocumentoRepository versoes,
      ArquivoRepository arquivosMetadata,
      ArquivoArmazenamentoService armazenamento,
      AtleticaRepository atleticas,
      GestaoRepository gestoes,
      AtleticaResourceResolver resolver,
      AtividadeUsuarioService auditoria) {
    this.documentos = documentos;
    this.versoes = versoes;
    this.arquivosMetadata = arquivosMetadata;
    this.armazenamento = armazenamento;
    this.atleticas = atleticas;
    this.gestoes = gestoes;
    this.resolver = resolver;
    this.auditoria = auditoria;
  }

  @Transactional(readOnly = true)
  public List<DocumentoResponse> listar(
      String busca,
      CategoriaDocumento categoria,
      Long gestaoId,
      boolean incluirArquivados,
      Membro membro) {
    Specification<Documento> especificacaoDocumentos =
        (raizDocumento, consulta, criteriaBuilder) ->
            criteriaBuilder.equal(
                raizDocumento.get("atletica").get("id"), resolver.atleticaIdAtual());
    if (!incluirArquivados) {
      especificacaoDocumentos =
          especificacaoDocumentos.and(
              (raizDocumento, consulta, criteriaBuilder) ->
                  criteriaBuilder.isFalse(raizDocumento.get("arquivado")));
    }
    if (categoria != null) {
      especificacaoDocumentos =
          especificacaoDocumentos.and(
              (raizDocumento, consulta, criteriaBuilder) ->
                  criteriaBuilder.equal(raizDocumento.get("categoria"), categoria));
    }
    if (gestaoId != null) {
      validarGestao(gestaoId);
      especificacaoDocumentos =
          especificacaoDocumentos.and(
              (raizDocumento, consulta, criteriaBuilder) ->
                  criteriaBuilder.equal(raizDocumento.get("gestaoOrigemId"), gestaoId));
    }
    String termo = NomeNormalizado.optionalDisplayValue(busca);
    List<DocumentoResponse> resposta =
        documentos
            .findAll(
                especificacaoDocumentos,
                org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "updatedAt"))
            .stream()
            .filter(documento -> podeLer(documento, membro))
            .map(this::resposta)
            .sorted(
                Comparator.comparing(
                        (DocumentoResponse documentoResposta) ->
                            documentoResposta.versaoAtual() == null
                                ? documentoResposta.atualizadaEm()
                                : documentoResposta.versaoAtual().enviadoEm())
                    .reversed())
            .toList();
    if (termo == null) return resposta;
    String normalizado = termo.toLowerCase(Locale.ROOT);
    return resposta.stream()
        .filter(
            documentoResposta ->
                documentoResposta.titulo().toLowerCase(Locale.ROOT).contains(normalizado)
                    || (documentoResposta.versaoAtual() != null
                        && documentoResposta
                            .versaoAtual()
                            .nomeArquivo()
                            .toLowerCase(Locale.ROOT)
                            .contains(normalizado)))
        .toList();
  }

  @Transactional(readOnly = true)
  public DocumentoResponse obter(Long documentoId, Membro membro) {
    Documento documento = buscar(documentoId);
    exigirLeitura(documento, membro);
    return resposta(documento);
  }

  @Transactional
  public DocumentoResponse criar(
      DocumentoCreateFields campos, MultipartFile arquivo, Membro membro) {
    exigirPublicacao(membro);
    Long atleticaId = resolver.atleticaIdAtual();
    CategoriaDocumento categoria = obrigatorio(campos.categoria(), "A categoria é obrigatória.");
    VisibilidadeDocumento visibilidade =
        obrigatorio(campos.visibilidade(), "A visibilidade é obrigatória.");
    Long gestaoId =
        campos.gestaoOrigemId() == null
            ? gestoes
                .findByAtletica_IdAndStatus(atleticaId, StatusGestao.ATIVA)
                .map(gestaoAtiva -> gestaoAtiva.getId())
                .orElse(null)
            : validarGestao(campos.gestaoOrigemId());
    Atletica atletica = resolver.requireByIdAndAtleticaId(atleticaId, atleticas::findByIdAndId);
    Documento documento =
        documentos.save(
            new Documento(
                atletica,
                NomeNormalizado.displayValue(campos.titulo()),
                categoria,
                NomeNormalizado.optionalDisplayValue(campos.descricao()),
                visibilidade,
                gestaoId));
    VersaoDocumento versao =
        salvarVersao(atletica, documento, 1, arquivo, gestaoId, campos.observacao(), membro);
    auditoria.registrar(
        "DOCUMENTO_CRIADO",
        "DOCUMENTO",
        documento.getId(),
        "Documento criado com a versão inicial.");
    return DocumentoResponse.from(documento, versaoResposta(versao));
  }

  @Transactional
  public DocumentoResponse atualizar(
      Long documentoId, DocumentoUpdateRequest request, Membro membro) {
    exigirPublicacao(membro);
    Documento documento = buscarTravado(documentoId);
    if (!documento.getVersion().equals(request.version()))
      throw new br.com.athletix.shared.exception.ConcurrentModificationException();
    Long gestaoId =
        request.gestaoOrigemId() == null ? null : validarGestao(request.gestaoOrigemId());
    documento.atualizar(
        NomeNormalizado.displayValue(request.titulo()),
        obrigatorio(request.categoria(), "A categoria é obrigatória."),
        NomeNormalizado.optionalDisplayValue(request.descricao()),
        obrigatorio(request.visibilidade(), "A visibilidade é obrigatória."),
        gestaoId,
        request.arquivado());
    auditoria.registrar(
        request.arquivado() ? "DOCUMENTO_ARQUIVADO" : "DOCUMENTO_ATUALIZADO",
        "DOCUMENTO",
        documentoId,
        "Metadados do documento atualizados.");
    return resposta(documento);
  }

  @Transactional(readOnly = true)
  public List<VersaoDocumentoResponse> listarVersoes(Long documentoId, Membro membro) {
    Documento documento = buscar(documentoId);
    exigirLeitura(documento, membro);
    return versoes
        .findAllByDocumento_IdAndAtletica_IdOrderByNumeroVersaoDesc(
            documentoId, resolver.atleticaIdAtual())
        .stream()
        .map(this::versaoResposta)
        .toList();
  }

  @Transactional
  public VersaoDocumentoResponse novaVersao(
      Long documentoId, VersaoDocumentoCreateFields campos, MultipartFile arquivo, Membro membro) {
    exigirPublicacao(membro);
    Documento documento = buscarTravado(documentoId);
    Long gestaoId =
        campos.gestaoId() == null
            ? documento.getGestaoOrigemId()
            : validarGestao(campos.gestaoId());
    Atletica atletica =
        resolver.requireByIdAndAtleticaId(resolver.atleticaIdAtual(), atleticas::findByIdAndId);
    VersaoDocumento versao =
        salvarVersao(
            atletica,
            documento,
            versoes.maiorNumeroVersao(documento.getId(), atletica.getId()) + 1,
            arquivo,
            gestaoId,
            campos.observacao(),
            membro);
    auditoria.registrar(
        "DOCUMENTO_NOVA_VERSAO",
        "VERSAO_DOCUMENTO",
        versao.getId(),
        "Nova versão de documento publicada.");
    return versaoResposta(versao);
  }

  @Transactional(readOnly = true)
  public Long arquivoDaVersao(Long documentoId, Long versaoId, Membro membro) {
    Documento documento = buscar(documentoId);
    exigirLeitura(documento, membro);
    return versoes
        .findByIdAndDocumento_IdAndAtletica_Id(versaoId, documentoId, resolver.atleticaIdAtual())
        .orElseThrow(ResourceNotFoundException::new)
        .getArquivoId();
  }

  public boolean podeLer(Documento documento, Membro membro) {
    return documento.getVisibilidade() == VisibilidadeDocumento.MEMBROS
        || membro.getPerfilAcesso() == PerfilAcesso.ADMINISTRADOR
        || membro.getPerfilAcesso() == PerfilAcesso.FINANCEIRO
        || membro.getPerfilAcesso() == PerfilAcesso.GESTOR;
  }

  private void exigirLeitura(Documento documento, Membro membro) {
    if (!podeLer(documento, membro))
      throw new AccessDeniedException("Sem permissão para consultar este documento.");
  }

  private void exigirPublicacao(Membro membro) {
    if (membro.getPerfilAcesso() == PerfilAcesso.ADMINISTRADOR) return;
    if (membro.getPerfilAcesso() == PerfilAcesso.GESTOR
        && membro.getDepartamento().getResponsavelMembroId() != null
        && membro.getDepartamento().getResponsavelMembroId().equals(membro.getId())) return;
    throw new AccessDeniedException("Sem permissão para publicar documentos.");
  }

  private Documento buscar(Long documentoId) {
    return documentos
        .findByIdAndAtletica_Id(documentoId, resolver.atleticaIdAtual())
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Documento buscarTravado(Long documentoId) {
    return documentos
        .findByIdAndAtletica_IdForUpdate(documentoId, resolver.atleticaIdAtual())
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Long validarGestao(Long gestaoId) {
    return gestoes
        .findByIdAndAtletica_Id(gestaoId, resolver.atleticaIdAtual())
        .orElseThrow(ResourceNotFoundException::new)
        .getId();
  }

  private <T> T obrigatorio(T valor, String mensagem) {
    if (valor == null) throw new BusinessRuleException(mensagem);
    return valor;
  }

  private VersaoDocumento salvarVersao(
      Atletica atletica,
      Documento documento,
      int numero,
      MultipartFile arquivo,
      Long gestaoId,
      String observacao,
      Membro membro) {
    Long arquivoId = armazenamento.armazenar(arquivo, FinalidadeArquivo.DOCUMENTO, membro).id();
    return versoes.save(
        new VersaoDocumento(
            atletica,
            documento,
            numero,
            arquivoId,
            gestaoId,
            NomeNormalizado.optionalDisplayValue(observacao),
            membro.getId(),
            Instant.now()));
  }

  private DocumentoResponse resposta(Documento documento) {
    return DocumentoResponse.from(
        documento,
        versoes
            .findTopByDocumento_IdAndAtletica_IdOrderByNumeroVersaoDesc(
                documento.getId(), resolver.atleticaIdAtual())
            .map(this::versaoResposta)
            .orElse(null));
  }

  private VersaoDocumentoResponse versaoResposta(VersaoDocumento versaoDocumento) {
    String nome =
        arquivosMetadata
            .findMetadataByIdAndAtleticaId(
                versaoDocumento.getArquivoId(), resolver.atleticaIdAtual())
            .map(ArquivoMetadata::nomeOriginal)
            .orElse(null);
    return VersaoDocumentoResponse.from(versaoDocumento, nome);
  }
}
