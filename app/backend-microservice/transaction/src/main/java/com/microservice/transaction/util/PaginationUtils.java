package com.microservice.transaction.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PaginationUtils {

    private static final int MAX_PAGE_SIZE = 100;

    private PaginationUtils() {
        // Private constructor to hide the implicit public one
    }

    public static Pageable ensureSafePageSize(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            return PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort());
        }
        return pageable;
    }
}
