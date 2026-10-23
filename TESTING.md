# Tests und Abnahme

Stand: 23. Oktober 2026. Ziel ausschließlich Minecraft 26.2 / NeoForge 26.2.0.93 / JDK 25.

## Korrektur des früheren Lichtnachweises

Nach einer Fehlermeldung mit sinkenden FL/FK ohne sichtbare Wirkung wurde ein Fehler reproduziert: Die bisherigen Tests prüften nur den Auxiliary-Light-Eintrag. Vanilla-Luft liest diesen Eintrag nicht als eigene Emission; die behauptete Lichtwirkung war damit nicht ausreichend nachgewiesen. Der neue Test gegen `LightLayer.BLOCK` schlug vor der Korrektur fehl. Kostenanzeigen und die früheren Screenshots beweisen allein keine tatsächliche Beleuchtung.

Die Korrektur ergänzt eine auf die geprüften 26.2-Klassen begrenzte Mixin-Anbindung an den Blocklichtrechner, einen threadsicheren Quellenindex und die Behandlung leerer Sections. Es werden keine Lichtblöcke platziert. Beim Laden eines beobachteten Chunks werden aktive Quellen nachgesendet; beim Client-Unload wird der abgeleitete Index bereinigt. Eine erfolgreiche Aktivierung erzeugt zusätzlich einen kurzen Vanilla-Partikeleffekt.

Ergebnis des aktuellen Gradle-Laufs: **Build erfolgreich; 28 Kerntests und 12 eigene GameTests plus ein Vanilla-Test bestanden**. Die drei neuen GameTests prüfen tatsächliche Helligkeit, Nachbarlicht, Erlöschen, vollständig leere Sections sowie den vollständigen Handschuh-Itemaufruf mit exakt 30 FL und 5 FK. Der schnell laufende GameTestServer lässt seinem asynchronen Lichtworker dabei je Prüfversuch 1 ms Rechenzeit und wartet auf dessen Abschluss; diese Wartehilfe ist ausschließlich Testcode.

## Reproduzierbare Befehle

```bash
cd /workspace/Bad-Spell
source scripts/cloud-env.sh
./scripts/test-core.sh
./gradlew --no-daemon build runGameTestServer --max-workers=2
```

`install-cloud.sh` reproduziert die geprüfte JDK-/Proxy-/Truststore-Einrichtung, bei Bedarf die virtuelle Anzeige, und baut das Projekt. Es verändert keine Quelldateien. Alle Downloadprüfungen bleiben aktiv.

## Automatisierte Ergebnisse

| Prüfung | Ergebnis | Tatsächlicher Umfang |
|---|---|---|
| Java-Kern | 28/28 bestanden | Eigener JavaExec-Runner, Java 25, `-Xlint:all -Werror` |
| Gradle-Build | Bestanden | Gemeinsame und Clientklassen, Mod-Metadaten, Ressourcen und Mod-JAR |
| Ressourcen | Bestanden | Vier Itemdefinitionen, Modelle, 16×16-Texturen; identische DE/EN-Schlüssel (51) |
| Minecraft-GameTests | 12/12 Bad Spell bestanden | Zusätzlich 1 Vanilla-Test; Runner meldet insgesamt 13 bestandene erforderliche Tests |
| Grafischer Client | Gestartet und bedient | Minecraft 26.2 mit Software-OpenGL, Xvfb und integriertem Server |

Die Standardaufgaben `test` melden **NO-SOURCE** und zählen nicht als bestandene Tests. Die 28 tatsächlichen Kerntests laufen als `:core:coreTest` über `:core:check`. Eine anfängliche falsche Zuordnung zum Standard-Test-SourceSet wurde korrigiert, ohne Assertions oder die No-Test-Erkennung abzuschalten.

