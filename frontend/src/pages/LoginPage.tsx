import { useState } from "react";
import { useNavigate } from "react-router-dom";

import { authApi } from "../api/authApi";
import { useAuth } from "../context/AuthContext";

export default function LoginPage() {

    const navigate = useNavigate();
    const { login } = useAuth();

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (
        event: React.FormEvent<HTMLFormElement>
    ) => {

        event.preventDefault();

        setError(null);
        setLoading(true);

        try {

            const response = await authApi.login({
                username,
                password,
            });

            login(response.token);

            navigate("/");

        } catch (error) {

            setError("Invalid username or password");

        } finally {

            setLoading(false);

        }
    };

    return (
        <div>
            <h1>DnD Tenebres</h1>

            <form onSubmit={handleSubmit}>

                <div>
                    <label>
                        Username
                    </label>

                    <input
                        type="text"
                        value={username}
                        onChange={(event) =>
                            setUsername(event.target.value)
                        }
                    />
                </div>

                <div>
                    <label>
                        Password
                    </label>

                    <input
                        type="password"
                        value={password}
                        onChange={(event) =>
                            setPassword(event.target.value)
                        }
                    />
                </div>

                {error && (
                    <p>{error}</p>
                )}

                <button
                    type="submit"
                    disabled={loading}
                >
                    {loading ? "Logging in..." : "Login"}
                </button>

            </form>

            <button
                type="button"
                onClick={() => navigate("/register")}
            >
                Create account
            </button>

        </div>
    );
}
