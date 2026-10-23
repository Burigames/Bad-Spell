package com.badspell.formula;

import java.util.Objects;

/** Closed value types; no player-provided executable code. Coordinates include dimension. */
public sealed interface Value {
    enum Type { NUMBER, BOOLEAN, POSITION, REGION, EVENT, EFFECT }
    Type type();
    record Number(long value) implements Value { public Type type() { return Type.NUMBER; } }
    record Bool(boolean value) implements Value { public Type type() { return Type.BOOLEAN; } }
    record Position(String dimension, int x, int y, int z) implements Value {
        public Position { Objects.requireNonNull(dimension); if (dimension.isBlank()) throw new IllegalArgumentException("dimension"); }
        public Type type() { return Type.POSITION; }
    }
    record Region(Position center, int radius) implements Value {
        public Region { Objects.requireNonNull(center); if (radius < 0 || radius > 8) throw new IllegalArgumentException("radius 0..8"); }
        public Type type() { return Type.REGION; }
    }
    record Event(long tick) implements Value { public Type type() { return Type.EVENT; } }
    record Effect(String id, Region region, int strength, int durationTicks, long fieldCost, long focusCost) implements Value {
        public Effect {
            Objects.requireNonNull(id); Objects.requireNonNull(region);
            if (strength < 1 || durationTicks < 1 || fieldCost < 0 || focusCost < 0) throw new IllegalArgumentException("effect bounds");
        }
        public Type type() { return Type.EFFECT; }
    }
}
