package com.fivontwov.dto;

import lombok.Data;

@Data
public class VoteRequest {
    private Long userId;
    //-1 or 1
    private Integer value;
}
