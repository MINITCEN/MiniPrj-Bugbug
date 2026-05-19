package com.bug.catcher.domain.request.service;

import com.bug.catcher.domain.entity.Request;
import com.bug.catcher.domain.entity.RequestImage;
import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.request.dto.RequestDetailResponseDto;
import com.bug.catcher.domain.request.dto.RequestEditFormDto;
import com.bug.catcher.domain.request.dto.RequestFormDto;
import com.bug.catcher.domain.request.dto.RequestMediaFileUrlDto;
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

@RequiredArgsConstructor
@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final RequestImageRepository requestImageRepository;
    private final FileStore fileStore;

    // create
    @Transactional
    public void createRequest(Long loginUserId, RequestFormDto form) {
        User loginUser = userRepository.findById(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("로그인 사용자를 찾을 수 없습니다."));

        List<String> imageUrls = fileStore.storeImages(form.getImageFiles());
        String videoUrl = fileStore.storeVideo(form.getVideoFile());

        String replacedContent = replaceMediaSrc(form.getContent(), imageUrls, videoUrl);
        Request request = Request.builder()
                .user(loginUser)
                .status(form.getStatus())
                .approxLocation(form.getLocation())
                .exactLocation(form.getDetailLocation())
                .title(form.getTitle())
                .content(replacedContent)
                .occurrenceTime(form.getOccurrenceTime())
                .description(buildDescription(form))
                .videoUrl(videoUrl)
                .viewCount(0)
                .build();

        Request savedRequest = requestRepository.save(request);

        saveImages(savedRequest, imageUrls);
    }

    // read list
    @Transactional(readOnly = true)
    public List<Map<String, Object>> readRequestList() {
        List<Request> requests = requestRepository.findAll();

        return requests.stream().map(request -> {
            Map<String, Object> result = new LinkedHashMap<>();

            result.put("requestId", request.getId());
            result.put("status", request.getStatus());
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

    // detail
    @Transactional
    public RequestDetailResponseDto readRequestDetail(Long requestId) {
        int updatedCount = requestRepository.increaseViewCount(requestId);

        if (updatedCount == 0) {
            throw new IllegalArgumentException("해당 의뢰를 찾을 수 없습니다.");
        }

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("해당 의뢰를 찾을 수 없습니다."));

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

        // 2. 새 이미지 / 새 동영상 추가를 위해 게시글 조회
        Request request = requestRepository.findByIdAndUser_Id(requestId, loginUserId)
                .orElseThrow(() -> new IllegalStateException("게시글이 없거나 수정 권한이 없습니다."));

        // 3. 새 이미지가 있으면 추가
        addNewImages(request, form);

        // 4. 새 동영상이 있으면 추가
        addNewVideo(requestId, loginUserId, form);
    }

    // delete request
    @Transactional
    public void deleteRequest(Long requestId, Long loginUserId) {
        Request request = requestRepository.findByIdAndUser_Id(requestId, loginUserId)
                .orElseThrow(() -> new IllegalStateException("게시글이 없거나 삭제 권한이 없습니다."));

        requestRepository.delete(request);
    }

    // delete media
    @Transactional
    public void deleteMedia(Long requestId, Long loginUserId, RequestMediaFileUrlDto dto) {
        Request request = requestRepository.findByIdAndUser_Id(requestId, loginUserId)
                .orElseThrow(() -> new IllegalStateException("게시글이 없거나 삭제 권한이 없습니다."));

        deleteImages(requestId, dto.getImageUrls());

        deleteVideo(requestId, loginUserId, request, dto.getVideoUrl());
    }

    private void saveImages(Request request, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        for (String imageUrl : imageUrls) {
            RequestImage requestImage = RequestImage.builder()
                    .request(request)
                    .imageUrl(imageUrl)
                    .build();

            requestImageRepository.save(requestImage);
        }
    }

    private void addNewImages(Request request, RequestFormDto form) {
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

        saveImages(request, newImageUrls);
    }

    private void addNewVideo(Long requestId, Long loginUserId, RequestFormDto form) {
        MultipartFile videoFile = form.getVideoFile();

        if (videoFile == null || videoFile.isEmpty()) {
            return;
        }

        String dbVideoUrl = requestRepository.findVideoUrlByRequestIdAndUserId(requestId, loginUserId);

        if (dbVideoUrl != null && !dbVideoUrl.isBlank()) {
            throw new IllegalStateException("기존 영상을 삭제한 후 새 영상을 등록해 주세요.");
        }

        String newVideoUrl = fileStore.storeVideo(videoFile);

        int updatedCount = requestRepository.updateVideoUrl(requestId, loginUserId, newVideoUrl);

        if (updatedCount == 0) {
            throw new IllegalStateException("게시글이 없거나 수정 권한이 없습니다.");
        }
    }

    private void deleteImages(Long requestId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        requestImageRepository.deleteByRequestIdAndImageUrls(requestId, imageUrls);

        for (String imageUrl : imageUrls) {
            fileStore.deleteImageByUrl(imageUrl);
        }
    }

    private void deleteVideo(
            Long requestId,
            Long loginUserId,
            Request request,
            String videoUrl
    ) {
        if (videoUrl == null || videoUrl.isBlank()) {
            return;
        }

        String dbVideoUrl = request.getVideoUrl();

        if (dbVideoUrl == null || dbVideoUrl.isBlank()) {
            return;
        }

        if (!videoUrl.equals(dbVideoUrl)) {
            throw new IllegalStateException("삭제 요청한 동영상이 현재 게시글의 동영상과 일치하지 않습니다.");
        }

        int updatedCount = requestRepository.updateVideoUrl(requestId, loginUserId, null);

        if (updatedCount == 0) {
            throw new IllegalStateException("게시글이 없거나 삭제 권한이 없습니다.");
        }
        fileStore.deleteVideoByUrl(videoUrl);
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

    // content 경로 교체 메소드
    private String replaceMediaSrc(String content, List<String> imageUrls, String videoUrl) {
        if (content == null || content.isBlank()) {
            return content;
        }

        String replaced = content;

        if (imageUrls != null) {
            for (String imageUrl : imageUrls) {
                replaced = replaced.replaceFirst(
                        "<img([^>]*)src=\"[^\"]*\"([^>]*)>",
                        "<img$1src=\"" + imageUrl + "\"$2>"
                );
            }
        }

        if (videoUrl != null && !videoUrl.isBlank()) {
            replaced = replaced.replaceFirst(
                    "<video([^>]*)src=\"[^\"]*\"([^>]*)>",
                    "<video$1src=\"" + videoUrl + "\"$2>"
            );
        }

        return replaced;
    }


    @Transactional(readOnly = true)
    public RequestEditFormDto getEditForm(Long requestId, Long loginUserId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("의뢰글을 찾을 수 없습니다."));

        if (!request.getUser().getId().equals(loginUserId)) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }

        RequestFormDto form = new RequestFormDto();

        form.setTitle(request.getTitle());
        form.setContent(request.getContent());
        form.setStatus(request.getStatus());
        form.setLocation(request.getApproxLocation());
        form.setDetailLocation(request.getExactLocation());
        form.setOccurrenceTime(request.getOccurrenceTime());
        form.setAdditionalDescription(request.getDescription());

        RequestMediaFileUrlDto mediaUrl = new RequestMediaFileUrlDto();
//        form.setLatitude(request.getLatitude());
//        form.setLongitude(request.getLongitude());  나중에 추가할지도..?

        mediaUrl.setVideoUrl(request.getVideoUrl());

        List<String> imageUrls = request.getRequestImages()
                .stream()
                .map(requestImage -> requestImage.getImageUrl())
                .toList();

        mediaUrl.setImageUrls(imageUrls);

        RequestEditFormDto editForm = new RequestEditFormDto();
        editForm.setForm(form);
        editForm.setMediaUrl(mediaUrl);

        return editForm;
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }
}