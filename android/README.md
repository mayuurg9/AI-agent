NEXUS AI for CodeAssist

Recommended: open the cloned AI-agent repository root in CodeAssist. The root Gradle settings automatically map the Android app module to android/app. Alternatively, open this android/ folder directly as a standalone Gradle project.

Requirements: a recent CodeAssist 3.x release, Android SDK 35, and internet access to resolve AndroidX dependencies. CodeAssist builds with its on-device toolchain.

The Android app contains the NEXUS interface, but it does not run the Node.js server or store provider API keys. Deploy the repository's Node backend separately on a Node-compatible host. Configure GEMINI_API_KEY, GROQ_API_KEY, or OPENROUTER_API_KEY in that host's secret settings, and set APP_ORIGIN=https://appassets.androidplatform.net.

Launch the Android app, open Settings > Backend URL, and enter the deployed server's HTTPS URL. Keep API keys on the server. Never paste them into the Android app or commit them to GitHub.

The original Node/Express web application remains at the repository root.
