package br.com.athletix.shared.pagination;

import br.com.athletix.shared.exception.InvalidSortException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageableFactoryTest {

    private final PageableFactory factory = new PageableFactory();
    private final Map<String, String> allowedSorts = Map.of("name", "name", "createdAt", "createdAt", "id", "id");

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
        assertThrows(InvalidSortException.class,
                () -> factory.create(new PageQuery(0, 20, List.of("password,asc")), allowedSorts));
    }
}
