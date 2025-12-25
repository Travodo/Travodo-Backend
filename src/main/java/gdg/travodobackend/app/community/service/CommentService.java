package gdg.travodobackend.app.community.service;

import gdg.travodobackend.app.community.dto.CommentListResponse;
import gdg.travodobackend.app.community.dto.CommentRequest;
import gdg.travodobackend.app.community.dto.CommentResponse;
import gdg.travodobackend.app.community.entity.Comment;
import gdg.travodobackend.app.community.entity.Post;
import gdg.travodobackend.app.community.repository.CommentRepository;
import gdg.travodobackend.app.community.repository.PostRepository;
import gdg.travodobackend.app.user.entity.User;
import gdg.travodobackend.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 댓글 목록 조회
    public CommentListResponse getComments(Long postId, int page, int size) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> commentPage = commentRepository.findByPostAndParentIsNullAndDeletedFalseOrderByCreatedAtDesc(post, pageable);

        List<CommentResponse> commentResponses = commentPage.getContent().stream()
                .map(comment -> {
                    CommentResponse response = convertToResponse(comment);
                    // 대댓글 조회 (대댓글은 최신순 정렬)
                    Pageable replyPageable = PageRequest.of(0, 50); // 대댓글은 최대 50개까지
                    Page<Comment> repliesPage = commentRepository.findByParentAndDeletedFalseOrderByCreatedAtAsc(comment, replyPageable);
                    response.setReplies(repliesPage.getContent().stream()
                            .map(this::convertToResponse)
                            .collect(Collectors.toList()));
                    return response;
                })
                .collect(Collectors.toList());

        return CommentListResponse.builder()
                .content(commentResponses)
                .page(commentPage.getNumber())
                .size(commentPage.getSize())
                .totalElements(commentPage.getTotalElements())
                .totalPages(commentPage.getTotalPages())
                .hasNext(commentPage.hasNext())
                .hasPrevious(commentPage.hasPrevious())
                .build();
    }

    // 댓글 작성
    @Transactional
    public CommentResponse createComment(Long postId, Long userId, CommentRequest request) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findByIdAndDeletedFalse(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다"));
            
            // 대댓글의 부모가 해당 게시글의 댓글이어야 함
            if (!parent.getPost().getId().equals(postId)) {
                throw new IllegalArgumentException("잘못된 부모 댓글입니다");
            }
        }

        Comment comment = Comment.builder()
                .author(user)
                .post(post)
                .content(request.getContent())
                .parent(parent)
                .build();

        Comment savedComment = commentRepository.save(comment);
        
        // 게시글의 댓글 수 증가
        post.incrementCommentCount();

        CommentResponse response = convertToResponse(savedComment);
        response.setReplies(List.of()); // 새로 작성한 댓글이므로 대댓글은 없음
        return response;
    }

    // 댓글 수정
    @Transactional
    public CommentResponse updateComment(Long commentId, Long userId, CommentRequest request) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다");
        }

        comment.update(request.getContent());

        CommentResponse response = convertToResponse(comment);
        
        // 대댓글 조회
        Pageable replyPageable = PageRequest.of(0, 50);
        Page<Comment> repliesPage = commentRepository.findByParentAndDeletedFalseOrderByCreatedAtAsc(comment, replyPageable);
        response.setReplies(repliesPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList()));

        return response;
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다");
        }

        comment.delete();
        
        // 게시글의 댓글 수 감소
        comment.getPost().decrementCommentCount();
    }

    // DTO 변환
    private CommentResponse convertToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .author(gdg.travodobackend.app.community.dto.AuthorInfo.builder()
                        .id(comment.getAuthor().getId())
                        .nickname(comment.getAuthor().getNickname())
                        .profileImageUrl(comment.getAuthor().getProfileImageUrl())
                        .build())
                .content(comment.getContent())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}

