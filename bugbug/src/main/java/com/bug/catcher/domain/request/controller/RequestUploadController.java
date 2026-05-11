package com.bug.catcher.domain.request.controller;

import com.bug.catcher.global.file.FileStore;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/request/upload")
public class RequestUploadController {
    private final FileStore fileStore;
    public RequestUploadController(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    //이미지 등록 컨트롤러 메소드
    @PostMapping("/images")
    public Map<String, Object> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        List<String> imageUrls = fileStore.storeImages(files);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("urls", imageUrls);
        return result;
    }

    //비디오 등록 컨트롤러 메소드
    @PostMapping("/video")
    public Map<String, Object> uploadVideo(@RequestParam("file") MultipartFile file) {
        String videoUrl = fileStore.storeVideo(file);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("url", videoUrl);
        return result;
    }
}