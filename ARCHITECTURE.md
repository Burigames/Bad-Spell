# Architektur

## Plattform und Trennung

Minecraft Java 26.2, NeoForge 26.2.0.93, Java 25, ModDevGradle 2.0.148 und Gradle 9.2.1. Gemeinsamer Einstieg: `com.badspell.BadSpell`. Clientklassen liegen ausschließlich in `com.badspell.client`; ihr Einstieg wird nur auf dem Client geladen. Die Akzeptanzumgebung unter `src/acceptance` gehört nicht zur veröffentlichten JAR.

## Formelkern

`core` ist eine Minecraft-unabhängige Java-Bibliothek und wird in die Mod-JAR eingebettet. `Formula`, `FormulaCodec`, `FormulaSnapshot`, `RuneRegistry`, `FormulaValidator` und `FormulaEngine` bilden Graph, Datentypen, Snapshotformat, Registrierung, Prüfung und Auswertung ab.

Werte sind Zahlen, Wahr/Falsch, Positionen, Vektoren, Wesen, Texte, Bereiche, Ereignisse und Effektpläne. Pflichtports und Parametergrenzen stammen aus dem tatsächlichen Register. Bedingungen werten nur den gewählten Zweig aus. Module enthalten unveränderliche eigene Unterformeln mit benannten Eingaben und typisierten Rückgaben. Grenzen: 128 expandierte Knoten, 512 Auswertungsoperationen, Modultiefe acht und 30 KB Netzwerk-Snapshot.

`ActivationService` prüft Zielreichweite, Stabilität, Budget und Energie, bereitet die Weltaktion vor und führt sie erst anschließend aus. `EnergyAccount` hält FL/FK-Buchung und Ablehnung konsistent. Bündel enthalten höchstens acht geeignete flache Effekte; alle Teilaktionen werden vor der ersten Mutation vorbereitet. Transfers und Bindungen sind nicht bündelbar.

## Weltadapter und Geräte

`SpellEffects` ist der gemeinsame Serveradapter für Licht, Wärme, Kälte, Schild, Heilung, Stoß, Zielinformation, Bindungen, Transporte und Siegel. `SpellTargets` ermittelt Wesen und freie Zielpunkte; ein Raycast-MISS wird nicht als getroffener Block behandelt. Ungültige Ziele verursachen keinen Energieabzug. Spieleraktionen werden auf dem Server erneut geprüft.

Ein Runenhalter verwendet `DeviceCasters`: einen nicht gespawnten, separat verwalteten NeoForge-FakePlayer mit Besitzer-UUID am Gerät. Weder echte Spieler noch der globale FakePlayer-Cache werden dafür bewegt. Reichweite, Rechte und Effekte werden vom Gerät aus geprüft. Geräte zahlen FL, keinen Spieler-FK, und haben einen Positionsursprung statt eines künstlichen lebenden Selbst.

Geräteaufträge speichern Snapshot, Start-/Folgezeit, Intervall und verbleibende Durchläufe im Block-NBT. Energiemangel pausiert, ohne einen Durchlauf abzuziehen. Entfernen/Ändern der Platte beendet den Auftrag. Start/Stop-Menüabsichten werden auf Besitzer und Reichweite geprüft. Redstone verwendet eine steigende Flanke.

`LightFields`, `SpellFields`, `RuneBoards` und `WorldBindings` persistieren Weltdaten. Gerätefelder speichern zusätzlich ihren Anker, damit endliche Wirkungen auch ohne eingeloggten Besitzer weiterlaufen. Ablauf richtet sich nach gespeicherter Weltzeit. Ungeladene Chunks werden durch Spielmechaniken nicht erzwungen. Schild-/Wärmerechte vergleichen Besitzer-UUIDs und keine FakePlayer-Objektidentitäten.

## Herstellung und Leitung

`StationBlockEntity`, `StationMenu` und `ProcessingRecipe` bilden tatsächliche Inventare, Zellladung, Adapter, Tickfortschritt und transaktionale Ausgaben ab. 55 Verarbeitungsrezepte und 13 Vanilla-Rezepte sind datengetrieben. Zutatenzuordnung unterstützt überlappende Ingredients über begrenzten Maximalfluss.

Passende Rezepte werden nach Spezifität, anschließend deterministisch nach ID gewählt. Eingangsänderungen verwerfen die zuvor gewählte Rezeptur; aufeinanderfolgende Mondessenz-/Mondsalz-Verarbeitung darf nicht am alten Cache hängenbleiben. Eingangs-Snapshots sind nur Laufzeitcache; persistierter Arbeitsfortschritt bleibt beim ersten Neuaufbau des Caches erhalten.

`com.badspell.conduction` enthält den passiven Feldkristall und `CrystalConduction`. Eine begrenzte Rückwärtssuche folgt ausgerichteten Kristallen zu eigenen versorgten Stationen: maximal 128 besuchte Knoten, höchstens 16 Kristalle je Pfad, 80 FL/s je Kristall. Nur Quell- und Zielzellen speichern Energie; Übertragung zieht exakt die gutgeschriebene Menge ab. Keine Funkverbindungen, Kollektoren oder zusätzlichen Speicher werden dadurch vorgetäuscht.

## Oberfläche und Ressourcen

Editor, Runenlexikon, Bibliothek, Historie und Kodex teilen Typfarben und Pergamentgestaltung. Die Auswahl kann über Name, Beschreibung, Port und Typ suchen. Eine offene Eingangszeile beschränkt den Ausgangsfilter und erstellt die Verbindung automatisch. Neue Knoten werden frei platziert und sichtbar zentriert.

Stations-Slotindizes bleiben im Netzwerk stabil, während die Arbeitsbereiche nach Stationsart angeordnet werden. Ein gemeinsamer Rezeptführer wird aus den echten Verarbeitungsdateien generiert. Öffentliche Oberflächenpakete enthalten nur Glyphen, nicht private Graphen. Das Monokel erhält serverseitig freigegebene Diagnosedaten.

3D-Itemmodelle verwenden einen gemeinsamen Blocktextur-Atlas; gemischte Atlasreferenzen sind in Minecraft 26.2 nicht zulässig und werden bereits beim Build zurückgewiesen. Kollisionsformen werden aus denselben Stationsquadern erzeugt, nach Ausrichtung gedreht und zwischengespeichert.

## Erweiterung

Neue Operatoren benötigen echte Registerverträge, reine Auswertung, Lokalisierung, Dokumentation/Glyphenzuordnung und Tests. Neue Wirkungen benötigen zusätzlich einen geprüften Weltadapter; bestehende Glyphencodes werden nicht umnummeriert. Größere Netze, Forschung, Weltinhalte und Kreaturen sind nachfolgende Meilensteine, kein Bestandteil der M1-Abnahme.
