package com.badspell.formula;

import java.util.*;

/** Pure interpreter: returns an effect plan; never mutates energy, inventory or world. */
public final class FormulaEngine {
    public static final int MAX_OPERATIONS=512;
    private final RuneRegistry registry;
    public FormulaEngine(RuneRegistry registry) { this.registry=Objects.requireNonNull(registry); }
    public Value.Effect plan(Formula formula,RuneRegistry.Context context) {
        var errors=new FormulaValidator(registry).validate(formula);
        if(!errors.isEmpty()) throw new InvalidFormula(errors);
        Map<String,Formula.Node> nodes=new HashMap<>();
        formula.nodes().forEach(n->nodes.put(n.id(),n));
        Map<String,Map<String,String>> inputs=new HashMap<>();
        formula.edges().forEach(e->inputs.computeIfAbsent(e.to(),k->new TreeMap<>()).put(e.port(),e.from()));
        return (Value.Effect)run(formula,context,new int[]{0},0,new HashSet<>());
    }
    private Value run(Formula formula,RuneRegistry.Context context,int[] operations,int depth,Set<UUID> active) {
        if(depth>8||!active.add(formula.id()))throw new IllegalArgumentException("Recursive module or depth above 8");
        Map<String,Formula.Node> nodes=new HashMap<>();formula.nodes().forEach(n->nodes.put(n.id(),n));
        Map<String,Map<String,String>> inputs=new HashMap<>();formula.edges().forEach(e->inputs.computeIfAbsent(e.to(),k->new TreeMap<>()).put(e.port(),e.from()));
        try{return evaluate(formula.output(),nodes,inputs,new HashMap<>(),context,operations,depth,active,formula.owner());}finally{active.remove(formula.id());}
    }
    private Value evaluate(String id,Map<String,Formula.Node> nodes,Map<String,Map<String,String>> inputs,
                           Map<String,Value> cache,RuneRegistry.Context context,int[] operations,int depth,Set<UUID> active,UUID owner) {
        if(cache.containsKey(id)) return cache.get(id);
        if(++operations[0]>MAX_OPERATIONS) throw new IllegalStateException("Tick operation budget exhausted");
        var node=nodes.get(id); var rune=registry.get(node);
        Map<String,Value> values=new HashMap<>();
        Map<String,String> ports=inputs.getOrDefault(id,Map.of());
        if(node.rune().startsWith("badspell:if_")) {
            // Unselected branches do not evaluate, spend energy or request a world action.
            var condition=(Value.Bool)evaluate(ports.get("condition"),nodes,inputs,cache,context,operations,depth,active,owner);
            String selected=condition.value()?"yes":"no";
            Value result=evaluate(ports.get(selected),nodes,inputs,cache,context,operations,depth,active,owner);
            cache.put(id,result); return result;
        }
        for(var e:ports.entrySet()) values.put(e.getKey(),evaluate(e.getValue(),nodes,inputs,cache,context,operations,depth,active,owner));
        if(node.rune().equals("badspell:call")) {
            Formula child=FormulaCalls.decode(node);if(!child.owner().equals(owner))throw new IllegalArgumentException("Private module");
            var errors=new FormulaValidator(registry).validate(child,rune.output());if(!errors.isEmpty())throw new InvalidFormula(errors);
            var environment=new HashMap<>(context.environment());values.forEach((name,value)->environment.put("input:"+name,value));
            var result=run(child,new RuneRegistry.Context(context.target(),context.tick(),context.entity(),context.look(),environment),operations,depth+1,active);cache.put(id,result);return result;
        }
        Value value;
        try{value=rune.operation().evaluate(Map.copyOf(values),node.parameters(),context);}
        catch(IllegalArgumentException|ArithmeticException error){throw new EvaluationFailure(node.id(),node.rune(),error);}

        if(value==null || value.type()!=rune.output()) throw new IllegalStateException("Registered rune returned incorrect type: "+node.rune());
        cache.put(id,value); return value;
    }
    public static final class EvaluationFailure extends IllegalArgumentException {
        private static final long serialVersionUID=1L;
        public final String node,rune;
        EvaluationFailure(String node,String rune,RuntimeException cause){super(cause.getMessage(),cause);this.node=node;this.rune=rune;}
    }
    public static final class InvalidFormula extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
        private final ArrayList<FormulaValidator.Diagnostic> diagnostics;
        public InvalidFormula(List<FormulaValidator.Diagnostic> diagnostics) { super(diagnostics.toString()); this.diagnostics=new ArrayList<>(diagnostics); }
        public List<FormulaValidator.Diagnostic> diagnostics() { return List.copyOf(diagnostics); }
    }
}
