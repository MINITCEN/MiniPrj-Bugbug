(function () {
    document.querySelectorAll('[data-password-toggle]').forEach((button) => {
        const field = button.closest('.login-form__field');
        const input = field ? field.querySelector('input[type="password"], input[type="text"]') : null;

        if (!input) {
            return;
        }

        button.addEventListener('click', () => {
            const isHidden = input.type === 'password';
            input.type = isHidden ? 'text' : 'password';
            button.classList.toggle('is-visible', isHidden);
            button.setAttribute('aria-label', isHidden ? '비밀번호 숨기기' : '비밀번호 보기');
            input.focus();
        });
    });
}());
