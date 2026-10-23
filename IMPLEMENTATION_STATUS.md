# Implementierungsstatus – Bad Spell

Ziel: Minecraft Java 26.2 · NeoForge · JDK 25 · com.badspell · 0.1.0-alpha.
Derzeit ein gebauter erster Mod-Stand mit Formelkern, vier Items, Canvas und serverseitigem Lichtzauber, **keine vollständige Mod und kein durchspielbarer Survivalstand**. Stand: 23.10.2026.

Status: Nicht begonnen / In Entwicklung / Implementiert, aber ungetestet / Getestet / Bekannte Probleme. „Getestet“ beschreibt ausschließlich den ausdrücklich genannten Testumfang.

## Ursprüngliche 161 Konzepte (PDF Anhang A)

Alle 161 Konzepte sind hier einzeln erfasst. Ein getesteter Hilfsdatentyp markiert kein ganzes Gameplayfeature als fertig.

| Kategorie | PDF-ID | Feature | Status |
|---|---|---|---|
| Magische Gegenstände (9) | 01 | Runenmeißel | Nicht begonnen |
| Magische Gegenstände (9) | 02 | Runenhandschuh | In Entwicklung: vier Q1-Slots; Export und Slotwechsel server-/clientgetestet; Q2/Q3 offen |
| Magische Gegenstände (9) | 03 | Arkanes Monokel | Nicht begonnen |
| Magische Gegenstände (9) | 04 | Feldkristall | Nicht begonnen |
| Magische Gegenstände (9) | 05 | Runentinte | In Entwicklung: echter Verbrauch/Refill getestet; Herstellung offen |
| Magische Gegenstände (9) | 06 | Runenplatte | Nicht begonnen |
| Magische Gegenstände (9) | 07 | Formelbuch | In Entwicklung: Canvas bedient, Serveraktionen getestet; Bibliothek/Module offen |
| Magische Gegenstände (9) | 08 | Feldkompass | Nicht begonnen |
| Magische Gegenstände (9) | 09 | Resonanzgabel | Nicht begonnen |
| Blöcke (20) | 01 | Gravurstation | Nicht begonnen |
| Blöcke (20) | 02 | Alchemietisch | Nicht begonnen |
| Blöcke (20) | 03 | Arkaner Schmelzofen | Nicht begonnen |
| Blöcke (20) | 04 | Ritualsockel | Nicht begonnen |
| Blöcke (20) | 05 | Arkanwerkbank | Nicht begonnen |
| Blöcke (20) | 06 | Runenblock | Nicht begonnen |
| Blöcke (20) | 07 | Kristallkabel | Nicht begonnen |
| Blöcke (20) | 08 | Kristall-Konnektor | Nicht begonnen |
| Blöcke (20) | 09 | Kristall-Relais | Nicht begonnen |
| Blöcke (20) | 10 | Runenhalter | Nicht begonnen |
| Blöcke (20) | 11 | Arkanstein | Nicht begonnen |
| Blöcke (20) | 12 | Kristallglas | Nicht begonnen |
| Blöcke (20) | 13 | Arkane Laterne | Nicht begonnen |
| Blöcke (20) | 14 | Runenbücherregal | Nicht begonnen |
| Blöcke (20) | 15 | Kristallsäule | Nicht begonnen |
| Blöcke (20) | 16 | Feldkristallerz | Nicht begonnen |
| Blöcke (20) | 17 | Arkanerzblock | Nicht begonnen |
| Blöcke (20) | 18 | Runenholzstamm | Nicht begonnen |
| Blöcke (20) | 19 | Leuchtmoos | Nicht begonnen |
| Blöcke (20) | 20 | Resonanzkristall | Nicht begonnen |
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
| Weltgenerierung (23) | 17 | Mondblüte | Nicht begonnen |
| Weltgenerierung (23) | 18 | Runenholz | Nicht begonnen |
| Weltgenerierung (23) | 19 | Ätherquelle | Nicht begonnen |
| Weltgenerierung (23) | 20 | Resonanzgeode | Nicht begonnen |
| Weltgenerierung (23) | 21 | Die Zersplitterung | Nicht begonnen |
| Weltgenerierung (23) | 22 | Arkane Stürme | Nicht begonnen |
| Weltgenerierung (23) | 23 | Feldanomalien | Nicht begonnen |
| Zauberfortschritt (8) | 01 | Forschung und Entschlüsselung | Nicht begonnen |
| Zauberfortschritt (8) | 02 | Lernen von NPCs | Nicht begonnen |
| Zauberfortschritt (8) | 03 | Wissensbasierter Fortschritt | Nicht begonnen |
| Zauberfortschritt (8) | 04 | Ausrüstungsbasierter Fortschritt | Nicht begonnen |
| Zauberfortschritt (8) | 05 | Eigene Formeln entwickeln | In Entwicklung: Canvas und Serverprüfung getestet; vollständiger Editor offen |
| Zauberfortschritt (8) | 06 | Formeln finden | Nicht begonnen |
| Zauberfortschritt (8) | 07 | Formeln von NPCs erwerben | Nicht begonnen |
| Zauberfortschritt (8) | 08 | Bestehende Formeln analysieren | Nicht begonnen |
| Alchemie (10) | 01 | Trankbrauen | Nicht begonnen |
| Alchemie (10) | 02 | Essenzgewinnung | Nicht begonnen |
| Alchemie (10) | 03 | Transmutation | Nicht begonnen |
| Alchemie (10) | 04 | Materialveredelung | Nicht begonnen |
| Alchemie (10) | 05 | Magische Elixiere | Nicht begonnen |
| Alchemie (10) | 06 | Alchemistische Bomben | Nicht begonnen |
| Alchemie (10) | 07 | Magische Tinten | Nicht begonnen |
| Alchemie (10) | 08 | Homunkuli | Nicht begonnen |
| Alchemie (10) | 09 | Katalysatoren | Nicht begonnen |
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
| Automatisierung (15) | 02 | Runenlogik | In Entwicklung (nur Java-Kern) |
| Automatisierung (15) | 03 | Magische Zeitschaltungen | Nicht begonnen |
| Automatisierung (15) | 04 | Fernsteuerung | Nicht begonnen |
| Automatisierung (15) | 05 | Gegenstandstransport | Nicht begonnen |
| Automatisierung (15) | 06 | Magische Teleporter | Nicht begonnen |
| Automatisierung (15) | 07 | Schwebende Plattformen | Nicht begonnen |
| Automatisierung (15) | 08 | Magische Förderanlagen | Nicht begonnen |
| Automatisierung (15) | 09 | Automatisches Crafting | Nicht begonnen |
| Automatisierung (15) | 10 | Automatisierte Alchemie | Nicht begonnen |
| Automatisierung (15) | 11 | Magischer Bergbau | Nicht begonnen |
| Automatisierung (15) | 12 | Magische Landwirtschaft | Nicht begonnen |
| Automatisierung (15) | 13 | Magische Barrieren | Nicht begonnen |
| Automatisierung (15) | 14 | Automatische Verteidigung | Nicht begonnen |
| Automatisierung (15) | 15 | Magische Beleuchtung | Nicht begonnen |
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

