import { useNavigate } from "react-router-dom";

interface GameSectionPlaceholderPageProps {
    title: string;
}

export default function GameSectionPlaceholderPage({ title }: GameSectionPlaceholderPageProps) {
    const navigate = useNavigate();

    return (
        <main className="game-section-placeholder">
            <section>
                <h1>{title}</h1>
                <p>Раздел находится в разработке.</p>
                <button type="button" onClick={() => navigate("/")}>Вернуться в игру</button>
            </section>
        </main>
    );
}
