package com.bug.catcher.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "saved_hunters")
public class SavedHunter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "save_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hunter_id")
    private Hunter hunter;
}