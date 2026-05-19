package com.bug.catcher.domain.request.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestEditFormDto {
    private RequestFormDto form;
    private RequestMediaFileUrlDto mediaUrl;
}
