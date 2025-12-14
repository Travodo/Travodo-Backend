package gdg.travodobackend.app.community.controller;

import gdg.travodobackend.app.community.dto.*;
import gdg.travodobackend.app.community.entity.TravelTag;
import gdg.travodobackend.app.community.service.CommentService;
import gdg.travodobackend.app.community.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@Tag(name = "커뮤니티", description = "커뮤니티 게시글 및 댓글 관련 API")
public class CommunityController {

    private final PostService postService;
    private final CommentService commentService;
    private final gdg.travodobackend.app.community.service.PostBookmarkService postBookmarkService;
    private final gdg.travodobackend.app.community.service.PostReportService postReportService;

    // 게시글 목록 조회
    @GetMapping("/posts")
    @Operation(summary = "게시글 목록 조회", description = "여행 유형 필터와 정렬 기준으로 게시글 목록을 조회합니다")
    public ResponseEntity<PostListResponse> getPosts(
            @Parameter(description = "여행 유형 태그 (SOLO, FRIEND, COUPLE, FAMILY, RELAXATION)")
            @RequestParam(required = false) TravelTag tag,
            @Parameter(description = "정렬 기준 (recent: 최신순, popular: 인기순)")
            @RequestParam(defaultValue = "recent") String sort,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        PostListResponse response = postService.getPosts(tag, sort, page, size, userId);
        return ResponseEntity.ok(response);
    }

    // 게시글 상세 조회
    @GetMapping("/posts/{postId}")
    @Operation(summary = "게시글 상세 조회", description = "게시글 ID로 상세 정보를 조회합니다")
    public ResponseEntity<PostResponse> getPost(
            @Parameter(description = "게시글 ID")
            @PathVariable Long postId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        PostResponse response = postService.getPost(postId, userId);
        return ResponseEntity.ok(response);
    }

    // 게시글 작성
    @PostMapping("/posts")
    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다")
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        PostResponse response = postService.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 게시글 수정
    @PutMapping("/posts/{postId}")
    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다")
    public ResponseEntity<PostResponse> updatePost(
            @Parameter(description = "게시글 ID")
            @PathVariable Long postId,
            @Valid @RequestBody PostRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        PostResponse response = postService.updatePost(postId, userId, request);
        return ResponseEntity.ok(response);
    }

    // 게시글 삭제
    @DeleteMapping("/posts/{postId}")
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "게시글 ID")
            @PathVariable Long postId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    // 좋아요 추가
    @PostMapping("/posts/{postId}/likes")
    @Operation(summary = "게시글 좋아요", description = "게시글에 좋아요를 추가합니다")
    public ResponseEntity<Void> likePost(
            @Parameter(description = "게시글 ID")
            @PathVariable Long postId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        postService.likePost(postId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 좋아요 취소
    @DeleteMapping("/posts/{postId}/likes")
    @Operation(summary = "게시글 좋아요 취소", description = "게시글 좋아요를 취소합니다")
    public ResponseEntity<Void> unlikePost(
            @Parameter(description = "게시글 ID")
            @PathVariable Long postId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        postService.unlikePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    // 댓글 목록 조회
    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "댓글 목록 조회", description = "게시글의 댓글 목록을 조회합니다")
    public ResponseEntity<CommentListResponse> getComments(
            @Parameter(description = "게시글 ID")
            @PathVariable Long postId,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size) {
        
        CommentListResponse response = commentService.getComments(postId, page, size);
        return ResponseEntity.ok(response);
    }

    // 댓글 작성
    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다")
    public ResponseEntity<CommentResponse> createComment(
            @Parameter(description = "게시글 ID")
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        CommentResponse response = commentService.createComment(postId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 댓글 수정
    @PutMapping("/comments/{commentId}")
    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다")
    public ResponseEntity<CommentResponse> updateComment(
            @Parameter(description = "댓글 ID")
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        CommentResponse response = commentService.updateComment(commentId, userId, request);
        return ResponseEntity.ok(response);
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "댓글 ID")
            @PathVariable Long commentId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    // 게시글 저장
    @PostMapping("/posts/{postId}/bookmarks")
    @Operation(summary = "게시글 저장", description = "게시글을 저장합니다")
    public ResponseEntity<Void> bookmarkPost(
            @Parameter(description = "게시글 ID")
            @PathVariable Long postId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        postBookmarkService.bookmarkPost(userId, postId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 게시글 저장 취소
    @DeleteMapping("/posts/{postId}/bookmarks")
    @Operation(summary = "게시글 저장 취소", description = "저장한 게시글을 취소합니다")
    public ResponseEntity<Void> unbookmarkPost(
            @Parameter(description = "게시글 ID")
            @PathVariable Long postId,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        postBookmarkService.unbookmarkPost(userId, postId);
        return ResponseEntity.noContent().build();
    }

    // 저장한 게시글 목록 조회
    @GetMapping("/bookmarks")
    @Operation(summary = "저장한 게시글 목록 조회", description = "저장한 게시글 목록을 조회합니다")
    public ResponseEntity<PostListResponse> getBookmarkedPosts(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        PostListResponse response = postBookmarkService.getBookmarkedPosts(userId, page, size);
        return ResponseEntity.ok(response);
    }

    // 게시글 신고
    @PostMapping("/posts/{postId}/reports")
    @Operation(summary = "게시글 신고", description = "게시글을 신고합니다")
    public ResponseEntity<Void> reportPost(
            @Parameter(description = "게시글 ID")
            @PathVariable Long postId,
            @Valid @RequestBody PostReportRequest request,
            Authentication authentication) {
        
        Long userId = (Long) authentication.getPrincipal();
        postReportService.reportPost(userId, postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

