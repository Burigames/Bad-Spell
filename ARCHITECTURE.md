# Architektur

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


## Abhängigkeitsrichtung

`src/main/java/com/badspell` ist der gemeinsame NeoForge-Einstieg. Er erzeugt Registry und Aktivierungsdienst ohne Clientklassen. Registrierung, Netzwerk, Itemkomponenten und temporäres Licht sind implementiert. Der Mod-Stand wurde gebaut und im Minecraft-GameTestServer gestartet; der grafische Client wurde anschließend mit integriertem Server bedient. Echter Mehrclientbetrieb ist noch nicht geprüft.

`core` ist Java 25 ohne externe Abhängigkeiten. Der Mod-Build bindet seine Klassen in dieselbe Mod-JAR ein. Der Core referenziert weder Minecraft noch NeoForge noch Client-APIs.

```
NeoForge common / spätere Client- und Netzwerkadapter
                   ↓
            ActivationService
              ↙          ↘
     FormulaEngine     EnergyAccount
          ↓
 FormulaValidator ← RuneRegistry
          ↑
 FormulaModules → Formula / Snapshot / Codec / Store

spätere Stationsadapter → Recipe / ProductionPlanner / MachineInventory
```

## Formeln

`Formula` kopiert Knoten-, Kanten- und Parametercontainer unveränderlich. `Value` ist ein geschlossenes Typsystem. Registrierte serverseitige Operationen statt freiem Skriptcode erzeugen Werte oder Effektpläne. `FormulaValidator` prüft Limits, IDs, Ports, Parameter, Typen, Zyklen und unverbundene Knoten. `FormulaEngine` memoisiert Knotenwerte und wertet bei FALLS nur den gewählten Effektzweig aus. Ein Plan verändert niemals eine Welt.

`FormulaModules` expandiert explizit bereitgestellte Moduldefinitionen, bindet Konstanten und kontrolliert Rekursion, maximal acht Ebenen sowie 128 expandierte Knoten. Aktuell ein Effektausgang, keine eingehenden dynamischen Modulports. Erst das expandierte Ergebnis bildet den zu aktivierenden vollständigen Snapshot. Externe Call-Mappings gehören noch nicht zum persistenten Codec. Der Serveradapter muss die Kopierrechte aller Module vor Bereitstellung der Bibliothek prüfen.

`FormulaCodec` hat ein binäres Schema 1, ein Megabyte Payloadlimit und Grenzen für alle Collections. Keine Java-Objektdeserialisierung. `FormulaSnapshot` erzeugt eine kanonische SHA-256-Prüfsumme; diese ist keine Signatur und kein Berechtigungsnachweis. `FormulaStore` speichert UUID-Dateinamen über atomaren Austausch und schützt Revision/Besitzer beim Schreiben. Es gibt noch keine Versionshistorie, Quarantäne, Migration, fsync-Dauerhaftigkeitsgarantie oder Minecraft-SavedData-Anbindung.

## Aktivierung und Sicherheit

`ActivationService` verlangt einen expliziten AKTIVIERE-Ausgang, ST ≥25, dieselbe Dimension, höchstens 12 Blöcke Abstand zum Lichtziel, ausreichendes Formelbudget sowie FL und FK. ST 25–49 erhöht FL um aufgerundete 25%. `EffectHost.prepare` ist eine Vertrauensgrenze: der spätere Serveradapter muss Besitz, Claims, komplette Wirkfläche, geladene Chunks, Gamerules, Zielzustand und Cooldowns prüfen. Er darf niemals beliebige Client-Kosten übernehmen.

Der vorbereitete Runnable muss eine atomare Weltaktion darstellen: Ein Fehler darf nur **vor** einer sichtbaren Weltänderung geworfen werden. Der Konto-Rollback kann keine bereits erfolgte Minecraft-Weltänderung rückgängig machen. Die Kerntests verwenden kontrollierte In-Memory-Aktionen. `MagicActions` bindet Punktlicht an `LightFields` an: geladener Chunk, Luftblock, Weltgrenze und Vanilla-Interaktionsrecht werden vor dem Verbrauch geprüft. Die GameTests prüfen Aktivierung und echten Lichtablauf. Dies ist keine allgemeine Transaktionsgarantie für zukünftige Block-/Entitätsänderungen; Claim-Mod-Adapter fehlen.

