# Gestaltungsregeln für Bad Spell

Verbindliche Richtung für neue und überarbeitete Inhalte: Minecraft-nahe Pixelgrafik und gut lesbare, sparsame Geometrie. Keine austauschbaren dunklen Würfel mit demselben Leuchtsymbol.

- Blocktexturen grundsätzlich 16×16 Pixel; deutliche Materialfamilien, wenige zusammenhängende Farbstufen, sparsame Glanzkanten. Holz bleibt Holz, Stein bleibt neutral, Metall erhält gerichtete helle Kanten.
- Runen und Kristalle sind Akzente. Flächiges Neon, Zufallsrauschen, schwarze Technikkacheln und unscharfe hochskalierte Grafiken vermeiden.
- Materialien erhalten erkennbare Silhouetten: Barren, Splitter, Staubhaufen, Harz, Flaschen, Spulen und Platten unterscheiden sich bereits ohne Beschriftung.
- Funktionsblöcke erhalten bei Bedarf eigene Cuboid-Modelle. Bauteile erklären ihre Funktion: Schleifscheibe und Kurbel, Pressstempel, Gravurplatte und Linse, Glasgefäße und Arbeitsfläche.
- Stationsmodelle bleiben innerhalb einer Blockhöhe. Wenige kräftige Bauteile bevorzugen; keine überladenen Kleinteile oder sich überlagernden Flächen. Partikel und Animationen ergänzen die Geometrie, ersetzen sie nicht.
- Werkzeugmodelle haben eigene Inventar-, Hand- und Bodenansichten. Alle Qualitätsstufen müssen auch im kleinen Inventarsymbol erkennbar bleiben.
- Zukünftige Mobs: zuerst klare Silhouette und lesbare Bewegungen, dann Details. Texturauflösung an Vanilla-Kreaturen vergleichbarer Größe orientieren. Keine fremden Modelle oder Texturen übernehmen.
- Vanilla-Materialien dürfen über Ressourcenreferenzen benutzt werden. Keine Vanilla-PNGs oder fremden Assetpakete in die Mod kopieren.

Reproduzierbare Pipeline: `generate-production-assets.py`, danach `generate-m1-assets.py`, zuletzt `style-assets.py`. Die letzte Stufe ist die maßgebliche visuelle Gestaltung und darf nicht durch den älteren Grundgenerator überschrieben werden.

Abnahme: Modelle und Texturen im echten Client bei Tageslicht, Dunkelheit, im Inventar und in der Hand betrachten. Ressourcenprüfung und PNG-Abmessungen allein belegen keine gute Darstellung.
