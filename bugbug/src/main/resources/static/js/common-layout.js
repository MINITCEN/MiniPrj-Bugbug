(function () {
    const logoutButtons = document.querySelectorAll('.js-logout-button');

    if (!logoutButtons.length) {
        return;
    }

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
}());
