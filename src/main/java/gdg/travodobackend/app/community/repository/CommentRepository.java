package gdg.travodobackend.app.community.repository;

import gdg.travodobackend.app.community.entity.Comment;
import gdg.travodobackend.app.community.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 삭제되지 않은 댓글 조회
    Optional<Comment> findByIdAndDeletedFalse(Long id);

    // 게시글의 삭제되지 않은 댓글 목록 (부모 댓글만, 최신순)
    Page<Comment> findByPostAndParentIsNullAndDeletedFalseOrderByCreatedAtDesc(Post post, Pageable pageable);

    // 부모 댓글의 대댓글 목록 (최신순)
    Page<Comment> findByParentAndDeletedFalseOrderByCreatedAtAsc(Comment parent, Pageable pageable);

    // 게시글의 모든 댓글 수 (삭제되지 않은 것만)
    long countByPostAndDeletedFalse(Post post);
}

