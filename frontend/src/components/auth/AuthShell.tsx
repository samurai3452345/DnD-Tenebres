import type { ReactNode } from "react";

interface AuthShellProps {
    children: ReactNode;
    eyebrow: string;
    title: string;
    subtitle: string;
    wide?: boolean;
    variant?: "login" | "register" | "neutral";
}

export default function AuthShell({
    children,
    eyebrow,
    title,
    subtitle,
    wide = false,
    variant = "neutral",
}: AuthShellProps) {
    return (
        <main className="auth-scene">
            <div className="auth-scene__veil" />
            <section className={`auth-card auth-card--ornate auth-card--${variant}${wide ? " auth-card--wide" : ""}`}>
                <img
                    className="auth-card__frame"
                    src="/assets/auth/dragon-auth-frame.png"
                    alt=""
                    aria-hidden="true"
                />
                <div className="auth-card__surface">
                    <header className="auth-card__header">
                        <span className="auth-card__eyebrow">{eyebrow}</span>
                        <h1>{title}</h1>
                        <div className="auth-divider" aria-hidden="true"><span /><b>◆</b><span /></div>
                        <p>{subtitle}</p>
                    </header>
                    {children}
                </div>
            </section>
            <p className="auth-scene__brand">DnD Tenebres</p>
        </main>
    );
}
