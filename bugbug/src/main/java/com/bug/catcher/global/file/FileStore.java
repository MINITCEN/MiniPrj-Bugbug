package com.bug.catcher.global.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Component
public class FileStore {

    @Value("${file.image-upload-dir:C:/bugbug/uploads/request-images/}")
    private String imageUploadDir;

    @Value("${file.video-upload-dir:C:/bugbug/uploads/request-videos/}")
    private String videoUploadDir;
    /**
     * 이미지 파일 저장
     */
    public String storeImage(MultipartFile file) {
        return storeFile(file, imageUploadDir, "/uploads/request-images/", "이미지 파일 저장 실패");
    }
    /**
     * 비디오 파일 저장
     */
    public String storeVideo(MultipartFile file) {
        return storeFile(file, videoUploadDir, "/uploads/request-videos/", "동영상 파일 저장 실패");
    }
    /**
     * 실제 파일 저장 공통 로직
     */
    private String storeFile(
            MultipartFile file,
            String uploadDir,
            String urlPrefix,
            String errorMessage
    ) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            Files.createDirectories(Paths.get(uploadDir));

            String originalFilename = file.getOriginalFilename();
            String storeFilename = UUID.randomUUID() + "_" + originalFilename;

            Path savePath = Paths.get(uploadDir, storeFilename);

            Files.copy(
                    file.getInputStream(),
                    savePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return urlPrefix + storeFilename;

        } catch (IOException e) {
            throw new RuntimeException(errorMessage, e);
        }
    }
}