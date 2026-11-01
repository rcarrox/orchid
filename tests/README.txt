Tests JVM d'OrchiD 1.2.3

Prérequis : Python 3 et JDK 17 (javac et java dans PATH ou JAVA_HOME).
Pour le test de compatibilité JSON des entrées, utiliser la dépendance de test
org.json:json:20240303, téléchargée depuis Maven Central :
https://repo.maven.apache.org/maven2/org/json/json/20240303/json-20240303.jar
Ce JAR n'est pas une dépendance ajoutée à l'APK : Android fournit déjà org.json.

Depuis le dossier des sources :
python3 tests/run_tests.py --json-jar /chemin/json-20240303.jar
Option : --java-home /chemin/jdk17

Les doublures android et Store sous tests/support servent au test d'import ZIP.
Celles sous tests/audio/support sont compilées séparément ; AudioTrack y suit
le cycle documenté STATE_NO_STATIC_DATA -> write -> STATE_INITIALIZED -> play.
Le test audio vérifie le code Bips réel avec ces doublures : il ne joue aucun son.
Il contrôle aussi Aucun, erreurs de création/écriture, libération, annulation et
plusieurs pistes indépendantes. Aucun essai sur le haut-parleur d'un téléphone.
Les tests support ne sont pas inclus dans les classes de l'application Android.

Nouveaux tests 1.2.4 :
python3 tests/run_audio_tests.py
Ce script n'a besoin que de Python 3 et du JDK (aucun SDK Android et aucun JAR
supplémentaire). Il vérifie l'ordonnanceur, Bips, la file de synthèse TTS,
le cache, le retry, l'absence de voix, l'annulation et l'intégration
TTS/MediaPlayer en lecture superposée, tous avec simulateurs JVM.
La commande run_tests.py exécute aussi ces nouveaux tests si org.json est fourni.
