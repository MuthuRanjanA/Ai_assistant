# AI Assistant

A modern, full-stack, multi-turn Conversational AI Assistant application built with **React**, **Vite**, **Spring Boot**, and the **Google Gemini API**.

---

## Description

**AI Assistant** is a clean, lightweight, portfolio-grade intelligent chatbot. It facilitates natural conversational exchanges with Google Gemini, preserves multi-turn conversational context, retains conversation histories locally in browser `localStorage`, formats responses with rich Markdown (including copyable syntax-highlighted code blocks), and provides modern features such as Voice-to-Text input and Text-to-Speech audio readout.

---

## Features

- **Multi-Turn Context Awareness**: Remembers prior context within the conversation so the LLM responds accurately to follow-up questions.
- **Google Gemini API Integration**: Direct integration with official Gemini models (`gemini-1.5-flash`, `gemini-1.5-pro`, `gemini-2.0-flash`).
- **Rich Markdown Rendering**: Formats headings, bold/italics, bullet lists, blockquotes, and tables.
- **Copyable Code Blocks**: One-click copying for code blocks and full AI responses.
- **Zero-Database Persistence**: Conversations and settings are stored locally in browser `localStorage` under `ai_assistant_conversations`.
- **Speech Capabilities**:
  - **Voice Input**: Web Speech Recognition API to speak prompts.
  - **Text-to-Speech**: SpeechSynthesis API to listen to assistant answers.
- **Dark & Light Themes**: Sleek glassmorphism dark theme (default) and clean light theme with instant switching.
- **Stop & Regenerate Generation**: Stop active LLM generation at any time with `AbortController` or regenerate the last response.
- **Responsive Design**: Designed for desktop, tablet, and mobile layouts with a slide-out sidebar.
- **Strict Security**: API keys are isolated on the Spring Boot backend and never exposed to the client.

---

## Architecture

```text
┌─────────────────────────────────────────────────────────┐
│                    React Frontend (Vite)                │
│  - ChatWindow & ChatMessage (ReactMarkdown, RemarkGFM)  │
│  - ChatInput (Auto-resizing Textarea & Web Speech API)  │
│  - Sidebar & History (Browser localStorage)             │
│  - Axios HTTP Client                                    │
└────────────────────────────┬────────────────────────────┘
                             │ POST /api/chat
                             │ (messages: [{role, content}])
                             ▼
┌─────────────────────────────────────────────────────────┐
│              Spring Boot REST API Backend               │
│  - CorsConfig (Permits http://localhost:5173)           │
│  - ChatController (POST /api/chat, GET /api/health)     │
│  - GeminiService (Translates DTOs & Invokes Gemini API) │
│  - Spring 6 RestClient HTTP Engine                      │
└────────────────────────────┬────────────────────────────┘
                             │ POST https://generativelanguage.googleapis.com
                             │ Header: x-goog-api-key: ${GEMINI_API_KEY}
                             ▼
┌─────────────────────────────────────────────────────────┐
│                 Google Gemini API Engine                │
│  - Model: gemini-1.5-flash                              │
└─────────────────────────────────────────────────────────┘
```

---

## Technology Stack

### Frontend
- **Framework**: React 18
- **Build Tool**: Vite
- **Language**: JavaScript (ES6+)
- **HTTP Client**: Axios
- **Markdown Renderer**: `react-markdown` + `remark-gfm`
- **Icons**: `lucide-react`
- **Styling**: Vanilla CSS (Custom Design System with CSS Variables)
- **Storage**: Browser `localStorage`

### Backend
- **Language**: Java 17+
- **Framework**: Spring Boot 3.3+
- **Build Tool**: Apache Maven
- **Modules**: Spring Web (`spring-boot-starter-web`), Spring Validation (`spring-boot-starter-validation`)
- **HTTP Client**: Spring 6 `RestClient`
- **LLM Engine**: Google Gemini REST API (`/v1beta/models/...`)

---

## Requirements

