package br.com.athletix.atletica.service;

import br.com.athletix.atletica.dto.DominioEmailPermitidoCreateRequest;
import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.entity.DominioEmailPermitido;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.atletica.repository.DominioEmailPermitidoRepository;
import br.com.athletix.shared.exception.ConflictException;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Operações de domínio sempre usam a atlética resolvida no contexto, nunca um ID do navegador. */
@Service
public class DominioEmailPermitidoService {

    private final DominioEmailPermitidoRepository dominioRepository;
    private final AtleticaRepository atleticaRepository;
    private final AtleticaResourceResolver resourceResolver;

    public DominioEmailPermitidoService(
            DominioEmailPermitidoRepository dominioRepository,
            AtleticaRepository atleticaRepository,
            AtleticaResourceResolver resourceResolver
    ) {
        this.dominioRepository = dominioRepository;
        this.atleticaRepository = atleticaRepository;
        this.resourceResolver = resourceResolver;
    }

    @Transactional
    public DominioEmailPermitido cadastrar(DominioEmailPermitidoCreateRequest request) {
        Long atleticaId = resourceResolver.atleticaIdAtual();
        String dominio = DominioEmail.normalize(request.dominio());
        if (dominioRepository.existsByAtletica_IdAndDominio(atleticaId, dominio)) {
            throw new ConflictException("O domínio já está permitido para esta atlética.");
        }

        Atletica atletica = resourceResolver.requireByIdAndAtleticaId(atleticaId, atleticaRepository::findByIdAndId);
        return dominioRepository.save(new DominioEmailPermitido(atletica, dominio));
    }

    @Transactional(readOnly = true)
    public List<DominioEmailPermitido> listarDaAtleticaAtual() {
        return dominioRepository.findAllByAtletica_IdOrderByDominioAsc(resourceResolver.atleticaIdAtual());
    }

    @Transactional(readOnly = true)
    public DominioEmailPermitido buscarDaAtleticaAtual(Long dominioId) {
        return resourceResolver.requireByIdAndAtleticaId(dominioId, dominioRepository::findByIdAndAtletica_Id);
    }
}
