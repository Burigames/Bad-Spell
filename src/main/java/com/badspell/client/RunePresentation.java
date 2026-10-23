package com.badspell.client;

import com.badspell.formula.*;
import java.util.*;
import net.minecraft.network.chat.Component;

/** Rune contracts always come from the real registry; prose is localization, never a second API. */
public final class RunePresentation {
    private RunePresentation(){}
    public static Component name(String id){return Component.translatable("rune.badspell."+id.substring(id.indexOf(':')+1));}
    public static Component type(Value.Type type){return Component.translatable("type.badspell."+type.name().toLowerCase(Locale.ROOT));}
    public static Component port(String key){var result=Component.translatable("port.badspell."+key);return result.getString().equals("port.badspell."+key)?Component.literal(key):result;}
    public static Component description(String id){String key=id.substring(id.indexOf(':')+1);if(key.startsWith("if_"))key="if";else if(key.startsWith("equal_"))key="equal";else if(key.startsWith("input_"))key="input";else if(key.startsWith("return_"))key="return";return Component.translatable("help.badspell."+key);}
    public static String contract(RuneRegistry.Rune rune){String inputs=rune.inputs().isEmpty()?Component.translatable("ui.badspell.no_inputs").getString():String.join(", ",new TreeMap<>(rune.inputs()).entrySet().stream().map(e->port(e.getKey()).getString()+": "+type(e.getValue()).getString()).toList());return inputs+" → "+type(rune.output()).getString();}
    public static boolean matches(String id,RuneRegistry.Rune rune,String query){String corpus=(id+" "+name(id).getString()+" "+description(id).getString()+" "+contract(rune)+" "+rune.inputs()+" "+rune.output()).toLowerCase(Locale.ROOT);for(String term:query.toLowerCase(Locale.ROOT).trim().split("\\s+"))if(!corpus.contains(term))return false;return true;}
    public static int relevance(String id,RuneRegistry.Rune rune,String query){String q=query.trim().toLowerCase(Locale.ROOT);if(q.isEmpty())return 0;if(id.replace('_',' ').contains(q)||name(id).getString().toLowerCase(Locale.ROOT).contains(q))return 0;if(rune.output().name().equalsIgnoreCase(q)||type(rune.output()).getString().equalsIgnoreCase(q))return 1;if(rune.inputs().values().stream().anyMatch(t->t.name().equalsIgnoreCase(q)||type(t).getString().equalsIgnoreCase(q)))return 2;return 3;}
    public static int color(Value.Type type){return switch(type){case EVENT->0xffa27732;case ENTITY->0xffa34d65;case POSITION->0xff357985;case VECTOR->0xffac6942;case REGION->0xff637a41;case EFFECT->0xff376e67;case NUMBER->0xff65629a;case BOOLEAN->0xff967832;case TEXT->0xff84557f;};}
}
