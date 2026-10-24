package com.badspell.research;

import com.badspell.formula.*;
import java.util.*;

/** Knowledge, not levels. Pure rules shared by server validation and the visual workbench. */
public final class ResearchRules {
    private ResearchRules(){}
    public static final RuneRegistry RUNES=RuneRegistry.standard();
    public static final Set<String> BASICS=Set.of("badspell:impulse","badspell:target_position","badspell:position","badspell:point","badspell:light","badspell:activate","badspell:number","badspell:boolean");
    public record Family(String id,String sample){}
    public static final List<Family> FAMILIES=List.of(new Family("geometry","badspell:field_crystal_shard"),new Family("logic","minecraft:redstone"),new Family("thermal","badspell:moon_blossom"),new Family("vital","badspell:rune_resin"),new Family("motion","badspell:resonance_shard"),new Family("binding","badspell:arcane_ingot"));
    public static Family family(String id){
        String n=id.substring(id.indexOf(':')+1);
        if(Set.of("heat","cold","light","fire","melt","freeze").contains(n))return FAMILIES.get(2);
        if(Set.of("heal","harm","shield","self","target_entity","entity_position","entity_health","equal_entity","equal_item","item","item_count").contains(n))return FAMILIES.get(3);
        if(Set.of("push","pull","lift","vector","look","direction","teleport","transfer","rotate","vector_add","vector_scale").contains(n))return FAMILIES.get(4);
        if(Set.of("bind","unbind","sign","check","seal","unseal","origin","store","read","write").contains(n))return FAMILIES.get(5);
        var r=RUNES.get(id);
        if(r!=null&&(r.output()==Value.Type.REGION||r.output()==Value.Type.POSITION||r.output()==Value.Type.VECTOR))return FAMILIES.get(0);
        return FAMILIES.get(1);
    }
    public static Set<String> ids(){var ids=new TreeSet<>(RUNES.ids());ids.add("badspell:call");return Set.copyOf(ids);}
    public static RuneRegistry.Rune definition(String id){return id.equals("badspell:call")?new RuneRegistry.Rune(Map.of(),Map.of(),Value.Type.EFFECT,(i,p,c)->{throw new UnsupportedOperationException("Module signature depends on snapshot");}):RUNES.get(id);}
    public static List<String> ports(String id){return definition(id).inputs().keySet().stream().sorted().toList();}
    /** Stable slot order per seed + fragment. Registry contract is the solution, not client-supplied data. */
    public static List<String> slots(long seed,String id){var slots=new ArrayList<>(ports(id));Collections.shuffle(slots,new Random(seed^id.hashCode()*0x9E3779B97F4A7C15L));return List.copyOf(slots);}
    public static List<Value.Type> solution(long seed,String id){var r=Objects.requireNonNull(definition(id));var types=new ArrayList<Value.Type>();for(String port:slots(seed,id))types.add(r.inputs().get(port));types.add(r.output());return List.copyOf(types);}
    public static int fieldCost(String id){return Math.clamp(5+definition(id).inputs().size()*5,5,30);}
    public static int inkCost(String id){return Math.clamp(1+definition(id).inputs().size(),1,4);}
    public static Set<String> used(Formula formula){var used=new TreeSet<String>();collect(formula,used,0,new int[1]);return Set.copyOf(used);}
    private static void collect(Formula formula,Set<String> used,int depth,int[] count){
        if(depth>8)throw new IllegalArgumentException("Module depth");
        for(var node:formula.nodes()){
            if(++count[0]>1024)throw new IllegalArgumentException("Research graph limit");
            if(node.rune().equals("badspell:call")){used.add(node.rune());collect(FormulaCalls.decode(node),used,depth+1,count);}
            else {if(RUNES.get(node.rune())==null)throw new IllegalArgumentException("Unknown rune");used.add(node.rune());}
        }
    }
    public static Set<String> missing(Formula formula,Set<String> known){var result=new TreeSet<>(used(formula));result.removeAll(known);return Set.copyOf(result);}
    public static int tier(Set<String> discovered,Set<String> stations,int decoded,boolean formula){
        if(decoded>=5&&stations.contains("arcane_smelter")&&discovered.contains("badspell:crystal_core"))return 2;
        if(formula&&stations.containsAll(Set.of("arcane_workbench","crystal_grinder")))return 1;
        return 0;
    }
}
