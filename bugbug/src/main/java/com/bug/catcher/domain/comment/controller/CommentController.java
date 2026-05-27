package com.bug.catcher.domain.comment.controller;

import com.bug.catcher.domain.comment.dto.CommentDto;
import com.bug.catcher.domain.comment.service.CommentService;
import com.bug.catcher.global.auth.CustomUserPrincipal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/requests/{requestId}/comments")
public class CommentController {

    private final CommentService commentService;

    @PreAuthorize("hasAnyRole('USER', 'HUNTER')")
    @PostMapping
    public ResponseEntity<CommentDto.Response> createComment(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @PathVariable Long requestId,
            @RequestBody CommentDto.CreateRequest requestDto) {

        CommentDto.Response response = commentService.createComment(requestId, loginUser.getUserId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('USER', 'HUNTER')")
    @PostMapping("/{commentId}/replies")
    public ResponseEntity<CommentDto.Response> createReply(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @PathVariable Long requestId,
            @PathVariable Long commentId,
            @RequestBody CommentDto.ReplyRequest requestDto) {

        CommentDto.Response response = commentService.createReply(requestId, commentId, loginUser.getUserId(), requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CommentDto.Response>> readComments(@PathVariable Long requestId) {
        List<CommentDto.Response> response = commentService.readRootComments(requestId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<CommentDto.Response>> readReplies(
            @PathVariable Long requestId,
            @PathVariable Long commentId) {

        List<CommentDto.Response> response = commentService.readChildComments(requestId, commentId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('USER', 'HUNTER') and @commentPermissionChecker.isOwner(#requestId, #commentId, principal.userId)")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long requestId,
            @PathVariable Long commentId) {

        commentService.deleteComment(requestId, commentId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'HUNTER') and @commentPermissionChecker.isOwner(#requestId, #commentId, principal.userId)")
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto.Response> updateComment(
            @PathVariable Long requestId,
            @PathVariable Long commentId,
            @RequestBody CommentDto.UpdateRequest requestDto) {

        CommentDto.Response response = commentService.updateComment(requestId, commentId, requestDto);
        return ResponseEntity.ok(response);
    }
}
