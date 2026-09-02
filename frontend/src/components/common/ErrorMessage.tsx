import React from 'react';

interface ErrorMessageProps {
    message: string | null;
}

export default function ErrorMessage({ message }: ErrorMessageProps) {
    if (!message) return null;

    return (
        <div className="error-message" role="alert" style={{ color: 'red', border: '1px solid red', padding: '10px', borderRadius: '4px', marginBottom: '10px' }}>
            <span>{message}</span>
        </div>
    );
}