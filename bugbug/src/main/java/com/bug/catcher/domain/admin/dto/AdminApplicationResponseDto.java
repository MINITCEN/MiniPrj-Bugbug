package com.bug.catcher.domain.admin.dto;

import com.bug.catcher.domain.entity.HunterApplication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminApplicationResponseDto {
    private Long id;
    private Long userId;
    private String email;
    private String name;
    private Boolean pledgeAgreed;
    private String status;
    private LocalDateTime createdAt;

    public static AdminApplicationResponseDto from(HunterApplication app) {
        return AdminApplicationResponseDto.builder()
                .id(app.getId())
                .userId(app.getUser().getId())
                .email(app.getUser().getEmail())
                .name(app.getName())
                .pledgeAgreed(app.getPledgeAgreed())
                .status(app.getStatus().name())
                .createdAt(app.getCreatedAt())
                .build();
    }
}
