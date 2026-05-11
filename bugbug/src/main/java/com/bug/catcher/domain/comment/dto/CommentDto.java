package com.bug.catcher.domain.comment.dto;

import com.bug.catcher.domain.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class CommentDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String content;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplyRequest {
        private String content;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String content;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long commentId;
        private Long requestId;
        private Long userId;
        private String userNickname;
        private Long parentCommentId;
        private String content;
        private Integer depth;
        private boolean isDeleted;
        private LocalDateTime createdAt;
        private List<Response> children;

        public static Response fromEntity(Comment comment) {
            return Response.builder()
                    .commentId(comment.getId())
                    .requestId(comment.getRequest().getId())
                    .userId(comment.getUser().getId())
                    .userNickname(comment.getUser().getNickname())
                    .parentCommentId(comment.getParentComment() == null ? null : comment.getParentComment().getId())
                    .content(comment.getContent())
                    .depth(comment.getDepth())
                    .isDeleted(comment.isDeleted())
                    .createdAt(comment.getCreatedAt())
                    .children(List.of())
                    .build();
        }

        public static Response fromEntity(Comment comment, List<Response> children) {
            return Response.builder()
                    .commentId(comment.getId())
                    .requestId(comment.getRequest().getId())
                    .userId(comment.getUser().getId())
                    .userNickname(comment.getUser().getNickname())
                    .parentCommentId(comment.getParentComment() == null ? null : comment.getParentComment().getId())
                    .content(comment.getContent())
                    .depth(comment.getDepth())
                    .isDeleted(comment.isDeleted())
                    .createdAt(comment.getCreatedAt())
                    .children(children)
                    .build();
        }
    }
}
