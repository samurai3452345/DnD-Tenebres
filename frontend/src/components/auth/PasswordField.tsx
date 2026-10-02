import { useState } from "react";
import { EyeIcon, LockIcon } from "./AuthIcons";

interface PasswordFieldProps {
    id: string;
    name: string;
    label: string;
    value: string;
    autoComplete: string;
    onChange: (value: string) => void;
}

export default function PasswordField({ id, name, label, value, autoComplete, onChange }: PasswordFieldProps) {
    const [visible, setVisible] = useState(false);
    return (
        <label className="auth-field" htmlFor={id}>
            <span className="sr-only">{label}</span>
            <LockIcon className="auth-field__icon" />
            <input id={id} name={name} type={visible ? "text" : "password"} value={value}
                onChange={(event) => onChange(event.target.value)} placeholder={label}
                autoComplete={autoComplete} minLength={8} maxLength={72} required />
            <button className="auth-field__action" type="button" onClick={() => setVisible((current) => !current)}
                aria-label={visible ? "Скрыть пароль" : "Показать пароль"}>
                <EyeIcon open={!visible} />
            </button>
        </label>
    );
}
