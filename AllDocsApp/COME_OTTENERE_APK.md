# Come ottenere l'APK di AllDocs (nessun comando da digitare)

Questa cartella contiene il progetto Android completo di AllDocs. Per
trasformarlo in un file .apk installabile sul tuo tablet/telefono, la build
avviene sui server di GitHub (gratuiti), non sul tuo computer.

## 1. Crea un account GitHub (se non lo hai già)

Vai su github.com e registrati — è gratuito, basta un'email.

## 2. Crea un nuovo repository

- Clicca sul "+" in alto a destra, poi "New repository"
- Dagli un nome, ad esempio "alldocs-app"
- Puoi lasciarlo "Private" (visibile solo a te)
- Clicca "Create repository"

## 3. Carica i file di questo progetto

- Nella pagina del repository appena creato, clicca "uploading an existing
  file" (o "Add file" > "Upload files" se il repository ha già contenuto)
- Trascina dentro l'intera cartella di questo progetto (quella che contiene
  "app", "settings.gradle", ".github", ecc.) — i browser moderni mantengono
  la struttura delle sottocartelle
- In basso scrivi un messaggio a piacere (es. "Primo caricamento") e
  clicca "Commit changes"

## 4. La build parte da sola

- Vai sulla scheda "Actions" in alto nel repository
- Vedrai un'esecuzione in corso chiamata "Build AllDocs APK" — parte
  automaticamente appena carichi i file
- Se non parte da sola, clicca "Build AllDocs APK" a sinistra, poi
  "Run workflow" in alto a destra
- Ci vogliono circa 5-10 minuti

## 5. Scarica l'APK

- Quando l'esecuzione ha il segno di spunta verde (completata), cliccaci
  sopra
- In fondo alla pagina trovi la sezione "Artifacts" con un file chiamato
  "AllDocs-apk" — cliccalo per scaricarlo (è uno zip)
- Apri lo zip: dentro trovi "app-debug.apk"

## 6. Installalo sul tablet/telefono

- Trasferisci "app-debug.apk" sul dispositivo (stesso metodo che usi già
  per il file AllDocs.html: email, chat, cavo, ecc.)
- Aprilo: Android chiederà di autorizzare "Installa app da sorgenti
  sconosciute" la prima volta — è normale per qualsiasi app installata
  fuori dal Play Store, non è un problema del file
- Una volta installata, l'icona "AllDocs" comparirà come un'app vera,
  senza passare dal browser

## Importante: i tuoi dati

La prima apertura dell'app partirà vuota, esattamente come quando abbiamo
rinominato il file HTML in passato. Appena installata, usa "Importa Backup
(JSON)" nella tab Dati per ricaricare clienti, catalogo, archivio
documenti — tutto quello che hai già.

## Se vuoi modificare qualcosa in futuro

Dimmi cosa vuoi cambiare, io aggiorno i file di questo stesso progetto
(il codice dell'app dentro app/src/main/assets/AllDocs.html, oppure il
codice nativo se serve), te lo rimando, tu ricarichi i file aggiornati su
GitHub (stessa procedura del punto 3) e la build produce automaticamente
un nuovo APK da scaricare.