## Energie und Herstellung

`EnergyAccount` hält ganzzahlige FL und FK mit Kapazitätsgrenzen und atomarer Doppelabbuchung. Er ist weder ein Kabelnetz noch ein spielbarer Feldspeicher. Der Minecraft-Adapter hält 100 FL in der Startzellen-Itemkomponente und FK im persistenten Spieler-Tag. FK regeneriert serverseitig in 3er-Schritten pro Sekunde nach mindestens 2 s Ruhe. Noch fehlen Feldnetze, Strompfade, Raten, Verluste und persistente Reservierungen.

`Recipe` hält eine konkret aufgelöste Zutatenalternative, Station, Ausgaben, Dauer und FL. `ProductionPlanner` berechnet transitive Chargen, Rohstoffe, Restbestände und Gesamtkosten; fehlende Rohstoffklassifikationen werden als Fehler behandelt. Zyklen werden auf dem tatsächlich benötigten Produktionspfad erkannt. Ein allgemeiner ökonomischer Beweis gegen alle verlustfreien Umwandlungskreise ist nicht implementiert.

`MachineInventory.complete` prüft Zutaten, Station und vollständige Ausgabekapazität vor dem Commit und verhindert Wiederholung derselben Job-UUID innerhalb derselben Instanz. Noch keine laufenden Prozesse, Flüssigkeiten, Katalysatorverschleiß, Slotregeln oder Persistenz. Der zukünftige Stationsadapter darf `complete` erst nach gültigem Prozess-Timer aufrufen. Der Satz abgeschlossener IDs muss für dauerhafte Servernutzung durch ein begrenztes persistentes Journal ersetzt werden.

## Noch anzubindende Module

Noch offen: Feldnetze, Maschinen-BlockEntities/Menüs, Spielerwissen, weitere Payloads, Alchemie, Rituale, Entitäten/KI, Weltgenerierung und Claim-Integration. Vier Itemtypen, ihre Komponenten und Buch-Payloads existieren bereits.

`com.badspell.client.BadSpellClient` ist ein ausschließlich für `Dist.CLIENT` registrierter Einstiegspunkt. Clientbound-Handler werden über das in 26.2 vorgesehene Client-Registrierungsereignis angebunden. Gemeinsame Klassen laden keine Clientklassen; der echte Server-Testlauf bestätigt den Serverstart. `FormulaBookScreen` rendert über die 26.2-API `GuiGraphicsExtractor`, benutzt `MouseButtonEvent` und `KeyEvent` und bietet einen ersten Canvas. Der grafische Client wurde auf Xvfb mit integriertem Server gestartet; Canvas, Editieren, Export, Wirken und reguläres Weltneuladen wurden bedient.

## Buch- und Lichtadapter

`MagicActions` erzeugt Buch-IDs ausschließlich auf dem Server. Speichern prüft gehaltenes Item, Besitzer, Formular-ID, Revision, Graph und Schreibpunkte. Die vollständige Formel ist auf 30.000 Netzwerkbytes begrenzt. Speicherrate: 10/1200 Ticks; Buchaktionsrate: 20/20 Ticks. Die letzten maximal 20 Vorgängerrevisionen liegen in der Buchkomponente; `FormulaHistoryScreen` zeigt ein paginiertes Archiv. Öffnen setzt einen gespeicherten Editorstand voraus; Wiederherstellen lädt einen Entwurf, dessen Speichern normal Tinte kostet und die Revision erhöht. History-Antworten sind auf 20 × 30.000 Bytes plus aktuellen Snapshot begrenzt; Anfragen auf eine pro 20 Ticks. Der Export übernimmt ausschließlich den gespeicherten Stand, unverändert in den ausgewählten von vier Q1-Handschuhslots. `GloveFormulas` prüft Besitzer aller belegten Slots, Graph und Prüfsumme. Erfolgreiche Schreiboperationen migrieren alte Ein-Slot-Daten verlustfrei in Slot 0. Der aktive Snapshot wird für bestehende Tooltips in die bisherigen Komponentenfelder gespiegelt. Leere Slots entfernen diesen Spiegel. `GloveSlot` überträgt nur Hand und Richtung ±1; Itemtyp, Besitzer und maximal ein Wechsel pro Tick werden serverseitig geprüft. Umschalt+Mausrad wird ausschließlich im Client verarbeitet; Inventarsynchronisierung und Speicherung nutzen Vanilla-Itemkomponenten. Ein späteres Buchupdate verändert diese Kopie nicht. Die GameTests prüfen diesen Ablauf mit NeoForge-FakePlayern; zusätzlich wurde der serialisierte Nachrichtenweg im echten Client mit integriertem Server bedient. Ein externer Mehrclient-Test bleibt offen.

