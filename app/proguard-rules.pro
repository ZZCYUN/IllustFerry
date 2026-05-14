# Preserve line info that helps diagnose release-only crashes.
-keepattributes SourceFile,LineNumberTable,Signature,*Annotation*,EnclosingMethod,InnerClasses

# Glide discovers the generated module and model loaders via annotations / generated code.
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl
-keep class com.bumptech.glide.load.model.GlideUrl { *; }

# Gson models are read reflectively and many fields are only referenced by serialized names.
-keep class JunZi.Pixiv.data.model.** { *; }
-keep class JunZi.Pixiv.data.network.PixivDnsUpdater$DnsJsonResponse { *; }
-keep class JunZi.Pixiv.data.network.PixivDnsUpdater$DnsAnswer { *; }

# SharedPreferences payloads also round-trip through Gson in release builds.
-keep class JunZi.Pixiv.AuthSession { *; }
-keep class JunZi.Pixiv.DownloadItem { *; }

# Keep generic signatures for Gson TypeToken.
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Local Pixiv proxy generates certificates at runtime in release builds.
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
