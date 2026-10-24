package com.badspell.client;

import com.google.gson.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;
import com.badspell.content.ModItems;

/** Generated from all shipped recipes: discovery masking, alternatives and bounded dependency expansion. */
public final class CraftingPathsScreen extends Screen {
    private final Screen parent;private List<JsonObject> all=List.of(),found=List.of();private String query="",station="";private boolean ready;private int tier=-1,time=-1,selected,offset,scroll,route;
    public CraftingPathsScreen(Screen parent){super(ResearchScreen.tr("paths"));this.parent=parent;}
    private boolean discovered(String id){return minecraft.player!=null&&minecraft.player.isCreative()||ClientResearch.state.discovered().contains(id);}
    private boolean visible(JsonObject r){return java.util.stream.Stream.concat(r.getAsJsonArray("inputs").asList().stream(),r.getAsJsonArray("outputs").asList().stream()).anyMatch(e->discovered(e.getAsJsonObject().get("id").getAsString()));}
    private String itemName(String id){return discovered(id)?new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(id))).getHoverName().getString():ResearchScreen.tr("category."+category(id)).getString();}
    private String category(String id){if(id.contains("glove")||id.contains("book")||id.contains("compass")||id.contains("fork")||id.contains("chisel")||id.contains("monocle"))return "tool";if(id.contains("ore")||id.contains("ingot")||id.contains("alloy"))return "metal";if(id.contains("crystal")||id.contains("shard")||id.contains("core"))return "crystal";if(id.contains("water")||id.contains("brew")||id.contains("solution")||id.contains("potion"))return "liquid";if(id.contains("wood")||id.contains("planks")||id.contains("log"))return "wood";return "component";}
    private String result(JsonObject r){return r.getAsJsonArray("outputs").get(0).getAsJsonObject().get("id").getAsString();}
    private int available(String id,boolean water){int n=0;for(int i=0;i<minecraft.player.getInventory().getContainerSize();i++){var s=minecraft.player.getInventory().getItem(i);if(!BuiltInRegistries.ITEM.getKey(s.getItem()).toString().equals(id))continue;if(water&&!s.getOrDefault(net.minecraft.core.component.DataComponents.POTION_CONTENTS,net.minecraft.world.item.alchemy.PotionContents.EMPTY).is(net.minecraft.world.item.alchemy.Potions.WATER))continue;n+=s.getCount();}return n;}
    private int fl(){int n=0;for(int i=0;i<minecraft.player.getInventory().getContainerSize();i++){var s=minecraft.player.getInventory().getItem(i);int cap=ModItems.cellCapacity(s);if(cap>0)n+=Math.clamp(s.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getIntOr("badspell_fl",cap),0,cap);}return n;}
    private boolean ready(JsonObject r){for(var entry:r.getAsJsonArray("inputs")){var i=entry.getAsJsonObject();if(available(i.get("id").getAsString(),i.has("water"))<i.get("count").getAsInt())return false;}return fl()>=r.get("energy").getAsInt();}
    private int rows(){return Math.max(1,(height-116)/23);}
    private int split(){return Math.max(140,width/3);}
    @Override protected void init(){if(all.isEmpty())try(var reader=minecraft.getResourceManager().getResource(Identifier.fromNamespaceAndPath("badspell","guides/paths.json")).orElseThrow().openAsReader()){all=JsonParser.parseReader(reader).getAsJsonArray().asList().stream().map(JsonElement::getAsJsonObject).toList();}catch(java.io.IOException|NoSuchElementException e){org.slf4j.LoggerFactory.getLogger(getClass()).warn("Cannot load crafting paths",e);}
        found=all.stream().filter(this::visible).filter(r->station.isEmpty()||r.get("station").getAsString().equals(station)).filter(r->tier<0||r.get("tier").getAsInt()==tier).filter(r->time<0||r.get("ticks").getAsInt()<=time).filter(r->!ready||ready(r)).filter(r->{String text=itemName(result(r));for(var i:r.getAsJsonArray("inputs"))text+=" "+itemName(i.getAsJsonObject().get("id").getAsString());return text.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));}).toList();
        selected=Math.clamp(selected,0,Math.max(0,found.size()-1));offset=Math.clamp(offset,0,Math.max(0,found.size()-rows()));
        var search=addRenderableWidget(new EditBox(font,17,34,width-118,18,ResearchScreen.tr("search")));search.setValue(query);search.setMaxLength(80);search.setHint(ResearchScreen.tr("path_search"));setInitialFocus(search);search.setResponder(s->{query=s;selected=offset=scroll=0;rebuildWidgets();});
        addRenderableWidget(new SpellButton(width-95,34,78,18,ResearchScreen.tr(ready?"ready_on":"ready_off"),b->{ready=!ready;selected=offset=scroll=0;rebuildWidgets();},ready)).setTooltip(net.minecraft.client.gui.components.Tooltip.create(ResearchScreen.tr("stock_help")));
        int w=(width-46)/3;
        addRenderableWidget(new SpellButton(17,57,w,19,station.isEmpty()?ResearchScreen.tr("all_stations"):Component.translatable("block."+(station.equals("furnace")||station.equals("crafting_table")?"minecraft.":"badspell.")+station),b->{var stations=new ArrayList<String>();stations.add("");all.stream().map(r->r.get("station").getAsString()).distinct().sorted().forEach(stations::add);station=stations.get((stations.indexOf(station)+1)%stations.size());selected=offset=scroll=0;rebuildWidgets();},false));
        addRenderableWidget(new SpellButton(23+w,57,w,19,tier<0?ResearchScreen.tr("all_tiers"):ResearchScreen.tr("tier",tier),b->{tier=tier==2?-1:tier+1;selected=offset=scroll=0;rebuildWidgets();},false));
        addRenderableWidget(new SpellButton(29+w*2,57,w,19,time<0?ResearchScreen.tr("all_times"):ResearchScreen.tr("max_seconds",time/20),b->{time=time<0?100:time==100?200:-1;selected=offset=scroll=0;rebuildWidgets();},false));
        for(int i=0;i<rows()&&i+offset<found.size();i++){int at=i+offset;var r=found.get(at);addRenderableWidget(new SpellButton(17,83+i*23,split()-26,21,Component.literal(font.plainSubstrByWidth(itemName(result(r)),split()-36)),b->{selected=at;scroll=0;rebuildWidgets();},selected==at));}
        addRenderableWidget(new SpellButton(split()+8,height-26,Math.max(60,width-split()-108),18,ResearchScreen.tr("route",route+1),b->{route=(route+1)%8;scroll=0;},false));
        addRenderableWidget(new SpellButton(width-87,height-26,70,18,Component.translatable("ui.badspell.back"),b->onClose(),false));
    }
    private record Line(int indent,String id,String suffix,boolean item){}
    private void tree(List<Line> lines,JsonObject r,int depth,Set<String> path,int batches){if(depth>10||lines.size()>180)return;String recipe=r.get("id").getAsString();if(!path.add(recipe)){lines.add(new Line(depth,"",ResearchScreen.tr("cycle").getString(),false));return;}
        lines.add(new Line(depth,"",Component.translatable("block."+(r.get("station").getAsString().equals("furnace")||r.get("station").getAsString().equals("crafting_table")?"minecraft.":"badspell.")+r.get("station").getAsString()).getString()+" · "+r.get("energy").getAsInt()*batches+" FL · "+r.get("ticks").getAsInt()*batches/20.0+" s",false));
        for(var e:r.getAsJsonArray("inputs")){var i=e.getAsJsonObject();String id=i.get("id").getAsString();lines.add(new Line(depth,id," × "+i.get("count").getAsInt()*batches,true));var alternatives=all.stream().filter(a->!a.get("id").getAsString().equals(recipe)&&a.getAsJsonArray("outputs").asList().stream().anyMatch(o->o.getAsJsonObject().get("id").getAsString().equals(id))).toList();if(!alternatives.isEmpty()&&depth<10){if(alternatives.size()>1)lines.add(new Line(depth+1,"",ResearchScreen.tr("alternatives",alternatives.size()).getString(),false));var choices=alternatives.stream().filter(a->!path.contains(a.get("id").getAsString())).sorted(Comparator.comparingInt(a->a.get("energy").getAsInt())).toList();var next=choices.isEmpty()?null:choices.get(route%choices.size());if(next!=null){int yield=next.getAsJsonArray("outputs").asList().stream().map(JsonElement::getAsJsonObject).filter(o->o.get("id").getAsString().equals(id)).mapToInt(o->o.get("count").getAsInt()).sum();tree(lines,next,depth+1,new HashSet<>(path),Math.ceilDiv(i.get("count").getAsInt()*batches,yield));}}}
        if(depth==0)for(var e:r.getAsJsonArray("outputs")){var o=e.getAsJsonObject();lines.add(new Line(0,o.get("id").getAsString()," × "+o.get("count").getAsInt()*batches+" →",true));}}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){g.fill(0,0,width,height,SpellTheme.BG);SpellTheme.panel(g,9,8,width-18,height-16);g.text(font,title,17,18,SpellTheme.INK,false);
        if(!found.isEmpty()){var lines=new ArrayList<Line>();tree(lines,found.get(selected),0,new HashSet<>(),1);int x=split()+8,y=88-scroll,maxWidth=width-x-20;g.enableScissor(x,82,width-17,height-33);
            for(var line:lines){int at=x+Math.min(line.indent(),5)*9;if(line.item()){if(discovered(line.id()))g.item(new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(line.id()))),at,y-3);else g.text(font,"?",at+4,y,SpellTheme.MUTED,false);g.text(font,font.plainSubstrByWidth(itemName(line.id())+line.suffix(),Math.max(10,maxWidth-(at-x)-23)),at+23,y+1,SpellTheme.INK,false);}else g.text(font,font.plainSubstrByWidth(line.suffix(),maxWidth-(at-x)),at,y+1,SpellTheme.ACCENT,false);y+=22;}
            g.disableScissor();scroll=Math.clamp(scroll,0,Math.max(0,lines.size()*22-(height-122)));}
        g.text(font,ResearchScreen.tr("path_count",found.size()),17,height-21,SpellTheme.MUTED,false);super.extractRenderState(g,mx,my,partial);}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){if(x<split()){offset=Math.clamp(offset-(int)Math.signum(sy),0,Math.max(0,found.size()-rows()));rebuildWidgets();}else scroll=Math.max(0,scroll-(int)(sy*22));return true;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
