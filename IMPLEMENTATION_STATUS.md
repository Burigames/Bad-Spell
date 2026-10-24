# Implementierungsstatus – Bad Spell

Stand: 24.10.2026. **M1: abgeschlossen im unten definierten Umfang. Die vollständige PDF-Mod ist weiterhin in Entwicklung.**

Plattform: Minecraft Java 26.2 · NeoForge 26.2.0.93 · Java 25 · ModDevGradle 2.0.148 · Gradle 9.2.1 · Mod-Version 0.1.0-alpha · Netzwerkprotokoll 4.

## Verbindlicher M1-Abschluss

Abnahme und reproduzierbare Prüfungen: [M1_ACCEPTANCE.md](docs/M1_ACCEPTANCE.md). Alle sechs zuletzt offenen Punkte sind umgesetzt bzw. geprüft: Feldkristall-Leitung, gemeinsamer Runenhalter-Interpreter, Werkzeuge/Qualitäten, Survival-Herstellungsroute, zwei echte Clients mit Neustart und bereinigte Dokumentation.

- 45 Kernprüfungen und 49 erforderliche Server-GameTests erfolgreich.
- 848 tatsächliche Rezeptschritte aus endlichen Rohstoff-/Vanilla-Fixtures; alle 18 Zielgegenstände und sieben Stationen. Mod-Rohstoffe wurden mit echten Survival-Drops gewonnen.
- Zwei echte Clients mit installierter JAR an einem dedizierten Entwicklungsserver; Buchrevision und Handschuh-Slot per Netzwerk bedient, fremder Stationszugriff abgewiesen.
- Nach Serverneustart: Geräteauftrag, bezahltes Feld, Oberflächenformel, Handschuh-Snapshot und aktiver Slot wiederhergestellt.
- Überarbeitete Itemmodelle mit getrennt erkennbaren Rohlingen/Qualitäten und einer für 26.2 geeigneten gemeinsamen Textur-Atlas-Zuordnung.

„Getestet“ bezeichnet den genannten Umfang, keine pauschale Abnahme weiterer PDF-Systeme. Natürliche Weltverteilung, Drittanbieter-Schutzmods und andere Auflösungen gehören nicht zu den behaupteten Prüfungen.

## Sämtliche 161 PDF-Konzepte

Diese Tabelle ist der aktuelle Stand; widersprüchliche alte Fortschrittsnotizen wurden entfernt.