Die Core-Suite prüft Determinismus über 100 Auswertungen, konkrete Lichtkosten, Port-/Parameterfehler, Zyklen, Limits, fehlende/duplizierte/verwaiste Knoten, Ressourcenunterdeckung, ST-Schwellen, Rechteablehnung, Reichweite, Formelbudget, atomare FL/FK-Abbuchung auch unter Konkurrenz, Fehler vor Commit, bedingte Effektauswahl, Snapshot-Unveränderlichkeit, Codec-Roundtrip und beschädigte Eingaben, Dateispeicherung/Besitzer/Revisionskonflikte, parametrisierte Module und maximale Tiefe, Outputkapazität, idempotenten Maschinenabschluss sowie transitive Produktionsmengen und Chargenreste.

Die eigenen GameTests sind:

- `badspell:registered_items`: tatsächliche Registrierung und Stacklimits.
- `badspell:light_lifecycle`: Aktivierung in einer echten Serverwelt, Auxiliary Light gesetzt, kein Weltblock ersetzt, exakte Energie und Entfernung nach Ablauf.
- `badspell:item_snapshot_persistence`: Graph-Roundtrip in einer echten Itemkomponente.
- `badspell:book_server_workflow`: Buch-ID/Autor, Revision, echter Tintenverbrauch, Vorgängerrevision, Export, unabhängige Handschuhkopie, abgewiesene veraltete Revision und tatsächlicher Tinten-Itemverbrauch beim Nachfüllen.
- `badspell:book_rejects_forgery`: abweichender Autor und negative Stärke ändern weder Buch noch Tinte.

Die letzten beiden Tests nutzen NeoForge-FakePlayer für Serveraktionen. Die Testwelt liegt ausschließlich unter `run/gametestserver`. Der Testlauf lädt den Mod im echten Serverkontext; die gepackte Hotfix-JAR wurde zusätzlich im separaten Mods-Ordner getestet (siehe Abschlussabschnitt). Ein Endnutzer-Launcher-Test bleibt offen.

## Tatsächlicher Clienttest

Der erste Start ohne Anzeige scheiterte erwartungsgemäß an `glfwInit`. Das wurde durch eine lokal installierte virtuelle Anzeige behoben. Xvfb stammt aus dem signierten Debian-Security-Repository, Version `2:21.1.16-1.3+deb13u4`; `xdotool`/`libxdo3` haben Version `1:3.20160805.1-5.1`. Keine Systempakete wurden überschrieben. Grafik läuft mit Software-OpenGL; Audio wurde auf den Null-Backend gestellt, weil kein physisches Audiogerät existiert.

```bash
source scripts/cloud-env.sh
source scripts/cloud-display.sh
./gradlew --no-daemon runClient --max-workers=2
```

Für die vorhandene, ausschließlich zur QA erzeugte Creative-Welt ist auch `./gradlew runClient -PqaWorld=BadSpell-QA` möglich. Diese aus der Testwelt kopierte Welt ist kein Survival-Abnahmeweltstand. Die virtuelle Anzeige wird pro neuer Cloud-Task erneut gestartet; Prozesse gehören nicht zum Snapshot.

Im echten Client mit integriertem Server geprüft:

1. Die vier eigenen Itemtexturen wurden korrekt im Inventar und in der Hand dargestellt.
2. Das Formelbuch öffnete über die registrierte Clientbound-Nachricht den Canvas.
3. Runen hinzufügen und Undo, Zoom/Pan sowie die Anordnungsfunktion wurden bedient. Der visuelle Test deckte überlappende Startknoten auf; Vorlage und Einpassen wurden korrigiert.
4. Eine inkompatible EVENT→REGION-Verbindung zeigte „Incompatible port types“; eine typgerechte Verbindung blieb gültig.
5. Die Lichtstärke wurde im Eigenschaftsfeld von 3 auf 4 erhöht, der geänderte Graph gespeichert und an den Handschuh exportiert. Nach Anordnung plus Parameteränderung zeigte das Buch 60 statt 64 Schreibpunkte.
6. Der unveränderte Startzauber verbrauchte 30 FL und 5 FK: Anzeige **70 FL / 95 FK**.
7. Die editierte Stärke-4-Formel verbrauchte 40 FL und 5 FK: Anzeige **60 FL / 95 FK**. Damit ist die Wirkung nicht nur ein fest verdrahteter Kostenwert für den Buchnamen.
8. Nach regulärem „Save and Quit to Title“ und erneutem Laden derselben Welt blieben Buch/Handschuh und verbleibende Zellladung erhalten. Erneutes Wirken der geänderten Formel reduzierte 60 auf **20 FL**, FK stand danach bei 95.

