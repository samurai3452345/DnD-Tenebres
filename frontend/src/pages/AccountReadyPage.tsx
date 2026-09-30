import { useNavigate } from "react-router-dom";
import AuthShell from "../components/auth/AuthShell";
import { useAuth } from "../context/AuthContext";

export default function AccountReadyPage() {
    const navigate = useNavigate();
    const { logout } = useAuth();

    const handleLogout = () => {
        logout();
        navigate("/login", { replace: true });
    };

    return (
        <AuthShell eyebrow="Клятва принята" title="Аккаунт создан"
            subtitle="Вход выполнен. Создание героя будет добавлено отдельным экраном по вашему следующему референсу.">
            <div className="account-ready">
                <div className="account-ready__sigil" aria-hidden="true">◆</div>
                <p>Учётная запись готова, а сессия сохранена. Вы сможете продолжить отсюда, когда появится мастер создания персонажа.</p>
                <button className="auth-secondary auth-secondary--red" type="button" onClick={handleLogout}>Выйти</button>
            </div>
        </AuthShell>
    );
}
