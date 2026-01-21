package com.dpp.ddp_study_management.common.dto;

import lombok.Data;

@Data
public class PageResponse<T> {
    private PaginationInfo pagination;
    private T content;
}