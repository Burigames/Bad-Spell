package com.badspell.formula;

import java.util.*;

/** Expands immutable, explicitly supplied module snapshots into an ordinary typed graph.
 * Modules currently expose one EFFECT output and bind parameters to constants.
 * A server must only supply module snapshots the actor may copy.
 */
public final class FormulaModules {
    public record ParameterAddress(String node,String parameter) {}
    public record Use(UUID module,Map<ParameterAddress,Value> bindings) {
        public Use { Objects.requireNonNull(module); bindings=Map.copyOf(bindings); }
    }
    public record Definition(FormulaSnapshot snapshot,Map<String,Use> calls) {
        public Definition { Objects.requireNonNull(snapshot); calls=Map.copyOf(calls); }
    }
    private FormulaModules() {}
    public static Formula expand(Definition root,Map<UUID,Definition> library) {
        var state=new Expansion(library);
        String output=state.append(root,Map.of(),0,new HashSet<>(),0,0);
        var f=root.snapshot().formula();
        return new Formula(f.id(),f.owner(),f.revision(),f.title(),state.nodes,state.edges,output,f.energyBudget());
    }
    private static final class Expansion {
        private final Map<UUID,Definition> library;
        private final List<Formula.Node> nodes=new ArrayList<>();
        private final List<Formula.Edge> edges=new ArrayList<>();
        Expansion(Map<UUID,Definition> library) { this.library=Map.copyOf(library); }
        String append(Definition def,Map<ParameterAddress,Value> bindings,int depth,Set<UUID> active,int offsetX,int offsetY) {
            if(depth>8) throw new IllegalArgumentException("Maximum module depth is 8");
            var f=def.snapshot().formula();
            if(!active.add(f.id())) throw new IllegalArgumentException("Recursive module call: "+f.id());
            if(f.nodes().size()>128 || f.edges().size()>512) throw new IllegalArgumentException("Module graph limit");
            Map<String,String> names=new HashMap<>(); Map<String,Formula.Node> source=new HashMap<>();
            for(var n:f.nodes()) if(source.putIfAbsent(n.id(),n)!=null) throw new IllegalArgumentException("Duplicate module node");
            for(var b:bindings.keySet()) if(!source.containsKey(b.node()) || !source.get(b.node()).parameters().containsKey(b.parameter())) throw new IllegalArgumentException("Unknown module parameter: "+b);
            for(var call:def.calls().keySet()) if(!source.containsKey(call) || !source.get(call).rune().equals("badspell:call")) throw new IllegalArgumentException("Invalid module call mapping");
            for(var n:f.nodes()) {
                if(n.rune().equals("badspell:call")) {
                    Use use=def.calls().get(n.id());
                    if(use==null || !n.parameters().isEmpty()) throw new IllegalArgumentException("Module call must have an explicit snapshot and bindings");
                    var child=library.get(use.module());
                    if(child==null || !child.snapshot().formula().id().equals(use.module())) throw new IllegalArgumentException("Missing module snapshot");
                    names.put(n.id(),append(child,use.bindings(),depth+1,active,Math.addExact(offsetX,n.x()),Math.addExact(offsetY,n.y())));
                } else {
                    if(nodes.size()>=128) throw new IllegalArgumentException("Expanded formula exceeds 128 nodes");
                    String id="n"+nodes.size(); names.put(n.id(),id);
                    var parameters=new HashMap<>(n.parameters());
                    bindings.forEach((address,value)-> { if(address.node().equals(n.id())) parameters.put(address.parameter(),value); });
                    nodes.add(new Formula.Node(id,n.rune(),Math.addExact(offsetX,n.x()),Math.addExact(offsetY,n.y()),parameters));
                }
            }
            for(var e:f.edges()) {
                if(!names.containsKey(e.from()) || !names.containsKey(e.to())) throw new IllegalArgumentException("Module edge has missing node");
                if(def.calls().containsKey(e.to())) throw new IllegalArgumentException("Current modules accept constant bindings, not incoming runtime ports");
                edges.add(new Formula.Edge(names.get(e.from()),names.get(e.to()),e.port()));
                if(edges.size()>512) throw new IllegalArgumentException("Expanded edge budget exceeded");
            }
            active.remove(f.id());
            String output=names.get(f.output()); if(output==null) throw new IllegalArgumentException("Missing module output");
            return output;
        }
    }
}
