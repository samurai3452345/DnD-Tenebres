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

    const validate = (submittedUsername: string, submittedPassword: string, submittedConfirmation: string) => {
        const normalized = submittedUsername.trim();
        if (normalized.length < 3 || normalized.length > 32) {
            return "Имя пользователя должно содержать от 3 до 32 символов.";
        }
        if (!/^[\p{L}\p{N}_.-]+$/u.test(normalized)) {
            return "В имени можно использовать буквы, цифры, точку, дефис и подчёркивание.";
        }
        if (submittedPassword.length < 8 || submittedPassword.length > 72) {
            return "Пароль должен содержать от 8 до 72 символов.";
        }
        if (!/\p{L}/u.test(submittedPassword) || !/\d/.test(submittedPassword)) {
            return "Добавьте в пароль хотя бы одну букву и одну цифру.";
        }
        if (submittedPassword !== submittedConfirmation) return "Пароли не совпадают.";
        return null;
    };

    const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const formData = new FormData(event.currentTarget);
        const submittedUsername = String(formData.get("username") ?? "").trim();
        const submittedPassword = String(formData.get("password") ?? "");
        const submittedConfirmation = String(formData.get("passwordConfirmation") ?? "");
        const validationError = validate(
            submittedUsername,
            submittedPassword,
            submittedConfirmation,
        );
        setError(validationError);
        if (validationError) return;

        setLoading(true);
        try {
            const response = await authApi.register({
                username: submittedUsername,
                password: submittedPassword,
            });
            login(response.token, true);
            navigate("/create-character", { replace: true });
        } catch (requestError) {
            setError(getAuthError(requestError, "Не удалось создать аккаунт. Попробуйте другое имя."));
        } finally {
            setLoading(false);
        }
    };

    return (
        <AuthShell eyebrow="Новая клятва" title="Регистрация"
            subtitle="Создайте аккаунт. Своего героя вы сможете создать отдельно." variant="register">
            <form className="auth-form" onSubmit={handleSubmit} noValidate>
                <label className="auth-field" htmlFor="register-username">
                    <span className="sr-only">Имя пользователя</span>
                    <UserIcon className="auth-field__icon" />
                    <input id="register-username" name="username" value={username} onChange={(event) => setUsername(event.target.value)}
                        placeholder="Имя пользователя" autoComplete="username" minLength={3} maxLength={32} required autoFocus />
                </label>
                <PasswordField id="register-password" name="password" label="Пароль" value={password}
                    onChange={setPassword} autoComplete="new-password" />
                <PasswordField id="register-password-confirm" name="passwordConfirmation" label="Подтвердите пароль" value={confirmPassword}
                    onChange={setConfirmPassword} autoComplete="new-password" />

                <p className="auth-hint">Минимум 8 символов, хотя бы одна буква и одна цифра.</p>
                <div className="auth-message-slot" aria-live="polite" aria-atomic="true">
                    {error && <div className="auth-message auth-message--error" role="alert">{error}</div>}
                </div>
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
