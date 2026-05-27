document.addEventListener('DOMContentLoaded', () => {
    // 1. 모바일 사이드 드로어 (Drawer) 열기/닫기 이벤트 리스너 바인딩
    const hamburgerBtn = document.querySelector('.mobile-hamburger-btn');
    const drawerCloseBtn = document.querySelector('.mobile-drawer__close');
    const drawerOverlay = document.querySelector('.mobile-drawer__overlay');
    const drawer = document.getElementById("mobile-drawer");

    if (hamburgerBtn && drawer) {
        hamburgerBtn.addEventListener('click', () => {
            drawer.classList.add("is-open");
            document.body.style.overflow = "hidden"; // 모바일 팝업 시 뒷배경 스크롤 방지
        });
    }

    const closeDrawer = () => {
        if (drawer) {
            drawer.classList.remove("is-open");
            document.body.style.overflow = ""; // 바디 스크롤 복원
        }
    };

    if (drawerCloseBtn) {
        drawerCloseBtn.addEventListener('click', closeDrawer);
    }
    if (drawerOverlay) {
        drawerOverlay.addEventListener('click', closeDrawer);
    }

    // 2. 비동기 로그아웃 이벤트 바인딩 (기존 비즈니스 기능 100% 보존)
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

// 3. 인라인 onclick 호출 대비용 글로벌 스코프 Fallback 노출
window.openMobileDrawer = function() {
    const drawer = document.getElementById("mobile-drawer");
    if (drawer) {
        drawer.classList.add("is-open");
        document.body.style.overflow = "hidden";
    }
};

window.closeMobileDrawer = function() {
    const drawer = document.getElementById("mobile-drawer");
    if (drawer) {
        drawer.classList.remove("is-open");
        document.body.style.overflow = "";
    }
};
