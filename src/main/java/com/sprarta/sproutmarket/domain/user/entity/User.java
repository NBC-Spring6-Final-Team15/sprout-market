package com.sprarta.sproutmarket.domain.user.entity;

import com.sprarta.sproutmarket.domain.common.Timestamped;
import com.sprarta.sproutmarket.domain.common.entity.Status;
import com.sprarta.sproutmarket.domain.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "rate")
    private int rate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Column
    private String profileImageUrl;

    public User(String username, String email, String password, String nickname, String phoneNumber, String address, UserRole userRole) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.userRole = userRole;
    }

    public static User forKakao(String username, String email, String nickname, String password, String phoneNumber, String address, String profileImageUrl, UserRole userRole) {
        User user = new User();
        user.username = username;
        user.email = email;
        user.nickname = nickname;
        user.password = password;
        user.phoneNumber = phoneNumber;
        user.address = address;
        user.profileImageUrl = profileImageUrl;
        user.userRole = userRole;
        return user;
    }

    public User(Long id, String email, UserRole userRole) {
        this.id = id;
        this.email = email;
        this.userRole = userRole;
    }

    public static User fromAuthUser(CustomUserDetails customUserDetails) {
        return new User(customUserDetails.getId(), customUserDetails.getEmail(), customUserDetails.getRole());
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void deactivate() {
        this.status = Status.DELETED;
    }

    public void activate() { this.status = Status.ACTIVE; }

    public void plusRate() {
        this.rate++;
    }

    public void minusRate() {
        this.rate--;
    }

    public void changeAddress(String newAddress) {
        if (newAddress == null || newAddress.isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 주소입니다.");
        }
        this.address = newAddress;
    }

    // 프로필 이미지 업데이트 메서드
    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}