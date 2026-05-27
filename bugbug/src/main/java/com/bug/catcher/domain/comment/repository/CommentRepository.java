package com.bug.catcher.domain.comment.repository;

import com.bug.catcher.domain.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 게시글의 최상위 댓글(parentComment가 null인 댓글)만 작성 순으로 조회한다.
    List<Comment> findByRequestIdAndParentCommentIsNullOrderByCreatedAtAsc(Long requestId);

    // 특정 부모 댓글에 달린 자식 댓글들을 작성 순으로 조회한다.
    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);

    // 댓글이 특정 게시글에 속해 있는지 함께 검증하면서 댓글을 조회한다.
    Optional<Comment> findByIdAndRequestId(Long commentId, Long requestId);
    boolean existsByIdAndRequestIdAndUser_Id(Long commentId, Long requestId, Long userId);

    // 특정 댓글에 자식 댓글이 하나 이상 존재하는지 확인한다.
    boolean existsByParentCommentId(Long parentCommentId);

}
