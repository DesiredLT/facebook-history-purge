# Vaeloria — local Android RPG

Native Android build of the Vaeloria/OOC RPG. No hosting, Vercel, Neon, Cloudflare, or payment card is required for the game runtime.

## Runtime architecture
- Android native UI (Java)
- SQLite local save + last 20 undo checkpoints
- Android Keystore encrypted Groq API key
- Groq `openai/gpt-oss-120b` Game Master using strict JSON Schema structured output
- Three contextual choices + freeform action input
- Character, resources, inventory, quests, world map, journal
- JSON save export/import
- Canonical starting checkpoint migrated from the existing Vaeloria database: Einoras in Luminara, year 923, The Broken Meridian

## Build
The GitHub Actions workflow builds a debug-signed APK. The APK can be installed directly on Android after allowing installation from the app used to open it.

## Groq key
The API key is entered once on-device. It is encrypted with Android Keystore and excluded from backups and exported save files. The key is never committed to source control.
