package br.com.athletix.arquivo.service;

import br.com.athletix.arquivo.dto.ArquivoMetadata;
import br.com.athletix.arquivo.repository.ArquivoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.shared.exception.ResourceNotFoundException;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Leitura só ocorre quando o módulo proprietário do recurso pai confirma sua visibilidade. */
@Service
public class ArquivoLeituraProtegidaService {
    private final ArquivoRepository arquivos;
    private final AtleticaResourceResolver resolver;
    private final List<ArquivoAcessoAoConteudo> autorizadores;

    public ArquivoLeituraProtegidaService(ArquivoRepository arquivos, AtleticaResourceResolver resolver,
                                          List<ArquivoAcessoAoConteudo> autorizadores) {
        this.arquivos = arquivos;
        this.resolver = resolver;
        this.autorizadores = autorizadores;
    }

    @Transactional(readOnly = true)
    public ArquivoConteudo abrir(Long arquivoId, Membro membro) {
        Long atleticaId = resolver.atleticaIdAtual();
        ArquivoMetadata metadata = arquivos.findMetadataByIdAndAtleticaId(arquivoId, atleticaId)
                .orElseThrow(ResourceNotFoundException::new);
        exigirAcessoDoRecursoPai(metadata, membro);
        byte[] conteudo = arquivos.findConteudoByIdAndAtleticaId(arquivoId, atleticaId)
                .orElseThrow(ResourceNotFoundException::new);
        return new ArquivoConteudo(metadata, conteudo);
    }

    private void exigirAcessoDoRecursoPai(ArquivoMetadata arquivo, Membro membro) {
        for (ArquivoAcessoAoConteudo autorizador : autorizadores) {
            ArquivoAcessoAoConteudo.DecisaoLeitura decisao = autorizador.decidirLeitura(arquivo, membro);
            if (decisao == ArquivoAcessoAoConteudo.DecisaoLeitura.PERMITIR) return;
            if (decisao == ArquivoAcessoAoConteudo.DecisaoLeitura.NEGAR) {
                throw new AccessDeniedException("Sem acesso ao recurso vinculado ao arquivo.");
            }
        }
        // Sem vínculo reconhecido, o arquivo é indistinguível de inexistente e nunca é exposto por escopo apenas.
        throw new ResourceNotFoundException();
    }

    public record ArquivoConteudo(ArquivoMetadata metadata, byte[] conteudo) {
    }
}
