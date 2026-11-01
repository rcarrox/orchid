# OrChiD 1.2.4 — Compiler un APK

Ce dossier inclut les sources corrigées de OrChiD 1.2.4 (versionCode 16) et un workflow GitHub Actions prêt à l'emploi.

## A. Depuis GitHub Actions (sans installer Android Studio)

1. Sur GitHub, créez un nouveau dépôt **privé** dédié à OrChiD, par exemple `OrChiD` (ne pas utiliser le dépôt OrChiP).
2. Décompressez `OrChiD_1.2.4_BUILD_GITHUB.zip`.
3. Envoyez **le contenu du dossier décompressé** à la racine du dépôt, y compris le dossier caché `.github` (ce n'est pas le ZIP qu'il faut envoyer). Alternative : exécutez les commandes `git` ci-dessous depuis le dossier.
4. Dans le dépôt, ouvrez l'onglet **Actions**, puis **OrChiD - compiler APK** > **Run workflow** (la première compilation peut aussi être lancée automatiquement par push sur `main`).
5. Une fois la compilation réussie, ouvrez l'exécution > **Artifacts** > **OrChiD-1.2.4-APK-debug** et téléchargez l'archive ; elle contient `app-debug.apk`.

Commandes `git` (si dépôt GitHub vide créé) :

    git init
    git add .
    git commit -m "OrChiD 1.2.4"
    git branch -M main
    git remote add origin https://github.com/VOTRE-COMPTE/OrChiD.git
    git push -u origin main

`github.com/VOTRE-COMPTE/OrChiD` est un EXEMPLE à remplacer par l'URL réelle.

## B. Depuis Android Studio sur Windows

1. Installez Android Studio (avec SDK Android 35 / Build Tools 35.0.0).
2. Ouvrez **le dossier `orchid`**, qui contient `settings.gradle`.
3. Laissez Gradle synchroniser les dépendances (réseau nécessaire).
4. Menu **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Le fichier devrait être généré dans : `orchid/app/build/outputs/apk/debug/app-debug.apk`.

## Note sur la signature et les données existantes

Les APK de test « debug » produits sur différentes machines/CI peuvent utiliser des signatures différentes. Android refusera la mise à jour d'une application existante avec une signature différente. **Ne désinstallez pas l'ancienne version sans sauvegarder la configuration** : cette désinstallation pourrait effacer les données locales. Pour des mises à jour successives préservant les données, utilisez toujours le même fichier keystore de signature (idéalement le keystore ayant servi à la version déjà installée).

## Version et limites

- Package Android : `fr.ilamartin.orchid`.
- Version : `1.2.4` / versionCode `16`.
- Tests JVM audio exécutables en local : `python3 tests/run_audio_tests.py`.
- Cette archive n'est PAS un APK et ne garantit pas la compilation tant que GitHub Actions / Android Studio n'ont pas tourné.
- Ne pas partager publiquement de secrets de signature ou de données utilisateur.
