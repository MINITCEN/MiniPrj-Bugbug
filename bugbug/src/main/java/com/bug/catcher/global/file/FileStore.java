package com.bug.catcher.global.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Component
public class FileStore {

    @Value("${file.upload-dir:C:/bugbug/uploads/request-images/}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            Files.createDirectories(Paths.get(uploadDir));

            String originalFilename = file.getOriginalFilename();
            String storeFilename = UUID.randomUUID() + "_" + originalFilename;

            Path savePath = Paths.get(uploadDir, storeFilename);
            Files.copy(file.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/request-images/" + storeFilename;

        } catch (IOException e) {
            throw new RuntimeException("이미지 파일 저장 실패", e);
        }
    }
}