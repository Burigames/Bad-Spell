package com.badspell.formula;

import java.util.*;

/** Bounded, deterministic diagnostics; invalid graphs cannot reach evaluation. */
public final class FormulaValidator {
    public enum Code { LIMIT, DUPLICATE_NODE, UNKNOWN_RUNE, UNKNOWN_NODE, UNKNOWN_PORT,
        MISSING_PORT, DUPLICATE_PORT, PARAMETER, TYPE, CYCLE, OUTPUT, DISCONNECTED }
    public record Diagnostic(Code code, String node, String port, String message) implements java.io.Serializable {}
    private final RuneRegistry registry;
    public FormulaValidator(RuneRegistry registry) { this.registry=Objects.requireNonNull(registry); }
    public List<Diagnostic> validateDefinition(Formula formula){var output=formula.nodes().stream().filter(n->n.id().equals(formula.output())).findFirst();Value.Type type=Value.Type.EFFECT;if(output.isPresent()&&output.get().rune().startsWith("badspell:return_")){var rune=safe(output.get());if(rune!=null)type=rune.output();}return validate(formula,type);}
    public List<Diagnostic> validate(Formula formula) {return validate(formula,Value.Type.EFFECT);}
    public List<Diagnostic> validate(Formula formula,Value.Type expected) {
        var errors=new ArrayList<>(validateGraph(formula,expected));checkModules(formula,0,new HashSet<>(),new int[]{0},errors,"");return List.copyOf(errors);
    }
    private void checkModules(Formula formula,int depth,Set<UUID> active,int[] count,List<Diagnostic> errors,String anchor){
        if(depth>8||!active.add(formula.id())){add(errors,Code.LIMIT,anchor,"snapshot","Recursive module or depth above 8");return;}
        count[0]+=formula.nodes().size();if(count[0]>128){add(errors,Code.LIMIT,anchor,"snapshot","Expanded formula exceeds 128 nodes");active.remove(formula.id());return;}
        for(var node:formula.nodes())if(node.rune().equals("badspell:call"))try{
            var child=FormulaCalls.decode(node);String at=anchor.isBlank()?node.id():anchor;
            if(!child.owner().equals(formula.owner())){add(errors,Code.PARAMETER,at,"snapshot","Private module owner mismatch");continue;}
            var rune=safe(node);
            if(rune!=null)for(var error:validateGraph(child,rune.output()))add(errors,error.code(),at,error.node()+"/"+error.port(),error.message());
            count[0]--;checkModules(child,depth+1,active,count,errors,at);
        }catch(IllegalArgumentException error){add(errors,Code.PARAMETER,node.id(),"snapshot","Invalid module snapshot");}
        active.remove(formula.id());
    }
    private List<Diagnostic> validateGraph(Formula formula,Value.Type expected) {
        List<Diagnostic> errors=new ArrayList<>();
        if (formula.nodes().isEmpty() || formula.nodes().size()>128 || formula.edges().size()>512) {
            return List.of(new Diagnostic(Code.LIMIT,"","","Expected 1..128 nodes and at most 512 edges"));
        }
        Set<String> moduleInputs=new HashSet<>();
        Map<String,Formula.Node> nodes=new LinkedHashMap<>();
        for (var n:formula.nodes()) {
            if (nodes.putIfAbsent(n.id(),n)!=null) add(errors,Code.DUPLICATE_NODE,n.id(),"","Node id already exists");
            var rune=safe(n);
            if (rune==null) { add(errors,Code.UNKNOWN_RUNE,n.id(),"","Rune is not registered on this server"); continue; }
            for (var parameter:rune.parameters().entrySet()) {
                Value v=n.parameters().get(parameter.getKey());
                if (v==null || !parameter.getValue().accepts(v)) add(errors,Code.PARAMETER,n.id(),parameter.getKey(),"Missing, mistyped or out of bounds parameter");
            }
            if(n.rune().startsWith("badspell:input_")&&(!(n.parameters().get("name") instanceof Value.Text name)||!name.value().matches("[a-zA-Z][a-zA-Z0-9_]{0,31}")))add(errors,Code.PARAMETER,n.id(),"name","Input name must be a short identifier");
            if(n.rune().startsWith("badspell:input_")&&n.parameters().get("name") instanceof Value.Text name&&!moduleInputs.add(name.value()))add(errors,Code.PARAMETER,n.id(),"name","Duplicate module input name");
            if(n.rune().equals("badspell:target_entity_id")){try{UUID.fromString(((Value.Text)n.parameters().get("id")).value());}catch(IllegalArgumentException|ClassCastException|NullPointerException error){add(errors,Code.PARAMETER,n.id(),"id","Expected a complete entity UUID");}}
            for (var key:new TreeSet<>(n.parameters().keySet())) if (!rune.parameters().containsKey(key)) add(errors,Code.PARAMETER,n.id(),key,"Unknown parameter");
        }
        Map<String,Set<String>> connected=new HashMap<>();
        Map<String,List<String>> dependencies=new HashMap<>();
        for (var e:formula.edges()) {
            var from=nodes.get(e.from()); var to=nodes.get(e.to());
            if (from==null || to==null) { add(errors,Code.UNKNOWN_NODE,e.to(),e.port(),"Edge references a missing node"); continue; }
            dependencies.computeIfAbsent(e.to(),k->new ArrayList<>()).add(e.from());
            if (!connected.computeIfAbsent(e.to(),k->new HashSet<>()).add(e.port())) add(errors,Code.DUPLICATE_PORT,e.to(),e.port(),"Input already connected");
            var source=safe(from); var target=safe(to);
            if (source==null || target==null) continue;
            var type=target.inputs().get(e.port());
            if (type==null) add(errors,Code.UNKNOWN_PORT,e.to(),e.port(),"Rune has no such input");
            else if (type!=source.output()) add(errors,Code.TYPE,e.to(),e.port(),"Expected "+type+", received "+source.output());
        }
        for (var n:nodes.values()) {
            var rune=safe(n); if(rune==null) continue;
            for (String port:new TreeSet<>(rune.inputs().keySet())) if (!connected.getOrDefault(n.id(),Set.of()).contains(port)) add(errors,Code.MISSING_PORT,n.id(),port,"Connect the required input");
        }
        Map<String,Integer> state=new HashMap<>();
        for (String id:nodes.keySet()) visit(id,dependencies,state,errors);
        var output=nodes.get(formula.output());
        if (output==null || safe(output)==null || safe(output).output()!=expected) add(errors,Code.OUTPUT,formula.output(),"","Output must produce an effect plan");
        Set<String> reachable=new HashSet<>(); collect(formula.output(),dependencies,reachable);
        for (String id:nodes.keySet()) if (!reachable.contains(id)) add(errors,Code.DISCONNECTED,id,"","Node does not contribute to output");
        return List.copyOf(errors);
    }
    private RuneRegistry.Rune safe(Formula.Node n){try{return registry.get(n);}catch(IllegalArgumentException error){return null;}}
    private static void add(List<Diagnostic> e,Code c,String n,String p,String message) { e.add(new Diagnostic(c,n,p,message)); }
    private static void visit(String id,Map<String,List<String>> deps,Map<String,Integer> state,List<Diagnostic> errors) {
        int s=state.getOrDefault(id,0);
        if (s==1) { add(errors,Code.CYCLE,id,"","Cyclic rune graph; use a scheduled event between executions"); return; }
        if(s==2) return;
        state.put(id,1);
        for(String dependency:deps.getOrDefault(id,List.of())) visit(dependency,deps,state,errors);
        state.put(id,2);
    }
    private static void collect(String id,Map<String,List<String>> deps,Set<String> reachable) {
        if (!reachable.add(id)) return;
        for(String source:deps.getOrDefault(id,List.of())) collect(source,deps,reachable);
    }
}