## Zusätzliche Mechaniken, Produktionsregeln und Querschnittsfunktionen

| Quelle | Feature | Status | Grenzen / nächste Arbeit |
|---|---|---|---|
| PDF 02 | Typisierte Graphen; maximal 128 Knoten, 512 Operationen | Getestet | Kernprüfungen und serverseitige Formelvalidierung; nicht alle Runen umgesetzt |
| PDF 02 | Logische Vergleiche, UND/ODER/NICHT, bedingte Effektwahl | Getestet | Numerischer Vergleich; Effektauswahl lazy; kein allgemeiner polymorpher FALLS-Port |
| PDF 02 | Verschachtelte Module bis 8 Ebenen und konstante Parameterbindungen | Getestet | Expansion einzelner Effektausgänge; dynamische Modulports offen |
| PDF 02 | Explizite Aktivierung, FL/FK, ST-Schwellen, Reichweite | Getestet | Kernprüfungen; Licht-Weltadapter separat getestet; keine Claim-Mod-Integration |
| PDF 02 | Zeitoperatoren, Entitätsziele, Bindung, übrige Runeffekte | Nicht begonnen | Nur die unten aufgeführten Operationen existieren |
| PDF 02/16 | Versionierte, begrenzte Formelserialisierung und SHA-256 | Getestet | Schema 1; Itemkomponenten-Roundtrip servergetestet; Prüfsumme ist keine Authentifizierung |
| PDF 16 | Formeldateien: Atomarer Austausch, Besitzer und Revisionskonflikte | Getestet | Separater Dateispeicher; zusätzlicher Itemadapter vorhanden; kein fsync-Crashnachweis |
| PDF 03 | 11 Stationsprofile W/OF/AW/KS/AT/AS/GS/RP/RS/ÄK/FS | Nicht begonnen | AP ist Teilfunktion des Alchemietischs; Vanilla W/OF noch ohne Bad-Spell-Rezepte |
| PDF 03 | Q0/Q1/Q2/Q3, Zwischenmaterialien, Flüssigkeiten, Behälterrücklauf | Nicht begonnen | Keine registrierten Materialien |
| PDF 03/17 | Transitive Produktionsplanung, konkrete Alternativen und Chargenreste | Getestet | Java-Modell mit expliziten Rohstoffen; keine Stationsintegration |
| PDF 03/17 | Atomarer Maschinenabschluss, Outputkapazität und Replay-Schutz | Getestet | Nur in-memory; kein Prozess-Timer oder persistentes Journal |
| PDF 04 | T0–T5 und vier Erwerbswege; pro Spieler gelerntes Wissen | Nicht begonnen | Kein XP-/Reputationssystem hinzufügen |
| PDF 05/15 | Formelbuch: fünf Ansichten, Zoom 25–400%, Pan und Verbindungen | In Entwicklung | Minecraft-Canvas, typisierte Verbindung, Zoom/Pan, Anordnen, numerisches Editieren, Undo und Speichern im Client bedient; fünf vollständige Ansichten fehlen |
| PDF 05/15 | Formelbuch: 100 Undo-Schritte, 20 Revisionen, Tinte, Exportrechte | In Entwicklung | Canvas-Undo 100, Bucharchiv begrenzt auf 20; Tinte/Revision/privater Export servergetestet; Archivbrowser mit paginierter Auswahl und Wiederherstellung als Entwurf implementiert; Server-Restore und Auswahl/Wiederherstellung/Speichern im Client geprüft; Paginierungs-/Grenzfallabnahme offen |
| PDF 05 | Meißel: 6 Flächen × 4×4 Slots; Vorschau ohne Verbrauch | Nicht begonnen |  |
| PDF 05 | Handschuh: 4/8/12 Slots, unveränderliche Importe, UUID/Revision | In Entwicklung | Vier Q1-Slots, unabhängige Importe, Slotwechsel, Altbestandsmigration und Besitzerprüfung servergetestet; Q2/Q3 mit 8/12 Slots und Spielerbackup fehlen |
| PDF 05 | Monokel ohne Handslot; Privatsphäre; Kompass 1024/4096 Blöcke | Nicht begonnen |  |
| PDF 06 | Runenholzset: Stamm, Brett, Treppe, Zaun, Setzling | Nicht begonnen |  |
| PDF 07/22 | Rüstungseinzelteile, Q2/Q3-Upgrades, Reparatur und Softcap 25% | Nicht begonnen |  |
| PDF 07 | Zubehörslots, Ladungen, 64-Pfeil-Köcher, Bewegungslimits | Nicht begonnen |  |
| PDF 08 | Fluch I–III, erreichbare Reinigung, Einwilligung bei Seelenbindung | Nicht begonnen |  |
| PDF 09 | 8 Essenzarten; Tränke, Beschichtungen, Bomben, Katalysatorverschleiß | Nicht begonnen |  |
| PDF 09 | Homunkulus: 3/Spieler, 1 Aufgabe, Wartung/Ablauf | Nicht begonnen |  |
| PDF 10 | Netztopologie, geladene Chunks, Kanäle, ACL, Prioritäten 1–5 | Nicht begonnen |  |
| PDF 02/10 | Kabel 80/320 FL/t; Verlust 2%/16; Konnektor/Relais-Limits | Nicht begonnen |  |
| PDF 04/10 | Kollektor 8/2/60 FL/t; Quellenlimit; Speicher 2000/12000 FL | Nicht begonnen | EnergyAccount ist kein Speicherblock; tragbare 100-FL-Zelle als Item vorhanden |
| PDF 10 | Vier Musteranlagen A–D einschließlich sicherem Portal und Tintenanlage | Nicht begonnen |  |
| PDF 11 | R3/R5/R7/Astral-Multiblöcke und fünf Ritualphasen | Nicht begonnen |  |
| PDF 11 | Ritualabbruchtabelle, anteiliger Verbrauch, Boss-Cooldown 2h | Nicht begonnen |  |
| PDF 11 | Max. 64 Großrituale, 3 Ritualbosse/Dimension, manuelle Bestätigung | Nicht begonnen |  |
| PDF 12 | Spawncap, friedliche Ernte, Taming/Summon-Regeln, 2 Vertraute | Nicht begonnen |  |
| PDF 13 | Bossphasen, Telegraphen, Konter, garantierte Erstbeute, HP-Skalierung | Nicht begonnen |  |
| PDF 14 | Strukturen/Loot, Pity nach 3 Dungeons, Abstände, kein automatisches Retrogen | Nicht begonnen |  |
| PDF 14 | Zersplitterung: 3 Routen × mindestens 3 Dungeonvarianten, sicherer Rückweg | Nicht begonnen |  |
| PDF 14 | Sternenstaub, Splittererz, Sternenscherben, instabile Essenz | Nicht begonnen |  |
| PDF 15 | FK/FL-HUD, Vorschauen, Tooltips, Tastatur-/Farbblindheitsmodus | Nicht begonnen |  |
| PDF 15 | Sechs optionale Tutorials, Deutsch/Englisch, Modelle/Texturen/Sound/VFX | In Entwicklung | Vier eigene Itemtexturen/-modelle und 51 DE/EN-Keys geprüft und dargestellt; Tutorials, weitere Assets und VFX fehlen  |
| PDF 16 | Serverautoritatives Networking, Ratelimits, Claim-Events, Gamerules | In Entwicklung | Payloads und Hand-/Besitzer-/Revisionsprüfung implementiert; Save/Export gegen FakePlayer getestet; integrierter Client-Nachrichtenweg geprüft; Mehrclient-/Claim-Integration offen  |
| PDF 16 | Persistenz von Spieler/Welt/Blöcken; Migration, Quarantäne, Jobjournal | Nicht begonnen | Formeldateien, Itemkomponenten, FK/FL sowie SavedData für Lichtablauf vorhanden; Crash-/Chunk-/Migrationsabnahme offen |
| PDF 16 | Serverconfigs general/field/ritual/alchemy und Datapack-Reload | Nicht begonnen |  |
| PDF 18 | Survivalpfad T0–T5; 5 UX-Tester; 10-Spieler-/Chunk-/Crash-/Modpacktests | Nicht begonnen | Keine Ingame-Tests behauptet |

