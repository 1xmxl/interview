package com.interview.user.domain.vo;

import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    private String username;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String email;
}
