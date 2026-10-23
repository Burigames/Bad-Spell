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
        return (Value.Effect)evaluate(formula.output(),nodes,inputs,new HashMap<>(),context,new int[]{0});
    }
    private Value evaluate(String id,Map<String,Formula.Node> nodes,Map<String,Map<String,String>> inputs,
                           Map<String,Value> cache,RuneRegistry.Context context,int[] operations) {
        if(cache.containsKey(id)) return cache.get(id);
        if(++operations[0]>MAX_OPERATIONS) throw new IllegalStateException("Tick operation budget exhausted");
        var node=nodes.get(id); var rune=registry.get(node.rune());
        Map<String,Value> values=new HashMap<>();
        Map<String,String> ports=inputs.getOrDefault(id,Map.of());
        if(node.rune().equals("badspell:if_effect")) {
            // Unselected branches do not evaluate, spend energy or request a world action.
            var condition=(Value.Bool)evaluate(ports.get("condition"),nodes,inputs,cache,context,operations);
            String selected=condition.value()?"yes":"no";
            Value result=evaluate(ports.get(selected),nodes,inputs,cache,context,operations);
            cache.put(id,result); return result;
        }
        for(var e:ports.entrySet()) values.put(e.getKey(),evaluate(e.getValue(),nodes,inputs,cache,context,operations));
        Value value=rune.operation().evaluate(Map.copyOf(values),node.parameters(),context);
        if(value==null || value.type()!=rune.output()) throw new IllegalStateException("Registered rune returned incorrect type: "+node.rune());
        cache.put(id,value); return value;
    }
    public static final class InvalidFormula extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
        private final ArrayList<FormulaValidator.Diagnostic> diagnostics;
        public InvalidFormula(List<FormulaValidator.Diagnostic> diagnostics) { super(diagnostics.toString()); this.diagnostics=new ArrayList<>(diagnostics); }
        public List<FormulaValidator.Diagnostic> diagnostics() { return List.copyOf(diagnostics); }
    }
}
