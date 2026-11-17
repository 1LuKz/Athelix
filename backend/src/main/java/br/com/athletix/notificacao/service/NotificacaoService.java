package br.com.athletix.notificacao.service;

import br.com.athletix.notificacao.dto.NotificacaoResponse;
import br.com.athletix.notificacao.entity.Notificacao;
import br.com.athletix.notificacao.repository.NotificacaoRepository;
import br.com.athletix.shared.exception.ResourceNotFoundException;
import br.com.athletix.shared.pagination.PageResponse;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificacaoService {
  private final NotificacaoRepository repository;
  private final AtleticaResourceResolver resolver;

  public NotificacaoService(NotificacaoRepository repository, AtleticaResourceResolver resolver) {
    this.repository = repository;
    this.resolver = resolver;
  }

  @Transactional(readOnly = true)
  public PageResponse<NotificacaoResponse> listar(
      Long membroId, boolean apenasNaoLidas, Pageable pageable) {
    Long atleticaId = resolver.atleticaIdAtual();
    Page<Notificacao> pagina =
        apenasNaoLidas
            ? repository.findAllByDestinatarioMembroIdAndAtleticaIdAndLidaEmIsNull(
                membroId, atleticaId, pageable)
            : repository.findAllByDestinatarioMembroIdAndAtleticaId(membroId, atleticaId, pageable);
    return PageResponse.from(pagina, NotificacaoResponse::from);
  }

  @Transactional(readOnly = true)
  public NotificacaoResponse.Resumo resumo(Long membroId) {
    return new NotificacaoResponse.Resumo(
        repository.countByDestinatarioMembroIdAndAtleticaIdAndLidaEmIsNull(
            membroId, resolver.atleticaIdAtual()));
  }

  @Transactional
  public NotificacaoResponse ler(Long id, Long membroId) {
    Notificacao n =
        repository
            .findByIdAndDestinatarioMembroIdAndAtleticaId(id, membroId, resolver.atleticaIdAtual())
            .orElseThrow(ResourceNotFoundException::new); // de outra pessoa = 404
    n.ler();
    return NotificacaoResponse.from(n);
  }
}
