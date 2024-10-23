package com.sprarta.sproutmarket.domain.auth.service;

import com.sprarta.sproutmarket.config.JwtUtil;
import com.sprarta.sproutmarket.domain.areas.service.AdministrativeAreaService;
import com.sprarta.sproutmarket.domain.auth.dto.request.SigninRequest;
import com.sprarta.sproutmarket.domain.auth.dto.request.SignupRequest;
import com.sprarta.sproutmarket.domain.auth.dto.response.SigninResponse;
import com.sprarta.sproutmarket.domain.auth.dto.response.SignupResponse;
import com.sprarta.sproutmarket.domain.common.enums.ErrorStatus;
import com.sprarta.sproutmarket.domain.common.exception.ApiException;
import com.sprarta.sproutmarket.domain.user.entity.User;
import com.sprarta.sproutmarket.domain.user.enums.UserRole;
import com.sprarta.sproutmarket.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdministrativeAreaService administrativeAreaService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void signupSuccess() {
        // Given
        SignupRequest request = new SignupRequest(
                "username",
                "email@example.com",
                "password",
                "nickname",
                "010-1234-5678",
                126.976889,
                37.575651,
                "USER"
        );

        User savedUser = new User(
                "username",
                "email@example.com",
                "encodedPassword",
                "nickname",
                "010-1234-5678",
                "서울특별시 종로구",
                UserRole.USER
        );

        // 모킹: 이미 이메일이 존재하지 않음
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        // 모킹: 비밀번호 인코딩
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // 모킹: 위도와 경도를 통해 행정구역(주소) 조회
        when(administrativeAreaService.findAdministrativeAreaByCoordinates(anyDouble(), anyDouble()))
                .thenReturn("서울특별시 종로구");

        // 모킹: 유저 저장
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // 모킹: JWT 토큰 생성
        when(jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getUserRole()))
                .thenReturn("jwt-token");

        // When
        SignupResponse response = authService.signup(request);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getBearerToken());

        // jwtUtil.createToken 호출 여부 확인
        verify(jwtUtil).createToken(savedUser.getId(), savedUser.getEmail(), savedUser.getUserRole());
    }

    @Test
    void signupFail_EmailAlreadyExists() {
        // Given
        SignupRequest request = new SignupRequest(
                "username",
                "email@example.com",
                "password",
                "nickname",
                "010-1234-5678",
                126.976889,
                37.575651,
                "USER"
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> authService.signup(request));
        assertEquals(ErrorStatus.BAD_REQUEST_EMAIL, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signinSuccess() {
        // Given
        SigninRequest request = new SigninRequest("email@example.com", "password");

        User user = new User(
                "username",
                "email@example.com",
                "encodedPassword",
                "nickname",
                "010-1234-5678",
                "서울특별시 종로구",
                UserRole.USER
        );

        // 모킹: 이메일로 유저를 찾음
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // 모킹: 비밀번호가 일치함
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // 모킹: JWT 토큰 생성
        when(jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole()))
                .thenReturn("jwt-token");

        // When
        SigninResponse response = authService.signin(request);

        // Then
        assertNotNull(response);  // 응답이 null이 아님을 확인
        assertEquals("jwt-token", response.getBearerToken());  // 반환된 토큰이 예상 값과 같은지 확인

        // jwtUtil.createToken 호출 여부 확인
        verify(jwtUtil).createToken(user.getId(), user.getEmail(), user.getUserRole());
    }


    @Test
    void signinFail_UserNotFound() {
        // Given
        SigninRequest request = new SigninRequest("email@example.com", "password");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> authService.signin(request));
        assertEquals(ErrorStatus.NOT_FOUND_AUTH_USER, exception.getErrorCode());
    }

    @Test
    void signinFail_WrongPassword() {
        // Given
        SigninRequest request = new SigninRequest("email@example.com", "password");
        User user = new User("username", "email@example.com", "encodedPassword", "nickname", "010-1234-5678", "서울특별시 종로구", UserRole.USER);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> authService.signin(request));
        assertEquals(ErrorStatus.BAD_REQUEST_PASSWORD, exception.getErrorCode());
    }

    @Test
    void signinFail_DeletedUser() {
        // Given
        SigninRequest request = new SigninRequest("email@example.com", "password");
        User user = new User("username", "email@example.com", "encodedPassword", "nickname", "010-1234-5678", "서울특별시 종로구", UserRole.USER);
        user.deactivate(); // 소프트 삭제 상태로 변경

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> authService.signin(request));
        assertEquals(ErrorStatus.NOT_FOUND_USER, exception.getErrorCode());
    }
}