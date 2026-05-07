package com.bug.catcher.global.infra;

import com.bug.catcher.domain.entity.ChatMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    // 로컬 저장소 경로 (프로젝트 최상단 uploads 폴더)
    private final String uploadDir = "uploads/";

    public String storeFile(MultipartFile file, ChatMessage.MessageType messageType) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("잘못된 파일입니다.");
        }

        // 파일 확장자 추출 (소문자로 변환)
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

        // 카테고리(MessageType)와 실제 파일 확장자가 맞는지 깐깐하게 검증
        if (messageType == ChatMessage.MessageType.IMAGE) {
            if (!extension.matches("\\.(jpg|jpeg|png|gif|webp|svg)$")) {
                throw new IllegalArgumentException("사진 카테고리에는 사진 파일(.jpg, .png 등)만 업로드 가능합니다.");
            }
        } else if (messageType == ChatMessage.MessageType.VIDEO) {
            if (!extension.matches("\\.(mp4|webm|ogg|mov|avi|wmv)$")) {
                throw new IllegalArgumentException("동영상 카테고리에는 영상 파일(.mp4 등)만 업로드 가능합니다.");
            }
        } else if (messageType == ChatMessage.MessageType.AUDIO) {
            if (!extension.matches("\\.(mp3|wav|ogg|m4a)$")) {
                throw new IllegalArgumentException("음성 카테고리에는 음성 파일(.mp3, .wav 등)만 업로드 가능합니다.");
            }
        }

        // uploads 폴더가 없으면 생성
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 파일 이름 중복을 막기 위해 UUID 사용
        String savedFilename = UUID.randomUUID().toString() + extension;

        // 파일 저장
        Path path = Paths.get(uploadDir + savedFilename);
        Files.write(path, file.getBytes());

        // 클라이언트가 접근할 수 있는 URL 경로 반환 (WebConfig에서 매핑됨)
        return "/uploads/" + savedFilename;
    }
}
