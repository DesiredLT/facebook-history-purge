# Vaeloria Android signing

GitHub Actions persists the debug signing keystore through `actions/cache` under the key `vaeloria-android-debug-signing-v1`.

This prevents Android package-signature mismatches between future side-loaded Vaeloria updates.
