package br.com.athletix.atletica.repository;

import br.com.athletix.atletica.entity.Atletica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AtleticaRepository extends JpaRepository<Atletica, Long> {

    boolean existsByCnpj(String cnpj);

    Optional<Atletica> findByCnpj(String cnpj);

    Optional<Atletica> findByIdAndId(Long id, Long atleticaId);
}
