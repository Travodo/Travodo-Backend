package gdg.travodobackend.app.community.service;

import gdg.travodobackend.app.community.dto.*;
import gdg.travodobackend.app.community.entity.Post;
import gdg.travodobackend.app.community.entity.PostLike;
import gdg.travodobackend.app.community.entity.TravelTag;
import gdg.travodobackend.app.community.repository.PostLikeRepository;
import gdg.travodobackend.app.community.repository.PostRepository;
import gdg.travodobackend.app.user.entity.User;
import gdg.travodobackend.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;

    private static final int SUMMARY_LENGTH = 100;

    // 게시글 목록 조회
    public PostListResponse getPosts(TravelTag tag, String sort, int page, int size, Long currentUserId) {
        Pageable pageable = createPageable(sort, page, size);
        Page<Post> postPage;

        if (tag != null) {
            if ("popular".equals(sort)) {
                postPage = postRepository.findByTagAndDeletedFalseOrderByPopularity(tag, pageable);
            } else {
                postPage = postRepository.findByTagAndDeletedFalseOrderByCreatedAtDesc(tag, pageable);
            }
        } else {
            if ("popular".equals(sort)) {
                postPage = postRepository.findByDeletedFalseOrderByPopularity(pageable);
            } else {
                postPage = postRepository.findByDeletedFalseOrderByCreatedAtDesc(pageable);
            }
        }

        // N+1 문제 해결: 사용자가 좋아요한 게시글 ID를 한 번에 조회
        List<Long> likedPostIds = currentUserId != null 
            ? postLikeRepository.findPostIdsByUserId(currentUserId)
            : java.util.Collections.emptyList();

        // LAZY 컬렉션 초기화 (트랜잭션 내에서)
        List<Post> posts = postPage.getContent();
        for (Post post : posts) {
            Hibernate.initialize(post.getTags());
        }

        return PostListResponse.builder()
                .content(posts.stream()
                        .map(post -> convertToSummary(post, likedPostIds))
                        .collect(Collectors.toList()))
                .page(postPage.getNumber())
                .size(postPage.getSize())
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .hasNext(postPage.hasNext())
                .hasPrevious(postPage.hasPrevious())
                .build();
    }

    // 게시글 상세 조회
    public PostResponse getPost(Long postId, Long currentUserId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        // LAZY 컬렉션 초기화 (트랜잭션 내에서)
        Hibernate.initialize(post.getTags());
        Hibernate.initialize(post.getImageUrls());

        // 좋아요 여부 확인
        boolean isLiked = currentUserId != null 
            && postLikeRepository.existsByUserAndPost(
                userRepository.findById(currentUserId).orElse(null), post);

        return convertToResponse(post, isLiked);
    }

    // 게시글 작성
    @Transactional
    public PostResponse createPost(Long userId, PostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Post post = Post.builder()
                .author(user)
                .title(request.getTitle())
                .content(request.getContent())
                .tripId(request.getTripId())
                .tags(request.getTags())
                .imageUrls(request.getImageUrls() != null ? request.getImageUrls() : java.util.Collections.emptyList())
                .thumbnailUrl(request.getThumbnailUrl())
                .build();

        Post savedPost = postRepository.save(post);
        // LAZY 컬렉션 초기화 (트랜잭션 내에서)
        Hibernate.initialize(savedPost.getTags());
        Hibernate.initialize(savedPost.getImageUrls());
        return convertToResponse(savedPost, false);  // 새로 작성한 게시글은 좋아요 안 눌림
    }

    // 게시글 수정
    @Transactional
    public PostResponse updatePost(Long postId, Long userId, PostRequest request) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다");
        }

        post.update(
                request.getTitle(),
                request.getContent(),
                request.getTags(),
                request.getImageUrls(),
                request.getThumbnailUrl()
        );

        // LAZY 컬렉션 초기화 (트랜잭션 내에서)
        Hibernate.initialize(post.getTags());
        Hibernate.initialize(post.getImageUrls());

        // 좋아요 여부 확인
        boolean isLiked = postLikeRepository.existsByUserAndPost(
                userRepository.findById(userId).orElse(null), post);

        return convertToResponse(post, isLiked);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다");
        }

        post.delete();
    }

    // 좋아요 추가
    @Transactional
    public void likePost(Long postId, Long userId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        if (postLikeRepository.existsByUserAndPost(user, post)) {
            throw new IllegalArgumentException("이미 좋아요를 누른 게시글입니다");
        }

        PostLike postLike = PostLike.builder()
                .user(user)
                .post(post)
                .build();

        postLikeRepository.save(postLike);
        post.incrementLikeCount();
    }

    // 좋아요 취소
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        PostLike postLike = postLikeRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new IllegalArgumentException("좋아요를 찾을 수 없습니다"));

        postLikeRepository.delete(postLike);
        post.decrementLikeCount();
    }

    // DTO 변환 메서드들
    private PostSummary convertToSummary(Post post, List<Long> likedPostIds) {
        String summary = post.getContent().length() > SUMMARY_LENGTH
                ? post.getContent().substring(0, SUMMARY_LENGTH) + "..."
                : post.getContent();

        boolean isLiked = likedPostIds.contains(post.getId());

        return PostSummary.builder()
                .id(post.getId())
                .author(AuthorInfo.builder()
                        .id(post.getAuthor().getId())
                        .nickname(post.getAuthor().getNickname())
                        .build())
                .title(post.getTitle())
                .summary(summary)
                .tripId(post.getTripId())
                .tags(post.getTags())
                .thumbnailUrl(post.getThumbnailUrl())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isLiked(isLiked)
                .createdAt(post.getCreatedAt())
                .build();
    }

    private PostResponse convertToResponse(Post post, boolean isLiked) {
        return PostResponse.builder()
                .id(post.getId())
                .author(AuthorInfo.builder()
                        .id(post.getAuthor().getId())
                        .nickname(post.getAuthor().getNickname())
                        .build())
                .title(post.getTitle())
                .content(post.getContent())
                .tripId(post.getTripId())
                .tags(post.getTags())
                .imageUrls(post.getImageUrls())
                .thumbnailUrl(post.getThumbnailUrl())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isLiked(isLiked)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private Pageable createPageable(String sort, int page, int size) {
        if ("popular".equals(sort)) {
            // 인기순은 쿼리에서 처리하므로 기본 정렬만 설정
            return PageRequest.of(page, size);
        } else {
            // 최신순
            return PageRequest.of(page, size, Sort.by("createdAt").descending());
        }
    }
}

