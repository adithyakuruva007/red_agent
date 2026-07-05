# Privacy Policy

**Effective Date:** July 5, 2026

At **Red**, we build with privacy as our core design principle. This Privacy Policy details how user data is handled in the Red AI Assistant (the "Application").

## Creator & Maintainer
Red is created, built, and maintained by **adithyakuruva007** (GitHub: [adithyakuruva007](https://github.com/adithyakuruva007)).

## 1. Local-First Design
Red is designed to be a local-first application. We believe your data belongs to you, and it should remain under your control.
* **No Server Storage:** We do not own, operate, or maintain any databases or servers that collect, store, or transmit your conversation logs, memories, sandbox files, or settings metadata.
* **Local Databases:** All chat histories, persistent memories, schedule entries, and sandbox environments are stored locally on your device.
* **Local Encryption:** Database files and secure settings are stored with native OS encryption (e.g. Android KeyStore/EncryptedSharedPreferences on Android).

## 2. API Keys & LLM Integrations
If you choose to configure external LLM services (such as Google Gemini, Anthropic Claude, OpenAI, DeepSeek, etc.):
* **Direct Connections:** Your API keys are stored securely on your local device. 
* **Transit Security:** When you send a message, the request is sent directly from your device to the configured LLM provider's API. No intermediate proxy servers are used.
* **Third-Party Policies:** Interaction with third-party APIs is governed by their respective privacy policies. Please consult the privacy policy of the providers you configure.

## 3. Storage & Files Access
Red requests storage access permission to operate local file integration features (e.g., local Obsidian vault sync, text context additions):
* **Local Use Only:** These permissions are used exclusively to allow the agent to read and write files locally on your device (e.g., in your `/sdcard/` or Obsidian folder).
* **No Uploads:** Your local files are never uploaded to any remote server or shared with any third party, except for any file content you explicitly select to pass to your configured LLM API provider in your prompts.

## 4. Sandbox Security
The embedded Alpine Linux sandbox runs locally inside the application's secure container. Any commands, shell executions, or files created in the sandbox remain strictly on your physical device.

## 5. Contact
If you have any questions about Red or this privacy policy, you can contact the creator via GitHub: [adithyakuruva007](https://github.com/adithyakuruva007).
