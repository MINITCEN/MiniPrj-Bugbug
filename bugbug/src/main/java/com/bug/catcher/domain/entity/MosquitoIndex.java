package com.bug.catcher.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "mosquito_index")
public class MosquitoIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "index_id")
    private Long id;

    private Float temperature;
    private Float humidity;

    // JSON 타입은 사용하는 DB에 따라 String으로 맵핑 후 DB Column 타입을 지정해줍니다.
    @Column(columnDefinition = "json")
    private String mapapiData;

    @Column(columnDefinition = "json")
    private String mqapiData;

    private LocalDateTime recordedAt;
}