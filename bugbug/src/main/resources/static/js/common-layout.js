document.addEventListener('DOMContentLoaded', () => {
    // 1. 비동기 로그아웃 이벤트 바인딩 (기존 비즈니스 기능 100% 보존 및 오작동 차단)
    const logoutButtons = document.querySelectorAll('.js-logout-button');
    
    if (logoutButtons.length > 0) {
        logoutButtons.forEach(button => {
            button.addEventListener('click', async () => {
                try {
                    const response = await fetch('/api/auth/logout', {
                        method: 'POST'
                    });

                    if (!response.ok) {
                        throw new Error(`Logout failed: ${response.status}`);
                    }

                    window.location.href = "/";
                } catch (error) {
                    console.error(error);
                    window.alert('로그아웃에 실패했습니다.');
                }
            });
        });
    }
});