| Kategorie | PDF-ID | Feature | Tatsächlicher Stand |
|---|---|---|---|
| Magische Gegenstände (9) | 01 | Runenmeißel | Getestet: physische 4×4-Felder, Haltbarkeit, Rechte, öffentliche Glyphen und Client-Editor. |
| Magische Gegenstände (9) | 02 | Runenhandschuh | Getestet: 4/8/12 Slots, eigene Snapshots, Sicherung, Export und Neustart mit echtem Client. |
| Magische Gegenstände (9) | 03 | Arkanes Monokel | Getestet: Kopfslot, begrenzte Sicht, Intensität und Fokusverbrauch; keine Rohstoff-Röntgensicht. |
| Magische Gegenstände (9) | 04 | Feldkristall | Getestet: gerichteter Leiter ohne Speicher/Erzeugung; Eigentümerprüfung, 80 FL/s, maximal 16 Kristalle. |
| Magische Gegenstände (9) | 05 | Runentinte | Getestet: 64 Schreibpunkte, echter Verbrauch, Nachfüllen und Herstellungsroute. |
| Magische Gegenstände (9) | 06 | Runenplatte | Getestet: Q1/Q2/Q3-Leerplatten, echte Gravur, unveränderliche Formelkopien und Import. |
| Magische Gegenstände (9) | 07 | Formelbuch | Getestet: visueller Editor, Typsuche, Portwahl, Bibliothek, Module, Historie und Netzwerkspeicherung. |
| Magische Gegenstände (9) | 08 | Feldkompass | Getestet: ortsfeste Signatur, Richtung/Signal, Q3-Reichweite und Kopierverhalten. |
| Magische Gegenstände (9) | 09 | Resonanzgabel | Getestet: Diagnose/Aktivierung, erfolgreiche Haltbarkeitskosten, Reparatur und Herstellung. |
| Blöcke (20) | 01 | Gravurstation | Getestet: Buch-/Plattengravur, Rezeptketten, Inventar/FL und mehrstufige Werkzeugfertigung. |
| Blöcke (20) | 02 | Alchemietisch | In Entwicklung: M1-Verarbeitung einschließlich Mondessenz/Mondsalz getestet; freie Alchemie später. |
| Blöcke (20) | 03 | Arkaner Schmelzofen | In Entwicklung: M1-Barren-/Legierungskette getestet; zusätzliche thermische Maschinenmechaniken später. |
| Blöcke (20) | 04 | Ritualsockel | Nicht begonnen |
| Blöcke (20) | 05 | Arkanwerkbank | Getestet: Primitive-Rezepte, Zellversorgung und sämtliche M1-Werkzeugketten. |
| Blöcke (20) | 06 | Runenblock | Nicht begonnen |
| Blöcke (20) | 07 | Kristallkabel | Nicht begonnen |
| Blöcke (20) | 08 | Kristall-Konnektor | Nicht begonnen |
| Blöcke (20) | 09 | Kristall-Relais | Nicht begonnen |
| Blöcke (20) | 10 | Runenhalter | Getestet: gemeinsamer Effektadapter, Flächen/Kombinationen, Offlinebetrieb, Start/Stop und persistente Zeitaufträge. |
| Blöcke (20) | 11 | Arkanstein | Implementiert, aber ungetestet: Block, Modell, Rezept, Loot |
| Blöcke (20) | 12 | Kristallglas | Nicht begonnen |
| Blöcke (20) | 13 | Arkane Laterne | Nicht begonnen |
| Blöcke (20) | 14 | Runenbücherregal | Nicht begonnen |
| Blöcke (20) | 15 | Kristallsäule | Nicht begonnen |
| Blöcke (20) | 16 | Feldkristallerz | In Entwicklung: Survival-Abbau und echte Drops getestet; natürliche Erzverteilung nicht vollständig abgenommen. |
| Blöcke (20) | 17 | Arkanerzblock | In Entwicklung: Survival-Abbau und Schmelzroute getestet; natürliche Erzverteilung nicht vollständig abgenommen. |
| Blöcke (20) | 18 | Runenholzstamm | In Entwicklung: Survival-Abbau und Bretter-/Werkzeugketten getestet; vollständiger Wald-Biomumfang später. |
| Blöcke (20) | 19 | Leuchtmoos | Implementiert, aber ungetestet: Multiface-Block, Vermehrung, Weltgen |
| Blöcke (20) | 20 | Resonanzkristall | In Entwicklung: Survival-Drops und Verarbeitung getestet; vollständige Geoden-/Biom-Abnahme später. |
| Ausrüstung (18) | 01 | Runenschwerter | Nicht begonnen |
| Ausrüstung (18) | 02 | Magierstäbe | Nicht begonnen |
| Ausrüstung (18) | 03 | Zauberbücher | Nicht begonnen |
| Ausrüstung (18) | 04 | Magische Bögen | Nicht begonnen |
| Ausrüstung (18) | 05 | Arkane Dolche | Nicht begonnen |
| Ausrüstung (18) | 06 | Magierroben | Nicht begonnen |
| Ausrüstung (18) | 07 | Runenrüstung | Nicht begonnen |
| Ausrüstung (18) | 08 | Kristallrüstung | Nicht begonnen |
| Ausrüstung (18) | 09 | Arkane Schilde | Nicht begonnen |
| Ausrüstung (18) | 10 | Magische Ringe | Nicht begonnen |
| Ausrüstung (18) | 11 | Amulette | Nicht begonnen |
| Ausrüstung (18) | 12 | Talismane | Nicht begonnen |
| Ausrüstung (18) | 13 | Magische Gürtel | Nicht begonnen |
| Ausrüstung (18) | 14 | Magische Umhänge | Nicht begonnen |
| Ausrüstung (18) | 15 | Schwebestiefel | Nicht begonnen |
| Ausrüstung (18) | 16 | Magischer Greifhaken | Nicht begonnen |
| Ausrüstung (18) | 17 | Arkaner Köcher | Nicht begonnen |
| Ausrüstung (18) | 18 | Magische Handschuhe | Nicht begonnen |
| Normale magische Mobs (18) | 01 | Geisterfuchs | Nicht begonnen |
| Normale magische Mobs (18) | 02 | Mondmotte | Nicht begonnen |
| Normale magische Mobs (18) | 03 | Kristallschildkröte | Nicht begonnen |
| Normale magische Mobs (18) | 04 | Himmelsqualle | Nicht begonnen |
| Normale magische Mobs (18) | 05 | Hohle Rüstung | Nicht begonnen |
| Normale magische Mobs (18) | 06 | Obsidianbestie | Nicht begonnen |
| Normale magische Mobs (18) | 07 | Arkanes Auge | Nicht begonnen |
| Normale magische Mobs (18) | 08 | Alchemist | Nicht begonnen |
| Normale magische Mobs (18) | 09 | Runenwächter | Nicht begonnen |
| Normale magische Mobs (18) | 10 | Artefakthändler | Nicht begonnen |
| Normale magische Mobs (18) | 11 | Abtrünniger Magier | Nicht begonnen |
| Normale magische Mobs (18) | 12 | Runendrohne | Nicht begonnen |
| Normale magische Mobs (18) | 13 | Lebendes Buch | Nicht begonnen |
| Normale magische Mobs (18) | 14 | Feldmanifestation | Nicht begonnen |
| Normale magische Mobs (18) | 15 | Runenbaum | Nicht begonnen |
| Normale magische Mobs (18) | 16 | Arkaner Hirsch | Nicht begonnen |
| Normale magische Mobs (18) | 17 | Kristallwyrm | Nicht begonnen |
| Normale magische Mobs (18) | 18 | Arkaner Koloss | Nicht begonnen |
| Bosse (10) | 01 | Kristallmatriarchin | Nicht begonnen |
| Bosse (10) | 02 | Eidwächter | Nicht begonnen |
| Bosse (10) | 03 | Ur-Elementar | Nicht begonnen |
| Bosse (10) | 04 | Der Beobachter | Nicht begonnen |
| Bosse (10) | 05 | Waldwyrm | Nicht begonnen |
| Bosse (10) | 06 | Der Erste Hirsch | Nicht begonnen |
| Bosse (10) | 07 | Tiefenverschlinger | Nicht begonnen |
| Bosse (10) | 08 | Frostseher | Nicht begonnen |
| Bosse (10) | 09 | Sturmrochen | Nicht begonnen |
| Bosse (10) | 10 | Sternendrache | Nicht begonnen |
| Weltgenerierung (23) | 01 | Arkanwald | Nicht begonnen |
| Weltgenerierung (23) | 02 | Kristallhöhlen | Nicht begonnen |
| Weltgenerierung (23) | 03 | Nebelhain | Nicht begonnen |
| Weltgenerierung (23) | 04 | Äthergipfel | Nicht begonnen |
| Weltgenerierung (23) | 05 | Verlassener Magierturm | Nicht begonnen |
| Weltgenerierung (23) | 06 | Runenkreis | Nicht begonnen |
| Weltgenerierung (23) | 07 | Vergessener Tempel | Nicht begonnen |
| Weltgenerierung (23) | 08 | Alchemistenhütte | Nicht begonnen |
| Weltgenerierung (23) | 09 | Astrales Observatorium | Nicht begonnen |
| Weltgenerierung (23) | 10 | Vergessenes Forschungslabor | Nicht begonnen |
| Weltgenerierung (23) | 11 | Runenlabyrinth | Nicht begonnen |
| Weltgenerierung (23) | 12 | Versiegelte Schatzkammer | Nicht begonnen |
| Weltgenerierung (23) | 13 | Arkane Schmiede | Nicht begonnen |
| Weltgenerierung (23) | 14 | Versunkene Bibliothek | Nicht begonnen |
| Weltgenerierung (23) | 15 | Feldkristalladern | Nicht begonnen |
| Weltgenerierung (23) | 16 | Arkanerz | Nicht begonnen |
| Weltgenerierung (23) | 17 | Mondblüte | Implementiert, aber ungetestet: Pflanze, Weltgen und Verarbeitung |
| Weltgenerierung (23) | 18 | Runenholz | Nicht begonnen |
| Weltgenerierung (23) | 19 | Ätherquelle | Nicht begonnen |
| Weltgenerierung (23) | 20 | Resonanzgeode | Nicht begonnen |
| Weltgenerierung (23) | 21 | Die Zersplitterung | Nicht begonnen |
| Weltgenerierung (23) | 22 | Arkane Stürme | Nicht begonnen |
| Weltgenerierung (23) | 23 | Feldanomalien | Nicht begonnen |
| Zauberfortschritt (8) | 01 | Forschung und Entschlüsselung | Nicht begonnen |
| Zauberfortschritt (8) | 02 | Lernen von NPCs | Nicht begonnen |
| Zauberfortschritt (8) | 03 | Wissensbasierter Fortschritt | Nicht begonnen |
| Zauberfortschritt (8) | 04 | Ausrüstungsbasierter Fortschritt | In Entwicklung: M1-Qualitätsketten Q1–Q3 getestet; übrige Ausrüstung und Forschungszugänge später. |
| Zauberfortschritt (8) | 05 | Eigene Formeln entwickeln | Getestet: eigene typisierte Graphen, Bedingungen, Module, Fehleranzeige und tatsächliche Weltwirkungen. |
| Zauberfortschritt (8) | 06 | Formeln finden | Nicht begonnen |
| Zauberfortschritt (8) | 07 | Formeln von NPCs erwerben | Nicht begonnen |
| Zauberfortschritt (8) | 08 | Bestehende Formeln analysieren | Nicht begonnen |
| Alchemie (10) | 01 | Trankbrauen | Nicht begonnen |
| Alchemie (10) | 02 | Essenzgewinnung | In Entwicklung: ausführbare Mondessenz-/Mondsalz-Routen getestet; übrige Essenzfamilien später. |
| Alchemie (10) | 03 | Transmutation | Nicht begonnen |
| Alchemie (10) | 04 | Materialveredelung | In Entwicklung: M1-Veredelungsketten getestet; allgemeine Transmutation nicht umgesetzt. |
| Alchemie (10) | 05 | Magische Elixiere | Nicht begonnen |
| Alchemie (10) | 06 | Alchemistische Bomben | Nicht begonnen |
| Alchemie (10) | 07 | Magische Tinten | Getestet: Runentintenherstellung und Schreibpunkte. |
| Alchemie (10) | 08 | Homunkuli | Nicht begonnen |
| Alchemie (10) | 09 | Katalysatoren | In Entwicklung: Quarz als Kristallisationskeim; kein allgemeines Katalysatorsystem. |
| Alchemie (10) | 10 | Alchemistische Forschung | Nicht begonnen |
| Verzauberungen und Flüche (10) | 01 | Runenverzauberung | Nicht begonnen |
| Verzauberungen und Flüche (10) | 02 | Lebewesenflüche | Nicht begonnen |
| Verzauberungen und Flüche (10) | 03 | Ortsflüche | Nicht begonnen |
| Verzauberungen und Flüche (10) | 04 | Fluchbrechung | Nicht begonnen |
| Verzauberungen und Flüche (10) | 05 | Seelenbindung | Nicht begonnen |
| Verzauberungen und Flüche (10) | 06 | Magische Siegel | Nicht begonnen |
| Verzauberungen und Flüche (10) | 07 | Temporäre Verstärkungen | Nicht begonnen |
| Verzauberungen und Flüche (10) | 08 | Magische Stabilität | In Entwicklung (nur Java-Kern) |
| Verzauberungen und Flüche (10) | 09 | Verzauberungsanalyse | Nicht begonnen |
| Verzauberungen und Flüche (10) | 10 | Verzauberungsmodifikation | Nicht begonnen |
| Automatisierung (15) | 01 | Magische Sensoren | Nicht begonnen |
| Automatisierung (15) | 02 | Runenlogik | Getestet: typisierte Logik, Bedingungen, Unterformeln und portable/stationäre Ausführung. |
| Automatisierung (15) | 03 | Magische Zeitschaltungen | Getestet: Verzögerung, Intervall, Begrenzung, Energiemangelpause und Geräte-Neustart. |
| Automatisierung (15) | 04 | Fernsteuerung | Nicht begonnen |
| Automatisierung (15) | 05 | Gegenstandstransport | In Entwicklung: lokale gebundene Inventartransfers getestet; vollständige Transportnetze später. |
| Automatisierung (15) | 06 | Magische Teleporter | Nicht begonnen |
| Automatisierung (15) | 07 | Schwebende Plattformen | Nicht begonnen |
| Automatisierung (15) | 08 | Magische Förderanlagen | Nicht begonnen |
| Automatisierung (15) | 09 | Automatisches Crafting | Nicht begonnen |
| Automatisierung (15) | 10 | Automatisierte Alchemie | Nicht begonnen |
| Automatisierung (15) | 11 | Magischer Bergbau | Nicht begonnen |
| Automatisierung (15) | 12 | Magische Landwirtschaft | Nicht begonnen |
| Automatisierung (15) | 13 | Magische Barrieren | In Entwicklung: endliche Schutzfelder getestet; dauerhafte Bau-Barrieren später. |
| Automatisierung (15) | 14 | Automatische Verteidigung | Nicht begonnen |
| Automatisierung (15) | 15 | Magische Beleuchtung | Getestet: echte temporäre Lichtfelder und bedingte Geräteaktivierung; Laternenblock später. |
| Rituale und Beschwörungen (20) | 01 | Beschwörungsrituale | Nicht begonnen |
| Rituale und Beschwörungen (20) | 02 | Transformationsrituale | Nicht begonnen |
| Rituale und Beschwörungen (20) | 03 | Schutzrituale | Nicht begonnen |
| Rituale und Beschwörungen (20) | 04 | Bindungsrituale | Nicht begonnen |
| Rituale und Beschwörungen (20) | 05 | Reinigungsrituale | Nicht begonnen |
| Rituale und Beschwörungen (20) | 06 | Wesenbeschwörung | Nicht begonnen |
| Rituale und Beschwörungen (20) | 07 | Bossbeschwörung | Nicht begonnen |
| Rituale und Beschwörungen (20) | 08 | Vertraute (Familiars) | Nicht begonnen |
| Rituale und Beschwörungen (20) | 09 | Temporäre Beschwörungen | Nicht begonnen |
| Rituale und Beschwörungen (20) | 10 | Dauerhafte Beschwörungen | Nicht begonnen |
| Rituale und Beschwörungen (20) | 11 | Mehrblock-Rituale | Nicht begonnen |
| Rituale und Beschwörungen (20) | 12 | Ritualkreise | Nicht begonnen |
| Rituale und Beschwörungen (20) | 13 | Ritualopfergaben | Nicht begonnen |
| Rituale und Beschwörungen (20) | 14 | Mehrspieler-Rituale | Nicht begonnen |
| Rituale und Beschwörungen (20) | 15 | Ritualbedingungen | Nicht begonnen |
| Rituale und Beschwörungen (20) | 16 | Portalsrituale | Nicht begonnen |
| Rituale und Beschwörungen (20) | 17 | Seelenrituale | Nicht begonnen |
| Rituale und Beschwörungen (20) | 18 | Astrale Rituale | Nicht begonnen |
| Rituale und Beschwörungen (20) | 19 | Ritualunterbrechung | Nicht begonnen |
| Rituale und Beschwörungen (20) | 20 | Ritualautomatisierung | Nicht begonnen |

