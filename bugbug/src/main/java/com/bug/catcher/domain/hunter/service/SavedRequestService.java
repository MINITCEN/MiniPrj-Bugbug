package com.bug.catcher.domain.hunter.service;

import com.bug.catcher.domain.entity.Hunter;
import com.bug.catcher.domain.entity.Request;
import com.bug.catcher.domain.entity.SavedRequest;
import com.bug.catcher.domain.hunter.repository.HunterRepository;
import com.bug.catcher.domain.hunter.repository.SavedRequestRepository;
import com.bug.catcher.domain.request.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavedRequestService {
    private final SavedRequestRepository savedRequestRepository;
    private final HunterRepository hunterRepository;
    private final RequestRepository requestRepository;

    @Transactional
    public boolean toggleSavedRequest(Long userId, Long requestId) {
        Hunter hunter = hunterRepository.findTopByUserIdOrderByIdDesc(userId)
                .orElseThrow(() -> new AccessDeniedException("헌터만 의뢰를 찜할 수 있습니다."));

        if (savedRequestRepository.existsByHunterIdAndRequestId(hunter.getId(), requestId)) {
            savedRequestRepository.deleteByHunterIdAndRequestId(hunter.getId(), requestId);
            return false;
        }

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("의뢰글을 찾을 수 없습니다."));

        SavedRequest savedRequest = SavedRequest.builder()
                .hunter(hunter)
                .request(request)
                .build();

        savedRequestRepository.save(savedRequest);
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isSavedRequest(Long userId, Long requestId) {
        return hunterRepository.findTopByUserIdOrderByIdDesc(userId)
                .map(hunter -> savedRequestRepository.existsByHunterIdAndRequestId(hunter.getId(), requestId))
                .orElse(false);
    }
}
