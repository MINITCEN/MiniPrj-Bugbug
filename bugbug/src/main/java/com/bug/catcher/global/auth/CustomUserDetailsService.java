package com.bug.catcher.global.auth;

import com.bug.catcher.domain.entity.AccountStatus;
import com.bug.catcher.domain.entity.User;
import com.bug.catcher.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("가입되지 않은 이메일입니다."));

        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            if (user.getBanEndDate() != null && LocalDateTime.now().isAfter(user.getBanEndDate())) {
                user.activate();
            } else {
                String endDateStr = user.getBanEndDate() != null
                        ? user.getBanEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        : "영구";
                throw new DisabledException("정지된 계정입니다. (정지 해제 일시: " + endDateStr + ")");
            }
        }

        if (user.getAccountStatus() == AccountStatus.WITHDRAWN) {
            throw new DisabledException("탈퇴한 계정입니다.");
        }

        return new CustomUserPrincipal(user);
    }
}
