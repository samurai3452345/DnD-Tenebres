import type { ReactNode } from "react";

interface AuthShellProps {
    children: ReactNode;
    eyebrow: string;
    title: string;
    subtitle: string;
    wide?: boolean;
}

export default function AuthShell({ children, eyebrow, title, subtitle, wide = false }: AuthShellProps) {
    return (
        <main className="auth-scene">
            <div className="auth-scene__veil" />
            <section className={`auth-card${wide ? " auth-card--wide" : ""}`}>
                <div className="auth-card__horn auth-card__horn--left" aria-hidden="true" />
                <div className="auth-card__horn auth-card__horn--right" aria-hidden="true" />
                <div className="auth-crest" aria-hidden="true">
                    <span className="auth-crest__wing">◆</span>
                    <span className="auth-crest__head">♜</span>
                    <span className="auth-crest__wing">◆</span>
                </div>
                <header className="auth-card__header">
                    <span className="auth-card__eyebrow">{eyebrow}</span>
                    <h1>{title}</h1>
                    <div className="auth-divider" aria-hidden="true"><span /><b>◆</b><span /></div>
                    <p>{subtitle}</p>
                </header>
                {children}
            </section>
            <p className="auth-scene__brand">DnD Tenebres</p>
        </main>
    );
}
