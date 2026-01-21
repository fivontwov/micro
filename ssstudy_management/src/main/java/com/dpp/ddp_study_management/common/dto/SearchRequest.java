package com.dpp.ddp_study_management.common.dto;

import com.dpp.ddp_study_management.common.exception.AppException;
import com.dpp.ddp_study_management.common.exception.ErrorCode;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class SearchRequest<T> {
    private String search;
    private Integer page;
    private Integer size;
    private String name;
    private String sort;

    public SearchRequest(String search, Integer page, Integer size, String name, String sort, Class<T> entityClass) {
        this.search = search;
        this.page = page;
        this.size = size;
        this.name = name;
        this.sort = sort;

        // Validation logic in constructor
        if (page != null && page < 0) {
            throw new AppException(ErrorCode.PAGE_INVALID);
        }

        if (size != null) {
            if (size <= 0) {
                throw new AppException(ErrorCode.SIZE_INVALID);
            }
            if (size > 100) {
                throw new AppException(ErrorCode.SIZE_TOO_LARGE);
            }
        }

        if (sort != null && !sort.equalsIgnoreCase("asc") && !sort.equalsIgnoreCase("desc")) {
            throw new AppException(ErrorCode.SORT_INVALID);
        }

        if (name != null) {
            List<String> validFields = getValidSortableFields(entityClass);
            if (!validFields.contains(name)) {
                throw new AppException(ErrorCode.SORT_FIELD_INVALID);
            }
        }
    }
    private List<String> getValidSortableFields(Class<T> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toList());
    }

    private static <T> String getDefaultSortField(Class<T> entityClass) {
        List<String> validFields = Arrays.stream(entityClass.getDeclaredFields())
                .map(Field::getName)
                .toList();

        // Prefer 'id' field if it exists, otherwise use the first field
        if (validFields.contains("id")) {
            return "id";
        } else if (!validFields.isEmpty()) {
            return validFields.get(0);
        } else {
            throw new AppException(ErrorCode.SORT_FIELD_INVALID);
        }
    }
}
