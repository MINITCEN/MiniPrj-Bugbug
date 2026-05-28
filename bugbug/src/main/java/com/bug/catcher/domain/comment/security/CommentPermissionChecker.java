package com.bug.catcher.domain.comment.security;

import com.bug.catcher.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentPermissionChecker {

    private final CommentRepository commentRepository;

    public boolean isOwner(Long requestId, Long commentId, Long userId) {
        return requestId != null
                && commentId != null
                && userId != null
                && commentRepository.existsByIdAndRequestIdAndUser_Id(commentId, requestId, userId);
    }
}
