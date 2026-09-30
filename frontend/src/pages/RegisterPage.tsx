import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { authApi } from "../api/authApi";
import { UserIcon } from "../components/auth/AuthIcons";
import AuthShell from "../components/auth/AuthShell";
import PasswordField from "../components/auth/PasswordField";
import { useAuth } from "../context/AuthContext";
import { getAuthError } from "../utils/authError";

export default function RegisterPage() {
    const navigate = useNavigate();
    const { login } = useAuth();
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const validate = () => {
        const normalized = username.trim();
        if (normalized.length < 3 || normalized.length > 32) {
            return "Имя пользователя должно содержать от 3 до 32 символов.";
        }
        if (!/^[\p{L}\p{N}_.-]+$/u.test(normalized)) {
            return "В имени можно использовать буквы, цифры, точку, дефис и подчёркивание.";
        }
        if (password.length < 8 || password.length > 72) {
            return "Пароль должен содержать от 8 до 72 символов.";
        }
        if (!/\p{L}/u.test(password) || !/\d/.test(password)) {
            return "Добавьте в пароль хотя бы одну букву и одну цифру.";
        }
        if (password !== confirmPassword) return "Пароли не совпадают.";
        return null;
    };

    const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const validationError = validate();
        setError(validationError);
        if (validationError) return;

        setLoading(true);
        try {
            const response = await authApi.register({ username: username.trim(), password });
            login(response.token, true);
            navigate("/account-ready", { replace: true });
        } catch (requestError) {
            setError(getAuthError(requestError, "Не удалось создать аккаунт. Попробуйте другое имя."));
        } finally {
            setLoading(false);
        }
    };

    return (
        <AuthShell eyebrow="Новая клятва" title="Регистрация"
            subtitle="Создайте аккаунт. Своего героя вы сможете создать отдельно.">
            <form className="auth-form" onSubmit={handleSubmit} noValidate>
                <label className="auth-field" htmlFor="register-username">
                    <span className="sr-only">Имя пользователя</span>
                    <UserIcon className="auth-field__icon" />
                    <input id="register-username" value={username} onChange={(event) => setUsername(event.target.value)}
                        placeholder="Имя пользователя" autoComplete="username" minLength={3} maxLength={32} required autoFocus />
                </label>
                <PasswordField id="register-password" label="Пароль" value={password}
                    onChange={setPassword} autoComplete="new-password" />
                <PasswordField id="register-password-confirm" label="Подтвердите пароль" value={confirmPassword}
                    onChange={setConfirmPassword} autoComplete="new-password" />

                <p className="auth-hint">Минимум 8 символов, хотя бы одна буква и одна цифра.</p>
                {error && <div className="auth-message auth-message--error" role="alert">{error}</div>}
                <button className="auth-submit auth-submit--blue" type="submit" disabled={loading}>
                    <span>{loading ? "Создаём аккаунт…" : "Зарегистрироваться"}</span>
                </button>
            </form>

            <footer className="auth-card__footer auth-card__footer--compact">
                <span>Уже есть аккаунт?</span>
                <Link className="auth-secondary auth-secondary--red" to="/login">Войти</Link>
            </footer>
        </AuthShell>
    );
}
