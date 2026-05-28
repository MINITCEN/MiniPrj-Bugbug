package com.bug.catcher.domain.comment.service;

import com.bug.catcher.domain.comment.dto.CommentDto;
import com.bug.catcher.domain.comment.repository.CommentRepository;
import com.bug.catcher.domain.entity.Comment;
import com.bug.catcher.domain.entity.Request;
import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.request.repository.RequestRepository;
import com.bug.catcher.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    /**
     * 게시글에 최상위 댓글을 생성한다.
     */
    @Transactional
    public CommentDto.Response createComment(Long requestId, Long loginUserId, CommentDto.CreateRequest requestDto) {
        Request request = getRequest(requestId);
        User user = getUser(loginUserId);

        Comment comment = Comment.builder()
                .request(request)
                .user(user)
                .content(requestDto.getContent())
                .depth(0)
                .build();

        return CommentDto.Response.fromEntity(commentRepository.save(comment));
    }

    /**
     * 부모 댓글 아래에 대댓글을 생성한다.
     */
    @Transactional
    public CommentDto.Response createReply(Long requestId, Long parentCommentId, Long loginUserId, CommentDto.ReplyRequest requestDto) {
        Request request = getRequest(requestId);
        User user = getUser(loginUserId);
        Comment parentComment = getCommentInRequest(parentCommentId, requestId);

        Comment reply = Comment.builder()
                .request(request)
                .user(user)
                .content(requestDto.getContent())
                .parentComment(parentComment)
                .depth(parentComment.getDepth() + 1)
                .build();

        parentComment.addChild(reply);
        return CommentDto.Response.fromEntity(commentRepository.save(reply));
    }

    /**
     * 특정 게시글의 최상위 댓글 목록을 생성 순으로 조회한다.
     */
    @Transactional(readOnly = true)
    public List<CommentDto.Response> readRootComments(Long requestId) {
        validateRequestExists(requestId);
        return commentRepository.findByRequestIdAndParentCommentIsNullOrderByCreatedAtAsc(requestId).stream()
                .map(this::toResponseTree)
                .toList();
    }

    /**
     * 특정 부모 댓글에 속한 자식 댓글 목록을 생성 순으로 조회한다.
     */
    @Transactional(readOnly = true)
    public List<CommentDto.Response> readChildComments(Long requestId, Long parentCommentId) {
        getCommentInRequest(parentCommentId, requestId);
        return commentRepository.findByParentCommentIdOrderByCreatedAtAsc(parentCommentId).stream()
                .map(this::toResponseTree)
                .toList();
    }

    /**
     * 댓글을 soft delete 처리한다.
     */
    @Transactional
    public void deleteComment(Long requestId, Long commentId) {
        Comment comment = getCommentInRequest(commentId, requestId);
        comment.markDeleted();
    }

    /**
     * 댓글 내용을 수정한다.
     */
    @Transactional
    public CommentDto.Response updateComment(Long requestId, Long commentId, CommentDto.UpdateRequest requestDto) {
        Comment comment = getCommentInRequest(commentId, requestId);
        comment.updateContent(requestDto.getContent());
        return CommentDto.Response.fromEntity(comment);
    }

    private Request getRequest(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    private Comment getCommentInRequest(Long commentId, Long requestId) {
        return commentRepository.findByIdAndRequestId(commentId, requestId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글에 속한 댓글이 아닙니다."));
    }

    private void validateRequestExists(Long requestId) {
        if (!requestRepository.existsById(requestId)) {
            throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        }
    }

    private CommentDto.Response toResponseTree(Comment comment) {
        List<CommentDto.Response> children = commentRepository.findByParentCommentIdOrderByCreatedAtAsc(comment.getId()).stream()
                .map(this::toResponseTree)
                .toList();
        return CommentDto.Response.fromEntity(comment, children);
    }
}