## Rezept-Checkliste

Die neutralen Rezeptbeispiele der PDF werden nicht als ladbare Minecraft-Rezepte ausgegeben. Jede Zeile benötigt echte Serializer/Stationsintegration und einen Survivalnachweis. Details und Mengen: [vollständiger PDF-Text](docs/DESIGN_SPECIFICATION.txt).

| Rezept-ID | Status |
|---|---|
| A-01 | Nicht begonnen |
| A-02 | Nicht begonnen |
| A-03 | Nicht begonnen |
| A-04 | Nicht begonnen |
| A-05 | Nicht begonnen |
| A-06 | Nicht begonnen |
| A-07 | Nicht begonnen |
| A-08 | Nicht begonnen |
| A-09 | Nicht begonnen |
| A-10 | Nicht begonnen |
| A-11 | Nicht begonnen |
| A-12 | Nicht begonnen |
| A-13 | Nicht begonnen |
| A-14 | Nicht begonnen |
| A-15 | Nicht begonnen |
| B-00 | Nicht begonnen |
| B-01 | Nicht begonnen |
| B-02 | Nicht begonnen |
| B-03 | Nicht begonnen |
| B-04 | Nicht begonnen |
| B-05 | Nicht begonnen |
| B-06 | Nicht begonnen |
| B-07 | Nicht begonnen |
| B-08 | Nicht begonnen |
| B-09 | Nicht begonnen |
| B-10 | Nicht begonnen |
| E-01 | Nicht begonnen |
| E-02 | Nicht begonnen |
| E-03 | Nicht begonnen |
| E-04 | Nicht begonnen |
| E-05 | Nicht begonnen |
| E-06 | Nicht begonnen |
| EQ-A01 | Nicht begonnen |
| EQ-A02 | Nicht begonnen |
| EQ-A03 | Nicht begonnen |
| EQ-A04 | Nicht begonnen |
| EQ-A05 | Nicht begonnen |
| EQ-A06 | Nicht begonnen |
| EQ-A07 | Nicht begonnen |
| EQ-S01 | Nicht begonnen |
| EQ-S02 | Nicht begonnen |
| EQ-S03 | Nicht begonnen |
| EQ-S04 | Nicht begonnen |
| EQ-W01 | Nicht begonnen |
| EQ-W02 | Nicht begonnen |
| EQ-W03 | Nicht begonnen |
| EQ-W04 | Nicht begonnen |
| EQ-W05 | Nicht begonnen |
| EQ-Z01 | Nicht begonnen |
| EQ-Z02 | Nicht begonnen |
| EQ-Z03 | Nicht begonnen |
| EQ-Z04 | Nicht begonnen |
| EQ-Z05 | Nicht begonnen |
| INF-01 | Nicht begonnen |
| INF-02 | Nicht begonnen |
| INF-03 | Nicht begonnen |
| INF-04 | Nicht begonnen |
| INF-05 | Nicht begonnen |
| INF-06 | Nicht begonnen |
| INF-07 | Nicht begonnen |
| INF-08 | Nicht begonnen |
| INF-09 | Nicht begonnen |
| INF-10 | Nicht begonnen |
| INF-11 | Nicht begonnen |
| INF-12 | Nicht begonnen |
| INF-13 | Nicht begonnen |
| INF-14 | Nicht begonnen |
| INF-15 | Nicht begonnen |
| INF-16 | Nicht begonnen |
| INF-17 | Nicht begonnen |
| INF-18 | Nicht begonnen |
| INF-19 | Nicht begonnen |
| INF-20 | Nicht begonnen |
| R-001 | Nicht begonnen |
| R-002 | Nicht begonnen |
| R-003 | Nicht begonnen |
| R-004 | Nicht begonnen |
| R-005 | Nicht begonnen |
| R-006 | Nicht begonnen |
| R-007 | Nicht begonnen |
| R-008 | Nicht begonnen |
| R-009 | Nicht begonnen |
| R-01 | Nicht begonnen |
| R-010 | Nicht begonnen |
| R-011 | Nicht begonnen |
| R-012 | Nicht begonnen |
| R-013 | Nicht begonnen |
| R-014 | Nicht begonnen |
| R-015 | Nicht begonnen |
| R-016 | Nicht begonnen |
| R-017 | Nicht begonnen |
| R-018 | Nicht begonnen |
| R-019 | Nicht begonnen |
| R-02 | Nicht begonnen |
| R-020 | Nicht begonnen |
| R-021 | Nicht begonnen |
| R-022 | Nicht begonnen |
| R-03 | Nicht begonnen |
| R-030 | Nicht begonnen |
| R-031 | Nicht begonnen |
| R-032 | Nicht begonnen |
| R-033 | Nicht begonnen |
| R-034 | Nicht begonnen |
| R-035 | Nicht begonnen |
| R-036 | Nicht begonnen |
| R-037 | Nicht begonnen |
| R-038 | Nicht begonnen |
| R-039 | Nicht begonnen |
| R-04 | Nicht begonnen |
| R-040 | Nicht begonnen |
| R-041 | Nicht begonnen |
| R-042 | Nicht begonnen |
| R-043 | Nicht begonnen |
| R-044 | Nicht begonnen |
| R-045 | Nicht begonnen |
| R-046 | Nicht begonnen |
| R-047 | Nicht begonnen |
| R-048 | Nicht begonnen |
| R-05 | Nicht begonnen |
| R-06 | Nicht begonnen |
| R-07 | Nicht begonnen |
| RT-01 | Nicht begonnen |
| RT-02 | Nicht begonnen |
| RT-03 | Nicht begonnen |
| RT-04 | Nicht begonnen |
| RT-05 | Nicht begonnen |
| RT-06 | Nicht begonnen |
| RT-07 | Nicht begonnen |
| RT-08 | Nicht begonnen |
| RT-09 | Nicht begonnen |
| RT-10 | Nicht begonnen |
| RT-11 | Nicht begonnen |
| RT-12 | Nicht begonnen |
| RT-13 | Nicht begonnen |
| RT-14 | Nicht begonnen |

