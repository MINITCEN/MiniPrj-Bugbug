(function () {
    const loginForm = document.getElementById('login-form');
    const loginMessage = document.getElementById('login-message');
    const redirectUrl = '/';

    if (!loginForm || !loginMessage) {
        return;
    }

    loginForm.addEventListener('submit', async event => {
        event.preventDefault();

        const formData = new FormData(loginForm);
        const payload = {
            email: formData.get('email')?.toString().trim() || '',
            password: formData.get('password')?.toString() || ''
        };

        clearMessage();

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            const message = await response.text();

            if (!response.ok) {
                showMessage(message || '로그인 요청에 실패했습니다.', true);
                return;
            }

            showMessage(message || '로그인에 성공했습니다.', false);
            window.location.href = redirectUrl;
        } catch (error) {
            console.error(error);
            showMessage('로그인 요청 중 오류가 발생했습니다.', true);
        }
    });

    function clearMessage() {
        loginMessage.textContent = '';
        loginMessage.classList.remove('is-error', 'is-success');
    }

    function showMessage(message, isError) {
        loginMessage.textContent = message;
        loginMessage.classList.remove('is-error', 'is-success');
        loginMessage.classList.add(isError ? 'is-error' : 'is-success');
    }
}());
