package com.bug.catcher.domain.map.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MosquitoApiResponse {
  @JsonProperty("MosquitoStatus")
  private MosquitoStatus mosquitoStatus;

  @Getter
  @Setter
  public static class MosquitoStatus{
    @JsonProperty("list_total_count")
    private int listTotalCount;

    @JsonProperty("RESULT")
    private ApiResult result;

    @JsonProperty("row")
    private List<MosquitoData> list = new ArrayList<>();
  }

  @Getter
  @Setter
  public static class MosquitoData{
    @JsonProperty("MOSQUITO_DATE")
    private String mosquitoDate;
    @JsonProperty("MOSQUITO_VALUE_WATER")
    private Double waterValue;
    @JsonProperty("MOSQUITO_VALUE_HOUSE")
    private Double houseValue;
    @JsonProperty("MOSQUITO_VALUE_PARK")
    private Double parkValue;
  }

  @Getter @Setter
  public static class ApiResult {
    @JsonProperty("CODE")
    private String code;    // 예: INFO-000, INFO-200
    @JsonProperty("MESSAGE")
    private String message; // 예: 정상 처리되었습니다.
  }
}
