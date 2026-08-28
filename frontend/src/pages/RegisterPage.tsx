import { useState } from "react";
import { useNavigate } from "react-router-dom";

import { authApi } from "../api/authApi";
import { useAuth } from "../context/AuthContext";

const POINT_BUY_COSTS: Record<number, number> = {
    8: 0,
    9: 1,
    10: 2,
    11: 3,
    12: 4,
    13: 5,
    14: 7,
    15: 9,
};

const TOTAL_POINTS = 27;

function getPointCost(value: number): number {
    return POINT_BUY_COSTS[value] ?? 0;
}
export default function RegisterPage() {

    const navigate = useNavigate();
    const { login } = useAuth();

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    const [name, setName] = useState("");

    const [strength, setStrength] = useState(8);
    const [dexterity, setDexterity] = useState(8);
    const [constitution, setConstitution] = useState(8);
    const [intelligence, setIntelligence] = useState(8);
    const [wisdom, setWisdom] = useState(8);
    const [charisma, setCharisma] = useState(8);

    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    const spentPoints =
        getPointCost(strength) +
        getPointCost(dexterity) +
        getPointCost(constitution) +
        getPointCost(intelligence) +
        getPointCost(wisdom) +
        getPointCost(charisma);

    const remainingPoints = TOTAL_POINTS - spentPoints;

    const changeAbility = (
        currentValue: number,
        setValue: React.Dispatch<React.SetStateAction<number>>,
        direction: number
    ) => {
        const newValue = currentValue + direction;

        if (newValue < 8 || newValue > 15) {
            return;
        }

        const oldCost = getPointCost(currentValue);
        const newCost = getPointCost(newValue);

        const newSpentPoints = spentPoints - oldCost + newCost;

        if (newSpentPoints > TOTAL_POINTS) {
            return;
        }

        setValue(newValue);
    };

    const handleSubmit = async (
        event: React.FormEvent<HTMLFormElement>
    ) => {

        event.preventDefault();

        setError(null);
        setLoading(true);

        try {

            const response = await authApi.register({
                username,
                password,

                playerRequest: {
                    name,
                    strength,
                    dexterity,
                    constitution,
                    intelligence,
                    wisdom,
                    charisma,
                },
            });

            login(response.token);

            navigate("/");

        } catch (error) {
              console.error("Registration error:", error);
              setError("Registration failed");

        } finally {

            setLoading(false);

        }
    };

    return (
        <div>

            <h1>Create Character</h1>

            <form onSubmit={handleSubmit}>

                <div>
                    <label>Username</label>

                    <input
                        value={username}
                        onChange={(event) =>
                            setUsername(event.target.value)
                        }
                        required
                    />
                </div>

                <div>
                    <label>Password</label>

                    <input
                        type="password"
                        value={password}
                        onChange={(event) =>
                            setPassword(event.target.value)
                        }
                        required
                    />
                </div>

                <div>
                    <label>Character name</label>

                    <input
                        value={name}
                        onChange={(event) =>
                            setName(event.target.value)
                        }
                        required
                    />
                </div>

                <h2>Ability Scores</h2>

                <p>
                    Points spent: {spentPoints} / {TOTAL_POINTS}
                </p>

                <p>
                    Remaining points: {remainingPoints}
                </p>

                <div>
                    <label>Strength</label>

                    <button
                        type="button"
                        onClick={() =>
                            changeAbility(strength, setStrength, -1)
                        }
                        disabled={strength <= 8}
                    >
                        -
                    </button>

                    <span>{strength}</span>

                    <button
                        type="button"
                        onClick={() =>
                            changeAbility(strength, setStrength, 1)
                        }
                        disabled={
                            strength >= 15 ||
                            spentPoints - getPointCost(strength) +
                            getPointCost(strength + 1) > TOTAL_POINTS
                        }
                    >
                        +
                    </button>

                    <span>
                        Cost: {getPointCost(strength)}
                    </span>
                </div>

               <div>
                   <label>Dexterity</label>

                   <button
                       type="button"
                       onClick={() =>
                           changeAbility(dexterity, setDexterity, -1)
                       }
                       disabled={dexterity <= 8}
                   >
                       -
                   </button>

                   <span>{dexterity}</span>

                   <button
                       type="button"
                       onClick={() =>
                           changeAbility(dexterity, setDexterity, 1)
                       }
                       disabled={
                           dexterity >= 15 ||
                           spentPoints - getPointCost(dexterity) +
                           getPointCost(dexterity + 1) > TOTAL_POINTS
                       }
                   >
                       +
                   </button>

                   <span>
                       Cost: {getPointCost(dexterity)}
                   </span>
               </div>

                <div>
                    <label>Constitution</label>

                    <button
                        type="button"
                        onClick={() =>
                            changeAbility(constitution, setConstitution, -1)
                        }
                        disabled={constitution <= 8}
                    >
                        -
                    </button>

                    <span>{constitution}</span>

                    <button
                        type="button"
                        onClick={() =>
                            changeAbility(constitution, setConstitution, 1)
                        }
                        disabled={
                            constitution >= 15 ||
                            spentPoints - getPointCost(constitution) +
                            getPointCost(constitution + 1) > TOTAL_POINTS
                        }
                    >
                        +
                    </button>

                    <span>
                        Cost: {getPointCost(constitution)}
                    </span>
                </div>

                <div>
                    <label>Intelligence</label>

                    <button
                        type="button"
                        onClick={() =>
                            changeAbility(intelligence, setIntelligence, -1)
                        }
                        disabled={intelligence <= 8}
                    >
                        -
                    </button>

                    <span>{intelligence}</span>

                    <button
                        type="button"
                        onClick={() =>
                            changeAbility(intelligence, setIntelligence, 1)
                        }
                        disabled={
                            intelligence >= 15 ||
                            spentPoints - getPointCost(intelligence) +
                            getPointCost(intelligence + 1) > TOTAL_POINTS
                        }
                    >
                        +
                    </button>

                    <span>
                        Cost: {getPointCost(intelligence)}
                    </span>
                </div>

                <div>
                    <label>Wisdom</label>

                    <button
                        type="button"
                        onClick={() =>
                            changeAbility(wisdom, setWisdom, -1)
                        }
                        disabled={wisdom <= 8}
                    >
                        -
                    </button>

                    <span>{wisdom}</span>

                    <button
                        type="button"
                        onClick={() =>
                            changeAbility(wisdom, setWisdom, 1)
                        }
                        disabled={
                            wisdom >= 15 ||
                            spentPoints - getPointCost(wisdom) +
                            getPointCost(wisdom + 1) > TOTAL_POINTS
                        }
                    >
                        +
                    </button>

                    <span>
                        Cost: {getPointCost(wisdom)}
                    </span>
                </div>

                <div>
                    <label>Charisma</label>

                    <button
                        type="button"
                        onClick={() =>
                            changeAbility(charisma, setCharisma, -1)
                        }
                        disabled={charisma <= 8}
                    >
                        -
                    </button>

                    <span>{charisma}</span>

                    <button
                        type="button"
                        onClick={() =>
                            changeAbility(charisma, setCharisma, 1)
                        }
                        disabled={
                            charisma >= 15 ||
                            spentPoints - getPointCost(charisma) +
                            getPointCost(charisma + 1) > TOTAL_POINTS
                        }
                    >
                        +
                    </button>

                    <span>
                        Cost: {getPointCost(charisma)}
                    </span>
                </div>

                {error && (
                    <p>{error}</p>
                )}

              <button
                  type="submit"
                  disabled={loading || spentPoints !== TOTAL_POINTS}
              >
                  {loading
                      ? "Creating..."
                      : spentPoints === TOTAL_POINTS
                          ? "Create Character"
                          : `Spend ${remainingPoints} more points`
                  }
              </button>

            </form>

            <button
                type="button"
                onClick={() => navigate("/login")}
            >
                Already have an account?
            </button>

        </div>
    );
}
