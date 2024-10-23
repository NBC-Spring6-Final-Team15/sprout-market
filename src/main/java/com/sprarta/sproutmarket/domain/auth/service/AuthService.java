package com.sprarta.sproutmarket.domain.auth.service;

import com.sprarta.sproutmarket.config.JwtUtil;
import com.sprarta.sproutmarket.domain.areas.service.AdministrativeAreaService;
import com.sprarta.sproutmarket.domain.auth.dto.request.SigninRequest;
import com.sprarta.sproutmarket.domain.auth.dto.request.SignupRequest;
import com.sprarta.sproutmarket.domain.auth.dto.response.SigninResponse;
import com.sprarta.sproutmarket.domain.auth.dto.response.SignupResponse;
import com.sprarta.sproutmarket.domain.common.entity.Status;
import com.sprarta.sproutmarket.domain.common.enums.ErrorStatus;
import com.sprarta.sproutmarket.domain.common.exception.ApiException;
import com.sprarta.sproutmarket.domain.user.entity.User;
import com.sprarta.sproutmarket.domain.user.enums.UserRole;
import com.sprarta.sproutmarket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AdministrativeAreaService administrativeAreaService;

    @Transactional
    public SignupResponse signup(SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(ErrorStatus.BAD_REQUEST_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        UserRole userRole = UserRole.of(request.getUserRole());

        // 위도와 경도를 이용해 행정구역 조회
        String address = administrativeAreaService.findAdministrativeAreaByCoordinates(request.getLongitude(), request.getLatitude());

        User newUser = new User(
                request.getUsername(),
                request.getEmail(),
                encodedPassword,
                request.getNickname(),
                request.getPhoneNumber(),
                address,
                userRole
        );
        User savedUser = userRepository.save(newUser);

        String bearerToken = jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), userRole);

        return new SignupResponse(bearerToken);
    }

    public SigninResponse signin(SigninRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new ApiException(ErrorStatus.NOT_FOUND_AUTH_USER));

        // 1소프트 삭제된 유저인지 확인
        if (user.getStatus() == Status.DELETED) {
            throw new ApiException(ErrorStatus.NOT_FOUND_USER);
        }

        // 로그인 시 이메일과 비밀번호가 일치하지 않을 경우 401을 반환합니다.
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException(ErrorStatus.BAD_REQUEST_PASSWORD);
        }

        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole());

        return new SigninResponse(bearerToken);
    }
}
