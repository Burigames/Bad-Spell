# Tests und Qualitätssicherung

## Aktueller M1-Stand – 24.10.2026

- Gradle-Build erfolgreich, einschließlich Ressourcen- und 26.2-Atlasprüfung.
- **45 Kernprüfungen und 49 erforderliche Server-GameTests bestanden.**
- **848 echte Rezeptschritte**, sieben Stationen und alle 18 Zielgegenstände aus endlichen Rohstoff-/Vanilla-Fixtures. Mod-Rohstoffe wurden über Survival-Drops gewonnen; keine vorgegebenen Mod-Zwischenprodukte.
- Zwei echte Clients mit installierter JAR auf einem dedizierten Entwicklungsserver, Client-Buchrevision, Handschuhexport und belegter Slotwechsel.
- Nach sauberem Stopp/Neustart: Geräteauftrag, bezahltes Feld, Oberflächengraph, Handschuh-Snapshot und aktiver Slot geprüft.
- Fremder Stationszugriff tatsächlich im zweiten Client abgewiesen. Meißel-Editor, Gabelaktivierung, Monokelanzeige und Kompassbindung im Besitzerclient bedient.
- In der korrigierten JAR keine Meldungen `Unable to bake item model`, `Missing item model` oder `Multiple atlases`.

Abnahmeumfang und Reproduktion: [M1_ACCEPTANCE.md](docs/M1_ACCEPTANCE.md). Nachweise: [M1_CLOSURE_VALIDATION.log](docs/M1_CLOSURE_VALIDATION.log), [M1_ACCEPTANCE_EVENTS.log](docs/M1_ACCEPTANCE_EVENTS.log) und [M1_ACCEPTANCE.properties](docs/M1_ACCEPTANCE.properties).

## Relevante Regressionen

Formelprüfung, Typfehler, Zyklen, begrenzte Auswertung, Modulbesitz/Schachtelung, Lazy-Branches, deterministische Kosten und Snapshot-Codec. Tatsächliche Weltwirkungen für Licht, Kälte, Hitze, Schild, Heilung und Stoß. Eigenes Selbst und Entity-Raycasts, Wandverdeckung, sichere leere Zweige, freies Luftziel innerhalb der Reichweite, gleichzeitige Kälte vor einem Stoß und Ablehnung ohne Teilausführung/Energiekosten.

Geräte werden mit derselben Auswertung geprüft: Kugellicht, Kombinationen, Offline-Feldticks, gespeicherte Verzögerung, Stop-Rechte und NBT-Laderunde. Leitertests prüfen reale Zellgutschrift und -abzug, 80-FL/s-Budget, Richtung, Besitzer und Unterbrechung. Ein weiterer Test prüft die Gabelreparatur ohne erneute Formelplatte.

Die Mondsalz-Prüfung beginnt mit Mondessenz auf derselben Station und setzt danach Quarz hinzu. Die Station muss den alten Rezeptcache verwerfen, Mondsalz statt Essenz produzieren und beide Kosten exakt abbuchen.

## Automatisierung

```bash
./gradlew build runGameTestServer
python3 scripts/check-recipe-reachability.py
python3 scripts/plan-m1-acceptance.py
```

Der strukturelle Rezeptcheck ersetzt keine Energiebilanz oder Spielprüfung. Der zusätzliche Materialplan wird unabhängig mit einem Bestandszähler nachgerechnet, damit geschachtelte Rezepte keine bereits reservierten Zutaten verbrauchen. Der QA-Lauf prüft danach registrierte Vanilla-Rezepte und lässt Modstationen mit echten Welt-Ticks arbeiten. Zur Beschleunigung wird die Tickrate im Test zeitweise auf 200 erhöht und anschließend auf 20 zurückgestellt. Vanilla-Ofenlaufzeit wird nicht separat nachgespielt; dessen registriertes Rezept und Zutaten/Ergebnis werden geprüft.

## Umgebungsgrenzen

Clienttests liefen mit Software-Rendering auf einem virtuellen Display, GUI-Skalierung 2 und reduzierter Sichtweite. Der lokale Offline-Test benötigt keine Mojang-Anmeldung; fehlende Services-Schlüssel betreffen dort Authentifizierung/Chat und wurden nicht als Modfehler gewertet. Xvfb kann einmal eine nicht verfügbare Standard-Cursorform melden; die getesteten Oberflächen blieben bedienbar.

Keine bekannte kritische Blockade innerhalb des definierten M1-Umfangs. Dies ist keine Zusage für alle Drittanbieter-Mods, sämtliche Auflösungen, natürliche Erzverteilung, große Netze oder noch nicht implementierte PDF-Inhalte. Historische Prüfprotokolle bleiben im Repository, sind aber nicht der aktuelle Abnahmestand.
