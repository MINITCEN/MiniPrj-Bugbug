package com.bug.catcher.domain.admin.service;

import com.bug.catcher.domain.admin.dto.AdminUserResponseDto;
import com.bug.catcher.domain.entity.Hunter;
import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.hunter.repository.HunterRepository;
import com.bug.catcher.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final HunterRepository hunterRepository;

    public Page<AdminUserResponseDto> getUsers(String role, Pageable pageable) {
        Page<User> users;
        // role 파라미터가 비어있거나 null이면 전체 조회
        if (role == null || role.trim().isEmpty()) {
            users = userRepository.findAll(pageable);
        } else {
            // role 파라미터가 있으면 해당 역할로 필터링 조회
            users = userRepository.findByRole(role, pageable);
        }
        
        // 엔티티를 DTO로 변환하여 반환
        return users.map(AdminUserResponseDto::from);
    }

    @Transactional
    public void approveHunter(Long hunterId) {
        // 1. 헌터 정보를 찾습니다.
        Hunter hunter = hunterRepository.findById(hunterId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 헌터 신청 내역입니다."));

        // 2. 헌터와 연결된 유저 정보를 가져와서 권한을 'HUNTER'로 승격시킵니다.
        User user = hunter.getUser();
        if ("HUNTER".equals(user.getRole())) {
            throw new IllegalStateException("이미 헌터로 승인된 유저입니다.");
        }
        
        user.updateRole("HUNTER");
        // 더티 체킹(Dirty Checking)으로 인해 별도의 save 호출 없이 DB에 반영됩니다.
    }
}
