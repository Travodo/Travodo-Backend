package gdg.travodobackend.app.community.repository;

import gdg.travodobackend.app.community.entity.Post;
import gdg.travodobackend.app.community.entity.PostLike;
import gdg.travodobackend.app.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // 사용자가 특정 게시글에 좋아요를 눌렀는지 확인
    Optional<PostLike> findByUserAndPost(User user, Post post);

    // 게시글에 대한 좋아요 수
    long countByPost(Post post);

    // 사용자가 좋아요를 누른 게시글들 확인용
    boolean existsByUserAndPost(User user, Post post);
}

