package br.com.athletix.evento.anexo.repository;

import br.com.athletix.evento.anexo.entity.AnexoEvento;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnexoEventoRepository extends JpaRepository<AnexoEvento, Long> {
  List<AnexoEvento> findAllByEvento_IdAndAtletica_IdOrderByCreatedAtAsc(
      Long eventoId, Long atleticaId);

  Optional<AnexoEvento> findByIdAndEvento_IdAndAtletica_Id(Long id, Long eventoId, Long atleticaId);

  List<AnexoEvento> findAllByArquivoIdAndAtletica_Id(Long arquivoId, Long atleticaId);
}
