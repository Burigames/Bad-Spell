package com.badspell.formula;

import java.util.*;

/** Reference graph, not a special-cased spell in the interpreter. */
public final class ExampleFormulas {
    private ExampleFormulas() {}
    public static final List<String> TEMPLATES=List.of("light","heal","push","heat","cold","shield","see","night_lamp","signature","self_light","frost_push","adaptive_ward");
    public static Formula template(String kind,UUID owner,Value.Position anchor) {
        if(!TEMPLATES.contains(kind)||kind.equals("light"))return light(owner);
        if(List.of("self_light","frost_push","adaptive_ward").contains(kind))return advanced(kind,owner);
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
    private static Formula advanced(String kind,UUID owner){
        var nodes=new ArrayList<Formula.Node>();var edges=new ArrayList<Formula.Edge>();
        class Graph {
            void node(String id,String rune,int x,int y,Map<String,Value> p){nodes.add(new Formula.Node(id,"badspell:"+rune,x,y,p));}
            void node(String id,String rune,int x,int y){node(id,rune,x,y,Map.of());}
            void wire(String from,String to,String port){edges.add(new Formula.Edge(from,to,port));}
        }
        var g=new Graph();g.node("impulse","impulse",0,0);boolean aimed=kind.equals("frost_push");g.node("actor",aimed?"target_entity":"self",0,100);if(aimed)g.wire("impulse","actor","trigger");g.node("position","entity_position",164,100);g.wire("actor","position","target");g.node("region",kind.equals("adaptive_ward")?"circle":"point",328,100,kind.equals("adaptive_ward")?Map.of("radius",new Value.Number(1)):Map.of());g.wire("position","region","position");String result;
        if(kind.equals("self_light")){g.node("effect","light",492,100,Map.of("strength",new Value.Number(5),"duration_ticks",new Value.Number(200)));g.wire("region","effect","region");result="effect";}
        else if(aimed){g.node("cold","cold",492,100,Map.of("strength",new Value.Number(1),"duration_ticks",new Value.Number(100)));g.wire("region","cold","region");g.node("direction","direction",164,220);g.node("push","push",328,220,Map.of("strength",new Value.Number(1)));g.wire("actor","push","target");g.wire("direction","push","vector");g.node("combine","combine",656,100);g.wire("cold","combine","first");g.wire("push","combine","second");g.node("available","has_target",492,260);g.node("none","none",656,260);g.node("choose","if_effect",820,100);g.wire("available","choose","condition");g.wire("combine","choose","yes");g.wire("none","choose","no");result="choose";}
        else {g.node("health","entity_health",164,220);g.wire("actor","health","target");g.node("threshold","number",164,320,Map.of("value",new Value.Number(12)));g.node("injured","greater",328,220);g.wire("threshold","injured","a");g.wire("health","injured","b");g.node("heal","heal",492,220,Map.of("points",new Value.Number(2)));g.wire("actor","heal","target");g.node("shield","shield",492,100,Map.of("strength",new Value.Number(1),"duration_ticks",new Value.Number(100)));g.wire("region","shield","region");g.node("choose","if_effect",656,100);g.wire("injured","choose","condition");g.wire("heal","choose","yes");g.wire("shield","choose","no");result="choose";}
        g.node("activate","activate",aimed?984:820,100);g.wire(result,"activate","effect");g.wire("impulse","activate","trigger");return new Formula(UUID.nameUUIDFromBytes(("badspell:"+kind).getBytes(java.nio.charset.StandardCharsets.UTF_8)),owner,1,switch(kind){case "self_light"->"Eigenlicht";case "frost_push"->"Froststoß";default->"Wachsamer Schutz";},nodes,edges,"activate",1000);
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
