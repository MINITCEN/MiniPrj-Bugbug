package com.bug.catcher.domain.request.service;

import com.bug.catcher.domain.entity.Request;
import com.bug.catcher.domain.request.dto.RequestFormDto;
import com.bug.catcher.domain.request.repository.RequestImageRepository;
import com.bug.catcher.domain.request.repository.RequestRepository;
import com.bug.catcher.global.file.FileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class RequestService {

    private final RequestRepository requestRepository;
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
    public void createRequest(RequestFormDto form) {
        Request request = Request.builder()
                .status("WAITING")
                .approxLocation(form.getLocation())
                .exactLocation(form.getDetailLocation())
                .title(form.getTitle())
                .content(form.getContent())
                .occurrenceTime(form.getOccurrenceTime())
                .description(buildDescription(form))
                .viewCount(0)
                .build();
        requestRepository.save(request);
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
    public Request readRequestDetail(Long requestId) {
        int updatedCount = requestRepository.increaseViewCount(requestId);
        if (updatedCount == 0) {
            throw new IllegalArgumentException("해당 의뢰를 찾을 수 없습니다.");
        }
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("해당 의뢰를 찾을 수 없습니다."));
    }

    // update
    @Transactional
    public void updateRequest(Long requestId, Long loginUserId, RequestFormDto form) {
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
    }

    //delete
    //단, requestImage가 있을 때는 주의 필요(참조키 제약 조건 상 에러가 날 수 있음)
    @Transactional
    public void deleteRequest(Long requestId, Long loginUserId) {
        requestRepository.findById(requestId).orElseThrow(() -> new IllegalArgumentException("해당 의뢰를 찾을 수 없습니다."));
        requestRepository.delete(requestId, loginUserId);
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
}