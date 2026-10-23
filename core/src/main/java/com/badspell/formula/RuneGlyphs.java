package com.badspell.formula;

import java.util.List;

/** Stable public symbol codes describe visible rune families, never parameters, edges or private snapshots. */
public final class RuneGlyphs {
    public static final List<String> FAMILIES=List.of("target","position","environment","impulse","light","heat","cold","push","shield","heal","see","point","ray","circle","sphere","path","equal","greater","and","or","not","if","delay","interval","limit","sign","bind","transfer","seal","check","activate","abort","measure","call","return","vector","input","none","direction","number","boolean","entity_position","entity_health","has_target","offset","add","subtract","multiply","combine");
    private RuneGlyphs(){}
    public static int code(String rune){String name=rune.substring(rune.indexOf(':')+1);if(name.startsWith("target_")||name.equals("self"))name="target";else if(name.startsWith("sign_"))name="sign";else if(name.startsWith("environment_"))name="environment";else if(name.startsWith("equal_"))name="equal";else if(name.startsWith("if_"))name="if";else if(name.startsWith("return_"))name="return";else if(name.startsWith("input_"))name="input";else if(name.startsWith("path_"))name="path";else if(name.equals("transfer_energy"))name="transfer";int index=FAMILIES.indexOf(name);if(index<0)throw new IllegalArgumentException("No public rune glyph: "+rune);return index+1;}
}
