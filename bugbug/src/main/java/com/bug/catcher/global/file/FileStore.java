package com.bug.catcher.global.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FileStore {

    @Value("${file.image-upload-dir:C:/bugbug/uploads/request-images/}")
    private String imageUploadDir;

    @Value("${file.video-upload-dir:C:/bugbug/uploads/request-videos/}")
    private String videoUploadDir;

    /**
     * 이미지 파일 여러 개 저장
     * - 이미지가 없으면 빈 리스트 반환
     */
    public List<String> storeImages(List<MultipartFile> imageFiles) {
        List<String> imageUrls = new ArrayList<>();
        if (imageFiles == null || imageFiles.isEmpty()) {
            return imageUrls;
        }
        for (MultipartFile imageFile : imageFiles) {
            if (imageFile == null || imageFile.isEmpty()) {
                continue;
            }
            String imageUrl = storeFile(imageFile, imageUploadDir, "/uploads/request-images/", "이미지 파일 저장 실패");
            imageUrls.add(imageUrl);
        }
        return imageUrls;
    }

    /**
     * 동영상 파일 1개 저장
     * - 동영상이 없으면 null 반환
     */
    public String storeVideo(MultipartFile videoFile) {
        if (videoFile == null || videoFile.isEmpty()) {
            return null;
        }
        return storeFile(videoFile, videoUploadDir, "/uploads/request-videos/", "동영상 파일 저장 실패");
    }

    /**
     * 실제 파일 1개 저장 공통 로직
     */
    private String storeFile(MultipartFile file, String uploadDir, String urlPrefix, String errorMessage) {
        try {
            Files.createDirectories(Paths.get(uploadDir));

            String originalFilename = file.getOriginalFilename();
            String storeFilename = UUID.randomUUID() + "_" + originalFilename;

            Path savePath = Paths.get(uploadDir, storeFilename);
            Files.copy(file.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);

            return urlPrefix + storeFilename;
        } catch (IOException e) {
            throw new RuntimeException(errorMessage, e);
        }
    }
}