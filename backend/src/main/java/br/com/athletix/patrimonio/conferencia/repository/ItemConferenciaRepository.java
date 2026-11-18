package br.com.athletix.patrimonio.conferencia.repository;

import br.com.athletix.patrimonio.conferencia.entity.ItemConferencia;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ItemConferenciaRepository extends JpaRepository<ItemConferencia, Long> {
  @Query(
      """
      select coalesce(sum(item.quantidadeEsperada), 0) as unidadesEsperadas,
             coalesce(sum(case when item.quantidadeVerificada is null then 0
                               when item.quantidadeVerificada > item.quantidadeEsperada then item.quantidadeEsperada
                               else item.quantidadeVerificada end), 0) as unidadesVerificadasValidas
      from ItemConferencia item
      where item.conferencia.id = :conferenciaId
      """)
  ProgressoDashboard calcularProgressoDashboard(Long conferenciaId);

  interface ProgressoDashboard {
    Long getUnidadesEsperadas();

    Long getUnidadesVerificadasValidas();
  }

  List<ItemConferencia> findAllByConferencia_IdOrderByItemIdAsc(Long conferenciaId);

  Optional<ItemConferencia> findByConferencia_IdAndItemId(Long conferenciaId, Long itemId);
}
