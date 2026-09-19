package com.alldocs.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
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
        //
        // Alcune app di gestione file "vecchio stile" (es. ES Gestore File) restituiscono un
        // Uri senza il permesso di lettura esplicito, che la WebView poi non riesce a leggere
        // ("File non leggibile" anche se il file esiste). Per questo:
        // 1) chiediamo esplicitamente il permesso di lettura (e persistente) sull'Uri scelto;
        // 2) forziamo SEMPRE la finestra "Apri con" (Intent.createChooser) invece di lasciare
        //    che Android riapra da solo l'ultima app usata — così l'utente può scegliere ogni
        //    volta Google Drive, Material Files, ecc. invece di restare bloccato sull'app
        //    "File" di sistema che con la ricerca è scomoda da usare;
        // 3) suggeriamo di aprire direttamente la cartella Download, dove di solito stanno i
        //    file esportati da questa stessa app.
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webViewParam: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileChooserCallback?.onReceiveValue(null)
                fileChooserCallback = filePathCallback
                val baseIntent = fileChooserParams?.createIntent()
                if (baseIntent == null) {
                    fileChooserCallback = null
                    return false
                }
                baseIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                baseIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        val downloadsUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download")
                        baseIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, downloadsUri)
                    } catch (e: Exception) {
                        // Alcuni produttori non supportano questo suggerimento: nessun problema,
                        // il selettore si apre comunque, solo senza partire già dalla cartella Download.
                    }
                }
                val chooser = Intent.createChooser(baseIntent, "Scegli il file da importare")
                return try {
                    startActivityForResult(chooser, FILE_CHOOSER_REQUEST_CODE)
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
            val uri = if (resultCode == Activity.RESULT_OK) data?.data else null
            if (uri != null) {
                // Se l'app che ha restituito il file lo consente, teniamo il permesso di lettura:
                // altrimenti la WebView a volte riceve l'Uri ma non riesce a leggerne i byte
                // ("File non leggibile" pur avendo scelto un file valido).
                try {
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: Exception) {
                    // Non tutti i provider (es. Google Drive) supportano il permesso persistente
                    // per un Uri "usa e getta" come questo: non è un errore, si prosegue comunque.
                }
            }
            val results: Array<Uri>? = if (uri != null) arrayOf(uri) else null
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
