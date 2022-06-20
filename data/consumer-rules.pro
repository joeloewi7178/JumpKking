-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
-keep class com.joeloewi.data.entity.ReportCardEntity { *; }

-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.*
-dontwarn org.openjsse.net.ssl.OpenJSSE
-dontwarn java.lang.invoke.StringConcatFactory