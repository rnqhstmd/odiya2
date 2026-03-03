package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class UserService {

    private final UserRepository userRepository;

    @Value("${service.default-profile-image-url}")
    private String defaultProfileImageUrl;

    @Transactional
    public User findOrCreateUser(Long kakaoId, String nickname, String profileImageUrl) {
        return userRepository.findActiveByKakaoId(kakaoId)
            .orElseGet(() -> {
                String resolvedProfileImageUrl = profileImageUrl != null
                    ? profileImageUrl
                    : defaultProfileImageUrl;
                User newUser = User.create(kakaoId, nickname, resolvedProfileImageUrl);
                return userRepository.save(newUser);
            });
    }

    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        return userRepository.findActiveById(userId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 회원입니다."));
    }

    @Transactional
    public User updateNickname(Long userId, String newNickname) {
        User user = getUser(userId);
        if (userRepository.existsByNicknameAndDeletedAtIsNullAndIdNot(newNickname, user.getId())) {
            throw new CoreException(ErrorType.CONFLICT, "이미 사용 중인 닉네임입니다.");
        }
        user.changeNickname(newNickname);
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfileImage(Long userId, String newUrl) {
        User user = getUser(userId);
        user.changeProfileImageUrl(newUrl);
        return userRepository.save(user);
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = getUser(userId);
        user.delete();
        userRepository.save(user);
    }
}
