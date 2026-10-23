package com.badspell.machines;

import com.badspell.field.EnergyAccount;
import java.util.*;

/** Single-station in-memory transaction model. Persistence/world adapters are not implemented yet.
 * Callers run on the server thread. All recipe outputs are checked before any input or FL is removed.
 */
public final class MachineInventory {
    private final String station;
    private final int capacity;
    private final Map<String,Integer> items=new TreeMap<>();
    private final Set<UUID> committed=new HashSet<>();
    public MachineInventory(String station,int capacity,Map<String,Integer> initial) {
        this.station=Objects.requireNonNull(station);this.capacity=capacity;
        if(capacity<0)throw new IllegalArgumentException("capacity");
        initial.forEach((id,n)->{if(n<0)throw new IllegalArgumentException("negative inventory");if(n>0)items.put(id,n);});
        if(total(items)>capacity)throw new IllegalArgumentException("inventory overflow");
    }
    public synchronized Map<String,Integer> contents() { return Map.copyOf(items); }
    /** Only commit after the adapter's process timer reaches recipe.ticks(). */
    public synchronized boolean complete(UUID jobId,Recipe recipe,EnergyAccount energy) {
        if(committed.contains(jobId))return false;
        if(!station.equals(recipe.station()))return false;
        Map<String,Integer> next=new TreeMap<>(items);
        for(var e:recipe.inputs().entrySet()) {
            int held=next.getOrDefault(e.getKey(),0);if(held<e.getValue())return false;
            if(held==e.getValue())next.remove(e.getKey());else next.put(e.getKey(),held-e.getValue());
        }
        for(var e:recipe.outputs().entrySet())next.merge(e.getKey(),e.getValue(),Math::addExact);
        if(total(next)>capacity)return false;
        return energy.transact(recipe.fieldCost(),0,()->{items.clear();items.putAll(next);committed.add(jobId);});
    }
    private static long total(Map<String,Integer> inventory) { return inventory.values().stream().mapToLong(Integer::longValue).sum(); }
}
