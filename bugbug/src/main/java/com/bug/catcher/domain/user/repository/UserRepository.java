package com.bug.catcher.domain.user.repository;

import com.bug.catcher.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 정보를 찾는 메서드 (중복 가입 방지 및 로그인 시 ID 확인할 때 꼭 필요합니다!)
    Optional<User> findByEmail(String email);
    //로그인은 auth도메인에서 하지만 이걸 유저도메인에 두는 이유는 유저테이블을 관리하는건
    //이 리포지터리에서만 관리할거임. 어쏘는 세션발급역할만. 자체적인 직원 명부는 가지고 있지않음.

    // 회원가입 시 중복 체크용 (이메일)
    boolean existsByEmail(String email);

    // 회원가입 시 중복 체크용 (닉네임)
    boolean existsByNickname(String nickname);


}