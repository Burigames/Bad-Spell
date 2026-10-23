# Herstellung und Stationen

Zielversion: Minecraft Java 26.2 / NeoForge 26.2.0.93, Bad Spell 0.1.0-alpha. Dies ist der aktuelle implementierte Produktionsumfang, keine vollständige PDF-Mod.

## Bedienung

Station platzieren und rechtsklicken. Sechs linke Slots sind Eingänge, vier rechte Slots Ausgänge. Der obere mittlere Slot nimmt den **Startzellenadapter**, der untere mittlere Slot die **Start-Feldzelle** auf. Ohne FL-Kosten wird kein Adapter benötigt. Der Balken zeigt die Verarbeitungszeit. 20 Ticks entsprechen bei normaler Servergeschwindigkeit einer Sekunde.

Die Station pausiert bei fehlender Energie, fehlendem Adapter oder vollem Ausgang. Material und FL werden erst bei erfolgreichem Abschluss verbraucht. Der Adapter bleibt erhalten. Die Feldzelle liefert maximal 100 FL. Eine leere Zelle an der normalen Werkbank in ein Gehäuse umwandeln und an der Arkanwerkbank wieder befüllen. Restladung geht bei dieser Umwandlung verloren. Es gibt noch kein Kabelnetz.

Der erste Benutzer bzw. Platzierende bindet die Station an sich; fremde Spieler dürfen ihr Menü nicht öffnen. Dies ist kein vollständiger Claim-/Griefingschutz: Abbau und Hopper sind nicht eigentümergeschützt. Hopper oben liefern Zutaten, seitlich Zellen/Adapter und unten entnehmen sie Ergebnisse.

## Startpfad

1. Feldkristallerz abbauen. Roher Feldkristall + Feuerstein ergeben an der Werkbank grobe Splitter. Sand, Papier und Feuerstein ergeben Sandpapier; zwei grobe Splitter + Sandpapier ergeben einen Q0-Splitter.
2. Arkanwerkbank: drei Runenholzbretter oben; Kupfer / Werkbank / Kupfer in der Mitte; ein zusätzliches Runenholzbrett unten links und ein Q0-Splitter unten mittig. Runenholz stammt von den neuen Bäumen und Setzlingen.
3. In der Arkanwerkbank Zellengehäuse, Startzelle und Adapter herstellen, danach den primitiven Kristallschleifer. Die Werkbank selbst benötigt für diese Schritte keine FL.
4. Im Schleifer Feldkristalle in reguläre Splitter und diese in Staub verarbeiten. Danach Alchemietisch, Harz und Leitpaste herstellen.
5. Buchrohling an der normalen Werkbank, danach im Alchemietisch mit Runentinte zum Formelbuch verarbeiten. Weiter über Q1-Barren, Spulen, Legierung und Gravurstation zu Presse, Platten und Handschuh.

Neue Erze/Pflanzen/Bäume erscheinen in neu generierten Chunks. Ihre natürliche Verteilung und der gesamte Survivalpfad sind noch nicht im Spiel durchgespielt. Keine Abkürzung über bloß registrierte Erfolgsitems.

## Gravieren und Aktivieren

In der Gravurstation eine leere Q1-Runenplatte und das eigene gespeicherte Formelbuch in die Eingänge legen. Die Gravur bewahrt das Buch und erzeugt einen unveränderlichen Snapshot. Kosten: 30 + 4 FL pro Runenknoten, 80 Ticks. Q1 erlaubt bis zu 24 Knoten; mit der aktuellen 100-FL-Zelle sind jedoch nur Formeln bis 17 Knoten gravierbar.

Rechtsklick mit der gravierten Platte importiert sie in einen eigenen Handschuh. Alternativ die Platte in den ersten Eingang eines eigenen Runenhalters legen, Adapter und Zelle ergänzen und eine neue Redstone-Flanke auslösen. Der aktuelle Punktlichtzauber wirkt oberhalb des Halters und kostet 30 FL bei der Starterformel; Maschinen verbrauchen keine Spieler-FK. Andere Effektarten sind noch nicht implementiert.

## Ausführbare Stationsrezepte

