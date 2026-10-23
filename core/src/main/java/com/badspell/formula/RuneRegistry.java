package com.badspell.formula;

import java.util.*;
import static com.badspell.formula.Value.Type.*;

/** Trusted server registrations. Effects here produce plans, never touch a Minecraft world. */
public final class RuneRegistry {
    @FunctionalInterface public interface Operation { Value evaluate(Map<String, Value> inputs, Map<String, Value> parameters, Context context); }
    public record Context(Value.Position target,long tick,Value.Entity entity,Value.Vector look,Map<String,Value> environment) {
        public Context(Value.Position target,long tick){this(target,tick,null,new Value.Vector(0,0,1000),Map.of());}
        public Context {Objects.requireNonNull(target);Objects.requireNonNull(look);environment=Map.copyOf(environment);}
        public long start(){return environment.get("activation_tick") instanceof Value.Number n?n.value():tick;}
    }
    public record Parameter(Value.Type type, long min, long max) {
        boolean accepts(Value value) {
            if (value.type() != type) return false;
            return !(value instanceof Value.Number n) || (n.value() >= min && n.value() <= max);
        }
    }
    public record Rune(Map<String,Value.Type> inputs, Map<String,Parameter> parameters, Value.Type output, Operation operation) {
        public Rune { inputs=Map.copyOf(inputs); parameters=Map.copyOf(parameters); Objects.requireNonNull(output); Objects.requireNonNull(operation); }
    }
    private final Map<String,Rune> entries;
    public RuneRegistry(Map<String,Rune> entries) { this.entries=Map.copyOf(entries); }
    public Rune get(String id) { return entries.get(id); }
    public Rune get(Formula.Node node) {return node.rune().equals("badspell:call")?FormulaCalls.rune(node):get(node.rune());}
    public Set<String> ids() { return entries.keySet(); }
    private static Parameter numeric(long min,long max) { return new Parameter(NUMBER,min,max); }
    private static long number(Map<String,Value> values,String key) { return ((Value.Number)values.get(key)).value(); }
    public static RuneRegistry standard() {
        Map<String,Rune> r=new LinkedHashMap<>();
        r.put("badspell:impulse",new Rune(Map.of(),Map.of(),EVENT,(i,p,c)->new Value.Event(c.start())));
        r.put("badspell:position",new Rune(Map.of(),Map.of("value",new Parameter(POSITION,0,0)),POSITION,(i,p,c)->p.get("value")));
        r.put("badspell:target_position",new Rune(Map.of("trigger",EVENT),Map.of(),POSITION,(i,p,c)->c.target()));
        r.put("badspell:point",new Rune(Map.of("position",POSITION),Map.of(),REGION,(i,p,c)->new Value.Region((Value.Position)i.get("position"),0)));
        r.put("badspell:circle",new Rune(Map.of("position",POSITION),Map.of("radius",numeric(1,8)),REGION,(i,p,c)->new Value.Region((Value.Position)i.get("position"),(int)number(p,"radius"))));
        r.put("badspell:number",new Rune(Map.of(),Map.of("value",numeric(-1_000_000,1_000_000)),NUMBER,(i,p,c)->p.get("value")));
        r.put("badspell:boolean",new Rune(Map.of(),Map.of("value",new Parameter(BOOLEAN,0,0)),BOOLEAN,(i,p,c)->p.get("value")));
        r.put("badspell:greater",new Rune(Map.of("a",NUMBER,"b",NUMBER),Map.of(),BOOLEAN,(i,p,c)->new Value.Bool(number(i,"a")>number(i,"b"))));
        r.put("badspell:equal_number",new Rune(Map.of("a",NUMBER,"b",NUMBER),Map.of(),BOOLEAN,(i,p,c)->new Value.Bool(i.get("a").equals(i.get("b")))));
        r.put("badspell:and",new Rune(Map.of("a",BOOLEAN,"b",BOOLEAN),Map.of(),BOOLEAN,(i,p,c)->new Value.Bool(((Value.Bool)i.get("a")).value()&&((Value.Bool)i.get("b")).value())));
        r.put("badspell:or",new Rune(Map.of("a",BOOLEAN,"b",BOOLEAN),Map.of(),BOOLEAN,(i,p,c)->new Value.Bool(((Value.Bool)i.get("a")).value()||((Value.Bool)i.get("b")).value())));
        r.put("badspell:not",new Rune(Map.of("value",BOOLEAN),Map.of(),BOOLEAN,(i,p,c)->new Value.Bool(!((Value.Bool)i.get("value")).value())));
        r.put("badspell:if_effect",new Rune(Map.of("condition",BOOLEAN,"yes",EFFECT,"no",EFFECT),Map.of(),EFFECT,(i,p,c)->((Value.Bool)i.get("condition")).value()?i.get("yes"):i.get("no")));
        r.put("badspell:light",new Rune(Map.of("region",REGION),Map.of("strength",numeric(1,8),"duration_ticks",numeric(1,1200)),EFFECT,(i,p,c)->{
            int strength=(int)number(p,"strength"), ticks=(int)number(p,"duration_ticks");
            Value.Region region=(Value.Region)i.get("region");
            // Lichtfunke: 3 strength, 600 ticks, radius 0 => exactly 30 FL + 5 FK.
            long cost=Math.max(1,Math.floorDiv(Math.multiplyExact((long)strength,ticks)+59,60));
            cost=Math.multiplyExact(cost,1L+region.radius());
            return new Value.Effect("badspell:light",region,strength,ticks,cost,5);
        }));
        r.put("badspell:activate",new Rune(Map.of("effect",EFFECT,"trigger",EVENT),Map.of(),EFFECT,(i,p,c)->{var effect=(Value.Effect)i.get("effect");var event=(Value.Event)i.get("trigger");return new Value.Effect(effect.id(),effect.region(),effect.strength(),effect.durationTicks(),Math.addExact(effect.fieldCost(),event.interval()/20),effect.focusCost(),effect.target(),effect.vector(),event,effect.children());}));
        ExtendedRunes.register(r);
        return new RuneRegistry(r);
    }
}