Zusätzlich pro Variante: vier Robenteile, vier Runenrüstungsteile und vier Kristallrüstungsteile; Q2/Q3 benötigen getrennte Upgrade-/Montageschritte. R-023–029 sind in der PDF nicht definiert und werden nicht erfunden.

## Runenbibliothek

| PDF-Rune | Stand |
|---|---|
| ZIEL | In Entwicklung: nur Position aus kontrolliertem Kontext |
| POSITION | Getestet im Java-Kern; Weltadapter offen |
| UMGEBUNG | Nicht begonnen |
| IMPULS | Getestet im Java-Kern; Weltadapter offen |
| LICHT | Getestet im Java-Kern; Weltadapter offen |
| WÄRME | Nicht begonnen |
| KÄLTE | Nicht begonnen |
| SCHUB | Nicht begonnen |
| SCHILD | Nicht begonnen |
| HEIL | Nicht begonnen |
| SEHEN | Nicht begonnen |
| PUNKT | Getestet im Java-Kern; Weltadapter offen |
| STRAHL | Nicht begonnen |
| KREIS | Getestet im Java-Kern; Weltadapter offen |
| KUGEL | Nicht begonnen |
| PFAD | Nicht begonnen |
| GLEICH | In Entwicklung: nur Zahlen |
| GRÖSSER | Getestet im Java-Kern; Weltadapter offen |
| UND | Getestet im Java-Kern; Weltadapter offen |
| ODER | Getestet im Java-Kern; Weltadapter offen |
| NICHT | Getestet im Java-Kern; Weltadapter offen |
| FALLS | In Entwicklung: nur Effektauswahl |
| VERZÖGERE | Nicht begonnen |
| INTERVALL | Nicht begonnen |
| BEGRENZE | Nicht begonnen |
| SIGNA | Nicht begonnen |
| BINDE | Nicht begonnen |
| ÜBERTRAGE | Nicht begonnen |
| SIEGEL | Nicht begonnen |
| PRÜFE | Nicht begonnen |
| AKTIVIERE | Getestet im Java-Kern; Weltadapter offen |
| ABBRUCH | Nicht begonnen |
| MESSEN | Nicht begonnen |
| AUFRUF | In Entwicklung: Compiler-Expansion; konstante Bindungen, ein Effektausgang |
| RÜCKGABE | In Entwicklung: Modulausgang, keine eigenständige Runtime-Rune |

