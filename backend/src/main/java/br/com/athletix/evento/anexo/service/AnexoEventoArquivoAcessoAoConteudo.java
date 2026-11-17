package br.com.athletix.evento.anexo.service;

import br.com.athletix.arquivo.dto.ArquivoMetadata;
import br.com.athletix.arquivo.service.ArquivoAcessoAoConteudo;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.evento.anexo.entity.AnexoEvento;
import br.com.athletix.evento.anexo.repository.AnexoEventoRepository;
import br.com.athletix.evento.entity.Evento;
import br.com.athletix.evento.repository.EventoRepository;
import br.com.athletix.evento.responsabilidade.repository.ResponsabilidadeEventoRepository;
import br.com.athletix.membro.entity.Membro;
import org.springframework.stereotype.Service;

/** Aplica ao BLOB de evento a mesma regra dos metadados de anexos: organização ou administração. */
@Service
public class AnexoEventoArquivoAcessoAoConteudo implements ArquivoAcessoAoConteudo {
  private final AnexoEventoRepository anexos;
  private final EventoRepository eventos;
  private final ResponsabilidadeEventoRepository responsabilidades;

  public AnexoEventoArquivoAcessoAoConteudo(
      AnexoEventoRepository anexos,
      EventoRepository eventos,
      ResponsabilidadeEventoRepository responsabilidades) {
    this.anexos = anexos;
    this.eventos = eventos;
    this.responsabilidades = responsabilidades;
  }

  @Override
  public DecisaoLeitura decidirLeitura(ArquivoMetadata arquivo, Membro membro) {
    java.util.List<AnexoEvento> vinculos =
        anexos.findAllByArquivoIdAndAtletica_Id(arquivo.id(), arquivo.atleticaId());
    if (vinculos.isEmpty()) {
      return DecisaoLeitura.NAO_SE_APLICA;
    }
    if (membro == null || !arquivo.atleticaId().equals(membro.getAtleticaId())) {
      return DecisaoLeitura.NEGAR;
    }
    for (AnexoEvento anexo : vinculos) {
      Evento evento =
          eventos.findByIdAndAtletica_Id(anexo.getEventoId(), arquivo.atleticaId()).orElse(null);
      boolean permitido =
          evento != null
              && (membro.getPerfilAcesso() == PerfilAcesso.ADMINISTRADOR
                  || membro.getId().equals(evento.getCriadoPorMembroId())
                  || responsabilidades.existsByEvento_IdAndAtletica_IdAndMembro_IdAndAtiva(
                      evento.getId(), arquivo.atleticaId(), membro.getId(), true));
      if (permitido) return DecisaoLeitura.PERMITIR;
    }
    return DecisaoLeitura.NEGAR;
  }
}
