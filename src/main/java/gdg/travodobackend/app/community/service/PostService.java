package gdg.travodobackend.app.community.service;

import gdg.travodobackend.app.community.dto.*;
import gdg.travodobackend.app.community.entity.Post;
import gdg.travodobackend.app.community.entity.PostLike;
import gdg.travodobackend.app.community.entity.TravelTag;
import gdg.travodobackend.app.community.repository.PostBookmarkRepository;
import gdg.travodobackend.app.community.repository.PostLikeRepository;
import gdg.travodobackend.app.community.repository.PostRepository;
import gdg.travodobackend.app.travel.dto.TripResponse;
import gdg.travodobackend.app.travel.repository.TripRepository;
import gdg.travodobackend.app.upload.service.S3Service;
import gdg.travodobackend.app.user.entity.User;
import gdg.travodobackend.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostBookmarkRepository postBookmarkRepository;
    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final S3Service s3Service;

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
        
        // N+1 문제 해결: 사용자가 저장한 게시글 ID를 한 번에 조회
        List<Long> bookmarkedPostIds = currentUserId != null
            ? postBookmarkRepository.findPostIdsByUserId(currentUserId)
            : java.util.Collections.emptyList();

        // LAZY 컬렉션 초기화 (트랜잭션 내에서)
        List<Post> posts = postPage.getContent();
        for (Post post : posts) {
            Hibernate.initialize(post.getTags());
        }

        return PostListResponse.builder()
                .content(posts.stream()
                        .map(post -> convertToSummary(post, likedPostIds, bookmarkedPostIds))
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
        
        // 북마크 여부 확인
        boolean isBookmarked = currentUserId != null
            && postBookmarkRepository.existsByUserAndPost(
                userRepository.findById(currentUserId).orElse(null), post);

        return convertToResponse(post, isLiked, isBookmarked);
    }

    // 게시글 작성 (이미지 파일 포함)
    @Transactional
    public PostResponse createPost(Long userId, PostRequest request, List<MultipartFile> imageFiles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        // 이미지 파일이 있으면 S3에 업로드
        List<String> imageUrls = new ArrayList<>();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            try {
                imageUrls = s3Service.uploadImages(imageFiles, "community");
                log.info("게시글 작성 시 {}개의 이미지 업로드 완료", imageUrls.size());
            } catch (Exception e) {
                log.error("이미지 업로드 실패: {}", e.getMessage(), e);
                throw new IllegalArgumentException("이미지 업로드에 실패했습니다: " + e.getMessage());
            }
        } else if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            // 이미지 파일이 없고 URL이 제공된 경우
            imageUrls = request.getImageUrls();
        }

        // 썸네일 URL 설정 (첫 번째 이미지를 썸네일로 사용)
        String thumbnailUrl = request.getThumbnailUrl();
        if (thumbnailUrl == null && !imageUrls.isEmpty()) {
            thumbnailUrl = imageUrls.get(0);
        }

        Post post = Post.builder()
                .author(user)
                .title(request.getTitle())
                .content(request.getContent())
                .tripId(request.getTripId())
                .tags(request.getTags())
                .imageUrls(imageUrls)
                .thumbnailUrl(thumbnailUrl)
                .build();

        Post savedPost = postRepository.save(post);
        // LAZY 컬렉션 초기화 (트랜잭션 내에서)
        Hibernate.initialize(savedPost.getTags());
        Hibernate.initialize(savedPost.getImageUrls());
        return convertToResponse(savedPost, false, false);  // 새로 작성한 게시글은 좋아요/북마크 안 눌림
    }

    // 게시글 수정 (이미지 파일 포함)
    @Transactional
    public PostResponse updatePost(Long postId, Long userId, PostRequest request, List<MultipartFile> imageFiles) {
        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        if (!post.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다");
        }

        // 이미지 파일이 있으면 S3에 업로드
        List<String> imageUrls = new ArrayList<>();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            try {
                imageUrls = s3Service.uploadImages(imageFiles, "community");
                log.info("게시글 수정 시 {}개의 이미지 업로드 완료", imageUrls.size());
            } catch (Exception e) {
                log.error("이미지 업로드 실패: {}", e.getMessage(), e);
                throw new IllegalArgumentException("이미지 업로드에 실패했습니다: " + e.getMessage());
            }
        } else if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            // 이미지 파일이 없고 URL이 제공된 경우
            imageUrls = request.getImageUrls();
        } else {
            // 둘 다 없으면 기존 이미지 유지
            imageUrls = post.getImageUrls();
        }

        // 썸네일 URL 설정
        String thumbnailUrl = request.getThumbnailUrl();
        if (thumbnailUrl == null && !imageUrls.isEmpty()) {
            thumbnailUrl = imageUrls.get(0);
        } else if (thumbnailUrl == null) {
            thumbnailUrl = post.getThumbnailUrl();
        }

        post.update(
                request.getTitle(),
                request.getContent(),
                request.getTags(),
                imageUrls,
                thumbnailUrl,
                request.getTripId()
        );

        // LAZY 컬렉션 초기화 (트랜잭션 내에서)
        Hibernate.initialize(post.getTags());
        Hibernate.initialize(post.getImageUrls());

        // 좋아요 여부 확인
        boolean isLiked = postLikeRepository.existsByUserAndPost(
                userRepository.findById(userId).orElse(null), post);
        
        // 북마크 여부 확인
        boolean isBookmarked = postBookmarkRepository.existsByUserAndPost(
                userRepository.findById(userId).orElse(null), post);

        return convertToResponse(post, isLiked, isBookmarked);
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
    private PostSummary convertToSummary(Post post, List<Long> likedPostIds, List<Long> bookmarkedPostIds) {
        String summary = post.getContent().length() > SUMMARY_LENGTH
                ? post.getContent().substring(0, SUMMARY_LENGTH) + "..."
                : post.getContent();

        boolean isLiked = likedPostIds.contains(post.getId());
        boolean isBookmarked = bookmarkedPostIds.contains(post.getId());

        // Trip 정보 조회
        TripResponse tripResponse = null;
        if (post.getTripId() != null) {
            tripResponse = tripRepository.findById(post.getTripId())
                    .map(trip -> {
                        // LAZY 컬렉션 초기화
                        Hibernate.initialize(trip.getMembers());
                        if (trip.getMembers() != null) {
                            trip.getMembers().forEach(member -> {
                                Hibernate.initialize(member.getUser());
                            });
                        }
                        return TripResponse.from(trip);
                    })
                    .orElse(null);
        }

        return PostSummary.builder()
                .id(post.getId())
                .author(AuthorInfo.builder()
                        .id(post.getAuthor().getId())
                        .nickname(post.getAuthor().getNickname())
                        .profileImageUrl(post.getAuthor().getProfileImageUrl())
                        .build())
                .title(post.getTitle())
                .summary(summary)
                .tripId(post.getTripId())
                .trip(tripResponse)
                .tags(post.getTags())
                .thumbnailUrl(post.getThumbnailUrl())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isLiked(isLiked)
                .isBookmarked(isBookmarked)
                .createdAt(post.getCreatedAt())
                .build();
    }

    private PostResponse convertToResponse(Post post, boolean isLiked, boolean isBookmarked) {
        // Trip 정보 조회
        TripResponse tripResponse = null;
        if (post.getTripId() != null) {
            tripResponse = tripRepository.findById(post.getTripId())
                    .map(trip -> {
                        // LAZY 컬렉션 초기화
                        Hibernate.initialize(trip.getMembers());
                        if (trip.getMembers() != null) {
                            trip.getMembers().forEach(member -> {
                                Hibernate.initialize(member.getUser());
                            });
                        }
                        return TripResponse.from(trip);
                    })
                    .orElse(null);
        }

        return PostResponse.builder()
                .id(post.getId())
                .author(AuthorInfo.builder()
                        .id(post.getAuthor().getId())
                        .nickname(post.getAuthor().getNickname())
                        .profileImageUrl(post.getAuthor().getProfileImageUrl())
                        .build())
                .title(post.getTitle())
                .content(post.getContent())
                .tripId(post.getTripId())
                .trip(tripResponse)
                .tags(post.getTags())
                .imageUrls(post.getImageUrls())
                .thumbnailUrl(post.getThumbnailUrl())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isLiked(isLiked)
                .isBookmarked(isBookmarked)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    // 내가 쓴 글 목록 조회
    public PostListResponse getMyPosts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> postPage = postRepository.findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(userId, pageable);

        // N+1 문제 해결: 사용자가 좋아요한 게시글 ID를 한 번에 조회
        List<Long> likedPostIds = postLikeRepository.findPostIdsByUserId(userId);
        
        // N+1 문제 해결: 사용자가 저장한 게시글 ID를 한 번에 조회
        List<Long> bookmarkedPostIds = postBookmarkRepository.findPostIdsByUserId(userId);

        // LAZY 컬렉션 초기화 (트랜잭션 내에서)
        List<Post> posts = postPage.getContent();
        for (Post post : posts) {
            Hibernate.initialize(post.getTags());
        }

        return PostListResponse.builder()
                .content(posts.stream()
                        .map(post -> convertToSummary(post, likedPostIds, bookmarkedPostIds))
                        .collect(Collectors.toList()))
                .page(postPage.getNumber())
                .size(postPage.getSize())
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .hasNext(postPage.hasNext())
                .hasPrevious(postPage.hasPrevious())
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