`LightFields` nutzt die tatsächlich vorhandene NeoForge-26.2-Auxiliary-Light-API, anstatt Lichtblöcke zu platzieren. Der Ablauf liegt als SavedData pro Dimension vor; beim Ablauf wird Licht in geladenen Chunks entfernt. Entladene Chunks werden nicht erzwungen geladen, sondern nach dem Wiederladen bereinigt. Es gibt höchstens 1024 eingetragene Felder pro Dimension; belegte Auxiliary-Light-Positionen werden abgewiesen. Das verhindert bewusst Überschreiben vorhandener Quellen, ist aber noch keine allgemeine Zusammenarbeit mit allen Lichtmods.

Die SavedData-/Chunk-Auxiliary-Daten sind noch nicht gemeinsam crashgetestet. Ein Absturz zwischen getrennten Chunk-/SavedData-Schreibvorgängen sowie Chunk-Unload-/Reload und mehrere konkurrierende Lichtmods benötigen eigene Tests und gegebenenfalls ein Chunk-Journal. Keine Crash-Konsistenz-Zusage für diesen Adapter.

## Korrigierte Feldbeleuchtung

`FieldLightSources` ist ein abgeleiteter, threadsicherer Quellenindex pro Welt und Chunk. `LightFields` initialisiert ihn aus SavedData, aktualisiert ihn vor Licht-Neuberechnungen und synchronisiert beim Chunkversand die aktiven Quellen an den Client. Clientseitig werden entladene Chunkindizes entfernt. `BlockLightEngineMixin` ergänzt die Feldemission mit `max(Vanillaemission, Feldemission)` und berücksichtigt Feldquellen bei der Chunk-Lichtinitialisierung. `LevelLightEngineMixin` verhindert, dass eine vollständig aus Luft bestehende Section mit aktiver Feldquelle aus der Lichtberechnung entfernt wird. Beim Erlöschen wird der tatsächliche Sectionzustand wiederhergestellt. Beide Mixins sind explizit in den Modmetadaten registriert.

Die bisherigen Auxiliary-Light-Daten alleine bewirkten bei Luft keine reale Beleuchtung; die vorherigen Assertions prüften nur den gespeicherten Wert. Neue Serverprüfungen lesen tatsächliches Blocklicht einschließlich Nachbarpositionen und Ablauf. Zusätzliche Partikel sind nur sichtbares Feedback und keine Ersatzprüfung für Licht.

## Produktionssystem

`production.ProcessingRecipe` ist ein datengetriebener Rezepttyp mit MC-26.2-`ItemStackTemplate`-Ausgaben. Zutaten werden per beschränktem Max-Flow auf sechs Slots verteilt; überlappende Tags benötigen keine greedy Sonderfälle. `StationBlockEntity` verarbeitet auf dem Server alle fünf Ticks, speichert Inventar/Eigentümer/Fortschritt/Rezeptfingerabdruck, simuliert zunächst die Ausgabebelegung und entnimmt Zutaten/FL erst beim Commit. Änderungen an Rezepten setzen ihren Fortschritt zurück. `StationMenu` synchronisiert Inventar, Zeit, FL und Diagnose; `StationScreen` liegt ausschließlich im Client.

Gravuren speichern FormulaCodec-Daten mit SHA-256-Snapshot. Der Runenhalter verwendet dieselbe FormulaEngine/ActivationService wie der Handschuh, aktuell ausschließlich für Punktlicht. Seine Redstone-Flanke wird persistent gespeichert. Kein Kabelnetz und keine Energiefelder für andere Effekte sind daraus abzuleiten.
