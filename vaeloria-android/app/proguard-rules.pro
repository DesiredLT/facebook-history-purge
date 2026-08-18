# Vaeloria uses explicit JSON serialization and Android components declared in the manifest.
# Keep field names on persisted state models stable across optimized release builds.
-keepclassmembers class lt.vaeloria.ooc.GameState { <fields>; }
-keepattributes SourceFile,LineNumberTable
