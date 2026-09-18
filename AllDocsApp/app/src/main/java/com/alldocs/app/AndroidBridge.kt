package com.alldocs.app

import android.content.Intent
import android.util.Base64
import android.util.Log
import android.webkit.JavascriptInterface
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Ponte JavaScript -> Android. La pagina AllDocs.html, quando gira dentro
 * questa app (e NON dentro un browser normale), rileva window.AndroidBridge
 * e lo usa al posto dei download via Blob del browser, che dentro una
 * WebView spesso non funzionano bene. Ogni metodo qui corrisponde a una
 * chiamata JS (vedi salvaBytes()/sharePdf() dentro AllDocs.html).
 */
class AndroidBridge(private val activity: MainActivity) {

    /**
     * Salva un file (backup JSON, export XLSX, PDF generato) nella cartella
     * privata dell'app e apre subito il selettore "Apri con" di Android,
     * cosicché l'utente possa scegliere dove tenerlo o quale app usare per
     * aprirlo (Excel, un lettore PDF, ecc.).
     */
    @JavascriptInterface
    fun saveFile(filename: String, base64Data: String, mimeType: String) {
        activity.runOnUiThread {
            try {
                val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                val dir = activity.getExternalFilesDir(null) ?: activity.filesDir
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, sanitizeFilename(filename))
                FileOutputStream(file).use { it.write(bytes) }

                val uri = FileProvider.getUriForFile(
                    activity, activity.packageName + ".fileprovider", file
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    activity.startActivity(Intent.createChooser(intent, "Apri con"))
                } catch (e: Exception) {
                    // Nessuna app in grado di aprire questo tipo di file: il file resta
                    // comunque salvato nella cartella dell'app, non è un errore bloccante.
                    Log.w("AllDocsBridge", "Nessuna app disponibile per aprire $filename", e)
                }
            } catch (e: Exception) {
                Log.e("AllDocsBridge", "Errore salvataggio file $filename", e)
            }
        }
    }

    /** Apre direttamente il pannello di condivisione Android (WhatsApp, Email...) per il PDF. */
    @JavascriptInterface
    fun sharePdf(filename: String, base64Data: String) {
        activity.runOnUiThread {
            try {
                val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                val dir = File(activity.cacheDir, "pdf")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, sanitizeFilename(filename))
                FileOutputStream(file).use { it.write(bytes) }

                val uri = FileProvider.getUriForFile(
                    activity, activity.packageName + ".fileprovider", file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                activity.startActivity(Intent.createChooser(intent, "Condividi PDF"))
            } catch (e: Exception) {
                Log.e("AllDocsBridge", "Errore condivisione PDF $filename", e)
            }
        }
    }

    private fun sanitizeFilename(name: String): String =
        name.replace(Regex("[^A-Za-z0-9._-]"), "_")
}
