import React from 'react';
import type { Player } from '../../types/player';
import HealthBar from './HealthBar';

interface PlayerStatsProps {
    player: Player;
}

export default function PlayerStats({ player }: PlayerStatsProps) {
    return (
        <div className="player-stats-panel" style={{ border: '1px solid #ccc', padding: '20px', borderRadius: '8px', background: '#fdfdfd', color: '#333' }}>
            <h2 style={{ marginTop: 0, borderBottom: '2px solid #eee', paddingBottom: '10px' }}>
                {player.playerName} <span style={{ fontSize: '1rem', color: '#777' }}>(Ур. {player.level})</span>
            </h2>

            <HealthBar currentHp={player.currentHp} maxHp={player.maxHp} />

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', margin: '15px 0' }}>
                <div><strong>Опыт:</strong> {player.experience}</div>
                <div><strong>Золото:</strong> {player.gold} 🪙</div>
                <div><strong>Броня (AC):</strong> {player.armorClass}</div>
                <div><strong>Очки статов:</strong> {player.statPoints}</div>
            </div>

            <h3 style={{ borderBottom: '1px solid #eee', paddingBottom: '5px', fontSize: '1.1rem' }}>Характеристики</h3>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px', fontSize: '0.95rem' }}>
                <div>💪 Сила: {player.totalStrength}</div>
                <div>🏃 Ловк.: {player.totalDexterity}</div>
                <div>🛡️ Тело.: {player.totalConstitution}</div>
                <div>🧠 Интел.: {player.totalIntelligence}</div>
                <div>🦉 Мудр.: {player.totalWisdom}</div>
                <div>✨ Харизм.: {player.totalCharisma}</div>
            </div>
        </div>
    );
}