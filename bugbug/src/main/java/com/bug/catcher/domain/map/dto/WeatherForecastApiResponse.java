package com.bug.catcher.domain.map.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherForecastApiResponse {

  @JsonProperty("response")
  private Response response;

  @Getter
  @Setter
  public static class Response {

    @JsonProperty("header")
    private Header header;

    @JsonProperty("body")
    private Body body;
  }

  @Getter
  @Setter
  public static class Header {

    @JsonProperty("resultCode")
    private String resultCode;

    @JsonProperty("resultMsg")
    private String resultMsg;
  }

  @Getter
  @Setter
  public static class Body {

    @JsonProperty("dataType")
    private String dataType;

    @JsonProperty("items")
    private Items items;

    @JsonProperty("pageNo")
    private Integer pageNo;

    @JsonProperty("numOfRows")
    private Integer numOfRows;

    @JsonProperty("totalCount")
    private Integer totalCount;
  }

  @Getter
  @Setter
  public static class Items {

    @JsonProperty("item")
    private List<ForecastItem> item = new ArrayList<>();
  }

  @Getter
  @Setter
  public static class ForecastItem {

    @JsonProperty("baseDate")
    private String baseDate;

    @JsonProperty("baseTime")
    private String baseTime;

    @JsonProperty("category")
    private String category;

    @JsonProperty("fcstDate")
    private String forecastDate;

    @JsonProperty("fcstTime")
    private String forecastTime;

    @JsonProperty("fcstValue")
    private String forecastValue;

    @JsonProperty("nx")
    private Integer nx;

    @JsonProperty("ny")
    private Integer ny;
  }
}
