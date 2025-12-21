package gdg.travodobackend.app.user.controller;

import gdg.travodobackend.app.community.dto.PostListResponse;
import gdg.travodobackend.app.community.service.PostService;
import gdg.travodobackend.app.user.dto.UserResponse;
import gdg.travodobackend.app.user.dto.UserUpdateRequest;
import gdg.travodobackend.app.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "사용자", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;
    private final PostService postService;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다")
    public ResponseEntity<UserResponse> getMyInfo(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserResponse response = userService.getUserInfo(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @Operation(summary = "프로필 수정", description = "현재 로그인한 사용자의 프로필 정보를 수정합니다 (닉네임 등)")
    public ResponseEntity<UserResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UserUpdateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        UserResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 이미지 업로드", description = "현재 로그인한 사용자의 프로필 이미지를 업로드합니다")
    public ResponseEntity<UserResponse> uploadProfileImage(
            Authentication authentication,
            @Parameter(description = "업로드할 프로필 이미지 파일")
            @RequestParam("file") MultipartFile file) {
        Long userId = (Long) authentication.getPrincipal();
        UserResponse response = userService.updateProfileImage(userId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/me/profile-image")
    @Operation(summary = "프로필 이미지 삭제", description = "현재 로그인한 사용자의 프로필 이미지를 삭제합니다")
    public ResponseEntity<UserResponse> deleteProfileImage(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserResponse response = userService.deleteProfileImage(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/posts")
    @Operation(summary = "내가 쓴 글 목록 조회", description = "현재 로그인한 사용자가 작성한 게시글 목록을 조회합니다")
    public ResponseEntity<PostListResponse> getMyPosts(
            Authentication authentication,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size) {
        Long userId = (Long) authentication.getPrincipal();
        PostListResponse response = postService.getMyPosts(userId, page, size);
        return ResponseEntity.ok(response);
    }
}

