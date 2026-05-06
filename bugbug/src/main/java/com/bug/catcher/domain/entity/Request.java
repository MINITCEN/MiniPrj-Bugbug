package com.bug.catcher.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "requests")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String status;
    private LocalDateTime createdAt;
    private String approxLocation;
    private String exactLocation;
    private String title;
    private String content;
    private LocalDateTime occurrenceTime;

    @Column(columnDefinition = "text")
    private String description;

    private Integer viewCount;
}