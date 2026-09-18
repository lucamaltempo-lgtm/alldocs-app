# Regole ProGuard/R8 minime: teniamo il bridge JS->Android accessibile per riflessione
-keepclassmembers class com.alldocs.app.AndroidBridge {
    public *;
}
