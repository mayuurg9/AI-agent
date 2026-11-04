NEXUS AI for CodeAssist

Open this android/ folder in CodeAssist as a Gradle/Android project, not the repository root.

Requirements: Android SDK 35, JDK 17, and internet access for Gradle dependencies.

The Android app contains the NEXUS interface, but it does not run the Node.js server or store provider API keys. Deploy the repository's Node backend separately on a Node-compatible host. Configure GEMINI_API_KEY, GROQ_API_KEY, or OPENROUTER_API_KEY in that host's secret settings, and set APP_ORIGIN=https://appassets.androidplatform.net.

Launch the Android app, open Settings > Backend URL, and enter the deployed server's HTTPS URL. Keep API keys on the server. Never paste them into the Android app or commit them to GitHub.

The original Node/Express web application remains at the repository root.
