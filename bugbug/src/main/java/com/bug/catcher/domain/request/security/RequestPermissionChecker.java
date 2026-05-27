package com.bug.catcher.domain.request.security;

import com.bug.catcher.domain.request.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestPermissionChecker {

    private final RequestRepository requestRepository;

    public boolean isOwner(Long requestId, Long userId) {
        return requestId != null
                && userId != null
                && requestRepository.existsByIdAndUser_Id(requestId, userId);
    }
}
