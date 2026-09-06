import React from 'react';
import { Bot, Sun, Moon, Plus } from 'lucide-react';

export default function Header({ 
  theme, 
  onToggleTheme, 
  onNewChat, 
  serverStatus 
}) {
  return (
    <header className="app-header">
      <div className="header-left">
        <div className="brand">
          <div className="brand-icon-wrapper">
            <Bot size={22} />
          </div>
          <span>AI Assistant</span>
        </div>

        <div className="status-badge" title={`Server status: ${serverStatus}`}>
          <div className={`status-dot ${serverStatus === 'DOWN' ? 'offline' : ''}`} />
          <span>{serverStatus === 'DOWN' ? 'Offline' : 'Online'}</span>
        </div>
      </div>

      <div className="header-right">
        <button 
          className="btn-primary" 
          onClick={onNewChat}
          title="Start a new conversation"
        >
          <Plus size={18} />
          <span>New Chat</span>
        </button>

        <button 
          className="btn-icon" 
          onClick={onToggleTheme}
          title={theme === 'dark' ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
          aria-label="Toggle Theme"
        >
          {theme === 'dark' ? <Sun size={19} /> : <Moon size={19} />}
        </button>
      </div>
    </header>
  );
}
