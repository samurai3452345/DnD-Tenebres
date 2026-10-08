import React, { useEffect, useMemo, useState } from "react";

import Button from "../common/Button";
import type {
    CombatAbilityState,
    CombatAction,
    CombatActionRequest,
    CombatPotionState,
} from "../../types/combat";

interface CombatActionsProps {
    allowedActions: CombatAction[];
    abilities: CombatAbilityState[];
    potions: CombatPotionState[];
    currentEnemyId: number | null;
    onAction: (request: CombatActionRequest) => void;
    disabled?: boolean;
}

function unavailableReason(reason: string | null) {
    if (reason === "MAGIC_FOCUS_REQUIRED") return "нужно магическое оружие";
    if (reason === "NOT_ENOUGH_MANA") return "не хватает маны";
    return "недоступно";
}

export default function CombatActions({
    allowedActions,
    abilities,
    potions,
    currentEnemyId,
    onAction,
    disabled,
}: CombatActionsProps) {
    const availableAbilities = useMemo(() => abilities.filter((ability) => ability.available), [abilities]);
    const availablePotions = useMemo(() => potions.filter((potion) => potion.available), [potions]);
    const [abilityId, setAbilityId] = useState<number | null>(null);
    const [itemId, setItemId] = useState<number | null>(null);
    const allows = (action: CombatAction) => allowedActions.includes(action);

    useEffect(() => {
        setAbilityId((current) => availableAbilities.some((ability) => ability.id === current)
            ? current
            : availableAbilities[0]?.id ?? null);
    }, [availableAbilities]);

    useEffect(() => {
        setItemId((current) => availablePotions.some((potion) => potion.itemId === current)
            ? current
            : availablePotions[0]?.itemId ?? null);
    }, [availablePotions]);

    const actionRequest = (
        action: CombatAction,
        selectedAbilityId: number | null = null,
        selectedItemId: number | null = null,
    ): CombatActionRequest => ({
        action,
        targetId: action === "ATTACK" || action === "CAST_SPELL" ? currentEnemyId : null,
        abilityId: selectedAbilityId,
        itemId: selectedItemId,
    });

    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', padding: '15px', background: '#f5f5f5', borderRadius: '8px' }}>
            <div style={{ display: 'flex', gap: '10px' }}>
                <Button
                    variant="primary"
                    disabled={disabled || !allows("ATTACK") || currentEnemyId === null}
                    onClick={() => onAction(actionRequest("ATTACK"))}
                    style={{ flex: 1 }}
                >
                    ⚔️ Атаковать
                </Button>
                <Button
                    variant="secondary"
                    disabled={disabled || !allows("FLEE")}
                    onClick={() => onAction(actionRequest("FLEE"))}
                    style={{ flex: 1 }}
                >
                    🏃 Сбежать
                </Button>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1fr) auto', gap: '10px', alignItems: 'center', marginTop: '5px' }}>
                <select
                    aria-label="Заклинание"
                    value={abilityId ?? ""}
                    onChange={(event) => setAbilityId(Number(event.target.value))}
                    disabled={disabled || availableAbilities.length === 0}
                    style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc', flexGrow: 1 }}
                >
                    {availableAbilities.length === 0 && <option value="">Нет доступных заклинаний</option>}
                    {abilities.map((ability) => (
                        <option key={ability.id} value={ability.id} disabled={!ability.available}>
                            {ability.name} — {ability.manaCost} маны
                            {!ability.available ? ` (${unavailableReason(ability.unavailableReason)})` : ""}
                        </option>
                    ))}
                </select>
                <Button
                    variant="primary"
                    disabled={disabled || !allows("CAST_SPELL") || abilityId === null || currentEnemyId === null}
                    onClick={() => onAction(actionRequest("CAST_SPELL", abilityId))}
                >
                    ✨ Применить
                </Button>
                <select
                    aria-label="Зелье"
                    value={itemId ?? ""}
                    onChange={(event) => setItemId(Number(event.target.value))}
                    disabled={disabled || availablePotions.length === 0}
                    style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc', flexGrow: 1 }}
                >
                    {availablePotions.length === 0 && <option value="">Нет доступных зелий</option>}
                    {potions.map((potion) => (
                        <option key={potion.itemId} value={potion.itemId} disabled={!potion.available}>
                            {potion.name} × {potion.amount}
                        </option>
                    ))}
                </select>
                <Button
                    variant="primary"
                    disabled={disabled || !allows("USE_POTION") || itemId === null}
                    onClick={() => onAction(actionRequest("USE_POTION", null, itemId))}
                >
                    🧪 Выпить
                </Button>
            </div>
        </div>
    );
}
