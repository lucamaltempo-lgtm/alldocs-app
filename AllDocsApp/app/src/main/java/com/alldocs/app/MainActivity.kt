package com.alldocs.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

/**
 * Involucro nativo minimale per AllDocs: carica l'app (un unico file HTML
 * autosufficiente) dagli assets in una WebView, con storage locale attivo
 * e un ponte verso funzioni native Android (salvataggio file, condivisione
 * PDF) tramite AndroidBridge.kt — vedi quella classe per i dettagli.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true // essenziale: qui vive il localStorage di clienti/catalogo/documenti
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true

        webView.addJavascriptInterface(AndroidBridge(this), "AndroidBridge")

        // Gestisce i pulsanti "Importa" (CSV/XLSX/JSON): l'HTML usa <input type="file">
        // standard, e questa callback apre il selettore di file nativo di Android.
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webViewParam: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileChooserCallback?.onReceiveValue(null)
                fileChooserCallback = filePathCallback
                val intent = fileChooserParams?.createIntent()
                return try {
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE)
                    true
                } catch (e: Exception) {
                    fileChooserCallback = null
                    false
                }
            }
        }

        webView.webViewClient = WebViewClient()

        webView.loadUrl("file:///android_asset/AllDocs.html")
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            val results: Array<Uri>? =
                if (resultCode == Activity.RESULT_OK && data?.data != null) {
                    arrayOf(data.data!!)
                } else {
                    null
                }
            fileChooserCallback?.onReceiveValue(results)
            fileChooserCallback = null
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // L'app stessa gestisce già il "doppio indietro per uscire" via JS/history.pushState;
        // qui lasciamo che sia la pagina a decidere, tranne se può ancora tornare indietro
        // nella cronologia della WebView (caso raro, ma corretto da gestire).
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val FILE_CHOOSER_REQUEST_CODE = 51426
    }
}
