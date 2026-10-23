# Bad Spell – Anleitung für die vier vorhandenen Items

**Gültig für:** Bad Spell 0.1.0-alpha, Minecraft Java 26.2, NeoForge 26.2.0.93, Java 25.

Dieser Build enthält die vier ursprünglichen Werkzeuge plus 37 Material-/Plattenitems und 17 Blöcke einschließlich sieben Stationen. Du kannst eine Punktlichtformel bearbeiten, speichern, in einen Handschuh kopieren, gravieren und wirken. Die erste Herstellungskette ist implementiert; weitere Zauberwirkungen, Mobs und Dimensionen fehlen. Siehe [Herstellung](HERSTELLUNG.md).

## Korrektur für den ersten veröffentlichten Build

Wenn FL/FK sinkt, aber kein Licht erscheint, kann noch die erste fehlerhafte JAR installiert sein. Dieser Fehler wurde nachgewiesen: Der gespeicherte Lichteintrag erzeugte allein keine Emission in Luft. Die aktuelle JAR im Repository enthält die Korrektur und einen kurzen Partikeleffekt. Ersetze die alte Datei im Mods-Ordner und starte das Spiel bzw. den Server neu; auf einem Server auch die Clients aktualisieren. Nur eine Bad-Spell-JAR gleichzeitig installieren. Bereits exportierte Formeln bleiben gültig. Andere Effekte als Punktlicht werden durch diesen Hotfix nicht ergänzt.

## Items erhalten

Öffne eine Creative-Welt und nimm im Creative-Tab **Bad Spell** je ein Formelbuch, einen Runenhandschuh und eine Start-Feldzelle sowie einige Runentintenflakons.

Alternativ mit Befehlsberechtigung:

```mcfunction
/give @s badspell:formula_book
/give @s badspell:rune_glove
/give @s badspell:field_cell
/give @s badspell:rune_ink 4
```

Verwende für den Einstieg nur einen Handschuh und eine Feldzelle im Inventar. Bei mehreren Exemplaren verwendet der Buch-Export den zuerst gefundenen Handschuh; beim Wirken wird die zuerst gefundene Feldzelle verwendet. Restladungen mehrerer Zellen werden nicht addiert.

## Schnellstart: das erste Licht

1. Behalte Handschuh und Feldzelle im Inventar.
2. Nimm das **Formelbuch** in die Hand und rechtsklicke. Beim ersten Öffnen erhältst du automatisch die Vorlage **Lichtfunke** mit 64 Schreibpunkten.
3. Lasse die Vorlage zunächst unverändert. Klicke **An Handschuh / To glove**. Für die unveränderte Startvorlage ist vorheriges Speichern nicht nötig.
4. Schließe den Editor mit **Esc** und nimm den **Runenhandschuh** in die Hand.
5. Ziele aus kurzer Entfernung auf die Oberfläche eines Blocks, sodass direkt davor ein freier Luftblock liegt. Rechtsklicke mit dem Handschuh.
6. Bei Erfolg erscheint **Aktiviert / Activated** und die verbleibende Energie. Eine frische Zelle hat anschließend **70 FL**, dein Fokus **95 FK**.

Lichtfunke erzeugt ein schwaches temporäres Licht der Stärke 3 für **600 Spielticks**, bei normalen 20 TPS ungefähr **30 Sekunden**. Dabei wird kein Block platziert. In einer dunklen Umgebung ist das Licht besser erkennbar als am Tag. Die Reichweite beträgt höchstens **12 Blöcke**.

## 1. Formelbuch – Formeln bearbeiten und exportieren

**Item-ID:** `badspell:formula_book`

Rechtsklick öffnet den visuellen Editor. Ein erstmals geöffnetes Buch und seine Formel werden dem öffnenden Spieler zugeordnet. Ein anderer Spieler kann dieses private Buch aktuell nicht bearbeiten oder exportieren.

### Bedienung

| Aktion | Bedienung |
| --- | --- |
| Rune auswählen | Knoten mit der linken Maustaste anklicken |
| Rune verschieben | Knoten mit der linken Maustaste ziehen |
| Rune hinzufügen | Links auf einen Eintrag mit `+` klicken |
| Ansicht vergrößern/verkleinern | Mausrad über der Arbeitsfläche |
| Arbeitsfläche verschieben | Mittlere Maustaste gedrückt halten und ziehen |
| Runenpalette scrollen | Mausrad über der linken Runenliste |
| Runen verbinden | Ausgang rechts oben am Quellknoten anklicken, dann den passenden Eingang links am Zielknoten |
| Zahl ändern | Knoten auswählen, rechts beim Parameter `−` oder `+` klicken |
| Booleschen Wert ändern | Knoten auswählen und den Parameter mit `−` oder `+` umschalten |
| Ausgewählte Rune löschen | **Löschen / Delete** oder Entf-Taste |
| Effektausgang bestimmen | Passenden Knoten auswählen, **Ausgang / Output** klicken |
| Anordnung vereinfachen | **Anordnen / Layout** |
| Änderung rückgängig machen | **Zurück / Undo** oder Strg+Z |
| Änderung wiederholen | **Wiederholen / Redo** oder Strg+Y |
| Graph kontrollieren | **Prüfen / Validate** |

