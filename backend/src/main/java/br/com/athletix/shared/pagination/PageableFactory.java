package br.com.athletix.shared.pagination;

import br.com.athletix.shared.exception.InvalidSortException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Cria paginações determinísticas e aceita somente campos de ordenação explicitamente liberados.
 */
@Component
public class PageableFactory {

  public Pageable create(PageQuery query, Map<String, String> allowedSorts) {
    List<Sort.Order> orders = new ArrayList<>();
    List<String> requestedSorts = normalizeRequestedSorts(query.sort());

    for (String requestedSort : requestedSorts) {
      orders.add(toOrder(requestedSort, allowedSorts));
    }

    if (orders.isEmpty()) {
      orders.add(Sort.Order.asc("id"));
    } else if (orders.stream().noneMatch(order -> order.getProperty().equals("id"))) {
      orders.add(Sort.Order.asc("id"));
    }

    return PageRequest.of(query.resolvedPage(), query.resolvedSize(), Sort.by(orders));
  }

  private List<String> normalizeRequestedSorts(List<String> requestedSorts) {
    List<String> normalizedSorts = new ArrayList<>();
    if (requestedSorts == null) {
      return normalizedSorts;
    }
    for (String requestedSort : requestedSorts) {
      String direction = requestedSort.trim();
      if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
        normalizedSorts.add(requestedSort);
        continue;
      }
      if (normalizedSorts.isEmpty()) {
        throw new InvalidSortException(requestedSort);
      }
      int previousIndex = normalizedSorts.size() - 1;
      String previousSort = normalizedSorts.get(previousIndex);
      if (previousSort.isBlank() || previousSort.contains(",")) {
        throw new InvalidSortException(requestedSort);
      }
      normalizedSorts.set(previousIndex, previousSort + "," + direction);
    }
    return normalizedSorts;
  }

  private Sort.Order toOrder(String requestedSort, Map<String, String> allowedSorts) {
    String[] parts = requestedSort.split(",", -1);
    if (parts.length > 2 || parts[0].isBlank()) {
      throw new InvalidSortException(requestedSort);
    }

    String apiField = parts[0].trim();
    String property = allowedSorts.get(apiField);
    if (property == null) {
      throw new InvalidSortException(apiField);
    }

    Sort.Direction direction;
    try {
      direction =
          parts.length == 1 ? Sort.Direction.ASC : Sort.Direction.fromString(parts[1].trim());
    } catch (IllegalArgumentException exception) {
      throw new InvalidSortException(requestedSort);
    }

    return new Sort.Order(direction, property);
  }
}
