package gdg.travodobackend.app.community.service;

import gdg.travodobackend.app.community.dto.PostListResponse;
import gdg.travodobackend.app.community.dto.PostSummary;
import gdg.travodobackend.app.community.entity.Post;
import gdg.travodobackend.app.community.entity.PostBookmark;
import gdg.travodobackend.app.community.repository.PostBookmarkRepository;
import gdg.travodobackend.app.community.repository.PostLikeRepository;
import gdg.travodobackend.app.community.repository.PostRepository;
import gdg.travodobackend.app.travel.dto.TripResponse;
import gdg.travodobackend.app.travel.repository.TripRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostBookmarkService {

    private final PostBookmarkRepository postBookmarkRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final TripRepository tripRepository;

    // 게시글 저장
    @Transactional
    public void bookmarkPost(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        if (postBookmarkRepository.existsByUserAndPost(user, post)) {
            throw new IllegalArgumentException("이미 저장한 게시글입니다");
        }

        PostBookmark bookmark = PostBookmark.builder()
                .user(user)
                .post(post)
                .build();

        postBookmarkRepository.save(bookmark);
    }

    // 게시글 저장 취소
    @Transactional
    public void unbookmarkPost(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        PostBookmark bookmark = postBookmarkRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new IllegalArgumentException("저장한 게시글이 아닙니다"));

        postBookmarkRepository.delete(bookmark);
    }

    // 저장한 게시글 목록 조회
    public PostListResponse getBookmarkedPosts(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostBookmark> bookmarkPage = postBookmarkRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        // N+1 문제 해결: 사용자가 좋아요한 게시글 ID를 한 번에 조회
        List<Long> likedPostIds = userId != null 
            ? postLikeRepository.findPostIdsByUserId(userId)
            : Collections.emptyList();

        // LAZY 컬렉션 초기화
        List<Post> posts = bookmarkPage.getContent().stream()
                .map(PostBookmark::getPost)
                .filter(post -> !post.getDeleted())
                .collect(Collectors.toList());

        for (Post post : posts) {
            Hibernate.initialize(post.getTags());
        }
        
        // 북마크한 게시글 ID 목록 (모두 북마크된 상태)
        List<Long> bookmarkedPostIds = posts.stream()
                .map(Post::getId)
                .collect(Collectors.toList());

        return PostListResponse.builder()
                .content(posts.stream()
                        .map(post -> convertToSummary(post, likedPostIds, bookmarkedPostIds))
                        .collect(Collectors.toList()))
                .page(bookmarkPage.getNumber())
                .size(bookmarkPage.getSize())
                .totalElements(bookmarkPage.getTotalElements())
                .totalPages(bookmarkPage.getTotalPages())
                .hasNext(bookmarkPage.hasNext())
                .hasPrevious(bookmarkPage.hasPrevious())
                .build();
    }

    // 게시글 저장 여부 확인
    public boolean isBookmarked(Long userId, Long postId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        Post post = postRepository.findByIdAndDeletedFalse(postId).orElse(null);
        if (post == null) {
            return false;
        }

        return postBookmarkRepository.existsByUserAndPost(user, post);
    }

    private PostSummary convertToSummary(Post post, List<Long> likedPostIds, List<Long> bookmarkedPostIds) {
        String summary = post.getContent().length() > 100
                ? post.getContent().substring(0, 100) + "..."
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
                .author(gdg.travodobackend.app.community.dto.AuthorInfo.builder()
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
}
