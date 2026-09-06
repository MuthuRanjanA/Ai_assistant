import React from 'react';
import { MessageSquare, Plus, Trash2 } from 'lucide-react';

export default function Sidebar({
  conversations,
  activeId,
  onSelectChat,
  onNewChat,
  onDeleteChat,
  onClearAll
}) {
  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <button 
          className="btn-primary" 
          style={{ width: '100%', justifyContent: 'center' }} 
          onClick={onNewChat}
        >
          <Plus size={18} />
          <span>New Chat</span>
        </button>
      </div>

      <div className="sidebar-conversations">
        <div className="section-label">Conversations ({conversations.length})</div>
        
        {conversations.length === 0 ? (
          <div style={{ padding: '1rem', color: 'var(--text-muted)', fontSize: '0.85rem', textAlign: 'center' }}>
            No previous chats yet.
          </div>
        ) : (
          conversations.map((chat) => (
            <div
              key={chat.id}
              className={`conversation-item ${chat.id === activeId ? 'active' : ''}`}
              onClick={() => onSelectChat(chat.id)}
            >
              <div className="conversation-title" title={chat.title}>
                <MessageSquare size={16} style={{ flexShrink: 0 }} />
                <span>{chat.title || 'Untitled Conversation'}</span>
              </div>

              <button
                className="btn-delete-chat"
                onClick={(e) => {
                  e.stopPropagation();
                  if (window.confirm('Delete this conversation?')) {
                    onDeleteChat(chat.id);
                  }
                }}
                title="Delete chat"
                aria-label="Delete chat"
              >
                <Trash2 size={15} />
              </button>
            </div>
          ))
        )}
      </div>

      {conversations.length > 0 && (
        <div className="sidebar-footer">
          <button 
            className="btn-danger" 
            onClick={() => {
              if (window.confirm('Are you sure you want to clear ALL conversations from localStorage?')) {
                onClearAll();
              }
            }}
          >
            <Trash2 size={16} />
            <span>Clear All Chats</span>
          </button>
        </div>
      )}
    </aside>
  );
}
