package com.playerslog.backend.goll.dto;

import com.playerslog.backend.goll.domain.GollStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PatchGollRequest {
    private GollStatus status;
}
