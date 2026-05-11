package com.bug.catcher.domain.user.repository;

import com.bug.catcher.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 정보를 찾는 메서드 (중복 가입 방지 및 로그인 시 ID 확인할 때 필요)
    Optional<User> findByEmail(String email);
    //로그인은 auth도메인에서 하지만 이걸 유저도메인에 두는 이유는 유저테이블을 관리하는건
    //이 리포지터리에서만 관리할거임. 어쏘는 세션발급역할만. 자체적인 직원 명부는 가지고 있지않음.

    // 회원가입 시 중복 체크용 (이메일)
    boolean existsByEmail(String email);

    // 회원가입 시 중복 체크용 (닉네임)
    boolean existsByNickname(String nickname);

    // 역할(role)로 사용자를 필터링하고 페이징 처리하는 메서드 (재혁 0508)
    Page<User> findByRole(String role, Pageable pageable);

    // 닉네임, 전화번호, 주소를 한꺼번에 업데이트하는 쿼리
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.nickname = :nickname, u.phoneNumber = :phone, u.address = :address WHERE u.id = :userId")
    void updateUserInfo(@Param("userId") Long userId,
                        @Param("nickname") String nickname,
                        @Param("phone") String phone,
                        @Param("address") String address);

    //권한 업데이트
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.role = :role WHERE u.id = :userId")
    void updateUserRole(@Param("userId") Long userId, @Param("role") String role);

}