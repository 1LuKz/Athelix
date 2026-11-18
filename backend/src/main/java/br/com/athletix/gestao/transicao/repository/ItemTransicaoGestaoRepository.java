package br.com.athletix.gestao.transicao.repository;

import br.com.athletix.gestao.transicao.entity.ItemTransicaoGestao;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemTransicaoGestaoRepository extends JpaRepository<ItemTransicaoGestao, Long> {
  List<ItemTransicaoGestao> findAllByTransicao_IdOrderByMembroIdAsc(Long transicaoId);

  Optional<ItemTransicaoGestao> findByTransicao_IdAndMembroId(Long transicaoId, Long membroId);
}
