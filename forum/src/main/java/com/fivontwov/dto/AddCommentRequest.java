package com.fivontwov.dto;

import lombok.Data;

@Data
public class AddCommentRequest {
    private Long userId;
    private Long parentCommentId;
    private String body;
}