Der Zoom reicht von 25 bis 400 Prozent. Bei sehr kleiner Darstellung zuerst näher heranzoomen, um Ports treffen zu können. Viele Runenbezeichner erscheinen aktuell als englische technische Namen.

### Die Startformel verstehen

Die Vorlage besteht aus fünf Knoten und diesen Verbindungen:

| Quelle | Ziel | Eingangsport am Ziel |
| --- | --- | --- |
| `impulse` | `target_position` | `trigger` |
| `target_position` | `point` | `position` |
| `point` | `light` | `region` |
| `light` | `activate` | `effect` |
| `impulse` | `activate` | `trigger` |

`activate` ist der Formelausgang. Der `light`-Knoten besitzt die Parameter `strength = 3` und `duration_ticks = 600`.

Für einen ersten eigenen Versuch:

1. Wähle den `light`-Knoten.
2. Erhöhe `strength` von 3 auf 4.
3. Klicke **Prüfen**, danach **Speichern / Save**.
4. Klicke anschließend **An Handschuh**.
5. Wirke die aktualisierte Formel mit dem Handschuh.

Diese Variante kostet **40 FL und 5 FK** und bleibt ebenfalls 600 Ticks aktiv. Ändere möglichst nur einen Parameter auf einmal.

### Speichern und Historie

**Speichern** erzeugt eine neue Revision. Geänderte Knoten und Verbindungen benötigen Schreibpunkte, maximal acht pro Speicherung; auch verschobene Knoten zählen als Änderungen. Es sind höchstens zehn Speicheraktionen pro 1.200 Spielticks erlaubt, bei normalen TPS ungefähr pro Minute.

**Historie / History** zeigt bis zu 20 gespeicherte Vorgängerrevisionen. Speichere offene Änderungen vor dem Öffnen. Eine ausgewählte frühere Revision wird als **Entwurf** im Editor geladen. Erst **Speichern** übernimmt diesen Entwurf, verbraucht die üblichen Schreibpunkte und erzeugt eine neue Revision. Die Versionsnummer wird nicht zurückgedreht.

### Export

**An Handschuh** kopiert ausschließlich die zuletzt gespeicherte Formel in den aktuell ausgewählten Handschuhslot. Ungespeicherte Änderungen werden nicht exportiert.

Die Kopie bleibt unabhängig vom Buch: Wenn du das Buch später änderst, musst du erneut speichern und exportieren. Der Export ersetzt den bisherigen Inhalt des ausgewählten Slots; ein belegter Slot wird nicht automatisch übersprungen.

## 2. Runenhandschuh – Slot wählen und zaubern

**Item-ID:** `badspell:rune_glove`

Der Handschuh besitzt **vier Formelslots**. Ein frischer Handschuh ist leer und kann erst nach einem Export aus dem Buch zaubern.

- **Umschalt + Mausrad nach unten:** nächster Slot.
- **Umschalt + Mausrad nach oben:** vorheriger Slot.
- Nach Slot 4 folgt wieder Slot 1; rückwärts entsprechend umgekehrt.
- **Rechtsklick:** ausgewählte Formel wirken.

Halte den Handschuh zum Wechseln in der Hand. Der Wechsel unterstützt auch die Nebenhand; wenn beide Hände einen Handschuh halten, hat die Haupthand Vorrang. Die Einblendung zeigt Slotnummer und Formelname oder einen leeren Slot.

Um einen zweiten Slot zu belegen:

1. Halte den Handschuh und wähle mit Umschalt+Mausrad Slot 2.
2. Nimm das Formelbuch in die Hand; der Handschuh bleibt im Inventar.
3. Speichere die gewünschte Formel und klicke **An Handschuh**.
4. Nimm den Handschuh wieder in die Hand. Slot 2 ist weiterhin ausgewählt.

Ein leerer Slot wirkt keinen Ersatzzauber. Die Auswahl verbraucht keine Energie. Fremde private Formeln können nicht einfach von einem anderen Spieler genutzt werden.

Aktuell unterstützt die Spielanbindung ausschließlich **Punktlicht**, auch wenn der Editor weitere logische Bausteine anbietet. Ein syntaktisch gültiger Graph mit einer Kreisregion wird beim Wirken derzeit abgewiesen.

## 3. Start-Feldzelle – Energie für Zauber

**Item-ID:** `badspell:field_cell`

Eine frische Zelle enthält **100 FL (Feldladung)**. Sie muss beim Zaubern im Inventar liegen und wird automatisch verwendet. Du musst sie nicht in der Nebenhand halten oder rechtsklicken. Der Tooltip zeigt die Restladung.

Die unveränderte Startformel kostet pro erfolgreicher Anwendung 30 FL. Eine frische Zelle reicht deshalb für **drei Anwendungen**, danach bleiben 10 FL übrig.

Zusätzlich benötigt der Zauber persönlichen **Fokus (FK)**:

