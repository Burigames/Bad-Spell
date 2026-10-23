package com.badspell.formula;

import com.badspell.field.EnergyAccount;
import java.util.Objects;

/** Server-thread coordinator. A Minecraft adapter must recheck loaded chunks/claims inside prepare. */
public final class ActivationService {
    public enum Result { APPLIED, UNSTABLE, DENIED, OUT_OF_RANGE, BUDGET, INSUFFICIENT_ENERGY, WAITING, CANCELLED, SKIPPED }
    public interface EffectHost {
        /** Return null when denied. Prepared action must commit atomically, throwing only before world mutation. */
        Runnable prepare(Value.Effect effect);
    }
    private final FormulaEngine engine;
    public ActivationService(FormulaEngine engine) { this.engine=Objects.requireNonNull(engine); }
    public Result activate(Formula formula,RuneRegistry.Context context,Value.Position origin,int stability,
                           EnergyAccount account,EffectHost host) {
        if(stability<0 || stability>100) throw new IllegalArgumentException("ST 0..100");
        if(stability<25) return Result.UNSTABLE;
        var output=formula.nodes().stream().filter(n->n.id().equals(formula.output())).findFirst();
        if(output.isEmpty() || !output.get().rune().equals("badspell:activate")) throw new IllegalArgumentException("Explicit activation output required");
        Value.Effect effect=engine.plan(formula,context);
        if(effect.event().cancelled()||effect.event().remaining()==0)return Result.CANCELLED;
        if(context.tick()<effect.event().tick())return Result.WAITING;
        for(var part:effect.children().isEmpty()?java.util.List.of(effect):effect.children()){
        var target=part.region().center();
        double dx=(double)origin.x()-target.x(),dy=(double)origin.y()-target.y(),dz=(double)origin.z()-target.z();
        if(!origin.dimension().equals(target.dimension()) || dx*dx+dy*dy+dz*dz>(part.id().equals("badspell:light")?144:256)) return Result.OUT_OF_RANGE;
        }
        long fieldCost=effect.fieldCost();
        if(stability<50) fieldCost=Math.addExact(fieldCost,Math.floorDiv(Math.addExact(fieldCost,3),4));
        if(fieldCost>formula.energyBudget()) return Result.BUDGET;
        if(account.balance().field()<fieldCost||account.balance().focus()<effect.focusCost())return Result.INSUFFICIENT_ENERGY;
        // Explicit empty branch is control flow, not a world adapter. Interval sensor fees still apply.
        if(effect.id().equals("badspell:none"))return account.transact(fieldCost,effect.focusCost(),()->{})?Result.SKIPPED:Result.INSUFFICIENT_ENERGY;
        Runnable action=host.prepare(effect);
        if(action==null) return Result.DENIED;
        return account.transact(fieldCost,effect.focusCost(),action)?Result.APPLIED:Result.INSUFFICIENT_ENERGY;
    }
}
