package com.bug.catcher.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "daily_region_mosquito_index")
public class DailyRegionMosquitoIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_region_mosquito_index_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "index_date", nullable = false)
    private LocalDate indexDate;

    @Column(name = "mosquito_index", nullable = false)
    private Double mosquitoIndex;
}