Ensure you have the following installed on your machine:
- **Node.js**: v18.0.0 or higher ([Download Node.js](https://nodejs.org/))
- **Java Development Kit (JDK)**: Java 17 or higher ([Download Eclipse Temurin / Oracle JDK](https://adoptium.net/))
- **Apache Maven**: 3.8+ (or use the included Maven wrapper)
- **Google Gemini API Key**: Free tier available from [Google AI Studio](https://aistudio.google.com/)

---

## Project Structure

```text
ai-assistant/
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── ChatWindow.jsx
│   │   │   ├── ChatMessage.jsx
│   │   │   ├── ChatInput.jsx
│   │   │   ├── Sidebar.jsx
│   │   │   └── Header.jsx
│   │   │
│   │   ├── services/
│   │   │   └── chatService.js
│   │   │
│   │   ├── App.jsx
│   │   ├── main.jsx
│   │   └── index.css
│   │
│   ├── index.html
│   ├── vite.config.js
│   ├── package.json
│   ├── .env.example
│   └── .gitignore
│
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/
│   │       │       └── example/
│   │       │           └── aiassistant/
│   │       │               ├── AiAssistantApplication.java
│   │       │               │
│   │       │               ├── controller/
│   │       │               │   └── ChatController.java
│   │       │               │
│   │       │               ├── service/
│   │       │               │   └── GeminiService.java
│   │       │               │
│   │       │               ├── dto/
│   │       │               │   ├── ChatRequest.java
│   │       │               │   ├── ChatResponse.java
│   │       │               │   └── Message.java
│   │       │               │
│   │       │               └── config/
│   │       │                   └── CorsConfig.java
│   │       │
│   │       └── resources/
│   │           └── application.properties
│   │
│   ├── pom.xml
│   ├── .env.example
│   └── .gitignore
│
└── README.md
```

---

## Gemini API Key Setup

1. Navigate to [Google AI Studio](https://aistudio.google.com/).
2. Sign in with your Google account.
3. Click **"Get API key"** and create a new API key.
4. Keep this key confidential. **Never commit this key to Git.**

---

## Backend Setup

1. Open PowerShell and navigate to the `backend` directory:
   ```powershell
   cd backend
   ```

2. Set your environment variables in PowerShell:
   ```powershell
   $env:GEMINI_API_KEY="your_actual_gemini_api_key_here"
   $env:GEMINI_MODEL="gemini-1.5-flash"
   ```

   *(Optionally create a local `.env` file for reference or IDE run configurations).*

3. Build and test the project using Maven:
   ```powershell
   mvn clean install
   ```

4. Run the Spring Boot application:
   ```powershell
   mvn spring-boot:run
   ```

   The backend will start at: `http://localhost:8080`

---

## Frontend Setup

1. Open a new PowerShell terminal and navigate to the `frontend` directory:
   ```powershell
   cd frontend
   ```

2. Create a `.env` file from `.env.example`:
   ```powershell
   Copy-Item .env.example .env
   ```

3. Install required npm packages:
   ```powershell
   npm install
   ```

4. Start the Vite development server:
   ```powershell
   npm run dev
   ```

   The frontend will be available at: `http://localhost:5173`

---

## Environment Variables

### Backend (`backend/.env.example`)
| Variable | Description | Default / Example |
|---|---|---|
| `GEMINI_API_KEY` | Your Google Gemini API Secret Key | *(Required)* |
| `GEMINI_MODEL` | Gemini Model version | `gemini-1.5-flash` |

### Frontend (`frontend/.env.example`)
| Variable | Description | Default |
|---|---|---|
| `VITE_API_URL` | Base URL of the Spring Boot backend | `http://localhost:8080` |

---

## Running the Application

### 1. Start Backend:
```powershell
# In /ai-assistant/backend
$env:GEMINI_API_KEY="AIzaSy..."
mvn spring-boot:run
```

### 2. Start Frontend:
```powershell
# In /ai-assistant/frontend
npm run dev
```

Open your browser at `http://localhost:5173`.

---

## API Documentation

### 1. Health Check
- **Endpoint**: `GET /api/health`
- **Description**: Verifies if the Spring Boot backend service is operational.
- **Request Headers**: None
- **Response**: `200 OK`
```json
{
  "status": "UP"
}
```

---

### 2. Chat Generation
- **Endpoint**: `POST /api/chat`
- **Description**: Submits conversation messages and retrieves the AI-generated response.
- **Request Headers**: `Content-Type: application/json`
- **Request Body**:
```json
{
  "messages": [
    {
      "role": "user",
      "content": "What is Spring Boot?"
    }
  ]
}
```

- **Response Body**: `200 OK`
```json
{
  "response": "Spring Boot is an open-source Java-based framework used to create microservices and stand-alone web applications quickly..."
}
```

- **Multi-Turn Context Example Request**:
```json
{
  "messages": [
    {
      "role": "user",
      "content": "What is Java?"
    },
    {
      "role": "assistant",
      "content": "Java is a popular object-oriented programming language."
    },
    {
      "role": "user",
      "content": "What are its main advantages?"
    }
  ]
}
```

---

## Postman Testing Guide

### Test 1: GET `/api/health`
1. Open Postman.
2. Select HTTP Method: `GET`.
3. URL: `http://localhost:8080/api/health`.
4. Click **Send**.
5. **Expected Status**: `200 OK`.
6. **Expected Body**:
   ```json
   {
     "status": "UP"
   }
   ```

### Test 2: POST `/api/chat`
1. Select HTTP Method: `POST`.
2. URL: `http://localhost:8080/api/chat`.
3. Under **Headers**, verify `Content-Type: application/json`.
4. Under **Body**, select **raw** and format as **JSON**.
5. Paste:
   ```json
   {
     "messages": [
       {
         "role": "user",
         "content": "Explain Java Streams in one short paragraph."
       }
     ]
   }
   ```
6. Click **Send**.
7. **Expected Status**: `200 OK`.
8. **Expected Body**:
   ```json
   {
     "response": "Java Streams, introduced in Java 8, are sequences of elements supporting sequential and parallel aggregate operations..."
   }
   ```

---

## Troubleshooting

| Problem | Cause | Solution |
|---|---|---|
| **CORS Error** in browser console | Backend not configured or port mismatch | Verify Spring Boot `CorsConfig.java` allows `http://localhost:5173`. |
| **503 Service Unavailable** / *API key not configured* | `GEMINI_API_KEY` is missing in backend | Set `$env:GEMINI_API_KEY="your_key"` before running `mvn spring-boot:run`. |
| **401 / 403 Forbidden** from Gemini | Invalid Gemini API Key | Check and regenerate your API key in Google AI Studio. |
| **429 Rate Limit Exceeded** | Gemini free tier threshold reached | Wait 60 seconds before sending additional requests. |
| **Failed to fetch / Connection refused** | Spring Boot backend is not running | Ensure `mvn spring-boot:run` is active on port `8080`. |

---

## Security

- **Backend Secret Isolation**: The `GEMINI_API_KEY` is strictly accessed on the backend server and never passed to the browser or bundled in frontend client scripts.
- **Git Protection**: All `.env` files and `target/` builds are defined in `.gitignore`.
- **CORS Restricted**: Backend CORS policy is scoped to `localhost:5173` without exposing open wildcards.
- **Input Sanitization & Validation**: Java DTOs employ `@Valid`, `@NotBlank`, and `@NotEmpty` to reject empty or malformed requests before processing.
- **Zero Information Leakage**: Exception handlers catch errors and produce sanitized client-friendly error messages without revealing internal stack traces.

---

## Future Improvements

- Streaming LLM output (Server-Sent Events / SSE)
- File and image upload analysis (Gemini Vision)
- Conversation export to PDF/Markdown files
- Configurable System Prompt / Persona selector
