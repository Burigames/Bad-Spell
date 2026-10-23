package com.badspell.machines;

import java.util.*;

/** Fully resolved recipe alternative; quality is encoded by distinct material IDs. */
public record Recipe(String id,String station,Map<String,Integer> inputs,Map<String,Integer> outputs,long fieldCost,int ticks) {
    public Recipe {
        Objects.requireNonNull(id); Objects.requireNonNull(station);
        inputs=Map.copyOf(inputs);outputs=Map.copyOf(outputs);
        if(inputs.isEmpty() || outputs.isEmpty() || fieldCost<0 || ticks<1) throw new IllegalArgumentException("recipe bounds");
        inputs.forEach(Recipe::positive); outputs.forEach(Recipe::positive);
    }
    private static void positive(String id,Integer count) { if(id==null || id.isBlank() || count==null || count<=0) throw new IllegalArgumentException("ingredient bounds"); }
}
