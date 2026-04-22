package com.rbac.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecureDataResponse {

    private String message;
    private String accessedBy;
    private long timestamp;
    private String note;

}