import React, { useState, useEffect } from 'react';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import ChatWindow from './components/ChatWindow';
import ChatInput from './components/ChatInput';
import { sendChatMessage, checkHealth } from './services/chatService';

const STORAGE_KEY = 'ai_assistant_conversations';
const THEME_KEY = 'ai_assistant_theme';

export default function App() {
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem(THEME_KEY) || 'dark';
  });

  const [conversations, setConversations] = useState(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      return saved ? JSON.parse(saved) : [];
    } catch (e) {
      return [];
    }
  });

  const [activeChatId, setActiveChatId] = useState(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      const list = JSON.parse(saved);
      return list.length > 0 ? list[0].id : null;
    }
    return null;
  });

  const [messages, setMessages] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [serverStatus, setServerStatus] = useState('ONLINE');

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem(THEME_KEY, theme);
  }, [theme]);

  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(conversations));
    } catch (e) {
      console.error(e);
    }
  }, [conversations]);

  useEffect(() => {
    if (activeChatId) {
      const current = conversations.find(c => c.id === activeChatId);
      if (current) {
        setMessages(current.messages || []);
      }
    } else {
      setMessages([]);
    }
    setError(null);
  }, [activeChatId]);

  // Periodic health check
  useEffect(() => {
    const testHealth = async () => {
      const res = await checkHealth();
      setServerStatus(res.status === 'UP' ? 'ONLINE' : 'DOWN');
    };
    testHealth();
    const interval = setInterval(testHealth, 20000);
    return () => clearInterval(interval);
  }, []);

  const handleToggleTheme = () => {
    setTheme(prev => prev === 'dark' ? 'light' : 'dark');
  };

  const handleNewChat = () => {
    setActiveChatId(null);
    setMessages([]);
    setError(null);
  };

  const handleDeleteChat = (id) => {
    const updated = conversations.filter(c => c.id !== id);
    setConversations(updated);
    if (activeChatId === id) {
      if (updated.length > 0) {
        setActiveChatId(updated[0].id);
      } else {
        setActiveChatId(null);
        setMessages([]);
      }
    }
  };

  const handleClearAll = () => {
    setConversations([]);
    setActiveChatId(null);
    setMessages([]);
  };

  const handleSendMessage = async (textToSend) => {
    if (!textToSend || isLoading) return;

    setError(null);
    const timestamp = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const userMessage = { role: 'user', content: textToSend, timestamp };

    const newMessages = [...messages, userMessage];
    setMessages(newMessages);
    setIsLoading(true);

    let currentId = activeChatId;

    if (!currentId) {
      currentId = 'chat_' + Date.now();
      const title = textToSend.slice(0, 32) + (textToSend.length > 32 ? '...' : '');
      const newConversation = {
        id: currentId,
        title: title,
        createdAt: new Date().toISOString(),
        messages: newMessages
      };
      setConversations(prev => [newConversation, ...prev]);
      setActiveChatId(currentId);
    } else {
      setConversations(prev =>
        prev.map(c => c.id === currentId ? { ...c, messages: newMessages } : c)
      );
    }

    try {
      const data = await sendChatMessage(newMessages);
      const aiMessage = {
        role: 'assistant',
        content: data.response,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      };

      const finalMessages = [...newMessages, aiMessage];
      setMessages(finalMessages);

      setConversations(prev =>
        prev.map(c => c.id === currentId ? { ...c, messages: finalMessages } : c)
      );
      setServerStatus('ONLINE');
    } catch (err) {
      console.error('Chat error:', err);
      setError(err.message || 'Unable to get response. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="app-container">
      <Sidebar
        conversations={conversations}
        activeId={activeChatId}
        onSelectChat={setActiveChatId}
        onNewChat={handleNewChat}
        onDeleteChat={handleDeleteChat}
        onClearAll={handleClearAll}
      />

      <div className="main-content">
        <Header
          theme={theme}
          onToggleTheme={handleToggleTheme}
          onNewChat={handleNewChat}
          serverStatus={serverStatus}
        />

        <div className="chat-window">
          <ChatWindow
            messages={messages}
            isLoading={isLoading}
            error={error}
            onSendMessage={handleSendMessage}
          />

          <ChatInput
            onSendMessage={handleSendMessage}
            isLoading={isLoading}
          />
        </div>
      </div>
    </div>
  );
}
