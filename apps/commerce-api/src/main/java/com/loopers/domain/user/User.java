package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(name = "kakao_id", nullable = false)
    private Long kakaoId;

    @Column(name = "nickname", nullable = false, length = 20)
    private String nickname;

    @Column(name = "profile_image_url", nullable = false)
    private String profileImageUrl;

    protected User() {}

    private User(Long kakaoId, String nickname, String profileImageUrl) {
        guardNickname(nickname);
        this.kakaoId = kakaoId;
        this.nickname = nickname.trim();
        this.profileImageUrl = profileImageUrl;
    }

    public static User create(Long kakaoId, String nickname, String profileImageUrl) {
        return new User(kakaoId, nickname, profileImageUrl);
    }

    public Long getKakaoId() {
        return kakaoId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void changeNickname(String newNickname) {
        guardNickname(newNickname);
        this.nickname = newNickname.trim();
    }

    public void changeProfileImageUrl(String newUrl) {
        if (newUrl == null || newUrl.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "프로필 이미지 URL은 비어 있을 수 없습니다.");
        }
        this.profileImageUrl = newUrl;
    }

    private void guardNickname(String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "닉네임은 비어 있을 수 없습니다.");
        }
        if (nickname.trim().isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "닉네임은 공백으로만 구성될 수 없습니다.");
        }
        if (nickname.length() > 20) {
            throw new CoreException(ErrorType.BAD_REQUEST, "닉네임은 최대 20자까지 입력할 수 있습니다.");
        }
    }
}
