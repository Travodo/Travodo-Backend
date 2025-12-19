package gdg.travodobackend.app.community.service;

import gdg.travodobackend.app.community.dto.PostReportRequest;
import gdg.travodobackend.app.community.entity.Post;
import gdg.travodobackend.app.community.entity.PostReport;
import gdg.travodobackend.app.community.repository.PostReportRepository;
import gdg.travodobackend.app.community.repository.PostRepository;
import gdg.travodobackend.app.user.entity.User;
import gdg.travodobackend.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostReportService {

    private final PostReportRepository postReportRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 게시글 신고
    @Transactional
    public void reportPost(Long userId, Long postId, PostReportRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Post post = postRepository.findByIdAndDeletedFalse(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다"));

        // 자신의 게시글은 신고할 수 없음
        if (post.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("자신의 게시글은 신고할 수 없습니다");
        }

        // 이미 신고한 게시글인지 확인
        if (postReportRepository.existsByUserAndPost(user, post)) {
            throw new IllegalArgumentException("이미 신고한 게시글입니다");
        }

        PostReport report = PostReport.builder()
                .user(user)
                .post(post)
                .reason(request.getReason())
                .build();

        postReportRepository.save(report);
    }
}
