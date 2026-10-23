package com.badspell.field;

/** FL and FK share one monitor: a failed debit can never consume only one resource. */
public final class EnergyAccount {
    public record Balance(long field,long focus) {}
    private long field,focus;
    private final long fieldCapacity,focusCapacity;
    public EnergyAccount(long fieldCapacity,long focusCapacity,long field,long focus) {
        if(fieldCapacity<0 || focusCapacity<0 || field<0 || focus<0 || field>fieldCapacity || focus>focusCapacity) throw new IllegalArgumentException("energy bounds");
        this.fieldCapacity=fieldCapacity; this.focusCapacity=focusCapacity; this.field=field; this.focus=focus;
    }
    public synchronized Balance balance() { return new Balance(field,focus); }
    public synchronized boolean spend(long fieldCost,long focusCost) {
        if(fieldCost<0 || focusCost<0) throw new IllegalArgumentException("negative energy cost");
        if(field<fieldCost || focus<focusCost) return false;
        field-=fieldCost; focus-=focusCost; return true;
    }
    public synchronized long chargeField(long offered) {
        if(offered<0) throw new IllegalArgumentException("negative charge");
        long accepted=Math.min(offered,fieldCapacity-field); field+=accepted; return accepted;
    }
    public synchronized long restoreFocus(long offered) {
        if(offered<0) throw new IllegalArgumentException("negative focus");
        long accepted=Math.min(offered,focusCapacity-focus); focus+=accepted; return accepted;
    }
    /** Caller executes a prevalidated atomic action under the account lock; a throwing action must have no effects. */
    public synchronized boolean transact(long fieldCost,long focusCost,Runnable atomicAction) {
        if(!spend(fieldCost,focusCost)) return false;
        try { atomicAction.run(); }
        catch(RuntimeException | Error failure) { field+=fieldCost; focus+=focusCost; throw failure; }
        return true;
    }
}
