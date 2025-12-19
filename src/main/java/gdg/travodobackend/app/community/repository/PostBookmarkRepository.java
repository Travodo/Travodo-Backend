package gdg.travodobackend.app.community.repository;

import gdg.travodobackend.app.community.entity.Post;
import gdg.travodobackend.app.community.entity.PostBookmark;
import gdg.travodobackend.app.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {

    Optional<PostBookmark> findByUserAndPost(User user, Post post);

    boolean existsByUserAndPost(User user, Post post);

    Page<PostBookmark> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    void deleteByUserAndPost(User user, Post post);
    
    List<PostBookmark> findByUser(User user);
    
    // 사용자가 저장한 게시글 ID 목록 조회 (N+1 문제 해결)
    @Query("SELECT pb.post.id FROM PostBookmark pb WHERE pb.user.id = :userId")
    List<Long> findPostIdsByUserId(@Param("userId") Long userId);
}
