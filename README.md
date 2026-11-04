# NEXUS AI

A self-hostable AI agent workspace with provider switching, multi-step tool use, persistent notes and tasks, and a polished responsive interface.

## Stack
- Node.js 20+ and Express backend
- Vanilla JavaScript frontend
- Gemini, Groq, and OpenRouter through OpenAI-compatible chat completions
- Server-side provider keys; browser never receives secrets

## Run locally
1. Copy `.env.example` to `.env`.
2. Add at least one provider key.
3. Run `npm install`.
4. Run `npm run dev`.
5. Open http://localhost:3000.

Default models: Gemini `gemini-2.5-flash`, Groq `llama-3.3-70b-versatile`, OpenRouter `meta-llama/llama-3.3-70b-instruct:free`. Availability and free quotas vary by provider and account.

## Included tools
Calculator, current time, save/list notes, create/list tasks, and complete tasks. The agent cannot execute shell commands or arbitrary code.

## Security
Never commit `.env` or place API keys in frontend code. This is a local-development starter, not a hardened public multi-user deployment. Add authentication, rate limiting, HTTPS, and persistent storage before exposing it publicly.
