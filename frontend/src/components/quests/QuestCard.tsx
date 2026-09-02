import React from 'react';
import type { PlayerQuest, QuestTemplate } from '../../types/quest';
import QuestProgress from './QuestProgress';
import Button from '../common/Button';

interface QuestCardProps {
    questTemplate?: QuestTemplate;
    playerQuest?: PlayerQuest;
    onAccept?: (id: number) => void;
    onTurnIn?: (id: number) => void;
}

export default function QuestCard({ questTemplate, playerQuest, onAccept, onTurnIn }: QuestCardProps) {
    const template = playerQuest ? playerQuest.questTemplate : questTemplate;
    if (!template) return null;

    const isAvailable = !!questTemplate && !playerQuest;
    const isActive = playerQuest?.questStatus === 'ACTIVE';
    const isCompleted = playerQuest?.questStatus === 'COMPLETED';

    return (
        <div style={{ border: '1px solid #dcdcdc', padding: '15px', borderRadius: '8px', background: '#fff', marginBottom: '15px', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' }}>
            <h3 style={{ margin: '0 0 8px 0', color: '#2c3e50' }}>📜 {template.name}</h3>
            <p style={{ margin: '0 0 15px 0', fontSize: '0.9rem', color: '#7f8c8d' }}>{template.description}</p>

            <div style={{ fontSize: '0.85rem', background: '#f8f9fa', padding: '10px', borderRadius: '6px', marginBottom: '10px' }}>
                <div><strong>🎯 Цель:</strong> {template.targetIdentifier} ({template.targetCount} шт.)</div>
                <div><strong>🎁 Награда:</strong> {template.rewardXp} XP | {template.rewardGold} 🪙</div>
            </div>

            {playerQuest && (
                <QuestProgress current={playerQuest.currentProgress} target={template.targetCount} />
            )}

            <div style={{ marginTop: '15px', textAlign: 'right' }}>
                {isAvailable && onAccept && (
                    <Button onClick={() => onAccept(template.id)}>Взять квест</Button>
                )}
                {isActive && (
                    <span style={{ color: '#e67e22', fontWeight: 'bold', fontSize: '0.9rem' }}>В процессе...</span>
                )}
                {isCompleted && onTurnIn && (
                    <Button onClick={() => onTurnIn(playerQuest!.id)} style={{ backgroundColor: '#27ae60' }}>Сдать квест</Button>
                )}
            </div>
        </div>
    );
}