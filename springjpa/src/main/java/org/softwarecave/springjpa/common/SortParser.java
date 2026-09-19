package org.softwarecave.springjpa.common;

import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

public final class SortParser {
    private SortParser() {
    }

    public static Sort parse(List<String> sort, Set<String> allowedProperties) {
        if (sort == null || sort.isEmpty()) {
            return Sort.unsorted();
        }
        return Sort.by(sort.stream().map(value -> parseOrder(value, allowedProperties)).toList());
    }

    private static Sort.Order parseOrder(String value, Set<String> allowedProperties) {
        String[] parts = value.split(",", 2);
        String property = parts[0].trim();
        if (!allowedProperties.contains(property)) {
            throw new InvalidSortSpecException("Sorting by '%s' is not allowed".formatted(property));
        }
        Sort.Direction direction = parts.length == 2 ? Sort.Direction.fromString(parts[1].trim()) : Sort.Direction.ASC;
        return new Sort.Order(direction, property);
    }
}
