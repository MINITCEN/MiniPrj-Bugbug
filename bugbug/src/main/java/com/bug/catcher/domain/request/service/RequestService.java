package com.bug.catcher.domain.request.service;

import com.bug.catcher.domain.chat.repository.ChatRoomRepository;
import com.bug.catcher.domain.entity.ChatRoom;
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
import com.bug.catcher.domain.hunter.repository.ApplicationRepository;
import com.bug.catcher.domain.hunter.service.HunterService;
import com.bug.catcher.global.file.FileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final RequestImageRepository requestImageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final FileStore fileStore;
    private final ApplicationRepository applicationRepository;
    private final HunterService hunterService;

    // create
    @Transactional
    public void createRequest(Long loginUserId, RequestFormDto form) {
        User loginUser = userRepository.findById(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("로그인 사용자를 찾을 수 없습니다."));

        List<String> imageUrls = fileStore.storeImages(form.getImageFiles());
        String videoUrl = fileStore.storeVideo(form.getVideoFile());

        String status = form.getStatus() == null || form.getStatus().isBlank()
                ? "대기 중"
                : form.getStatus();

        Request request = Request.builder()
                .user(loginUser)
                .status(status)
                .approxLocation(form.getLocation())
                .exactLocation(form.getDetailLocation())
                .title(form.getTitle())
                .content(form.getContent())
                .occurrenceTime(form.getOccurrenceTime())
                .description(form.getDescription())
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

    // read list for paging
    @Transactional(readOnly = true)
    public Page<Map<String, Object>> readRequestPage(String status, Pageable pageable) {
        Page<Request> requestPage = (status == null || status.isBlank())
                ? requestRepository.findAll(pageable)
                : requestRepository.findByStatus(status, pageable);
        return requestPage
                .map(request -> {
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
                });
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
    public void updateRequest(Long requestId, Long loginUserId, RequestFormDto form, RequestMediaFileUrlDto mediaUrlDto) {
        Request request = requestRepository.findByIdAndUser_Id(requestId, loginUserId)

                .orElseThrow(() -> new AccessDeniedException("게시글이 없거나 수정 권한이 없습니다."));

        String beforeStatus = request.getStatus();

        // 1. 사용자가 삭제한 기존 미디어 먼저 제거
        deleteMedia(requestId, loginUserId, mediaUrlDto);

        // 2. 새 파일 저장
        List<String> newImageUrls = fileStore.storeImages(form.getImageFiles());

        String newVideoUrl = null;
        if (form.getVideoFile() != null && !form.getVideoFile().isEmpty()) {
            String dbVideoUrl = requestRepository.findVideoUrlByRequestIdAndUserId(requestId, loginUserId);

            if (dbVideoUrl != null && !dbVideoUrl.isBlank()) {
                throw new IllegalStateException("기존 영상을 삭제한 후 새 영상을 등록해 주세요.");
            }

            newVideoUrl = fileStore.storeVideo(form.getVideoFile());
        }

        request.updateDetails(
                form.getTitle(),
                form.getContent(),
                form.getLocation(),
                form.getDetailLocation(),
                form.getOccurrenceTime(),
                form.getDescription(),
                form.getStatus()
        );
        updateCompletedHunter(request, form);

        updateMatchedHunterGradeIfCompletionChanged(requestId, beforeStatus, form.getStatus());


        // 5. 새 이미지 URL DB 저장
        saveImages(request, newImageUrls);

        // 6. 새 동영상 URL DB 저장
        if (newVideoUrl != null && !newVideoUrl.isBlank()) {
            requestRepository.updateVideoUrl(requestId, loginUserId, newVideoUrl);
        }
    }

    private void updateMatchedHunterGradeIfCompletionChanged(Long requestId, String beforeStatus, String afterStatus) {
        if (isCompleted(beforeStatus) == isCompleted(afterStatus)) {
            return;
        }

        applicationRepository.findByRequestId(requestId)
                .forEach(application -> hunterService.updateHunterLevel(application.getHunter().getId()));
    }

    private boolean isCompleted(String status) {
        return "완료".equals(status);
    }

    // delete request
    @Transactional
    public void deleteRequest(Long requestId, Long loginUserId) {
        Request request = requestRepository.findByIdAndUser_Id(requestId, loginUserId)
                .orElseThrow(() -> new AccessDeniedException("게시글이 없거나 삭제 권한이 없습니다."));

        List<String> imageUrls = request.getRequestImages()
                .stream()
                .map(RequestImage::getImageUrl)
                .toList();
        String videoUrl = request.getVideoUrl();

        requestRepository.delete(request);

        imageUrls.forEach(fileStore::deleteImageByUrl);
        fileStore.deleteVideoByUrl(videoUrl);
    }

    // delete media
    @Transactional
    public void deleteMedia(Long requestId, Long loginUserId, RequestMediaFileUrlDto dto) {
        if (dto == null) {
            return;
        }

        Request request = requestRepository.findByIdAndUser_Id(requestId, loginUserId)
                .orElseThrow(() -> new AccessDeniedException("게시글이 없거나 삭제 권한이 없습니다."));

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
            throw new IllegalStateException("동영상 삭제 처리 중 게시글 상태가 변경되었습니다.");
        }
        fileStore.deleteVideoByUrl(videoUrl);
    }

    @Transactional(readOnly = true)
    public RequestEditFormDto getEditForm(Long requestId, Long loginUserId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("의뢰글을 찾을 수 없습니다."));

        if (!request.getUser().getId().equals(loginUserId)) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }
        RequestFormDto form = new RequestFormDto();

        form.setTitle(request.getTitle());
        form.setContent(request.getContent());
        form.setStatus(request.getStatus());
        form.setCompletedHunterId(request.getCompletedHunter() != null ? request.getCompletedHunter().getId() : null);
        form.setLocation(request.getApproxLocation());
        form.setDetailLocation(request.getExactLocation());
        form.setOccurrenceTime(request.getOccurrenceTime());
        form.setDescription(request.getDescription());

        RequestMediaFileUrlDto mediaUrl = new RequestMediaFileUrlDto();

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

    private void updateCompletedHunter(Request request, RequestFormDto form) {
        // 완료 상태가 아니면 이전에 저장된 완료 헌터 정보를 제거한다.
        if (!"완료".equals(form.getStatus())) {
            request.updateCompletedHunter(null);
            return;
        }

        List<ChatRoom> reservedRooms = chatRoomRepository
                .findByRequestIdAndReservedAtIsNotNullOrderByReservedAtDesc(request.getId());

        // 완료 처리는 채팅방에서 예약 수락된 헌터가 있을 때만 가능하다.
        if (reservedRooms.isEmpty()) {
            throw new IllegalStateException("예약 완료된 헌터가 없어 의뢰를 완료 처리할 수 없습니다.");
        }

        ChatRoom reservedRoom = reservedRooms.get(0);
        if (reservedRoom.getHunter() == null) {
            throw new IllegalStateException("예약 완료된 채팅방에 헌터 정보가 없습니다.");
        }

        // 사용자가 헌터를 고르지 않고, 예약 완료 채팅방의 헌터를 완료 헌터로 자동 저장한다.
        request.completeBy(reservedRoom.getHunter());
    }

}