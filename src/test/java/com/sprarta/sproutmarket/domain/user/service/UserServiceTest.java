package com.sprarta.sproutmarket.domain.user.service;

import com.sprarta.sproutmarket.domain.common.enums.ErrorStatus;
import com.sprarta.sproutmarket.domain.common.exception.ApiException;
import com.sprarta.sproutmarket.domain.user.dto.request.UserChangePasswordRequest;
import com.sprarta.sproutmarket.domain.user.dto.request.UserDeleteRequest;
import com.sprarta.sproutmarket.domain.user.dto.response.UserResponse;
import com.sprarta.sproutmarket.domain.user.entity.CustomUserDetails;
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
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private CustomUserDetails authUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock user data
        user = new User(1L, "username", "email@example.com", "encodedOldPassword", "nickname", "010-1234-5678", "address", UserRole.USER);
        authUser = new CustomUserDetails(user);
    }

    @Test
    void getUser_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        UserResponse response = userService.getUser(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("email@example.com", response.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUser_Failure_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> userService.getUser(1L));
        assertEquals(ErrorStatus.NOT_FOUND_USER, exception.getErrorCode());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void changePassword_Success() {
        // Given
        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword123", "newPassword456!");

        String encodedOldPassword = "$2a$10$k8bk1sFsX0jntulcF2ryJ.TuC.1D5zzbv4D.HbHa1/7BGr3pv6ryy";
        String encodedNewPassword = "$2a$10$CUzO5QICg/F78291f4iy7uYVhzwSMA6jjD2uYkkrvfvPHu7gltHLG";

        user.changePassword(encodedOldPassword);

        // When
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword123", encodedOldPassword)).thenReturn(true);
        when(passwordEncoder.encode("newPassword456!")).thenReturn(encodedNewPassword);

        userService.changePassword(authUser, request);

        // Then
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).matches("oldPassword123", encodedOldPassword);
        verify(passwordEncoder, times(1)).encode("newPassword456!");
        assertEquals(encodedNewPassword, user.getPassword());
    }

    @Test
    void changePassword_Failure_WrongOldPassword() {
        // Given
        UserChangePasswordRequest request = new UserChangePasswordRequest("wrongOldPassword", "NewPassword123!");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPassword", user.getPassword())).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> userService.changePassword(authUser, request));
        assertEquals(ErrorStatus.BAD_REQUEST_PASSWORD, exception.getErrorCode());
        verify(passwordEncoder, times(1)).matches("wrongOldPassword", user.getPassword());
        verify(passwordEncoder, times(0)).encode(anyString());
    }

    @Test
    void changePassword_Failure_SameAsOldPassword() {
        // Given
        UserChangePasswordRequest request = new UserChangePasswordRequest("encodedOldPassword", "encodedOldPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("encodedOldPassword", user.getPassword())).thenReturn(true);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> userService.changePassword(authUser, request));
        assertEquals(ErrorStatus.BAD_REQUEST_NEW_PASSWORD, exception.getErrorCode());
        verify(passwordEncoder, times(1)).matches("encodedOldPassword", user.getPassword());
        verify(passwordEncoder, times(0)).encode(anyString());
    }

    @Test
    void deleteUser_Success() {
        // Given
        UserDeleteRequest request = new UserDeleteRequest("encodedOldPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("encodedOldPassword", user.getPassword())).thenReturn(true); // Correct password

        // When
        userService.deleteUser(authUser, request);

        // Then
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).matches("encodedOldPassword", user.getPassword()); // Check password
        verify(userRepository, times(1)).delete(user); // Verify user deletion
    }

    @Test
    void deleteUser_Failure_WrongPassword() {
        // Given
        UserDeleteRequest request = new UserDeleteRequest("wrongPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", user.getPassword())).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> userService.deleteUser(authUser, request));
        assertEquals(ErrorStatus.BAD_REQUEST_PASSWORD, exception.getErrorCode());
        verify(passwordEncoder, times(1)).matches("wrongPassword", user.getPassword());
        verify(userRepository, times(0)).delete(any());
    }
}