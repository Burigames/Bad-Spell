# Feature- und Abhängigkeitsübersicht

Die 161 Konzepte, 135 konkreten Rezept-IDs, 35 Kernrunen und zwölf Referenzzauber stehen einzeln in IMPLEMENTATION_STATUS.md. Die vollständigen Werte und Montageketten bleiben im 58-seitigen Designtext nachvollziehbar.

| Bereich | Voraussetzung | Nachweis vor Freigabe |
|---|---|---|
| Universelle Runensprache | Typen, Graphen, validierte Operationen, Energie, Rechte | Determinismus, ungültige Graphen, Limits, alle erlaubten Effekte |
| Formelbuch | Core, Server-Persistenz, Payloads, Client-Canvas | Editieren, Verbindungen, Parameter, Tinte, Undo/Redo, Revisionen, Kopierrechte |
| Handschuh / Meißel / Monokel / Kompass | Formelbuch, Snapshots, Fokus, tragbare FL, ACL | Serverprüfung, Slots, Oberflächenraster, öffentliche Analyse und sichere Signaturen |
| Ressourcen / frühe Stationen | Worldgen oder spezifizierte Rohstoffquellen, Vanilla-Einstieg | B-00–B-10 ohne Creative und ohne Kreisrezepte |
| Industrielle Produktion | KS/AT/AS, Halbzeuge, Energie, Eingänge/Ausgänge | Mengen, FL, Zeit, Restbestände, Output voll, Crash-/Replay-Sicherheit |
| Ausrüstung | Produktionsketten, Qualität, Snapshots, Gravur | Alle Montagevorschriften Anhang C, Reparatur, Softcaps, aktive Wirkung |
| Alchemie / Flüche | Stoffe, Flüssigkeiten, Katalysatoren, Zustände, Rechte | Keine positiven Stoffkreise; erreichbare Reinigung I–III |
| Netze / Automation | Quellen, getrennte Speicher, Kabel, ACL, Maschinenports | Durchsatz/Verluste, Chunkgrenzen, sichere Transfers, vier Musteranlagen |
| Rituale / Vertraute | Graphen, Multiblöcke, Energie, Opferjournal, Signaturen | R3/R5/R7, Abbruchtabelle, Besitz, Ablauf, Wartung und Limits |
| Welt / Dimension | Datapack-Registries, Strukturen, Loot, sichere Portale | Kein automatisches Retrogen, erreichbare Rohstoffe, sicherer Rückweg |
| Kreaturen / NPCs / Bosse | Worldgen, KI, Animationen, Formeleffekte, Fortschritt | Alle 18/10 Begegnungen, Phasen, Konter, Loot und Populationsgrenzen |
| Release | Alles zuvor, Assets/DE/EN, Tutorials, Multiplayer | T0–T5-Survivalweg, fünf neue UX-Tester, Crash-/Last-/Claim-/Modpacktests |

## Nächste zusammenhängende Implementierung

1. Den bereits geprüften Buch-/Handschuh-/Lichtpfad um vollständige Buchansichten und Mehrfachslots erweitern; danach zusätzliche Client- und dedizierte Mehrspielertests.
2. Wissensdaten und zusätzliche Handschuhslots integrieren; den Canvas um Bibliothek, Parametereingänge, Module, Historie und Freigaben erweitern.
3. Den ersten vollständigen Survivalpfad aus Abschnitt 17 implementieren: Rohkristall → B-00/B-01 → frühe Arkanwerkbank → Startzelle → Schleifer → Staub → Alchemietisch → Harz/Paste/Spule → Q0-Barren → Schmelzofen → Q1/Q2 → Gravur/Presse → Kollektor/Speicher.
4. Dabei echte Rohstoffquellen, Rezeptserializer, stationäre Inventare, Prozesszeit und FL integrieren. Der vorhandene ProductionPlanner ist nur ein Hilfskern und ersetzt diese Schritte nicht.
5. Erst nach Survival-Abnahme die darauf aufbauenden Ausrüstungs-, Automations-, Ritual- und Weltphasen freigeben. Das Gesamtziel bleibt der vollständige Umfang der PDF.
