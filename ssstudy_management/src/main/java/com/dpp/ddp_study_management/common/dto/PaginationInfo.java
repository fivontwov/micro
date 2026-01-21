package com.dpp.ddp_study_management.common.dto;

import lombok.Data;

@Data
public class PaginationInfo {
    private int page;
    private int limit;
    private int total;
    private int totalPages;
}