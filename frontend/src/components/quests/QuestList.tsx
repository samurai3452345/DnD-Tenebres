import React from 'react';
import type { PlayerQuest, QuestTemplate } from '../../types/quest';
import QuestCard from './QuestCard';

interface QuestListProps {
    title: string;
    templates?: QuestTemplate[];
    playerQuests?: PlayerQuest[];
    onAccept?: (id: number) => void;
    onTurnIn?: (id: number) => void;
}

export default function QuestList({ title, templates, playerQuests, onAccept, onTurnIn }: QuestListProps) {
    const hasItems = (templates && templates.length > 0) || (playerQuests && playerQuests.length > 0);

    return (
        <div style={{ marginBottom: '25px' }}>
            <h2 style={{ borderBottom: '2px solid #eee', paddingBottom: '10px', color: '#333' }}>{title}</h2>
            {!hasItems ? (
                <div style={{ color: '#95a5a6', fontStyle: 'italic', padding: '10px' }}>Список пуст.</div>
            ) : (
                <div>
                    {templates?.map(t => <QuestCard key={t.id} questTemplate={t} onAccept={onAccept} />)}
                    {playerQuests?.map(pq => <QuestCard key={pq.id} playerQuest={pq} onTurnIn={onTurnIn} />)}
                </div>
            )}
        </div>
    );
}