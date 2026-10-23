package com.badspell.formula;

import java.util.*;

/** Reference graph, not a special-cased spell in the interpreter. */
public final class ExampleFormulas {
    private ExampleFormulas() {}
    public static final List<String> TEMPLATES=List.of("light","heal","push","heat","cold","shield","see","night_lamp","signature");
    public static Formula template(String kind,UUID owner,Value.Position anchor) {
        if(!TEMPLATES.contains(kind)||kind.equals("light"))return light(owner);
        var nodes=new ArrayList<Formula.Node>();var edges=new ArrayList<Formula.Edge>();
        boolean entity=List.of("heal","push","see").contains(kind);boolean interval=kind.equals("night_lamp");
        nodes.add(new Formula.Node("impulse",interval?"badspell:interval":"badspell:impulse",0,0,interval?Map.of("ticks",new Value.Number(40)):Map.of()));
        nodes.add(new Formula.Node("target",kind.equals("heal")?"badspell:self":entity?"badspell:target_entity":"badspell:target_position",154,0,Map.of()));
        if(!kind.equals("heal"))edges.add(new Formula.Edge("impulse","target","trigger"));
        String result="effect";
        if(entity){
            Map<String,Value> parameters=kind.equals("heal")?Map.of("points",new Value.Number(2)):kind.equals("push")?Map.of("strength",new Value.Number(1)):Map.of();
            nodes.add(new Formula.Node("effect","badspell:"+kind,308,0,parameters));edges.add(new Formula.Edge("target","effect","target"));
            if(kind.equals("push")){nodes.add(new Formula.Node("vector","badspell:vector",154,100,Map.of("x",new Value.Number(0),"y",new Value.Number(600),"z",new Value.Number(300))));edges.add(new Formula.Edge("vector","effect","vector"));}
        }else{
            nodes.add(new Formula.Node("point",kind.equals("shield")?"badspell:circle":"badspell:point",0,100,kind.equals("shield")?Map.of("radius",new Value.Number(2)):Map.of()));edges.add(new Formula.Edge("target","point","position"));
            String effect=interval||kind.equals("signature")?"light":kind;nodes.add(new Formula.Node("effect","badspell:"+effect,154,100,Map.of("strength",new Value.Number(interval?5:kind.equals("signature")?4:1),"duration_ticks",new Value.Number(interval?40:100))));edges.add(new Formula.Edge("point","effect","region"));
            if(interval){nodes.add(new Formula.Node("brightness","badspell:environment_light",0,200,Map.of()));nodes.add(new Formula.Node("threshold","badspell:number",154,200,Map.of("value",new Value.Number(7))));nodes.add(new Formula.Node("bright","badspell:greater",308,200,Map.of()));nodes.add(new Formula.Node("dark","badspell:not",462,200,Map.of()));nodes.add(new Formula.Node("nothing","badspell:none",308,100,Map.of()));nodes.add(new Formula.Node("choose","badspell:if_effect",462,100,Map.of()));edges.addAll(List.of(new Formula.Edge("brightness","bright","a"),new Formula.Edge("threshold","bright","b"),new Formula.Edge("bright","dark","value"),new Formula.Edge("dark","choose","condition"),new Formula.Edge("effect","choose","yes"),new Formula.Edge("nothing","choose","no")));result="choose";}
            if(kind.equals("signature")){nodes.add(new Formula.Node("anchor","badspell:position",0,200,Map.of("value",anchor)));nodes.add(new Formula.Node("sign_a","badspell:sign",154,200,Map.of("frequency",new Value.Number(1))));nodes.add(new Formula.Node("sign_b","badspell:sign",308,200,Map.of("frequency",new Value.Number(1))));nodes.add(new Formula.Node("match","badspell:equal_text",462,200,Map.of()));nodes.add(new Formula.Node("dim","badspell:light",308,100,Map.of("strength",new Value.Number(1),"duration_ticks",new Value.Number(100))));nodes.add(new Formula.Node("choose","badspell:if_effect",462,100,Map.of()));edges.addAll(List.of(new Formula.Edge("target","sign_a","position"),new Formula.Edge("anchor","sign_b","position"),new Formula.Edge("sign_a","match","a"),new Formula.Edge("sign_b","match","b"),new Formula.Edge("point","dim","region"),new Formula.Edge("match","choose","condition"),new Formula.Edge("effect","choose","yes"),new Formula.Edge("dim","choose","no")));result="choose";}
        }
        nodes.add(new Formula.Node("activate","badspell:activate",0,300,Map.of()));edges.add(new Formula.Edge(result,"activate","effect"));edges.add(new Formula.Edge("impulse","activate","trigger"));
        String title=switch(kind){case "heal"->"Behutsame Heilung";case "push"->"Behutsamer Schub";case "heat"->"Wärmefeld";case "cold"->"Kältefeld";case "shield"->"Schutzkreis";case "see"->"Feldblick";case "night_lamp"->"Nachtlampe";case "signature"->"Signaturlicht";default->kind;};
        return new Formula(UUID.nameUUIDFromBytes(("badspell:"+kind).getBytes(java.nio.charset.StandardCharsets.UTF_8)),owner,1,title,nodes,edges,"activate",1000);
    }
    public static Formula light(UUID owner) {
        return new Formula(UUID.fromString("919339fc-53d3-4215-a960-c3a03a201001"),owner,1,"Lichtfunke",List.of(
            new Formula.Node("impulse","badspell:impulse",0,0,Map.of()),
            new Formula.Node("target","badspell:target_position",140,0,Map.of()),
            new Formula.Node("point","badspell:point",280,0,Map.of()),
            new Formula.Node("light","badspell:light",420,0,Map.of("strength",new Value.Number(3),"duration_ticks",new Value.Number(600))),
            new Formula.Node("activate","badspell:activate",560,0,Map.of())),List.of(
            new Formula.Edge("impulse","target","trigger"),
            new Formula.Edge("target","point","position"),
            new Formula.Edge("point","light","region"),
            new Formula.Edge("light","activate","effect"),
            new Formula.Edge("impulse","activate","trigger")),"activate",100);
    }
}
