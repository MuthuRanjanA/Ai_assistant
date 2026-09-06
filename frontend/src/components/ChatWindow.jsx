import React, { useEffect, useRef } from 'react';
import ChatMessage from './ChatMessage';
import { Bot, AlertCircle } from 'lucide-react';

export default function ChatWindow({
  messages,
  isLoading,
  error,
  onSendMessage
}) {
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isLoading]);

  const quickPrompts = [
    "What is the difference between Spring Boot and Spring MVC?",
    "Write a Java method to reverse a string efficiently.",
    "Explain RESTful API design best practices."
  ];

  return (
    <div className="messages-container">
      <div className="messages-inner">
        {messages.length === 0 ? (
          <div className="welcome-container">
            <div className="welcome-badge">
              <Bot size={16} />
              <span>AI Assistant</span>
            </div>

            <h1 className="welcome-title">How can I help you today?</h1>
            <p className="welcome-subtitle">
              Ask anything, write code, or explore ideas.
            </p>

            <div className="prompt-suggestions">
              {quickPrompts.map((prompt, index) => (
                <div
                  key={index}
                  className="suggestion-card"
                  onClick={() => onSendMessage(prompt)}
                >
                  <div className="suggestion-desc">{prompt}</div>
                </div>
              ))}
            </div>
          </div>
        ) : (
          messages.map((msg, index) => (
            <ChatMessage
              key={index}
              message={msg}
            />
          ))
        )}

        {/* Loading Indicator */}
        {isLoading && (
          <div className="message-row ai">
            <div className="message-avatar ai">
              <Bot size={18} />
            </div>
            <div className="message-body">
              <div className="message-bubble">
                <div className="typing-indicator">
                  <div className="typing-dot"></div>
                  <div className="typing-dot"></div>
                  <div className="typing-dot"></div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Error Banner */}
        {error && (
          <div className="error-banner">
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <AlertCircle size={18} />
              <span>{error}</span>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>
    </div>
  );
}
