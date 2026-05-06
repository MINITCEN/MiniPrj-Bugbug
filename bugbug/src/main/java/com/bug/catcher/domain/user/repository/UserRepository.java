package com.bug.catcher.domain.user.repository;

import com.bug.catcher.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 정보를 찾는 메서드 (중복 가입 방지 및 로그인 시 ID 확인할 때 꼭 필요합니다!)
    Optional<User> findByEmail(String email);

}