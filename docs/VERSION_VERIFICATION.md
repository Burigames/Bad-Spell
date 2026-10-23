# Erneute Versionsprüfung am 23. Oktober 2026

Offizielle Maven-Metadaten: https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml

Erneut per HTTPS abgerufen und XML geparst: letzte veröffentlichte Version mit Präfix 26.2 ist `26.2.0.93`, ohne Beta-/Snapshot-Suffix. Bestehende Abhängigkeiten bleiben unverändert. Build und echter Minecraft-26.2-Testserver haben diese Version verwendet.

Offizielle Dokumentation: https://docs.neoforged.net/docs/gettingstarted/ (in dieser Sitzung erneut abgerufen). Zusätzlich die tatsächlich aufgelösten NeoForge-26.2.0.93-Quellen geprüft, insbesondere `InputEvent.MouseScrollingEvent` für Scrollwerte, Abbruch des Vanilla-Scrollings und Client-Eventbus. Registrierung über die vorhandenen 26.2-Netzwerk-APIs; keine APIs einer anderen Minecraft-Version übernommen.

Die genaue Werkzeugliste steht in README.md. Nachweis des aktuellen Builds und aller Tests: SLOT_HISTORY_VALIDATION.log.
