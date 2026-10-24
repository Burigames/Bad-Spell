package com.badspell.client;

import com.badspell.research.*;
import com.badspell.network.Packets;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Uncluttered journal: overview, rune discoveries, materials and actual recipe paths. */
public final class ResearchScreen extends Screen {
    private final Screen parent;private boolean revealSelection;private int tab,offset;private String selected="badspell:circle",query="";private List<String> found=List.of();
    public ResearchScreen(Screen parent){this(parent,true);}
    public ResearchScreen(Screen parent,boolean request){this(parent,request,"");}
    public ResearchScreen(Screen parent,boolean request,String focus){super(tr("title"));this.parent=parent;if(ResearchRules.ids().contains(focus)){tab=1;selected=focus;revealSelection=true;}if(request)ClientResearch.request(false);}
    public static Component tr(String key,Object...args){return Component.translatable("research.badspell."+key,args);}
    public void refresh(){rebuildWidgets();}
    private int rows(){return Math.max(1,(height-104)/23);}
    private int split(){return Math.max(130,width/3);}
    private void action(int action){ClientPacketDistributor.sendToServer(new Packets.ResearchAction(action,selected,List.of(),false));}
    @Override protected void init(){
        String[] tabs={"overview","runes","materials"};for(int i=0;i<tabs.length;i++){int at=i;addRenderableWidget(new SpellButton(16+i*84,33,80,19,tr(tabs[i]),b->{tab=at;query="";offset=0;revealSelection=at==1;rebuildWidgets();},tab==i));}
        addRenderableWidget(new SpellButton(width-94,33,78,19,tr("paths_short"),b->minecraft.gui.setScreen(new CraftingPathsScreen(this)),false));
        addRenderableWidget(new SpellButton(width-86,height-26,70,18,Component.translatable("ui.badspell.back"),b->onClose(),false));
        if(tab==0)return;
        found=(tab==1?ResearchRules.ids().stream():ClientResearch.state.discovered().stream()).filter(id->(id+" "+name(id).getString()).toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))).sorted(Comparator.comparing(id->name(id).getString())).toList();
        if(revealSelection){offset=Math.max(0,found.indexOf(selected));revealSelection=false;}offset=Math.clamp(offset,0,Math.max(0,found.size()-rows()));
        var search=addRenderableWidget(new EditBox(font,17,60,split()-27,18,tr("search")));search.setMaxLength(80);search.setValue(query);search.setHint(tr("search"));setInitialFocus(search);search.setResponder(s->{query=s;offset=0;rebuildWidgets();});
        for(int i=0;i<rows()&&i+offset<found.size();i++){String id=found.get(i+offset);addRenderableWidget(new SpellButton(17,84+i*23,split()-27,21,Component.literal((tab==1?(ClientResearch.knows(id)?"✓ ":"◇ "):"")+font.plainSubstrByWidth(name(id).getString(),split()-48)),b->{selected=id;rebuildWidgets();},selected.equals(id)));}
        if(tab==1&&ResearchRules.ids().contains(selected)){
            int x=split()+12,w=width-x-18;
            var prepare=addRenderableWidget(new SpellButton(x,height-72,w,19,tr(ClientResearch.knows(selected)?"write_lesson":"prepare"),b->action(ClientResearch.knows(selected)?3:1),false));
            prepare.setTooltip(Tooltip.create(tr(ClientResearch.knows(selected)?"lesson_cost":"prepare_cost",new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(ResearchRules.family(selected).sample()))).getHoverName())));
            if(!ClientResearch.knows(selected))addRenderableWidget(new SpellButton(x,height-49,w,19,tr("analyze"),b->minecraft.gui.setScreen(new ResearchAnalysisScreen(this,selected)),true));
        }
    }
    private Component name(String id){return tab==1?RunePresentation.name(id):new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(id))).getHoverName();}
    private int paragraph(GuiGraphicsExtractor g,Component c,int x,int y,int w,int color){for(var line:font.split(c,w)){g.text(font,line,x,y,color,false);y+=12;}return y+5;}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){g.fill(0,0,width,height,SpellTheme.BG);SpellTheme.panel(g,9,8,width-18,height-16);g.text(font,title,17,18,SpellTheme.INK,false);g.text(font,ClientResearch.state.known().size()+" / "+ResearchRules.ids().size(),width-87,18,SpellTheme.ACCENT,false);
        if(tab==0){int x=24,w=width-48,y=66;var s=ClientResearch.state;int tier=ResearchRules.tier(Set.copyOf(s.discovered()),Set.copyOf(s.stations()),s.decoded(),s.formula());y=paragraph(g,tr("tier",tier),x,y,w,SpellTheme.ACCENT);y=paragraph(g,tr("overview_body"),x,y,w,SpellTheme.INK);y=paragraph(g,tr("proof_t1",s.formula()?"✓":"—",s.stations().contains("arcane_workbench")?"✓":"—",s.stations().contains("crystal_grinder")?"✓":"—"),x,y,w,SpellTheme.INK);y=paragraph(g,tr("proof_t2",s.decoded(),s.stations().contains("arcane_smelter")?"✓":"—",s.discovered().contains("badspell:crystal_core")?"✓":"—"),x,y,w,SpellTheme.INK);if(y<height-57)paragraph(g,tr("future_tiers"),x,y,w,SpellTheme.MUTED);}
        else{int x=split()+12,w=width-x-18;SpellTheme.rule(g,x,61,w);if(tab==1&&ResearchRules.ids().contains(selected)){int y=69;g.text(font,font.plainSubstrByWidth(RunePresentation.name(selected).getString(),w),x,y,SpellTheme.INK,false);y+=20;
                var family=ResearchRules.family(selected);var sample=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(family.sample())));g.item(sample,x,y);g.text(font,font.plainSubstrByWidth(sample.getHoverName().getString(),w-24),x+23,y+4,SpellTheme.ACCENT,false);y+=26;
                g.enableScissor(x,y,width-17,height-88);paragraph(g,ClientResearch.knows(selected)?RunePresentation.description(selected):tr("family."+family.id()),x,y,w,SpellTheme.INK);g.disableScissor();
            }else if(tab==2){int y=72;y=paragraph(g,tr("material_body",ClientResearch.state.discovered().size()),x,y,w,SpellTheme.INK);if(ClientResearch.state.discovered().contains(selected)){var stack=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(selected)));g.item(stack,x,y+5);paragraph(g,stack.getHoverName(),x+25,y+8,w-25,SpellTheme.ACCENT);}}}
        String message=ClientResearch.state.message();if(!message.isEmpty())g.text(font,font.plainSubstrByWidth(tr(message).getString(),width-112),17,height-21,message.equals("mismatch")?SpellTheme.ERROR:SpellTheme.ACCENT,false);super.extractRenderState(g,mx,my,partial);}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){if(tab!=0){offset=Math.clamp(offset-(int)Math.signum(sy),0,Math.max(0,found.size()-rows()));rebuildWidgets();}return true;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
