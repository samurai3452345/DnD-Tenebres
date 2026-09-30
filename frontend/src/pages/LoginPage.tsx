import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import { authApi } from "../api/authApi";
import { UserIcon } from "../components/auth/AuthIcons";
import AuthShell from "../components/auth/AuthShell";
import PasswordField from "../components/auth/PasswordField";
import { useAuth } from "../context/AuthContext";
import { getAuthError } from "../utils/authError";

export default function LoginPage() {
    const navigate = useNavigate();
    const { login } = useAuth();
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [remember, setRemember] = useState(true);
    const [notice, setNotice] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        setError(null);
        setNotice(null);
        setLoading(true);
        try {
            const response = await authApi.login({ username: username.trim(), password });
            login(response.token, remember);
            navigate(response.hasCharacter ? "/" : "/account-ready", { replace: true });
        } catch (requestError) {
            setError(getAuthError(requestError, "Неверное имя пользователя или пароль."));
        } finally {
            setLoading(false);
        }
    };

    return (
        <AuthShell eyebrow="Врата в Тенебрис" title="Вход"
            subtitle="Назовите своё имя, странник, и продолжите путь.">
            <form className="auth-form" onSubmit={handleSubmit} noValidate>
                <label className="auth-field" htmlFor="login-username">
                    <span className="sr-only">Имя пользователя</span>
                    <UserIcon className="auth-field__icon" />
                    <input id="login-username" value={username} onChange={(event) => setUsername(event.target.value)}
                        placeholder="Имя пользователя" autoComplete="username" maxLength={32} required autoFocus />
                </label>
                <PasswordField id="login-password" label="Пароль" value={password}
                    onChange={setPassword} autoComplete="current-password" />

                <div className="auth-options">
                    <label className="auth-checkbox">
                        <input type="checkbox" checked={remember} onChange={(event) => setRemember(event.target.checked)} />
                        <span aria-hidden="true" />Запомнить меня
                    </label>
                    <button className="auth-link" type="button"
                        onClick={() => setNotice("Восстановление пароля появится в следующем обновлении.")}>
                        Забыли пароль?
                    </button>
                </div>

                {error && <div className="auth-message auth-message--error" role="alert">{error}</div>}
                {notice && <div className="auth-message" role="status">{notice}</div>}
                <button className="auth-submit auth-submit--red" type="submit" disabled={loading}>
                    <span>{loading ? "Открываем врата…" : "Войти"}</span>
                </button>
            </form>

            <footer className="auth-card__footer">
                <span>Ещё не зарегистрировались?</span>
                <Link className="auth-secondary" to="/register">Зарегистрироваться</Link>
            </footer>
        </AuthShell>
    );
}