## Runensystem und zusätzliche M1-Inhalte

91 registrierte Operatoren/Typvarianten, erweiterbare Modulaufrufe, maximal 128 expandierte Knoten und 512 Auswertungsoperationen. Die vollständigen Namen, Beschreibungen, Pflichtports und Parametergrenzen stehen im spielinternen Runenlexikon.

Getestete Wirkungsadapter: Licht (Punkt/Kreis/Kugel/Strahl/Pfad), Wärme, Kälte, Schild, Heilung, Stoß, Weseninformation, lokale Bindung/Item-/FL-Übertragung und Siegel. Bündel sind auf acht geeignete Effekte begrenzt; Geräte verwenden Ursprung statt eines lebenden Selbst und zahlen FL statt Spielerfokus. Keine beliebige Java-Ausführung aus Formeln.

Zusätzlich spielbar: Q1/Q2/Q3-Feldzellen, Zelladapter, Schleifer und Runenpresse, 55 Verarbeitungsrezepte sowie 13 Vanilla-Rezepte. Der Arkane Kodex enthält neun bebilderte Kapitel und ist auch mit G erreichbar.

## Folgende Meilensteine

Forschung/Entschlüsselung, vollständige Automatisierungsnetze mit Kabeln/Speichern/Kollektoren, Rituale, weitere Ausrüstung, Weltbiome/Strukturen, Mobs, NPCs, Bosse und Dimensionen bleiben entsprechend der Tabelle offen. M1-Abschluss bedeutet nicht Abschluss dieser Systeme.
