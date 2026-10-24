# Forschung und Spielfortschritt

M2 ergänzt den M1-Runenkern um persönliches Wissen. Es gibt keine Magie-XP und keinen Pflicht-Skilltree. Neue Spieler kennen acht Grundrunen für den ersten Lichtzauber und seine Werte. Im Kreativmodus stehen alle Runen zum Bauen zur Verfügung.

## Erste Rune entschlüsseln

1. Ein eigenes Formelbuch einmal öffnen und im Inventar behalten. Es enthält anfangs 64 Schreibpunkte. Eine geladene Feldzelle mitnehmen.
2. G → **Forschung** → **Runen** öffnen. Für den Einstieg **Kreis** wählen.
3. Einen Feldkristallsplitter und ein Papier bereithalten. **Fragment vorbereiten** verbraucht genau diese beiden Gegenstände und legt ein echtes Runenfragment ins Inventar. Ein freier Platz ist nötig.
4. **Analyse öffnen**. Links stehen drei Messhinweise. Rechts jeden Anschluss durch Klicken auf den passenden Typ stellen. Beim Kreis: `position` = Position; `→` = Region. Das Mausrad zeigt weitere Anschlüsse, wenn der Platz nicht reicht.
5. **Prüflauf** drücken. Der Kreis kostet 10 FL und zwei Schreibpunkte. Erfolg verbraucht das Fragment und speichert die Rune dauerhaft.
6. Die Rune ist jetzt im Editor verwendbar. Bereits bekannte Runen erneut zu prüfen, kostet nichts. Ein falscher, vollständig ausgefüllter Prüflauf verbraucht FL/Tinte, aber nicht das Fragment. Fehlende Energie oder ungültige Daten verbrauchen nichts.

Das Fragment gehört zur Welt, nicht zum Finder. Portreihenfolge und Lösung bleiben für Welt und Runentyp stabil. Eine fremde Formel darf lesbar bleiben; unbekannte Runen lassen sich daraus nicht frei speichern oder in den Handschuh exportieren. Eingebettete Module werden ebenfalls geprüft.

## Materialproben

| Familie | Eine Probe pro Fragment | Beispiele |
|---|---|---|
| Geometrie | Feldkristallsplitter | Orte, Feldformen, räumliche Berechnungen |
| Logik | Redstone | Zahlen, Bedingungen, zeitliche Abläufe, Module |
| Thermik | Mondblüte | Licht, Wärme und Kälte |
| Leben | Runenharz | Wesenbezug, Heilung und Schutz |
| Bewegung | Resonanzsplitter | Kräfte, Bewegung und Transport |
| Bindung | Arkanbarren | Signaturen, Bindungen und Geräteursprung |

Die tatsächliche Probe steht bei jeder Rune im Register. Der Katalog umfasst alle vorhandenen Operatoren plus den Modulaufruf. Analysepreise werden aus der echten Anzahl typisierter Eingänge abgeleitet: 5–30 FL und 1–4 Schreibpunkte. Es werden keine Fantasie-Ports für eine zweite Runensprache gepflegt. Beim Modulaufruf wird das Verständnis der Modulhülle gelernt; seine tatsächlichen Ports stammen weiterhin vom eingebetteten Graphen.

## Wissen bewusst weitergeben

Eine bekannte Rune auswählen → **Lehrblatt verfassen**. Das kostet ein Papier, 10 FL und zwei Schreibpunkte. Das Blatt anschließend einem Mitspieler geben. Erst dessen Rechtsklick lehrt die Rune und verbraucht das Blatt. Aufheben, danebenstehen oder dieselbe Gruppe zu haben, teilt kein Wissen. Kennt der Empfänger die Rune bereits, bleibt das Blatt erhalten. Ein Blatt aus einer anderen Welt gilt nicht als gültiger Nachweis.

## Nachweise statt Level

- **T0:** Einstieg und Grundlagen; kein XP-Balken.
- **T1:** Eine gültige eigene Formel gespeichert sowie Arkanwerkbank und Kristallschleifer selbst platziert oder als Eigentümer benutzt.
- **T2:** Fünf unterschiedliche Runen entschlüsselt, Arkanofen nachgewiesen und einen veredelten Kristallkern aufgenommen.
- **T3–T5:** Bleiben offen, bis die zugehörigen Dungeons, Rituale, Bosskerne und Dimensionen implementiert sind. Keine künstlichen Ersatzabschlüsse.

Diese Stufen erklären die nächsten sinnvollen Aufgaben. Sie sperren nicht pauschal alle Geräte hinter Levels. Runenwissen begrenzt tatsächlich das freie Formelbauen; Werkzeug- und Materialketten bleiben echte Produktionsanforderungen. Bestehende M1-Qualitätswerkzeuge werden nicht nachträglich entwertet.

## Handwerkswege

Im Forschungsregister **Handwerkswege** öffnen. Alle 68 vorhandenen Rezeptdateien werden in dieselbe Ansicht übernommen, einschließlich Vanilla-Werkbank/Ofen und Nebenprodukten. Ein Rezept erscheint, sobald eine seiner Komponenten aufgenommen wurde. Noch unbekannte Materialien erscheinen als Kategorien statt verratener Namen.

Suche nach bekannten Ergebnissen/Materialien; filtere Station, empfohlene T-Stufe und Dauer. **Vorrat: ja** prüft aktuell Zutaten und mitgeführte FL. Eine passende zugängliche Station, deren Versorgung und freie Ausgabeslots sind weiterhin erforderlich – die Anzeige reserviert keine Weltinventare. Rechts steht der Abhängigkeitsbaum mit echten Mengen, FL und Batchdauer. Bei Alternativen wechselt **Route** den dargestellten Beschaffungsweg. Mausrad links bewegt die Rezeptliste, rechts den Baum.

Die Ansicht kennt die mitgelieferten Bad-Spell-Rezepte. Andere Mods, Tags mit frei austauschbaren Alternativen und später hinzugefügte Datapack-Rezepte sind nicht automatisch Teil dieser generierten Ansicht. Die eigentliche Herstellung verwendet weiterhin die serverseitigen Rezepte.

## Bestehende Welten und Multiplayer

Beim ersten Laden eines alten Spielerprofils werden gültige eigene Formeln aus privater Bibliothek, Inventar und allen Handschuh-Slots einmalig als bekannt übernommen. Danach schaltet das bloße Aufnehmen eines Formelbuchs oder Handschuhs nichts frei. Tod, Abmelden und Dimensionswechsel setzen Forschung nicht zurück. Die Weltdateien sind wie bisher vor Updates zu sichern.

Client und Server benötigen dieselbe JAR: **Netzwerkprotokoll 5**. Für Einzelspieler ist kein separater Server erforderlich.

Neue Struktur-Fundstücke, NPC-Unterricht und Einmal-Schriftrollen folgen mit den jeweiligen Welt-/NPC-Systemen; sie werden durch M2 nicht vorgetäuscht.
