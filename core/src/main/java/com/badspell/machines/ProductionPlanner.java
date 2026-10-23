package com.badspell.machines;

import java.util.*;

/** Resolves concrete recipe choices into ordered batches; preserves batch leftovers.
 * Raw resources are explicit, never inferred from missing recipe definitions.
 */
public final class ProductionPlanner {
    public record Batch(Recipe recipe,int count) {}
    public record Plan(List<Batch> batches,Map<String,Long> rawInputs,Map<String,Long> leftovers,long fieldCost,long ticks) {
        public Plan { batches=List.copyOf(batches);rawInputs=Map.copyOf(rawInputs);leftovers=Map.copyOf(leftovers); }
    }
    private final Map<String,Recipe> recipes;
    private final Set<String> raw;
    public ProductionPlanner(Map<String,Recipe> selectedRecipes,Set<String> raw) {
        recipes=Map.copyOf(selectedRecipes); this.raw=Set.copyOf(raw);
        recipes.forEach((item,recipe)-> { if(!recipe.outputs().containsKey(item)) throw new IllegalArgumentException("Selected recipe does not produce "+item); });
    }
    public Plan plan(String item,long count,Map<String,Long> stock) {
        if(count<=0) throw new IllegalArgumentException("positive request required");
        var s=new State(stock); s.require(item,count,new LinkedHashSet<>(),0);
        return new Plan(s.batches,s.rawInputs,s.available,s.field,s.ticks);
    }
    private final class State {
        final Map<String,Long> available=new TreeMap<>(),rawInputs=new TreeMap<>();
        final List<Batch> batches=new ArrayList<>(); long field,ticks;
        State(Map<String,Long> stock) { stock.forEach((id,n)->{if(n<0) throw new IllegalArgumentException("negative stock");if(n>0)available.put(id,n);}); }
        void require(String item,long required,Set<String> visiting,int depth) {
            if(depth>128 || batches.size()>4096) throw new IllegalArgumentException("Production planning budget exceeded");
            long held=available.getOrDefault(item,0L),used=Math.min(required,held);
            if(used==held) available.remove(item); else available.put(item,held-used);
            required-=used; if(required==0)return;
            if(raw.contains(item)) { rawInputs.merge(item,required,Math::addExact); return; }
            Recipe recipe=recipes.get(item);
            if(recipe==null) throw new IllegalArgumentException("Unreachable material: "+item);
            if(!visiting.add(item)) throw new IllegalArgumentException("Production cycle: "+visiting+" -> "+item);
            int output=recipe.outputs().get(item);
            long batchesNeeded=1+Math.floorDiv(required-1,output);
            int n=Math.toIntExact(batchesNeeded);
            for(var e:new TreeMap<>(recipe.inputs()).entrySet()) require(e.getKey(),Math.multiplyExact((long)e.getValue(),n),visiting,depth+1);
            visiting.remove(item);
            for(var e:recipe.outputs().entrySet()) available.merge(e.getKey(),Math.multiplyExact((long)e.getValue(),n),Math::addExact);
            long remaining=available.get(item)-required;
            if(remaining==0) available.remove(item);else available.put(item,remaining);
            field=Math.addExact(field,Math.multiplyExact(recipe.fieldCost(),n));
            ticks=Math.addExact(ticks,Math.multiplyExact((long)recipe.ticks(),n));
            batches.add(new Batch(recipe,n));
        }
    }
}
