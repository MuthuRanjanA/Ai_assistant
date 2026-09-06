import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 45000, // 45 seconds for LLM generation
});

/**
 * Send multi-turn messages to backend LLM chat endpoint
 * @param {Array<{role: string, content: string}>} messages
 * @param {AbortSignal} [signal] Optional abort signal to cancel requests
 * @returns {Promise<{response: string}>}
 */
export const sendChatMessage = async (messages, signal) => {
  try {
    const payload = {
      messages: messages.map(msg => ({
        role: msg.role === 'assistant' ? 'assistant' : 'user',
        content: msg.content
      }))
    };

    const response = await apiClient.post('/api/chat', payload, { signal });
    return response.data;
  } catch (error) {
    if (axios.isCancel(error)) {
      throw new Error('Generation stopped by user.');
    }

    if (error.response) {
      // Server responded with error status
      const errorMessage = error.response.data?.error || 
                           error.response.data?.message || 
                           `Server error (${error.response.status}). Please try again.`;
      throw new Error(errorMessage);
    } else if (error.request) {
      // Request made but no response received (Backend down or CORS issue)
      throw new Error('Unable to connect to backend server. Make sure the Spring Boot backend is running on http://localhost:8080.');
    } else {
      throw new Error(error.message || 'An unexpected error occurred.');
    }
  }
};

/**
 * Health check endpoint test
 * @returns {Promise<{status: string}>}
 */
export const checkHealth = async () => {
  try {
    const response = await apiClient.get('/api/health');
    return response.data;
  } catch (error) {
    return { status: 'DOWN', error: error.message };
  }
};

export default {
  sendChatMessage,
  checkHealth
};
