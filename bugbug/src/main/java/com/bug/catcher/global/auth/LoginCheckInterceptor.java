package com.bug.catcher.global.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 방문자의 세션(금고)을 확인합니다.
        HttpSession session = request.getSession(false);

        // 2. 세션이 아예 없거나, 우리가 만든 'LOGIN_USER' 출입증이 없다면?
        if (session == null || session.getAttribute(SessionConst.LOGIN_USER) == null) {

            // 한글 깨짐 방지 설정
            response.setContentType("text/plain;charset=UTF-8");
            // 401 (Unauthorized - 미인증) 에러 상태 코드를 보냅니다.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("로그인이 필요한 서비스입니다.");

            // false를 반환하면 더 이상 안쪽(Controller)으로 들어가지 못하고 여기서 차단됩니다!
            return false;
        }

        // 3. 출입증이 무사히 확인되면 true를 반환하여 통과시킵니다.
        return true;
    }
}