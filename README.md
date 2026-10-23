# Bad Spell

## Neue Runenwerkstatt und bebilderter Kodex

Aktuelle Bedienung: [USABILITY_REWORK.md](docs/USABILITY_REWORK.md). Runenauswahl mit Beschreibungs-/Port-/Typsuche, kompatible Quellen direkt aus Eingangszeilen, stationsspezifische Arbeitszonen und Rezeptansichten, gemeinsamer Workshop-Assetstil. G oder Arkaner Kodex öffnet neun bebilderte Tutorialkapitel.

Neue Runen: Wesenposition, Wesen-Gesundheit, Ziel vorhanden, Versatz, Addieren, Subtrahieren, Multiplizieren, Effekte bündeln. Neue Vorlagen: Eigenlicht, Froststoß, Wachsamer Schutz. Direktinteraktion mit Kreaturen wird als Handschuhzauber behandelt; Heilablehnungen werden konkret erklärt. Netzwerkprotokoll 3 erfordert denselben Build auf Client und Server. Die Gesamtmod und M1 bleiben unvollständig.

## M1-Entwicklungsstand vom 23.10.2026

Dieser Abschnitt ersetzt widersprechende Angaben älterer Fortschrittsnotizen weiter unten. M1 ist noch **nicht vollständig abgenommen**. Die neue Clientdarstellung und ein vollständiger Survival-/Mehrspieler-Durchlauf sind offen.

- Typisierte Vektor-, Text-, Entity-, Ereignis- und Regionswerte; verschachtelte Snapshot-Module, benannte Eingaben, typisierte Rückgaben, Bedingungen, Vergleich und begrenzte Auswertung.
- Lichtfelder einschließlich Kugel/Pfad, Heilung, Stoß, Hitze, Kälte, Schild und Zielinformation; verzögerte/begrenzte/periodische Aktivierung mit Energieprüfung je Ausführung. Endliche Felder und Spieleraufträge werden gespeichert.
- Private Formel-Bibliothek mit Suche, Tags und neun komponierten Vorlagen. Eigene Formelmodule lassen sich als Knoten einfügen. Oberflächengravur ist auf 16 Runen je Blockseite begrenzt; nur Glyphen werden öffentlich übertragen.
- Runenmeißel, Resonanzgabel, Feldkompass/Q3, Monokel sowie Q2/Q3-Handschuhe, Zellen und Platten. Handschuhe besitzen 4/8/12 Slots, Zellen 100/300/600 FL. Zellladungen im Inventar werden zusammen verwendet.
- Lokale Bindungen, versiegelte Blöcke sowie begrenzte Item-/Energieübertragung. Offizielle Interaktionsereignisse können Weltaktionen verweigern.
- 54 datengetriebene Verarbeitungsrezepte plus 12 Vanilla-Rezepte. Sieben eigene Stationsmodelle, überarbeitete 16px-Texturen und 3D-Werkzeuge/Zellen. Stilregeln: [ASSET_STYLE.md](docs/ASSET_STYLE.md).

**Grenzen:** Der Runenhalter führt weiterhin nur Punktlicht aus. Umfangreiche Maschinensteuerung, Forschung, Mobs, Bosse, Dimensionen und vollständige Weltgenerierung sind nicht fertig. Kompatibilität mit einzelnen Schutzmods und echte Netzwerkclients sind mit den neuen Abläufen noch nicht geprüft. Keine Behauptung einer vollständigen Umsetzung aller PDF-Features.


Survival-Magie für **Minecraft Java Edition 26.2**, ausschließlich **NeoForge**, Java 25, Package `com.badspell`, Mod-ID `badspell`, Version `0.1.0-alpha`.

