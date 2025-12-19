package gdg.travodobackend.app.community.repository;

import gdg.travodobackend.app.community.entity.Post;
import gdg.travodobackend.app.community.entity.PostReport;
import gdg.travodobackend.app.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostReportRepository extends JpaRepository<PostReport, Long> {

    Optional<PostReport> findByUserAndPost(User user, Post post);

    boolean existsByUserAndPost(User user, Post post);
}
