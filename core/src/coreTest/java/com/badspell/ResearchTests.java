package com.badspell;

import com.badspell.formula.*;
import com.badspell.research.ResearchRules;
import java.util.*;

public final class ResearchTests {
    private ResearchTests(){}
    private static void check(boolean yes){if(!yes)throw new AssertionError("Research invariant");}
    public static int run() throws Exception {
        int tests=0;
        for(String id:ResearchRules.ids()){
            var def=ResearchRules.definition(id);var answer=ResearchRules.solution(17,id);check(answer.size()==def.inputs().size()+1);check(answer.getLast()==def.output());
            check(ResearchRules.solution(17,id).equals(answer));check(ResearchRules.slots(17,id).equals(ResearchRules.slots(17,id)));
            check(ResearchRules.fieldCost(id)>=5&&ResearchRules.fieldCost(id)<=30);check(ResearchRules.inkCost(id)>=1&&ResearchRules.inkCost(id)<=4);
            check(ResearchRules.FAMILIES.contains(ResearchRules.family(id)));
        }tests++;System.out.println("PASS M2 every registered rune has a stable bounded analysis");
        var f=ExampleFormulas.light(UUID.randomUUID());check(ResearchRules.missing(f,ResearchRules.BASICS).isEmpty());tests++;
        var child=ExampleFormulas.template("frost_push",f.owner(),new Value.Position("minecraft:overworld",0,64,0));
        var call=FormulaCalls.call("call",child,0,0);var nested=new Formula(UUID.randomUUID(),f.owner(),1,"nested",List.of(call),List.of(),"call",1000);
        var missing=ResearchRules.missing(nested,ResearchRules.BASICS);check(missing.contains("badspell:call")&&missing.contains("badspell:cold"));tests++;
        var known=new HashSet<>(ResearchRules.used(nested));check(ResearchRules.missing(nested,known).isEmpty());check(known.equals(ResearchRules.used(nested)));tests++;
        var deep=nested;for(int i=0;i<10;i++)deep=new Formula(UUID.randomUUID(),f.owner(),1,"deep",List.of(FormulaCalls.call("call",deep,0,0)),List.of(),"call",1000);
        boolean rejected=false;try{ResearchRules.used(deep);}catch(IllegalArgumentException e){rejected=true;}check(rejected);tests++;
        check(ResearchRules.tier(Set.of(),Set.of(),100,true)==0);check(ResearchRules.tier(Set.of(),Set.of("arcane_workbench","crystal_grinder"),0,true)==1);check(ResearchRules.tier(Set.of("badspell:crystal_core"),Set.of("arcane_smelter"),5,false)==2);check(ResearchRules.tier(Set.of("badspell:crystal_core"),Set.of("arcane_smelter"),4,false)==0);tests++;
        System.out.println("PASS M2 knowledge, nested-module and proof rules ("+tests+" checks)");return tests;
    }
}