Evidenz: [Formelbuch](docs/screenshots/formula-book.png), [editierter Zauber](docs/screenshots/edited-cast.png), [Zustand nach Weltneuladen](docs/screenshots/reloaded-cast.png). Die letzte kleine Änderung aktualisiert die Eigenschaftsbuttons nach einem Typfehler sofort; sie wurde nach dem manuellen Durchlauf erneut kompiliert und mit den automatisierten Prüfungen gebaut.

## Nicht ausgeführt oder noch offen

- Kein durchgehender Survivalpfad: Worldgen, Materialien, Stationsrezepte und Produktionsstationen fehlen noch.
- Kein vollständiger Runenkatalog oder vollständiges Formelbuch: Module nur im Core, Q2/Q3-Handschuhslots fehlen, keine fünf vollständigen Buchansichten.
- Kein Mehrclient-Test auf einem normalen dedizierten Server, kein Claim-Mod-/PvP-/Modpack-Test.
- Kein Hard-Crash-, Stromausfall-, Chunk-Unload-/Reload- oder allgemeiner Datenmigrationsnachweis. Reguläres Weltneuladen ersetzt diese Tests nicht.
- Keine Last-, Fünf-Personen-UX- oder vollständige Accessibility-Abnahme, keine physische Audioabnahme.
- Keine Prüfung aller Übersetzungen in einer tatsächlich auf Deutsch umgestellten Client-Sitzung; Schlüsselgleichheit wurde automatisch geprüft.
- Der 100-Schritte-Undo- und 20-Revisions-Grenzfall wurde noch nicht vollständig im Client gestresst.
- Unter Xvfb können nichtkritische X11-Cursorwarnungen auftreten. Die Entwicklungsanmeldung meldet fehlende Services-Chat-Schlüssel; der lokale Spieltest funktionierte. Keine zusätzlichen Anmeldedaten wurden angefordert.

## Konkrete Fortsetzung

Zuerst Buchbibliothek, dynamische Parameter/Module, Rechte und Q2/Q3-Handschuhupgrades mit 8/12 Slots vervollständigen. Vier Q1-Slots und der Archivbrowser sind inzwischen implementiert. Dann den in docs/FEATURE_DEPENDENCIES.md beschriebenen Survival-Bootstrap samt echten Rohstoffquellen, Inventaren, Rezeptserializern, Prozesszeiten und Feldversorgung implementieren. Danach die übrigen Mechaniken der vollständigen Checkliste schrittweise integrieren. Die Mod ist trotz erfolgreichem Build ausdrücklich noch nicht vollständig oder produktionsreif.

## Fortsetzung dieser Sitzung: vier Slots und Revisionsbrowser

Erneut ausgeführt: `./gradlew --offline --no-daemon build runGameTestServer --max-workers=2`. Ergebnis: Build erfolgreich, 28 Kerntests, 51 identische Übersetzungsschlüssel, neun eigene GameTests plus ein Vanilla-Test erfolgreich. Die Offline-Option verwendet bereits zuvor tatsächlich aufgelöste offizielle Abhängigkeiten. Online-Maven-Metadaten wurden in dieser Sitzung erneut abgerufen: letzte veröffentlichte 26.2-Version weiterhin 26.2.0.93. Die Deprecation-Warnung zur vorhandenen Tooltip-Override bleibt; kein Compilerfehler.

