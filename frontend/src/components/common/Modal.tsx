import React from 'react';

interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    title: string;
    children: React.ReactNode;
}

export default function Modal({ isOpen, onClose, title, children }: ModalProps) {
    if (!isOpen) return null;

    return (
        <div
            className="modal-overlay"
            onClick={onClose}
            style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 }}
        >
            <div
                className="modal-content"
                onClick={(e) => e.stopPropagation()} // Чтобы клик по самому окну не закрывал его
                style={{ backgroundColor: 'white', padding: '20px', borderRadius: '8px', minWidth: '300px', color: 'black' }}
            >
                <div className="modal-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #ccc', paddingBottom: '10px', marginBottom: '15px' }}>
                    <h2 style={{ margin: 0 }}>{title}</h2>
                    <button className="modal-close" onClick={onClose} style={{ cursor: 'pointer', background: 'none', border: 'none', fontSize: '1.5rem' }}>&times;</button>
                </div>
                <div className="modal-body">
                    {children}
                </div>
            </div>
        </div>
    );
}