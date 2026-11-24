package br.com.athletix.shared.pagination;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.athletix.shared.exception.InvalidSortException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PageableFactoryTest {

  private final PageableFactory factory = new PageableFactory();
  private final Map<String, String> allowedSorts =
      Map.of(
          "name",
          "name",
          "createdAt",
          "createdAt",
          "id",
          "id",
          "nome",
          "name",
          "criadaEm",
          "createdAt",
          "valor",
          "amount");

  @Test
  void appliesDefaultPageSizeAndStableIdOrder() {
    Pageable pageable = factory.create(new PageQuery(null, null, List.of()), allowedSorts);

    assertEquals(0, pageable.getPageNumber());
    assertEquals(20, pageable.getPageSize());
    assertEquals("id", pageable.getSort().getOrderFor("id").getProperty());
  }

  @Test
  void appendsIdAsTieBreakerForAllowedSort() {
    Pageable pageable = factory.create(new PageQuery(1, 50, List.of("name,desc")), allowedSorts);

    assertEquals(1, pageable.getPageNumber());
    assertEquals(50, pageable.getPageSize());
    assertEquals("DESC", pageable.getSort().getOrderFor("name").getDirection().name());
    assertEquals("ASC", pageable.getSort().getOrderFor("id").getDirection().name());
  }

  @Test
  void rejectsSortFieldsNotExplicitlyAllowed() {
    assertThrows(
        InvalidSortException.class,
        () -> factory.create(new PageQuery(0, 20, List.of("password,asc")), allowedSorts));
  }

  @Test
  void joinsSplitCreationFieldAndDescendingDirection() {
    Pageable pageable =
        factory.create(new PageQuery(0, 20, List.of("criadaEm", "desc")), allowedSorts);
    assertEquals(Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id")), pageable.getSort());
  }

  @Test
  void joinsSplitNameFieldAndAscendingDirection() {
    Pageable pageable = factory.create(new PageQuery(0, 20, List.of("nome", "asc")), allowedSorts);
    assertEquals(Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id")), pageable.getSort());
  }

  @Test
  void joinsMultipleSplitSortCriteriaInTheirOriginalOrder() {
    Pageable pageable =
        factory.create(new PageQuery(0, 20, List.of("nome", "asc", "valor", "desc")), allowedSorts);
    assertEquals(
        Sort.by(Sort.Order.asc("name"), Sort.Order.desc("amount"), Sort.Order.asc("id")),
        pageable.getSort());
  }

  @Test
  void defaultsFieldWithoutDirectionToAscending() {
    Pageable pageable = factory.create(new PageQuery(0, 20, List.of("nome")), allowedSorts);
    assertEquals(Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id")), pageable.getSort());
  }

  @Test
  void rejectsDirectionWithoutPrecedingField() {
    InvalidSortException exception =
        assertThrows(
            InvalidSortException.class,
            () -> factory.create(new PageQuery(0, 20, List.of("desc")), allowedSorts));
    assertEquals("Ordenação não permitida: desc.", exception.getMessage());
  }

  @Test
  void rejectsTwoConsecutiveDirections() {
    InvalidSortException exception =
        assertThrows(
            InvalidSortException.class,
            () ->
                factory.create(new PageQuery(0, 20, List.of("nome", "asc", "desc")), allowedSorts));
    assertEquals("Ordenação não permitida: desc.", exception.getMessage());
  }

  @Test
  void rejectsUnknownFieldAfterRejoiningItsDirection() {
    InvalidSortException exception =
        assertThrows(
            InvalidSortException.class,
            () ->
                factory.create(
                    new PageQuery(0, 20, List.of("campoInexistente", "desc")), allowedSorts));
    assertEquals("Ordenação não permitida: campoInexistente.", exception.getMessage());
  }

  @Test
  void acceptsMixedGroupedCriteriaAndDirectionsWithSpacesAndMixedCase() {
    Pageable pageable =
        factory.create(new PageQuery(0, 20, List.of("nome,asc", "valor", " DeSc ")), allowedSorts);
    assertEquals(
        Sort.by(Sort.Order.asc("name"), Sort.Order.desc("amount"), Sort.Order.asc("id")),
        pageable.getSort());
  }

  @Test
  void preservesOriginalTextWhenRejectingOrphanDirection() {
    InvalidSortException exception =
        assertThrows(
            InvalidSortException.class,
            () -> factory.create(new PageQuery(0, 20, List.of(" DeSc ")), allowedSorts));
    assertEquals("Ordenação não permitida:  DeSc .", exception.getMessage());
  }
}
