package com.bug.catcher.domain.map.controller;

import com.bug.catcher.domain.map.dto.MosquitoRegionSummaryResponse;
import com.bug.catcher.domain.map.service.MosquitoMapQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Mosquito Status", description = "모기 지수 및 지역 상세 조회 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/mosquito")
public class MosquitoIndexController {

  private final MosquitoMapQueryService mosquitoMapQueryService;

  @GetMapping("/summary/{regionId}")
  public ResponseEntity<MosquitoRegionSummaryResponse> getRegionSummary(@PathVariable Long regionId) {
    return ResponseEntity.ok(mosquitoMapQueryService.getRegionSummary(regionId));
  }
}
