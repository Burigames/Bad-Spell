# Bad Spell

Minecraft-Mod mit einer gemeinsamen, typisierten Runensprache für tragbare Zauber und stationäre Geräte. **M1 und M2 (Forschung) sind im vereinbarten Umfang abgeschlossen; die vollständige Mod aus dem Design-PDF bleibt in Entwicklung.** Der genaue Umfang und die Abnahme stehen in [M1_ACCEPTANCE.md](docs/M1_ACCEPTANCE.md); die M2-Abnahme in [M2_ACCEPTANCE.md](docs/M2_ACCEPTANCE.md); alle 161 PDF-Konzepte in [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md).

## Installation

- Minecraft **Java Edition 26.2**, **NeoForge 26.2.0.93**, **Java 25**.
- [Aktuelle JAR](releases/0.1.0-alpha/badspell-0.1.0-alpha.jar) und [SHA-256](releases/0.1.0-alpha/badspell-0.1.0-alpha.jar.sha256).
- Eine Bad-Spell-JAR in den jeweiligen `mods`-Ordner legen. Client und dedizierten Server gemeinsam aktualisieren; alte Kopien entfernen.
- Mod-ID `badspell`, Package `com.badspell`, Version `0.1.0-alpha`, Netzwerkprotokoll **5**. Ausschließlich NeoForge.

## Spielen

**G** öffnet den bebilderten Arkanen Kodex. Er enthält Einstieg, Runenverbindungen, Ziele, kombinierte/bedingte Zauber, Energie, Stationen und Fehlerhilfe. Das Codex-Item ist aus Buch, Tintenbeutel und Kupferbarren herstellbar.

M2: **G → Forschung** öffnet persönliches Wissen, Materialentdeckungen und Handwerkswege. Ein eigenes Formelbuch öffnen, Materialprobe und Papier zu einem Fragment vorbereiten und dessen Porttypen anhand der Messhinweise analysieren. FL und Tinte werden tatsächlich verbraucht. Bekannte Runen können über bewusst verfasste Lehrblätter weitergegeben werden; vorhandene eigene M1-Formeln migrieren einmalig. [Forschungsanleitung](docs/FORSCHUNG.md).

Formelbuch öffnen, eine bekannte Vorlage in der Bibliothek wählen, speichern und an den Handschuh exportieren. Beim Wirken werden geladene Feldzellen im Inventar gemeinsam genutzt. Umschalt+Mausrad wechselt den Handschuh-Slot; Umschalt+Rechtsklick beendet persönliche Zeitaufträge. Ein Klick auf eine Eingangszeile im Editor bietet passende Runen an und verbindet die gewählte Quelle automatisch.

Stationen besitzen eigene Arbeitsbereiche und eine `?`-Rezeptansicht. Der Runenhalter nimmt eine fertige Platte auf und führt über Redstone oder Start/Stop dieselben verfügbaren Wirkungsarten wie der Handschuh aus. Geräte verwenden **Ursprung** für ihre Position und bezahlen FL statt Spielerfokus. Ein Gerät ist kein lebendes **Selbst**.

Der Feldkristall leitet bei Bedarf Energie zwischen eigenen Stationen: Spitze zum Verbraucher, Rechtsklick mit leerer Hand dreht die Richtung. Beide Endpunkte brauchen Zelle und Zelladapter. Kein Speicher und keine Energieerzeugung; höchstens 16 Kristalle je Pfad, 80 FL/s je Kristall.

[Item-Anleitung](docs/ITEM_ANLEITUNG.md) · [Herstellung](docs/HERSTELLUNG.md) · [Editor und Kodex](docs/USABILITY_REWORK.md)

## Entwicklung

JDK **25**, Gradle-Wrapper **9.2.1**, ModDevGradle **2.0.148**, NeoForge **26.2.0.93**. Verifikation der Plattform: [VERSION_VERIFICATION.md](docs/VERSION_VERIFICATION.md). Offizielle Dokumentation: https://docs.neoforged.net/.

```bash
./gradlew build
./gradlew runGameTestServer
./gradlew runClient
./gradlew runServer
```

Die verteilbare Datei entsteht unter `build/libs/badspell-0.1.0-alpha.jar`; der unabhängige Formelkern ist darin enthalten. In dieser Cloud stellt `source scripts/cloud-env.sh` die installierte Java-25-Toolchain bereit; `scripts/cloud-display.sh` aktiviert bei Bedarf das virtuelle Display.

Aktuelle M2-Regression: **51 Kernprüfungen und 61 erforderliche Server-GameTests**.

M1-Abnahme: **45 Kernprüfungen und 49 Server-GameTests**, 848 reale Rezeptschritte, zwei echte Clients mit installierter JAR und dediziertem Server sowie Neustartprüfung. Details, Grenzen und Reproduktion: [TESTING.md](TESTING.md).

Architektur: [ARCHITECTURE.md](ARCHITECTURE.md) · Designergänzungen: [DESIGN_DECISIONS.md](DESIGN_DECISIONS.md) · Assets: [ASSET_STYLE.md](docs/ASSET_STYLE.md)
