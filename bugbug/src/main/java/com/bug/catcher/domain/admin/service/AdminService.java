package com.bug.catcher.domain.admin.service;

import com.bug.catcher.domain.admin.dto.AdminUserResponseDto;
import com.bug.catcher.domain.entity.ApplicationStatus;
import com.bug.catcher.domain.entity.Hunter;
import com.bug.catcher.domain.entity.HunterApplication;
import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.hunter.repository.HunterApplicationRepository;
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
    private final HunterApplicationRepository hunterApplicationRepository;

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

    // [추가] 승인 대기 중인 헌터 신청서 목록 조회
    public Page<com.bug.catcher.domain.admin.dto.AdminApplicationResponseDto> getPendingApplications(Pageable pageable) {
        return hunterApplicationRepository.findByStatus(ApplicationStatus.PENDING, pageable)
                .map(com.bug.catcher.domain.admin.dto.AdminApplicationResponseDto::from);
    }

    @Transactional
    public void approveHunterApplication(Long applicationId) {
        HunterApplication application = hunterApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("신청서를 찾을 수 없습니다."));

        // 1. 신청서 상태를 승인으로 변경
        application.updateStatus(ApplicationStatus.APPROVED);

        // 2. 해당 유저의 권한을 일반 유저에서 헌터로 변경
        User user = application.getUser();
        user.updateRole("HUNTER");

        // 3. [추가됨] 승인과 동시에 헌터 전용 프로필(엔티티) 생성
        Hunter hunter = Hunter.builder()
                .user(user)
                .name(application.getName())
                .pledgeAgreed(application.getPledgeAgreed())
                .grade("슬리퍼 전사") // 진화 시스템의 초기 등급 설정!
                .requestCount(0)
                .responseCount(0)
                .build();
        hunterRepository.save(hunter);
    }
}
