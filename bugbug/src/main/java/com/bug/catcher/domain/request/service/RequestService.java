package com.bug.catcher.domain.request.service;

import com.bug.catcher.domain.entity.Request;
import com.bug.catcher.domain.entity.RequestImage;
import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.request.dto.RequestDetailResponseDto;
import com.bug.catcher.domain.request.dto.RequestFormDto;
import com.bug.catcher.domain.request.repository.RequestImageRepository;
import com.bug.catcher.domain.request.repository.RequestRepository;
import com.bug.catcher.domain.user.repository.UserRepository;
import com.bug.catcher.global.file.FileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final RequestImageRepository requestImageRepository;
    private final FileStore fileStore;

//    @Transactional(readOnly = true)
//    public Page<Request> findRequestPage(int page) {
//        return requestRepository.findAll(
//                PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "createdAt"))
//        );
//    }

    //여기서부터 Request Talend 테스트
    //create
    @Transactional
    public void createRequest(Long loginUserId, RequestFormDto form) {
        User loginUser = userRepository.findById(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("로그인 사용자를 찾을 수 없습니다."));
        List<String> imageUrls = fileStore.storeImages(form.getImageFiles());
        String videoUrl = fileStore.storeVideo(form.getVideoFile());
        Request request = Request.builder()
                .user(loginUser)
                .status("WAITING")
                .approxLocation(form.getLocation())
                .exactLocation(form.getDetailLocation())
                .title(form.getTitle())
                .content(form.getContent())
                .occurrenceTime(form.getOccurrenceTime())
                .description(buildDescription(form))
                .videoUrl(videoUrl)
                .viewCount(0)
                .build();
        Request savedRequest = requestRepository.save(request);
        for (String imageUrl : imageUrls) {
            RequestImage requestImage = RequestImage.builder()
                    .request(savedRequest)
                    .imageUrl(imageUrl)
                    .build();

            requestImageRepository.save(requestImage);
        }
    }

    //read
    @Transactional(readOnly = true)
    public List<Map<String, Object>> readRequestList() {
        List<Request> requests = requestRepository.findAll();
        return requests.stream().map(request -> {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("requestId", request.getId());
            result.put("title", request.getTitle());
            result.put("content", request.getContent());
            result.put("approxLocation", request.getApproxLocation());
            result.put("exactLocation", request.getExactLocation());
            result.put("occurrenceTime", request.getOccurrenceTime());
            result.put("createdAt", request.getCreatedAt());
            result.put("description", request.getDescription());
            result.put("viewCount", request.getViewCount());
            return result;
        }).toList();
    }

    //상세보기
    //조회 수 증가
    @Transactional
    public RequestDetailResponseDto readRequestDetail(Long requestId) {
        int updatedCount = requestRepository.increaseViewCount(requestId);
        if (updatedCount == 0) {
            throw new IllegalArgumentException("해당 의뢰를 찾을 수 없습니다.");
        }
        Request request = requestRepository.findById(requestId)
                .orElseThrow(()->new IllegalArgumentException("해당 의뢰를 찾을 수 없습니다."));
        return RequestDetailResponseDto.responseDto(request);
    }

    // update
    @Transactional
    public void updateRequest(Long requestId, Long loginUserId, RequestFormDto form) {

        // 1. 게시글 기본 정보 수정 + 권한 검증
        int updatedCount = requestRepository.update(
                requestId,
                loginUserId,
                form.getTitle(),
                form.getContent(),
                form.getLocation(),
                form.getDetailLocation(),
                form.getOccurrenceTime()
        );
        if (updatedCount == 0) {
            throw new IllegalStateException("게시글이 없거나 수정 권한이 없습니다.");
        }
        // 2. 게시글 참조 객체 조회
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("게시글이 존재하지 않습니다."));
        // 3. 이미지 수정
        updateImages(request, form);
        // 4. 비디오 수정
        updateVideo(requestId, loginUserId, form);
    }

    //delete
    //단, requestImage가 있을 때는 주의 필요(참조키 제약 조건 상 에러가 날 수 있음)
    @Transactional
    public void deleteRequest(Long requestId, Long loginUserId) {
        Request request = requestRepository.findByIdAndUser_Id(requestId, loginUserId)
                .orElseThrow(() -> new IllegalStateException("게시글이 없거나 삭제 권한이 없습니다."));
        requestRepository.delete(request);
    }


    private String buildDescription(RequestFormDto form) {
        return """
                발생 위치: %s
                추가 설명: %s
                """.formatted(
                nullToBlank(form.getOccurrencePlace()),
                nullToBlank(form.getAdditionalDescription())
        );
    }
    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }


    // 이미지 업데이트 로직
    private void updateImages(Request request, RequestFormDto form) {
        Long requestId = request.getId();

        // 1. DB에 저장된 기존 이미지 URL 목록
        List<String> dbImageUrls = requestImageRepository.findByRequest_Id(requestId)
                .stream()
                .map(RequestImage::getImageUrl)
                .toList();

        // 2. 수정 화면에서 현재 남아 있는 기존 이미지 URL 목록
        // 람다식에서 사용할 변수이므로 재할당하지 않도록 한 번에 초기화
        List<String> formImageUrls =
                form.getImageUrls() == null ? List.of() : form.getImageUrls();

        // 3. DB에는 있는데 폼에는 없는 기존 이미지 URL 삭제
        List<String> deleteImageUrls = dbImageUrls.stream()
                .filter(dbUrl -> !formImageUrls.contains(dbUrl))
                .toList();

        if (!deleteImageUrls.isEmpty()) {
            requestImageRepository.deleteByRequestIdAndImageUrls(requestId, deleteImageUrls);
        }

        // 4. 이번 수정에서 새로 첨부된 파일은 전부 새 이미지로 저장
        if (form.getImageFiles() == null || form.getImageFiles().isEmpty()) {
            return;
        }

        List<MultipartFile> validImageFiles = form.getImageFiles().stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();

        if (validImageFiles.isEmpty()) {
            return;
        }
        List<String> newImageUrls = fileStore.storeImages(validImageFiles);

        for (String imageUrl : newImageUrls) {
            RequestImage requestImage = RequestImage.builder()
                    .request(request)
                    .imageUrl(imageUrl)
                    .build();

            requestImageRepository.save(requestImage);
        }
    }

    // 동영상 업데이트 로직
    private void updateVideo(Long requestId, Long loginUserId, RequestFormDto form) {

        String dbVUrl = requestRepository.findVideoUrlByRequestIdAndUserId(requestId, loginUserId);

        String formVUrl = form.getVideoUrl();
        MultipartFile videoFile = form.getVideoFile();

        String finalVideoUrl = dbVUrl;

        // 1. DB에는 기존 비디오가 있는데 폼에는 없으면 삭제된 것으로 판단
        if (dbVUrl != null && !dbVUrl.isBlank()
                && (formVUrl == null || formVUrl.isBlank())) {
            finalVideoUrl = null;
        }

        // 2. 새 비디오 파일이 있는 경우
        if (videoFile != null && !videoFile.isEmpty()) {
            // 기존 비디오가 아직 유지 중이면 새 등록 차단
            if (finalVideoUrl != null && !finalVideoUrl.isBlank()) {
                throw new IllegalStateException("기존 영상을 삭제한 후 새 영상을 등록해 주세요.");
            }

            // 새 비디오 저장
            finalVideoUrl = fileStore.storeVideo(videoFile);
        }

        // 3. 최종 videoUrl이 기존 DB 값과 다를 때만 update
        if (!Objects.equals(dbVUrl, finalVideoUrl)) {
            int updatedCount = requestRepository.updateVideoUrl(requestId, loginUserId, finalVideoUrl);
            if (updatedCount == 0) {
                throw new IllegalStateException("게시글이 없거나 수정 권한이 없습니다.");
            }
        }
    }
}