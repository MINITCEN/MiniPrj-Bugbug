package com.bug.catcher.domain.comment.controller;

import com.bug.catcher.domain.comment.dto.CommentDto;
import com.bug.catcher.domain.comment.service.CommentService;
import com.bug.catcher.domain.entity.User;
import com.bug.catcher.global.auth.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/requests/{requestId}/comments")
public class CommentController {

    private final CommentService commentService;

    /**
     * 게시글에 최상위 댓글을 생성한다.
     */
    @PostMapping
    public ResponseEntity<CommentDto.Response> createComment(
            @PathVariable Long requestId,
            @RequestBody CommentDto.CreateRequest requestDto,
            HttpServletRequest request) {

        User loginUser = getLoginUser(request);
        CommentDto.Response response = commentService.createComment(requestId, loginUser.getId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 특정 댓글 아래에 대댓글을 생성한다.
     */
    @PostMapping("/{commentId}/replies")
    public ResponseEntity<CommentDto.Response> createReply(
            @PathVariable Long requestId,
            @PathVariable Long commentId,
            @RequestBody CommentDto.ReplyRequest requestDto,
            HttpServletRequest request) {

        User loginUser = getLoginUser(request);
        CommentDto.Response response = commentService.createReply(requestId, commentId, loginUser.getId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 특정 게시글의 댓글 트리를 조회한다.
     */
    @GetMapping
    public ResponseEntity<List<CommentDto.Response>> readComments(@PathVariable Long requestId) {
        List<CommentDto.Response> response = commentService.readRootComments(requestId);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 부모 댓글의 자식 댓글 목록을 조회한다.
     */
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<CommentDto.Response>> readReplies(
            @PathVariable Long requestId,
            @PathVariable Long commentId) {

        List<CommentDto.Response> response = commentService.readChildComments(requestId, commentId);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 댓글을 soft delete 처리한다.
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long requestId,
            @PathVariable Long commentId,
            HttpServletRequest request) {

        User loginUser = getLoginUser(request);
        commentService.deleteComment(requestId, commentId, loginUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 댓글의 내용을 수정한다.
     */
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto.Response> updateComment(
            @PathVariable Long requestId,
            @PathVariable Long commentId,
            @RequestBody CommentDto.UpdateRequest requestDto,
            HttpServletRequest request) {

        User loginUser = getLoginUser(request);
        CommentDto.Response response = commentService.updateComment(requestId, commentId, loginUser.getId(), requestDto);
        return ResponseEntity.ok(response);
    }

    private User getLoginUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);
        if (loginUser == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        return loginUser;
    }
}
