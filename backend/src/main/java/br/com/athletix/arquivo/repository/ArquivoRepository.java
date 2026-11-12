package br.com.athletix.arquivo.repository;

import br.com.athletix.arquivo.dto.ArquivoMetadata;
import br.com.athletix.arquivo.entity.Arquivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ArquivoRepository extends JpaRepository<Arquivo, Long> {

    @Query("""
            select new br.com.athletix.arquivo.dto.ArquivoMetadata(a.id, a.atletica.id, a.nomeOriginal,
                a.mimeType, a.tamanhoBytes, a.hashConteudo, a.enviadoPorMembroId, a.enviadoEm)
            from Arquivo a where a.id = :id and a.atletica.id = :atleticaId
            """)
    Optional<ArquivoMetadata> findMetadataByIdAndAtleticaId(@Param("id") Long id,
                                                              @Param("atleticaId") Long atleticaId);

    @Query("select a.conteudo from Arquivo a where a.id = :id and a.atletica.id = :atleticaId")
    Optional<byte[]> findConteudoByIdAndAtleticaId(@Param("id") Long id, @Param("atleticaId") Long atleticaId);

    @Query("select coalesce(sum(a.tamanhoBytes), 0) from Arquivo a where a.atletica.id = :atleticaId")
    long totalTamanhoBytesByAtleticaId(@Param("atleticaId") Long atleticaId);
}
