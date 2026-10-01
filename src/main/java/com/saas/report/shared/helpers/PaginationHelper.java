package com.saas.report.shared.helpers;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PaginationHelper {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 200;

    public static Pageable getPageable(Integer page, Integer size) {
        int pageNumber = (page == null || page < 0) ? 0 : page;
        int pageSize = (size == null || size < 1) ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        return PageRequest.of(pageNumber, pageSize);
    }
}
