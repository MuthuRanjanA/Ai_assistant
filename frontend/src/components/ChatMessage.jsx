import React, { useState } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { Bot, User, Copy, Check } from 'lucide-react';

export default function ChatMessage({ message }) {
  const isUser = message.role === 'user';
  const [copied, setCopied] = useState(false);

  // Copy full response content
  const handleCopyText = async () => {
    try {
      await navigator.clipboard.writeText(message.content);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error('Failed to copy text', err);
    }
  };

  // Custom code block renderer for ReactMarkdown
  const renderers = {
    code({ node, inline, className, children, ...props }) {
      const match = /language-(\w+)/.exec(className || '');
      const language = match ? match[1] : '';
      const codeString = String(children).replace(/\n$/, '');

      if (!inline && language) {
        return <CodeBlock language={language} code={codeString} />;
      } else if (!inline) {
        return <CodeBlock language="text" code={codeString} />;
      }

      return (
        <code className={className} {...props}>
          {children}
        </code>
      );
    }
  };

  return (
    <div className={`message-row ${isUser ? 'user' : 'ai'}`}>
      <div className={`message-avatar ${isUser ? 'user' : 'ai'}`}>
        {isUser ? <User size={18} /> : <Bot size={18} />}
      </div>

      <div className="message-body">
        <div className="message-bubble">
          {isUser ? (
            <div style={{ whiteSpace: 'pre-wrap' }}>{message.content}</div>
          ) : (
            <div className="markdown-content">
              <ReactMarkdown 
                remarkPlugins={[remarkGfm]} 
                components={renderers}
              >
                {message.content}
              </ReactMarkdown>
            </div>
          )}
        </div>

        <div className="message-footer">
          <span>{message.timestamp || ''}</span>

          {!isUser && (
            <button 
              className="btn-msg-action" 
              onClick={handleCopyText} 
              title="Copy message"
              aria-label="Copy message"
            >
              {copied ? <Check size={14} color="var(--success)" /> : <Copy size={14} />}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

function CodeBlock({ language, code }) {
  const [copiedCode, setCopiedCode] = useState(false);

  const copyCode = async () => {
    try {
      await navigator.clipboard.writeText(code);
      setCopiedCode(true);
      setTimeout(() => setCopiedCode(false), 2000);
    } catch (err) {
      console.error('Failed to copy code snippet', err);
    }
  };

  return (
    <div className="code-block-wrapper">
      <div className="code-block-header">
        <span>{language || 'code'}</span>
        <button 
          className="btn-copy-code" 
          onClick={copyCode}
          title="Copy code"
        >
          {copiedCode ? (
            <>
              <Check size={12} color="var(--success)" />
              <span style={{ color: 'var(--success)' }}>Copied!</span>
            </>
          ) : (
            <>
              <Copy size={12} />
              <span>Copy</span>
            </>
          )}
        </button>
      </div>
      <pre>
        <code>{code}</code>
      </pre>
    </div>
  );
}
