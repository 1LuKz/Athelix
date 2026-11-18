package br.com.athletix.documento.repository;

import br.com.athletix.documento.entity.VersaoDocumento;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VersaoDocumentoRepository extends JpaRepository<VersaoDocumento, Long> {
  List<VersaoDocumento> findAllByDocumento_IdAndAtletica_IdOrderByNumeroVersaoDesc(
      Long documentoId, Long atleticaId);

  Optional<VersaoDocumento> findByIdAndDocumento_IdAndAtletica_Id(
      Long versaoId, Long documentoId, Long atleticaId);

  Optional<VersaoDocumento> findTopByDocumento_IdAndAtletica_IdOrderByNumeroVersaoDesc(
      Long documentoId, Long atleticaId);

  @Query(
      "select coalesce(max(versao.numeroVersao), 0) from VersaoDocumento versao "
          + "where versao.documento.id = :documentoId and versao.atletica.id = :atleticaId")
  int maiorNumeroVersao(Long documentoId, Long atleticaId);

  List<VersaoDocumento> findAllByArquivoIdAndAtletica_Id(Long arquivoId, Long atleticaId);
}
