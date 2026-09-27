# Add project specific ProGuard / R8 rules here.

# Enable aggressive optimization passes and class repackaging for obfuscation
-optimizationpasses 5
-allowaccessmodification
-repackageclasses ''
-keepparameternames

# Preserve data classes for Moshi and Firestore deserialization
-keep class com.zahidcodes.zvpn.model.** { *; }
-keepclassmembers class com.zahidcodes.zvpn.model.** { *; }

# Preserve ViewModels
-keepclassmembers class * extends androidx.lifecycle.ViewModel { *; }

# Optimize Android Coroutines & Compose
-dontwarn kotlinx.coroutines.**
