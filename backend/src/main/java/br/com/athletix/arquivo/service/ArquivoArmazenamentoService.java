package br.com.athletix.arquivo.service;

import br.com.athletix.arquivo.dto.ArquivoMetadata;
import br.com.athletix.arquivo.entity.Arquivo;
import br.com.athletix.arquivo.entity.FinalidadeArquivo;
import br.com.athletix.arquivo.repository.ArquivoRepository;
import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.exception.ResourceNotFoundException;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Locale;

/** Armazena BLOBs somente após a autorização do recurso pai feita pelo módulo chamador. */
@Service
public class ArquivoArmazenamentoService {
    private final ArquivoRepository arquivos;
    private final AtleticaRepository atleticas;
    private final AtleticaResourceResolver resolver;
    private final ArquivoArmazenamentoProperties properties;
    private final AtividadeUsuarioService auditoria;

    public ArquivoArmazenamentoService(ArquivoRepository arquivos, AtleticaRepository atleticas,
                                       AtleticaResourceResolver resolver, ArquivoArmazenamentoProperties properties,
                                       AtividadeUsuarioService auditoria) {
        this.arquivos = arquivos;
        this.atleticas = atleticas;
        this.resolver = resolver;
        this.properties = properties;
        this.auditoria = auditoria;
    }

    /**
     * O módulo pai precisa autorizar a operação e persistir o vínculo ao recurso na mesma transação.
     * Não existe endpoint público de upload para impedir arquivos órfãos ou sem finalidade.
     */
    @Transactional
    public ArquivoMetadata armazenar(MultipartFile upload, FinalidadeArquivo finalidade, Membro enviadoPor) {
        if (finalidade == null) {
            throw new BusinessRuleException("A finalidade do arquivo é obrigatória.");
        }
        if (upload == null || upload.isEmpty()) {
            throw new BusinessRuleException("O arquivo enviado é obrigatório.");
        }
        if (upload.getSize() > properties.getMaxTamanhoBytes()) {
            throw new BusinessRuleException("O arquivo excede o tamanho máximo permitido.");
        }
        Long atleticaId = resolver.atleticaIdAtual();
        if (!atleticaId.equals(enviadoPor.getAtleticaId())) {
            throw new ResourceNotFoundException();
        }
        Atletica atletica = atleticas.findByIdForUpdate(atleticaId).orElseThrow(ResourceNotFoundException::new);
        byte[] conteudo = conteudo(upload);
        if (conteudo.length == 0 || conteudo.length > properties.getMaxTamanhoBytes()) {
            throw new BusinessRuleException("O arquivo excede o tamanho máximo permitido ou está vazio.");
        }
        TipoArquivo tipo = validarTipo(upload.getOriginalFilename(), upload.getContentType(), conteudo, finalidade);
        long usado = arquivos.totalTamanhoBytesByAtleticaId(atleticaId);
        if (usado > properties.getOrcamentoPorAtleticaBytes()
                || conteudo.length > properties.getOrcamentoPorAtleticaBytes() - usado) {
            throw new BusinessRuleException("O orçamento de armazenamento da atlética foi atingido.");
        }
        Arquivo salvo = arquivos.save(new Arquivo(atletica, nomeSeguro(upload.getOriginalFilename()), tipo.mimeType,
                conteudo.length, hashSha256(conteudo), enviadoPor.getId(), Instant.now(), conteudo));
        auditoria.registrar("ARQUIVO_ENVIADO", "ARQUIVO", salvo.getId(), "Arquivo protegido enviado.");
        return metadata(salvo);
    }

    private byte[] conteudo(MultipartFile upload) {
        try {
            return upload.getBytes();
        } catch (IOException exception) {
            throw new BusinessRuleException("Não foi possível ler o arquivo enviado.");
        }
    }

    private TipoArquivo validarTipo(String nomeOriginal, String mimeDeclarado, byte[] conteudo,
                                    FinalidadeArquivo finalidade) {
        String nome = nomeSeguro(nomeOriginal);
        String extensao = extensao(nome);
        TipoArquivo detectado = detectarAssinatura(conteudo);
        if (detectado == null || !detectado.extensaoAceita(extensao)) {
            throw new BusinessRuleException("A assinatura ou a extensão do arquivo não é permitida.");
        }
        String mime = mimeDeclarado == null ? "" : mimeDeclarado.trim().toLowerCase(Locale.ROOT);
        if (!detectado.mimeType.equals(mime)) {
            throw new BusinessRuleException("O tipo MIME declarado não corresponde ao conteúdo do arquivo.");
        }
        if (finalidade == FinalidadeArquivo.DOCUMENTO && detectado != TipoArquivo.PDF) {
            throw new BusinessRuleException("Documentos aceitam apenas arquivos PDF.");
        }
        return detectado;
    }

    private TipoArquivo detectarAssinatura(byte[] conteudo) {
        if (comecaCom(conteudo, "%PDF-".getBytes(StandardCharsets.US_ASCII))) return TipoArquivo.PDF;
        if (comecaCom(conteudo, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A})) return TipoArquivo.PNG;
        if (conteudo.length >= 3 && (conteudo[0] & 0xff) == 0xff && (conteudo[1] & 0xff) == 0xd8
                && (conteudo[2] & 0xff) == 0xff) return TipoArquivo.JPEG;
        return null;
    }

    private boolean comecaCom(byte[] conteudo, byte[] assinatura) {
        if (conteudo.length < assinatura.length) return false;
        for (int indice = 0; indice < assinatura.length; indice++) {
            if (conteudo[indice] != assinatura[indice]) return false;
        }
        return true;
    }

    private String nomeSeguro(String original) {
        if (original == null) throw new BusinessRuleException("O nome original do arquivo é obrigatório.");
        String nome = original.replace('\\', '/');
        nome = nome.substring(nome.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "").trim();
        if (nome.isBlank() || nome.length() > 255) {
            throw new BusinessRuleException("O nome original do arquivo é inválido.");
        }
        return nome;
    }

    private String extensao(String nome) {
        int ponto = nome.lastIndexOf('.');
        return ponto < 1 || ponto == nome.length() - 1 ? "" : nome.substring(ponto + 1).toLowerCase(Locale.ROOT);
    }

    private String hashSha256(byte[] conteudo) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(conteudo));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível na JVM.", exception);
        }
    }

    private ArquivoMetadata metadata(Arquivo arquivo) {
        return new ArquivoMetadata(arquivo.getId(), arquivo.getAtleticaId(), arquivo.getNomeOriginal(),
                arquivo.getMimeType(), arquivo.getTamanhoBytes(), arquivo.getHashConteudo(),
                arquivo.getEnviadoPorMembroId(), arquivo.getEnviadoEm());
    }

    private enum TipoArquivo {
        PDF("application/pdf", "pdf"),
        PNG("image/png", "png"),
        JPEG("image/jpeg", "jpg", "jpeg");

        private final String mimeType;
        private final String[] extensoes;

        TipoArquivo(String mimeType, String... extensoes) {
            this.mimeType = mimeType;
            this.extensoes = extensoes;
        }

        private boolean extensaoAceita(String extensao) {
            for (String aceita : extensoes) if (aceita.equals(extensao)) return true;
            return false;
        }
    }
}
