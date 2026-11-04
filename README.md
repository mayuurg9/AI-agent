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


## Android app (CodeAssist)

A native Android client is included in the `android/` folder. In CodeAssist, clone the repository, then open/import the **android folder itself** as the Gradle project. Do not try to run the repository root as an Android app.

The Android client packages the NEXUS interface and connects to the Node.js backend over HTTPS. It does not run Node.js or hold provider API keys. Deploy the root Node app to a Node-compatible host, configure your provider key as a server secret, and set `APP_ORIGIN=https://appassets.androidplatform.net` (add any other required web origins as a comma-separated list). In the Android app, open Settings and enter the backend's HTTPS URL.

Build requirements: JDK 17, Android SDK 35, and Gradle dependency access. The Android project has not yet been built or tested inside CodeAssist, so the first Gradle sync may request SDK or plugin downloads.
