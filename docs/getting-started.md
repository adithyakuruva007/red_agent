# Getting Started

## Installation

Red can be built locally from source.

### Building from Source

To compile and build Red on your machine:

1. Clone the project sources locally.
2. Install **JDK 21**.
3. Open a terminal in the root directory.
4. Run the build commands:
   - **Desktop App**: `./gradlew :composeApp:run`
   - **Desktop package installer (Debian)**: `./gradlew :composeApp:packageDeb`
   - **Android App (APK)**: `./gradlew :androidApp:assembleFossDebug`

## First Steps

1. Launch Red — you will see the chat screen with an animated logo welcome.
2. Start chatting immediately using the **Free** tier (no API key needed).
3. For better models, open **Settings** and add a service (e.g. OpenAI, Gemini, DeepSeek).
4. Enter your API key — Red validates the connection and loads available models automatically.

## Adding a Service

1. Open Settings.
2. Tap **Add Service** and pick a provider.
3. Paste your API key.
4. Select a model from the dropdown.
5. Drag services to reorder — the first one is your primary, the rest are fallbacks.

See [Multi-Service](features/multi-service.md) for full details on providers and fallback behavior.
