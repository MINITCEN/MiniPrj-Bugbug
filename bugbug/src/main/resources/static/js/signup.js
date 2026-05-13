(function () {
    const signupForm = document.getElementById('signup-form');
    const signupMessage = document.getElementById('signup-message');
    const privacyAgreement = document.getElementById('privacy-agreement');
    const signupSubmit = document.getElementById('signup-submit');
    const redirectUrl = '/';

    if (!signupForm || !signupMessage || !privacyAgreement || !signupSubmit) {
        return;
    }

    syncSubmitState();
    privacyAgreement.addEventListener('change', syncSubmitState);

    signupForm.addEventListener('submit', async event => {
        event.preventDefault();

        if (!privacyAgreement.checked) {
            showMessage('개인정보 수집 및 이용 동의가 필요합니다.', true);
            syncSubmitState();
            return;
        }

        const formData = new FormData(signupForm);
        const payload = {
            nickname: formData.get('nickname')?.toString().trim() || '',
            email: formData.get('email')?.toString().trim() || '',
            password: formData.get('password')?.toString() || '',
            phoneNumber: formData.get('phoneNumber')?.toString().trim() || '',
            address: formData.get('address')?.toString().trim() || '',
            isPrivacyAgreed: privacyAgreement.checked
        };

        clearMessage();
        signupSubmit.disabled = true;

        try {
            const response = await fetch('/api/users/signup', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            const message = await response.text();

            if (!response.ok) {
                showMessage(message || '회원가입 요청에 실패했습니다.', true);
                syncSubmitState();
                return;
            }

            const loginResponse = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    email: payload.email,
                    password: payload.password
                })
            });

            if (!loginResponse.ok) {
                const loginMessage = await loginResponse.text();
                showMessage(loginMessage || '회원가입 후 자동 로그인에 실패했습니다.', true);
                syncSubmitState();
                return;
            }

            showMessage(message || '회원가입이 완료되었습니다.', false);
            window.location.href = redirectUrl;
        } catch (error) {
            console.error(error);
            showMessage('회원가입 요청 중 오류가 발생했습니다.', true);
            syncSubmitState();
        }
    });

    function syncSubmitState() {
        signupSubmit.disabled = !privacyAgreement.checked;
    }

    function clearMessage() {
        signupMessage.textContent = '';
        signupMessage.classList.remove('is-error', 'is-success');
    }

    function showMessage(message, isError) {
        signupMessage.textContent = message;
        signupMessage.classList.remove('is-error', 'is-success');
        signupMessage.classList.add(isError ? 'is-error' : 'is-success');
    }
}());