Neue Serverprüfungen:

- `glove_slots`: Export in getrennte Slots, kein Fallback bei Leerstand, unveränderlicher erster Snapshot, Wraparound und unabhängige Itemkopie.
- `glove_legacy_and_security`: verlustfreie Übernahme alter Ein-Slot-Daten; Besitzerprüfung auch bei leerem ausgewähltem Slot; abgewiesener Fremdimport und ungültige Richtung verändern nichts.
- `glove_server_intents`: ungültige Hand/Richtung, maximal ein Wechsel pro Tick, kein FL/FK-Verbrauch bei leerem Slot und keine Initialisierung falscher Items als Buch.
- `book_history_restore`: Archiv lesen kostet nichts, Wiederherstellung erhält die aktuelle Identität, erhöht die Revision, bezahlt normale Tinte und archiviert die ersetzte Fassung.

Im echten grafischen Minecraft-26.2-Client mit integriertem Server erneut bedient: Buchhistorie geöffnet, Revision 1 gewählt, als Entwurf geladen, gespeichert und danach Revisionen 2 und 1 weiterhin im Archiv gesehen. Umschalt+Mausrad wählte Slot 2/4 mit Leerstand; Export aus dem Buch wurde bestätigt, Zurückwechseln zeigte weiterhin Lichtfunke in Slot 1/4. Screenshots: `docs/screenshots/revision-history.png`, `revision-restored.png`, `glove-slot-selection.png`, `glove-slot-return.png`.

Nicht zusätzlich geprüft: Archivpaginierung auf kleinem Fenster, 20-Revisions-Grenzfall, neue Daten nach Hard-Crash, Slotbedienung in Nebenhand und Mehrclientbetrieb. Diese früheren manuellen Durchläufe betreffen die Development-Klassen; der Mods-Ordner-Test der gepackten Hotfix-JAR steht im Abschlussabschnitt. Frühere Clientnachweise oben stammen aus dem bereits vorhandenen Stand.

## Test der gepackten Hotfix-JAR im Mods-Ordner

Zusätzlich zu den Development-Tests wurde die tatsächlich gebaute JAR in einen separaten Ordner `work/packaged-client/mods` kopiert. Der Minecraft-26.2-/NeoForge-26.2.0.93-Client wurde mit den vorhandenen offiziellen Laufzeitbibliotheken gestartet, aber ohne `fml.modFolders` und ohne die Bad-Spell-/Core-Klassenverzeichnisse oder Core-JAR im Klassenpfad. Der Loadernachweis lautet `badspell (jar(mods/badspell-0.1.0-alpha.jar))`.

In einer Kopie der Creative-QA-Welt wurde die unveränderte Lichtfunke-Formel aus dem Buch exportiert und mit frischer Zelle im vollständig geschlossenen dunklen Steinraum gewirkt. Bestätigt: Partikel, sichtbare Bodenbeleuchtung und Anzeige `Activated | FL 70 | FK 95`. Nach Ablauf erlosch das Licht. Ein unveränderter Bodenbereich außerhalb von Partikeln, Hand und HUD wechselte von mittlerem RGB 11,63/11,63/11,63 auf 24,21/23,34/21,13 und nach Ablauf exakt zurück auf 11,63/11,63/11,63. Dies ergänzt die physikalischen Helligkeitsassertions des Servers um tatsächliche Clientdarstellung.

Unveränderte Screenshots: `docs/screenshots/light-before.png`, `light-after.png`, `light-expired.png`. Der frühere fehlende Nachweis der gepackten JAR wurde damit für diesen lokalen Mods-Ordner-Test nachgeholt. Ein separater Start über einen normalen Endnutzer-Launcher, fremde Shader/Lichtengines und mehrere externe Clients auf einem dedizierten Server bleiben ungetestet.
