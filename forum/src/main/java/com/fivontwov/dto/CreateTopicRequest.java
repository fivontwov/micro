package com.fivontwov.dto;

import lombok.Data;
@Data
public class CreateTopicRequest {
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    private Long userId;
    private String title;
    private String body;
}
