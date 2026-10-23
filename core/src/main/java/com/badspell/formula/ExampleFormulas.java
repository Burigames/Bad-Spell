package com.badspell.formula;

import java.util.*;

/** Reference graph, not a special-cased spell in the interpreter. */
public final class ExampleFormulas {
    private ExampleFormulas() {}
    public static Formula light(UUID owner) {
        return new Formula(UUID.fromString("919339fc-53d3-4215-a960-c3a03a201001"),owner,1,"Lichtfunke",List.of(
            new Formula.Node("impulse","badspell:impulse",0,0,Map.of()),
            new Formula.Node("target","badspell:target_position",140,0,Map.of()),
            new Formula.Node("point","badspell:point",280,0,Map.of()),
            new Formula.Node("light","badspell:light",420,0,Map.of("strength",new Value.Number(3),"duration_ticks",new Value.Number(600))),
            new Formula.Node("activate","badspell:activate",560,0,Map.of())),List.of(
            new Formula.Edge("impulse","target","trigger"),
            new Formula.Edge("target","point","position"),
            new Formula.Edge("point","light","region"),
            new Formula.Edge("light","activate","effect"),
            new Formula.Edge("impulse","activate","trigger")),"activate",100);
    }
}
