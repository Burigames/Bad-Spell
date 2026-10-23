package com.badspell.formula;

import java.io.IOException;
import java.util.*;

/** Embedded immutable module snapshots expose explicitly named, typed input runes. */
public final class FormulaCalls {
    private FormulaCalls(){}
    public static Formula.Node call(String id,Formula module,int x,int y) throws IOException {
        return new Formula.Node(id,"badspell:call",x,y,Map.of("snapshot",new Value.Text(Base64.getEncoder().encodeToString(FormulaCodec.encode(module)))));
    }
    public static Formula decode(Formula.Node node) {
        try {
            if(!(node.parameters().get("snapshot") instanceof Value.Text text)||text.value().length()>40000)throw new IllegalArgumentException("Missing module snapshot");
            return FormulaCodec.decode(Base64.getDecoder().decode(text.value()));
        }catch(IOException error){throw new IllegalArgumentException("Invalid module snapshot",error);}
    }
    public static RuneRegistry.Rune rune(Formula.Node node) {return rune(node,0);}
    private static RuneRegistry.Rune rune(Formula.Node node,int depth) {
        if(depth>8)throw new IllegalArgumentException("Module depth above 8");
        var module=decode(node);Map<String,Value.Type> inputs=new TreeMap<>();
        for(var n:module.nodes())if(n.rune().startsWith("badspell:input_")) {
            if(!(n.parameters().get("name") instanceof Value.Text name)||!name.value().matches("[a-zA-Z][a-zA-Z0-9_]{0,31}"))throw new IllegalArgumentException("Invalid input name");
            Value.Type type=switch(n.rune().substring("badspell:input_".length())){case "number"->Value.Type.NUMBER;case "boolean"->Value.Type.BOOLEAN;case "position"->Value.Type.POSITION;case "vector"->Value.Type.VECTOR;default->throw new IllegalArgumentException("Unknown module input");};
            if(inputs.putIfAbsent(name.value(),type)!=null)throw new IllegalArgumentException("Duplicate module input");
        }
        var output=module.nodes().stream().filter(n->n.id().equals(module.output())).findFirst().orElseThrow(()->new IllegalArgumentException("Module output missing"));
        var result=output.rune().equals("badspell:call")?rune(output,depth+1):RuneRegistry.standard().get(output.rune());if(result==null)throw new IllegalArgumentException("Unknown module output");
        return new RuneRegistry.Rune(inputs,Map.of("snapshot",new RuneRegistry.Parameter(Value.Type.TEXT,0,0)),result.output(),(i,p,c)->{throw new IllegalStateException("Module requires interpreter");});
    }
}
