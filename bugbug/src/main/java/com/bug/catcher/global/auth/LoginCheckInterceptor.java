package com.bug.catcher.global.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 방문자의 세션 확인.
        HttpSession session = request.getSession(false);

        // 2. 세션이 아예 없거나, 'LOGIN_USER' 출입증이 없는지 체크
        if (session == null || session.getAttribute(SessionConst.LOGIN_USER) == null) {

            // 한글 깨짐 방지 설정
            response.setContentType("text/plain;charset=UTF-8");
            // 401 (Unauthorized - 미인증)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("로그인이 필요한 서비스입니다.");

            // false를 반환하면 더 이상 들어가지 못하고 차단
            return false;
        }

        // 3. 출입증 확인되면 true를 반환하여 통과.
        return true;
    }
}