- Maximal 100 FK.
- Die Startformel verbraucht 5 FK.
- Nach mehr als zwei Sekunden ohne erfolgreichen Zauber beginnt die Erholung mit 3 FK pro Sekunde, bezogen auf Spielticks.

**FL lädt sich nicht von selbst auf.** Im aktuellen Build gibt es noch keine Aufladestation und kein Nachladerezept. Für weitere Tests musst du eine frische Zelle aus Creative oder per Befehl nehmen. Entferne die leere Zelle vorher aus deinem Inventar, damit sie nicht weiterhin zuerst ausgewählt wird.

## 4. Runentinte – Schreibpunkte nachfüllen

**Item-ID:** `badspell:rune_ink`

Runentinte ist ein Nachfüllitem für das Formelbuch. Ein neues Buch startet bereits mit **64 Schreibpunkten**.

Zum Nachfüllen:

1. Lege mindestens einen Runentintenflakon ins Inventar.
2. Öffne dein Formelbuch.
3. Klicke **Tinte füllen / Refill ink**.

Ein Flakon wird verbraucht und der Buchvorrat auf **64 Punkte** gesetzt. Bei teilweise gefülltem Buch gehen überschüssige Nachfüllpunkte verloren. Ist das Buch bereits voll, wird kein Flakon verbraucht. Rechtsklick mit dem Flakon allein hat keine Nachfüllfunktion.

## Wenn etwas nicht funktioniert

| Meldung oder Beobachtung | Was du tun kannst |
| --- | --- |
| **Leerer Formelslot** | Im Buch in genau diesen Slot exportieren oder zum belegten Slot wechseln. |
| **Eine Start-Feldzelle wird benötigt** | Eine Feldzelle ins Inventar legen. |
| **Zu wenig FL oder FK** | Zellladung prüfen; eine erschöpfte Zelle ersetzen oder auf FK-Erholung warten. |
| **Ziel gesperrt oder nicht unterstützt** | Näher an einen freien Luftblock vor einer Blockoberfläche zielen. Aktive Lichtpositionen nicht überlagern. Nur Punktlicht verwenden. |
| **Ziel außer Reichweite** | Näher herangehen; höchstens zwölf Blöcke Abstand. |
| **Formelbudget überschritten** | Stärke oder Dauer reduzieren. Die Startvorlage besitzt ein Budget von 100 FL. |
| **Unvereinbare Porttypen** | Nur typgleiche Aus- und Eingänge verbinden; zum Einstieg die Startverbindungen verwenden. |
| **Formeldaten sind ungültig** | **Prüfen** benutzen; fehlende Verbindungen, unverbundene Knoten und ungültige Ausgänge korrigieren. |
| **Buch oder Revision geändert** | Buch schließen und erneut öffnen. |
| **Diese private Formel gehört einem anderen Spieler** | Dein eigenes Buch und deinen eigenen Handschuh verwenden. |
| **Tinte fehlt oder das Buch ist bereits voll** | Runentinte ins Inventar legen und den angezeigten Buchvorrat kontrollieren. |
| **Zu viele Anfragen** | Warten; nicht wiederholt schnell speichern oder klicken. |
| Änderungen am Buch verändern den Zauber nicht | Nach dem Speichern erneut **An Handschuh** klicken und den richtigen Slot wählen. |
| Licht ist kaum sichtbar | In einer dunklen Umgebung testen. Stärke 3 ist absichtlich schwach. |

## Grenzen und Teststand

Im Entwicklungsclient geprüft wurden Buchbedienung, Lichtwirkung und Kosten, Export, Revisionswiederherstellung sowie Slotwechsel. Automatisierte Serverprüfungen decken unter anderem unabhängige Slots, Besitzerrechte, Migration alter Handschuhdaten und Wiederherstellung ab.

Die Anleitung beschreibt den implementierten Stand, keine vollständige Umsetzung des Designs. Insbesondere fehlen Survivalherstellung, weitere Effekte, Q2/Q3-Handschuhupgrades und ein vollständiges Energie- oder Maschinensystem. Die gepackte Hotfix-JAR wurde zusätzlich in einem separaten Mods-Ordner im Client geprüft. Ein Mehrclient-Test auf einem normalen dedizierten Server sowie Tests mit einem Endnutzer-Launcher und fremden Shadern stehen noch aus.

Weitere Einzelheiten: [Implementierungsstatus](../IMPLEMENTATION_STATUS.md) und [Testnachweise](../TESTING.md).


## Neue Oberfläche und Herstellung

Die Formelbuch-Seitenleiste hat jetzt die Reiter **Runen**, **Details** und **Prüfen**. Unter Runen suchen und platzieren; Knoten wählen und unter Details Zahlen direkt eingeben. Unter Prüfen erscheinen verständliche Diagnosen und Kosten. Rechtsklick auf einen Eingangsport löst dessen Verbindung. Speichern erhält den Bildausschnitt; vor Export speichern.

Für alle neuen Materialien, sieben Maschinen, Startenergie und Runenplatten: [Herstellungsanleitung](HERSTELLUNG.md). Nicht jede registrierte Zutat hat bereits ihre spätere PDF-Endverwendung.