| Ergebnis | Station | Eingänge | Ticks | FL |
|---|---|---|---:|---:|
| 1 × Feldzellengehäuse | Arkanwerkbank | 1 × copper_ingot, 1 × glass | 40 | 0 |
| 1 × Start-Feldzelle (100 FL) | Arkanwerkbank | 2 × redstone, 1 × Handgeschliffener Splitter Q0, 1 × coal, 1 × Feldzellengehäuse | 40 | 0 |
| 1 × Start-Feldzelle (100 FL) | Arkanwerkbank | 2 × redstone, 1 × Feldkristallsplitter, 1 × coal, 1 × Feldzellengehäuse | 40 | 0 |
| 1 × Startzellenadapter | Arkanwerkbank | 1 × copper_ingot, 1 × Handgeschliffener Splitter Q0, 1 × redstone | 40 | 0 |
| 1 × Kristallschleifer | Arkanwerkbank | 3 × iron_ingot, 1 × quartz_block, 2 × copper_ingot, 2 × Grober Kristallsplitter | 100 | 0 |
| 3 × Feldkristallsplitter | Kristallschleifer | 1 × Roher Feldkristall | 80 | 20 |
| 4 × Kristallstaub | Kristallschleifer | 2 × Feldkristallsplitter | 60 | 10 |
| 1 × Kristallkernrohling | Kristallschleifer | 4 × Feldkristallsplitter | 100 | 20 |
| 1 × Alchemietisch | Arkanwerkbank | 1 × brewing_stand, 2 × glass, 2 × Runenholzstamm, 1 × copper_ingot, 1 × Kristallstaub | 100 | 0 |
| 1 × Reinwasser | Alchemietisch | 1 × Wasserflasche (Wassertrank-Komponente), 1 × charcoal | 200 | 0 |
| 2 × Runenharz, 1 × glass_bottle | Alchemietisch | 2 × Runenholzstamm, 1 × Reinwasser | 100 | 10 |
| 4 × Leitpaste | Alchemietisch | 2 × Kristallstaub, 1 × redstone, 1 × Runenharz | 80 | 10 |
| 1 × Präzisionsspule | Arkanwerkbank | 2 × copper_ingot, 2 × Leitpaste, 1 × Feldkristallsplitter | 40 | 0 |
| 1 × Gereinigter Kristallkern Q1, 1 × glass_bottle | Alchemietisch | 1 × Kristallkernrohling, 1 × Reinwasser | 100 | 25 |
| 2 × Mondsud, 1 × glass_bottle | Alchemietisch | 2 × Mondblüte, 1 × Reinwasser | 160 | 15 |
| 1 × Mondsalz | Alchemietisch | 2 × Mondsud | 240 | 20 |
| 1 × Mondessenz | Alchemietisch | 2 × Mondsud | 200 | 20 |
| 1 × Ätherlösung, 1 × glass_bottle | Alchemietisch | 2 × Mondblüte, 1 × Kristallstaub, 1 × Reinwasser | 120 | 20 |
| 1 × Runentinte (64 Punkte) | Alchemietisch | 2 × Kristallstaub, 1 × Mondsud, 1 × ink_sac, 1 × glass_bottle | 100 | 10 |
| 1 × Formelbuch | Alchemietisch | 1 × Formelbuchrohling, 1 × Runentinte (64 Punkte) | 80 | 0 |
| 1 × Arkaner Schmelzofen | Arkanwerkbank | 1 × blast_furnace, 4 × Arkanbarren Q0, 2 × Leitpaste, 1 × Präzisionsspule | 160 | 0 |
| 1 × Arkanbarren Q1 | Arkaner Schmelzofen | 2 × Arkanerzbrocken, 1 × coal | 160 | 40 |
| 2 × Arkanlegierung Q2 | Arkaner Schmelzofen | 2 × Arkanbarren Q1, 1 × gold_ingot, 1 × Mondsalz | 180 | 75 |
| 2 × Arkanstaub | Kristallschleifer | 1 × Arkanbarren Q1 | 80 | 30 |
| 2 × Resonanzglas | Arkaner Schmelzofen | 2 × Resonanzsplitter, 2 × glass, 1 × Arkanstaub | 160 | 50 |
| 1 × Resonanzformling | Kristallschleifer | 4 × Resonanzsplitter | 100 | 30 |
| 1 × Resonanzkern Q2 | Arkaner Schmelzofen | 1 × Resonanzformling, 1 × Gereinigter Kristallkern Q1 | 200 | 100 |
| 1 × Präzisionslinse | Kristallschleifer | 1 × Resonanzglas, 2 × Feldkristallsplitter | 100 | 45 |
| 1 × Siegelbindemasse | Alchemietisch | 1 × Runenharz, 1 × Mondsalz | 100 | 10 |
| 1 × Gravurstation | Arkanwerkbank | 2 × Arkanlegierung Q2, 2 × Präzisionsspule, 1 × Präzisionslinse, 1 × Gereinigter Kristallkern Q1 | 160 | 0 |
| 1 × Stabilisatorsiegel | Gravurstation | 1 × Siegelbindemasse, 1 × Leitpaste | 100 | 30 |
| 1 × Runenpresse | Arkanwerkbank | 2 × iron_block, 2 × Arkanbarren Q1, 2 × Präzisionsspule, 1 × Stabilisatorsiegel | 160 | 0 |
| 2 × Leere Runenplatte Q1 | Runenpresse | 2 × Arkanbarren Q1, 2 × Leitpaste | 100 | 40 |
| 1 × Runenhandschuhrohling | Arkanwerkbank | 2 × leather, 1 × gold_ingot, 2 × Präzisionsspule, 1 × Gereinigter Kristallkern Q1 | 100 | 0 |
| 1 × Runenhandschuh | Gravurstation | 1 × Runenhandschuhrohling, 1 × Stabilisatorsiegel | 160 | 60 |
| 1 × Runenhalter | Arkanwerkbank | 2 × Arkanbarren Q1, 1 × copper_ingot, 1 × Leitpaste, 1 × Präzisionsspule | 100 | 0 |

Zusätzlich: Leuchtmoosbündel aus drei Leuchtmoosen an der Werkbank, dann im Vanilla-Ofen zu zwei Leuchtfasern. Unreine Arkanmischung aus drei Arkanerzbrocken, Quarz und Kohle wird im Vanilla-Ofen zu Q0-Arkanbarren.
