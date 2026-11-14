package br.com.athletix.patrimonio.conferencia.repository;
import br.com.athletix.patrimonio.conferencia.entity.ItemConferencia; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface ItemConferenciaRepository extends JpaRepository<ItemConferencia,Long>{ List<ItemConferencia> findAllByConferencia_IdOrderByItemIdAsc(Long id); Optional<ItemConferencia> findByConferencia_IdAndItemId(Long c,Long i); }