**Entwicklungsstand:** Formelbuch mit neuem Editor, vier Handschuh-Slots, funktionierendem Punktlicht sowie einer ersten echten Produktionskette: 37 zusätzliche Material-/Plattenitems, 17 Blöcke einschließlich sieben Verarbeitungsstationen und 36 Stationsrezepte. Inventare, Verarbeitungszeiten, Startzellenadapter und FL-Verbrauch laufen serverseitig. **Die vollständige PDF-Mod ist weiterhin in Entwicklung. Mobs, weitere Zauberwirkungen, Energienetze, Rituale und Dimensionen fehlen noch.** Einzelne Materialien sind Vorprodukte für noch offene Systeme. Testgrenzen stehen in [TESTING.md](TESTING.md), alle Konzepte in [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md).

## Verifizierte Entwicklungswerkzeuge

| Komponente | Version / Herkunft |
|---|---|
| Minecraft | 26.2, unverändert |
| NeoForge | 26.2.0.93, offizielles 26.2-MDK und Maven-Metadaten/POM geprüft |
| ModDevGradle | 2.0.148, offizielles MDK, Plugin tatsächlich aufgelöst |
| Gradle | 9.2.1, offizieller Wrapper, Distribution-SHA-256 gesetzt |
| JDK dieser Cloud-Instanz | Oracle JDK 25.0.5.0.2+1-LTS-2, SHA-256-verifiziert |
| Java-Ziel | 25, auch im Core |
| Direkte Fremdbibliotheken des Core | keine |

Das MDK stammt aus `NeoForgeMDKs/MDK-26.2-ModDevGradle`, Commit `a0340c5454c01e43ccf83977601a4d90afee6e1f`. NeoForge 26.2.0.93 ist eine veröffentlichte Version ohne Beta-/Snapshot-Suffix und die letzte 26.2-Version in den geprüften Maven-Metadaten. Keine API einer anderen Minecraft-Version wird als Ersatz verwendet. NeoForm 26.2-2 und NeoForm Runtime 2.0.31 werden durch diese Werkzeugversionen aufgelöst.

## Entwicklung

In dieser Cloud-Instanz:

```bash
cd /workspace/Bad-Spell
source scripts/cloud-env.sh
./scripts/test-core.sh
./gradlew --no-daemon :core:check --max-workers=2
./gradlew --no-daemon build runGameTestServer --max-workers=2
```

Außerhalb der Cloud genügt ein installiertes JDK 25 mit passendem `JAVA_HOME`; `cloud-env.sh` ist ausschließlich für diese Umgebung gedacht. Der Wrapper lädt Gradle mit Prüfsumme. Für den Cloud-Client zuerst `source scripts/cloud-display.sh`, dann `./gradlew runClient`. Auf einem Rechner mit vorhandener Anzeige nur `JAVA_HOME` setzen und `./gradlew runClient` starten. Dedizierter Entwicklungsserver: `./gradlew runServer`. Die Minecraft-EULA muss der Serverbetreiber selbst lesen und akzeptieren; dieses Projekt akzeptiert sie nicht automatisch.

Gebautes Mod-Artefakt: `build/libs/badspell-0.1.0-alpha.jar`. Der Entwicklungsstand lädt im echten Minecraft-26.2-GameTestServer. Ein separat erzeugtes Core-JAR allein ist keine Minecraft-Mod. Der Testlauf prüft die Development-Klassen; die aktuelle gepackte JAR wurde zusätzlich aus einem separaten Mods-Ordner im Client geprüft (TESTING.md).

Netzwerk: NeoForge-Dokumentation/Maven, Gradle-/Maven-Presets und Mojang-Hosts `libraries.minecraft.net`, `piston-meta.mojang.com`, `piston-data.mojang.com`, `resources.download.minecraft.net` werden benötigt. Die anfänglichen HTTP-403-Sperren sind behoben; JDK, Gradle, NeoForge und Minecraft wurden mit aktiver TLS-/Prüfsummenprüfung bezogen. Keine Mod-API-Schlüssel oder zusätzlichen Geheimnisse erforderlich.

## Aktuellen Funktionsstand im Client prüfen

