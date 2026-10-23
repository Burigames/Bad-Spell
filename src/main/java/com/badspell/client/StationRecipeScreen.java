package com.badspell.client;

import java.util.*;
import com.google.gson.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/** Read-only visual recipe guide generated from the shipped processing recipe JSON files. */
public final class StationRecipeScreen extends Screen {
    private final Screen parent;private String station;private int selected;private List<JsonObject> recipes=List.of();
    public StationRecipeScreen(Screen parent,String station){super(Component.translatable("ui.badspell.recipes"));this.parent=parent;this.station=station;}
    @Override protected void init(){var list=new ArrayList<JsonObject>();try(var reader=minecraft.getResourceManager().getResource(Identifier.fromNamespaceAndPath("badspell","guides/recipes.json")).orElseThrow().openAsReader()){for(var value:JsonParser.parseReader(reader).getAsJsonArray()){var recipe=value.getAsJsonObject();if(recipe.get("station").getAsString().equals(station))list.add(recipe);}}catch(java.io.IOException|NoSuchElementException error){org.slf4j.LoggerFactory.getLogger(StationRecipeScreen.class).warn("Cannot read Bad Spell recipe guide",error);}recipes=List.copyOf(list);selected=Math.clamp(selected,0,Math.max(0,recipes.size()-1));
        addRenderableWidget(new SpellButton(18,34,width-36,20,Component.translatable("block.badspell."+station),b->{var kinds=com.badspell.production.StationMenu.KINDS;station=kinds.get((kinds.indexOf(station)+1)%kinds.size());selected=0;rebuildWidgets();},false));
        addRenderableWidget(new SpellButton(18,height-29,30,18,Component.literal("<"),b->{selected=Math.max(0,selected-1);rebuildWidgets();},false));addRenderableWidget(new SpellButton(54,height-29,30,18,Component.literal(">"),b->{selected=Math.min(recipes.size()-1,selected+1);rebuildWidgets();},false));addRenderableWidget(new SpellButton(width-88,height-29,70,18,Component.translatable("ui.badspell.back"),b->onClose(),false));
    }
    private void item(GuiGraphicsExtractor g,String id,int count,int x,int y,int mx,int my,int labelWidth){var item=BuiltInRegistries.ITEM.getValue(Identifier.parse(id));var stack=new ItemStack(item);if(id.equals("minecraft:potion"))stack.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS,new net.minecraft.world.item.alchemy.PotionContents(net.minecraft.world.item.alchemy.Potions.WATER));g.fill(x-2,y-2,x+18,y+18,SpellTheme.CARD);g.item(stack,x,y);g.text(font,Integer.toString(count),x+10,y+10,SpellTheme.INK,false);g.text(font,font.plainSubstrByWidth(stack.getHoverName().getString(),labelWidth),x+23,y+4,SpellTheme.INK,false);if(mx>=x-2&&mx<x+24+labelWidth&&my>=y-2&&my<y+18)g.setTooltipForNextFrame(stack.getHoverName(),mx,my);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){g.fill(0,0,width,height,SpellTheme.BG);SpellTheme.panel(g,10,8,width-20,height-16);g.text(font,title,18,18,SpellTheme.INK,false);
        if(!recipes.isEmpty()){var r=recipes.get(selected);var inputs=r.getAsJsonArray("inputs");var outputs=r.getAsJsonArray("outputs");g.text(font,Component.translatable("ui.badspell.inputs"),24,67,SpellTheme.MUTED,false);g.text(font,Component.translatable("gui.badspell.outputs"),width/2+24,67,SpellTheme.MUTED,false);for(int i=0;i<inputs.size();i++){var entry=inputs.get(i).getAsJsonObject();String id=entry.get("ingredient").isJsonPrimitive()?entry.get("ingredient").getAsString():"minecraft:potion";item(g,id,entry.get("count").getAsInt(),26,84+i*22,mx,my,width/2-65);}g.text(font,"→",width/2-6,97,SpellTheme.ACCENT,false);for(int i=0;i<outputs.size();i++){var entry=outputs.get(i).getAsJsonObject();item(g,entry.get("id").getAsString(),entry.get("count").getAsInt(),width/2+24,84+i*22,mx,my,width/2-73);}g.text(font,Component.translatable("ui.badspell.recipe_cost",r.get("energy").getAsInt(),r.get("ticks").getAsInt()/20.0),24,height-46,SpellTheme.ACCENT,false);g.text(font,Component.literal((selected+1)+" / "+recipes.size()),100,height-24,SpellTheme.MUTED,false);}
        else {int y=72;for(var line:font.split(Component.translatable("station.badspell."+station+".help"),width-48)){g.text(font,line,24,y,SpellTheme.INK,false);y+=13;}}super.extractRenderState(g,mx,my,partial);}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
