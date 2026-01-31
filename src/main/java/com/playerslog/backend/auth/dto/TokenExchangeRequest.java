package com.playerslog.backend.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TokenExchangeRequest {
    private String code;
}
