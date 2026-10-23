# M1 development snapshot – 23.10.2026

Erweiterte Runensprache, Bibliothek/Module, neue Zauberwirkungen und Werkzeuge, Q2/Q3 sowie Vanilla-nahe Texturen und 3D-Modelle. 54 Verarbeitungsrezepte.

Build, 39 Kerntests und 33 Server-GameTests erfolgreich. Neue grafische Abläufe und vollständiger Survival-/Multiplayer-Durchlauf offen. M1 noch nicht vollständig abgenommen. Client und Server müssen dieselbe JAR nutzen (Netzwerkprotokoll 2). Minecraft 26.2, NeoForge 26.2.0.93, Java 25.

SHA256: `13a3853b1b657fb9ec59dba4447210a592db559da38e203230db9cc2c34dccb2`

# UI und Produktionsketten · 23. Oktober 2026

Minecraft 26.2 · NeoForge 26.2.0.93 · Java 25 · Modversion weiterhin 0.1.0-alpha.

- Neuer Formelbuch-Editor: flache Oberfläche, Runensuche, Reiter, direkte Zahlenfelder, Kosten/Diagnosen, Zoom/Pan bleibt beim Speichern erhalten.
- 37 neue Material-/Plattenitems und 17 Blöcke, darunter sieben echte Verarbeitungsstationen.
- 36 Stationsrezepte plus frühe Werkbank-/Ofenrezepte; sechs natürliche Rohstoffquellen.
- Getaktete Inventarverarbeitung mit FL-Verbrauch, Adapter, vollem-Ausgang-Schutz und Hopper-Seiten.
- Gravierte Formelsnapshots, Plattenimport in Handschuhe und Redstone-Aktivierung von Punktlicht am Runenhalter.
- 28 Coretests und 19 erforderliche GameTests inklusive Vanilla-Test bestanden. Details/Prüfgrenzen in TESTING.md.

**Weiterhin unvollständig:** keine neuen Mobs/Bosse, Dimensionen, Ritualsysteme, Kabelnetze oder weiteren Zauberwirkungen. Vollständiger Survival-Durchlauf ausstehend. Neue Weltgenquellen benötigen neu erzeugte Chunks.

Alte JAR ersetzen und Client/Server neu starten; nicht mehrere Bad-Spell-JARs gleichzeitig installieren. Anleitung: docs/HERSTELLUNG.md.

SHA-256: `f468b9aa1af2596fa2b544185eb8edbeb75116f18b04fa03427e3fce0fd3e80c`

---

# Licht-Hotfix · 23. Oktober 2026

Minecraft 26.2 · NeoForge 26.2.0.93 · Java 25 · Modversion weiterhin 0.1.0-alpha.

- Behoben: Lichtfunke verbrauchte FL/FK, ohne Luft tatsächlich zu beleuchten.
- Reale Blocklichtemission, Ausbreitung und Ablauf ohne Platzierung von Lichtblöcken.
- Lichtquellen funktionieren auch in vollständig leeren Sections.
- Aktive Quellen werden beim Versand eines Chunks an Clients synchronisiert.
- Kurzer Partikeleffekt bei erfolgreicher Aktivierung; aktuelle serverseitige Blickposition.
- 28 Kerntests und 12 eigene Minecraft-GameTests erfolgreich; gepackte JAR separat im Client geprüft.

Alte Bad-Spell-JAR ersetzen und Spiel bzw. Server neu starten. Bei Multiplayer auch Clients aktualisieren. Nicht beide JARs gleichzeitig installieren. Andere Zaubereffekte und die übrigen fehlenden Modsysteme sind weiterhin nicht implementiert.

SHA-256: `66ce9c6262c1553180ce7f0752b84d97acc010bdbf1d13c0785e8ff462d1be6e`
