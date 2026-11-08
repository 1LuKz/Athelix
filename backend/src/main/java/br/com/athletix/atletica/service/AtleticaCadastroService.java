package br.com.athletix.atletica.service;

import br.com.athletix.atletica.dto.AtleticaCreateRequest;
import br.com.athletix.atletica.dto.AtleticaUpdateRequest;
import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.shared.exception.ConcurrentModificationException;
import br.com.athletix.shared.exception.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

/** Regras específicas para cadastro de atléticas; nenhuma rota global é aberta neste protótipo. */
@Service
public class AtleticaCadastroService {

    private final AtleticaRepository atleticaRepository;

    public AtleticaCadastroService(AtleticaRepository atleticaRepository) {
        this.atleticaRepository = atleticaRepository;
    }

    @Transactional
    public Atletica criar(AtleticaCreateRequest request) {
        String cnpj = Cnpj.normalize(request.cnpj());
        ensureCnpjAvailable(cnpj, null);
        return atleticaRepository.save(new Atletica(
                request.nome().trim(),
                normalizeOptional(request.sigla()),
                normalizeOptional(request.instituicao()),
                cnpj,
                normalizeTimeZone(request.fusoHorario()),
                request.ativa()
        ));
    }

    @Transactional
    public void atualizar(Atletica atletica, AtleticaUpdateRequest request) {
        if (!atletica.getVersion().equals(request.version())) {
            throw new ConcurrentModificationException();
        }
        String cnpj = Cnpj.normalize(request.cnpj());
        ensureCnpjAvailable(cnpj, atletica.getId());
        atletica.atualizar(
                request.nome().trim(),
                normalizeOptional(request.sigla()),
                normalizeOptional(request.instituicao()),
                cnpj,
                normalizeTimeZone(request.fusoHorario()),
                request.ativa()
        );
    }

    private void ensureCnpjAvailable(String cnpj, Long currentAtleticaId) {
        if (cnpj == null || !atleticaRepository.existsByCnpj(cnpj)) {
            return;
        }
        atleticaRepository.findByCnpj(cnpj)
                .filter(atletica -> !atletica.getId().equals(currentAtleticaId))
                .ifPresent(atletica -> {
                    throw new ConflictException("O CNPJ já está vinculado a outra atlética.");
                });
    }

    private String normalizeTimeZone(String fusoHorario) {
        try {
            return ZoneId.of(fusoHorario.trim()).getId();
        } catch (RuntimeException exception) {
            throw new br.com.athletix.shared.exception.BusinessRuleException("O fuso horário informado é inválido.");
        }
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
