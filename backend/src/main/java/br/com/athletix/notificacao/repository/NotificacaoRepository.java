package br.com.athletix.notificacao.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.athletix.notificacao.entity.Notificacao;
import br.com.athletix.notificacao.entity.TipoNotificacao;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    Page<Notificacao> findAllByDestinatarioMembroIdAndAtleticaId(Long destinatarioMembroId, Long atleticaId,
                                                                 Pageable pageable);

    Page<Notificacao> findAllByDestinatarioMembroIdAndAtleticaIdAndLidaEmIsNull(Long destinatarioMembroId,
                                                                                Long atleticaId, Pageable pageable);

    Optional<Notificacao> findByIdAndDestinatarioMembroIdAndAtleticaId(Long id, Long destinatarioMembroId,
                                                                       Long atleticaId);

    boolean existsByDestinatarioMembroIdAndTarefaIdAndTipoAndDataReferencia(Long destinatarioMembroId, Long tarefaId,
                                                                            TipoNotificacao tipo, LocalDate dataReferencia);

    long countByDestinatarioMembroIdAndAtleticaIdAndLidaEmIsNull(Long destinatarioMembroId, Long atleticaId);
}