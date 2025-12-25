package gdg.travodobackend.app.community.repository;

import gdg.travodobackend.app.community.entity.Comment;
import gdg.travodobackend.app.community.entity.CommentLike;
import gdg.travodobackend.app.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByUserAndComment(User user, Comment comment);

    boolean existsByUserAndComment(User user, Comment comment);

    long countByComment(Comment comment);

    @Query("select cl.comment.id from CommentLike cl where cl.user.id = :userId and cl.comment.id in :commentIds")
    List<Long> findLikedCommentIds(
            @Param("userId") Long userId,
            @Param("commentIds") List<Long> commentIds
    );
}