Hilfsoperatoren `number` und `boolean` liefern typisierte Konstanten. LICHT besitzt einen Weltadapter für temporäres Punktlicht über NeoForge Auxiliary Light; Erzeugung und Ablauf ohne Blockersatz sind servergetestet. Kreislicht und weitere Varianten bleiben offen. Erweiterungsrune REINIGE: Nicht begonnen.

## Zwölf Referenzzauber (Anhang E)

| Zauber | Status |
|---|---|
| Lichtfunke | In Entwicklung: Graph/Kosten und echter serverseitiger Lichtablauf getestet; Client-Kernablauf getestet; Survival offen |
| Kristallblick | Nicht begonnen |
| Kleiner Schub | Nicht begonnen |
| Ruheglanz | Nicht begonnen |
| Wärmehauch | Nicht begonnen |
| Frosthauch | Nicht begonnen |
| Heilzeichen | Nicht begonnen |
| Kleiner Schild | Nicht begonnen |
| Echo-Ortung | Nicht begonnen |
| Wächterlampe | Nicht begonnen |
| Feldbrücke | Nicht begonnen |
| Ortsreinigung | Nicht begonnen |

## Phasen und Fortsetzung

1. **M0, begonnen:** offizielle 26.2-Abhängigkeiten und JDK 25; Build und GameTestServer erfolgreich; nächster Nachweis ist normaler Mehrclientbetrieb und verpackte JAR-Installation.
2. **M1/M2, begonnen:** Vorhandene Buch-/Handschuh-/Lichtanbindung vervollständigen: zusätzliche Slots, vollständige Buchansichten, Wissen, Rechte, Feldquellen und übrige Runen.
3. **M3:** Bootstrap B-00–B-10, sieben Verarbeitungsstationen, echte Rohstoffe/Rezepte, Lichtpfad ohne Creative.
4. **M4:** Alchemie/Verzauberung, Energiefluss, 15 Automationssysteme, Musteranlagen und RT-01–RT-07.
5. **M5:** Worldgen/Strukturen, 18 Kreaturen, zehn Bosse, Zersplitterung und alle Montageanhänge.
6. **M6:** Assets, Accessibility, DE/EN, Multiplayer/Crash-/Chunk-/Lasttests, Balancing, vollständiger T0–T5-Durchlauf.

Keine Phase gilt als abgeschlossen. Die nächsten konkreten Befehle und offenen Prüfungen stehen in TESTING.md; Architektur und verbleibende Grenzen in ARCHITECTURE.md.

## Aktueller Sitzungsabschluss

Der vorhandene Quellstand und der erneut extrahierte Text der 58-seitigen PDF stimmen überein. Weiterentwickelt wurden vier Q1-Handschuhslots mit Umschalt+Mausrad, unabhängigen Importsnapshots, Altbestandsmigration und serverseitigem Schutz sowie die Buchhistorie mit Wiederherstellung als Entwurf. Erfolgreicher Gradle-Build, 28 Kerntests und neun Bad-Spell-GameTests; beide neuen Bedienabläufe im grafischen Client geprüft. Dies schließt M2 nicht ab. M1/M3–M6 bleiben überwiegend offen, insbesondere alle tatsächlichen Produktionsstationen, Survivalpfade, vollständige Runenbibliothek, Weltinhalte und Bosse.
