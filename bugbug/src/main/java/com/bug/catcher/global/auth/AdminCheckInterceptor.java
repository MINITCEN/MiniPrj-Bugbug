package com.bug.catcher.global.auth;

import com.bug.catcher.domain.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        HttpSession session = request.getSession(false);
        
        // 세션이 아예 없거나, 유저 정보가 없으면 통과시키지 않습니다. (LoginCheckInterceptor가 먼저 잡겠지만 혹시 모를 방어)
        if (session == null || session.getAttribute(SessionConst.LOGIN_USER) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"로그인이 필요합니다.\"}");
            return false;
        }

        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        // 유저의 역할이 'ADMIN'이 아니라면 접근을 차단합니다.
        if (!"ADMIN".equals(loginUser.getRole())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 에러 반환
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"관리자 전용 기능입니다. 접근 권한이 없습니다.\"}");
            return false;
        }

        // 검문 통과
        return true;
    }
}
