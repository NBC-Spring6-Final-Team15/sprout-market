package com.sprarta.sproutmarket.domain.user.service;

import com.sprarta.sproutmarket.domain.areas.service.AdministrativeAreaService;
import com.sprarta.sproutmarket.domain.common.enums.ErrorStatus;
import com.sprarta.sproutmarket.domain.common.exception.ApiException;
import com.sprarta.sproutmarket.domain.image.profileImage.service.ProfileImageService;
import com.sprarta.sproutmarket.domain.image.s3Image.service.S3ImageService;
import com.sprarta.sproutmarket.domain.user.dto.request.UserChangePasswordRequest;
import com.sprarta.sproutmarket.domain.user.dto.request.UserDeleteRequest;
import com.sprarta.sproutmarket.domain.user.dto.response.UserAdminResponse;
import com.sprarta.sproutmarket.domain.user.dto.response.UserResponse;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
import com.sprarta.sproutmarket.domain.user.entity.User;
import com.sprarta.sproutmarket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdministrativeAreaService administrativeAreaService;
    private final ProfileImageService profileImageService;

    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        return new UserResponse(user.getId(), user.getEmail());
    }

    @Transactional
    public void changePassword(CustomUserDetails authUser, UserChangePasswordRequest userChangePasswordRequest) {

        // 유저 존재 여부 확인
        User user = userRepository.findById(authUser.getId()).orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        // 기존 비밀번호가 맞는지 확인
        if (!passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new ApiException(ErrorStatus.BAD_REQUEST_PASSWORD);
        }

        // 새 비밀번호가 기존 비밀번호와 동일한지 확인 (암호화하지 않은 상태로 비교)
        if (userChangePasswordRequest.getOldPassword().equals(userChangePasswordRequest.getNewPassword())) {
            throw new ApiException(ErrorStatus.BAD_REQUEST_NEW_PASSWORD);
        }

        // 새 비밀번호 암호화 후 저장
        user.changePassword(passwordEncoder.encode(userChangePasswordRequest.getNewPassword()));
    }

    @Transactional
    public void deleteUser(CustomUserDetails authUser, UserDeleteRequest userDeleteRequest) {

        // 유저 존재 여부 확인
        User user = userRepository.findById(authUser.getId()).orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(userDeleteRequest.getPassword(), user.getPassword())) {
            throw new ApiException(ErrorStatus.BAD_REQUEST_PASSWORD);
        }

        // 유저 비활성화 및 삭제
        user.deactivate();
    }

    @Transactional
    public void updateUserAddress(Long userId, double longitude, double latitude) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        String administrativeArea = administrativeAreaService.getAdministrativeAreaByCoordinates(longitude, latitude);

        user.changeAddress(administrativeArea);
    }

    // 탈퇴된 유저 복원
    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus.NOT_FOUND_USER));

        user.activate();
    }

    // ACTIVE, DELETED 상태 유저 모두 조회
    @Transactional
    public Page<UserAdminResponse> getAllUsers(Pageable pageable) {
        return userRepository
                .findAll(pageable)
                .map(UserAdminResponse::new);
    }
}

