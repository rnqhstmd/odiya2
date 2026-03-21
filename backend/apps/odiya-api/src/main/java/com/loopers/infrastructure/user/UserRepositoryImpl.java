package com.loopers.infrastructure.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id);
    }

    @Override
    public Optional<User> findActiveById(Long id) {
        return userJpaRepository.findActiveById(id);
    }

    @Override
    public Optional<User> findActiveByKakaoId(Long kakaoId) {
        return userJpaRepository.findActiveByKakaoId(kakaoId);
    }

    @Override
    public boolean existsByNicknameAndDeletedAtIsNull(String nickname) {
        return userJpaRepository.existsByNicknameAndDeletedAtIsNull(nickname);
    }

    @Override
    public boolean existsByNicknameAndDeletedAtIsNullAndIdNot(String nickname, Long id) {
        return userJpaRepository.existsByNicknameAndDeletedAtIsNullAndIdNot(nickname, id);
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public List<User> searchByNickname(String keyword, Long excludeUserId, Pageable pageable) {
        return userJpaRepository.searchByNickname(keyword, excludeUserId, pageable);
    }
}
