package gdg.travodobackend.app.community.repository;

import gdg.travodobackend.app.community.entity.Post;
import gdg.travodobackend.app.community.entity.TravelTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 삭제되지 않은 게시글 조회
    Optional<Post> findByIdAndDeletedFalse(Long id);

    // 삭제되지 않은 게시글 목록 조회 (기본 - 최신순)
    Page<Post> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    // 삭제되지 않은 게시글 목록 조회 (인기순 - 좋아요 수 + 댓글 수 기준)
    @Query("SELECT p FROM Post p WHERE p.deleted = false ORDER BY (p.likeCount + p.commentCount) DESC, p.createdAt DESC")
    Page<Post> findByDeletedFalseOrderByPopularity(Pageable pageable);

    // 태그로 필터링된 게시글 목록 (최신순)
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE p.deleted = false AND t = :tag ORDER BY p.createdAt DESC")
    Page<Post> findByTagAndDeletedFalseOrderByCreatedAtDesc(@Param("tag") TravelTag tag, Pageable pageable);

    // 태그로 필터링된 게시글 목록 (인기순)
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE p.deleted = false AND t = :tag ORDER BY (p.likeCount + p.commentCount) DESC, p.createdAt DESC")
    Page<Post> findByTagAndDeletedFalseOrderByPopularity(@Param("tag") TravelTag tag, Pageable pageable);

    // 여러 태그로 필터링된 게시글 목록 (최신순) - OR 조건
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE p.deleted = false AND t IN :tags ORDER BY p.createdAt DESC")
    Page<Post> findByTagsInAndDeletedFalseOrderByCreatedAtDesc(@Param("tags") List<TravelTag> tags, Pageable pageable);

    // 여러 태그로 필터링된 게시글 목록 (인기순) - OR 조건
    @Query("SELECT DISTINCT p FROM Post p JOIN p.tags t WHERE p.deleted = false AND t IN :tags ORDER BY (p.likeCount + p.commentCount) DESC, p.createdAt DESC")
    Page<Post> findByTagsInAndDeletedFalseOrderByPopularity(@Param("tags") List<TravelTag> tags, Pageable pageable);

    // 사용자가 작성한 게시글 목록
    Page<Post> findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(Long authorId, Pageable pageable);
}

