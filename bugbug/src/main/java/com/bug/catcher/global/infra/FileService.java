package com.bug.catcher.global.infra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload.dir:C:/bugbug-uploads/}")
    private String uploadDir;

    /**
     * 클라이언트로부터 받은 파일을 로컬에 저장하고, 접근 가능한 URL을 반환합니다.
     */
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // 1. 저장할 디렉토리가 없으면 자동으로 생성
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 2. 원본 파일명에서 확장자 추출 (예: image.jpg -> .jpg)
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = "";
            if (originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 3. 파일 이름 충돌을 막기 위해 UUID로 고유한 이름 생성
            String newFileName = UUID.randomUUID().toString() + extension;

            // 4. 실제 하드디스크에 파일 복사(저장)
            Path targetLocation = Paths.get(uploadDir).resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 5. 프론트엔드에서 이 파일을 볼 수 있는 URL(가상 경로) 반환
            // WebConfig에서 매핑한 /uploads/ 경로와 합쳐줍니다.
            return "/uploads/" + newFileName;

        } catch (IOException ex) {
            // 파일 저장 실패 시 예외 처리 (실전에서는 Custom Exception을 던지는 것이 좋습니다)
            throw new RuntimeException("파일 저장에 실패했습니다. " + ex.getMessage());
        }
    }
}