Neue Creative-Testwelt verwenden und im Tab **Bad Spell** Formelbuch, Runenhandschuh, Start-Feldzelle und Runentinte nehmen. Das Buch öffnet eine Lichtfunke-Vorlage. Runen lassen sich bewegen, hinzufügen, über typgleiche Ports verbinden und löschen. Mausrad im Canvas zoomt, mittlere Maustaste verschiebt die Fläche; das Mausrad in der linken Palette scrollt die Runenliste. Zahlen-/Boolwerte befinden sich im Reiter „Details“. Der Reiter „Runen“ bietet eine Suche, „Prüfen“ zeigt Diagnosen und Kosten. Änderungen speichern, danach **An Handschuh** exportieren; exportiert wird ausschließlich die gespeicherte Revision. Mit Handschuh und Zelle im Inventar auf eine nahe freie Stelle vor einem Block zielen und rechtsklicken. Licht benötigt 30 FL + 5 FK und bleibt 600 Simulationsticks aktiv.

Dieser Kernablauf wurde im Client ausgeführt; Editieren der Lichtstärke änderte die tatsächlichen Kosten. Reguläres Speichern und Weltneuladen wurde ebenfalls geprüft (TESTING.md). Ein erster Survival-Rezeptpfad existiert jetzt; siehe [Herstellungsanleitung](docs/HERSTELLUNG.md). Ein vollständiger Survival-Durchlauf wurde noch nicht geprüft. Andere Lichtformen und die übrigen Zauber sind nicht als Spielwirkung implementiert. Der Q1-Handschuh besitzt vier unabhängige Snapshot-Slots. Umschalt+Mausrad wählt den Slot in Haupt- oder Nebenhand. Export schreibt in den ausgewählten Slot; leere Slots wirken keinen Ersatzzauber. Q2/Q3-Upgrades fehlen noch. Über **Historie** lassen sich frühere gespeicherte Revisionen als Entwurf laden und mit normalen Tintenkosten in einer neuen Revision speichern. Vor dem Öffnen müssen Änderungen gespeichert sein.

## Unterlagen

- [Implementierungsstatus und vollständige Checkliste](IMPLEMENTATION_STATUS.md)
- [Architektur](ARCHITECTURE.md)
- [Designentscheidungen](DESIGN_DECISIONS.md)
- [Tests, Grenzen und Fortsetzung](TESTING.md)
- [Vollständiger extrahierter Designtext](docs/DESIGN_SPECIFICATION.txt), 58 PDF-Seiten
- Offizielle Dokumentation: https://docs.neoforged.net/

Repository-Lizenz: GPL v3 gemäß vorhandener LICENSE. Herkunft und MIT-Lizenz der MDK-Dateien: [MDK_LICENSE.txt](docs/MDK_LICENSE.txt). Keine Fremdassets eingebunden.

## JAR im Repository

Der aktuelle unvollständige Entwicklungsbuild liegt unter [releases/0.1.0-alpha/badspell-0.1.0-alpha.jar](releases/0.1.0-alpha/badspell-0.1.0-alpha.jar), inklusive SHA-256-Datei. Er benötigt Minecraft 26.2 und NeoForge 26.2.0.93. Noch kein vollständiger Survival-Spielstand; bekannte Grenzen siehe IMPLEMENTATION_STATUS.md und TESTING.md.

## Licht-Hotfix

Ein Fehler des ersten veröffentlichten Builds führte dazu, dass Lichtfunke FL/FK verbrauchte, aber Luftblöcke nicht tatsächlich beleuchtete. Die aktuelle JAR enthält die Korrektur für die Minecraft-Lichtberechnung und einen kurzen Partikeleffekt bei Aktivierung. Die Kosten bleiben unverändert. Neue Tests prüfen reale Helligkeit und Erlöschen. Ersetze die alte JAR; installiere nicht zwei Bad-Spell-JARs gleichzeitig. Andere Effekte als Punktlicht bleiben unimplementiert.

Aktuelle Ansichten: [Formeleditor](docs/screenshots/formula-editor.png), [Details](docs/screenshots/formula-details.png), [Kristallschleifer](docs/screenshots/crystal-grinder.png).
