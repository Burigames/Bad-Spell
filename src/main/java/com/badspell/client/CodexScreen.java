package com.badspell.client;

import com.badspell.content.ModItems;
import com.badspell.formula.*;
import com.badspell.network.Packets;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Illustrated field manual. Tutorials use the same real template graphs as the formula library. */
public final class CodexScreen extends Screen {
    private static final List<String> CHAPTERS=List.of("start","editor","targets","combine","logic","energy","stations","tools","problems");
    private final Screen parent;private int chapter,scroll,maxScroll;
    public CodexScreen(Screen parent){super(Component.translatable("ui.badspell.codex"));this.parent=parent;}
    private Component tr(String key){return Component.translatable("codex.badspell."+key);}
    private int left(){return width<400?92:112;}
    @Override protected void init(){for(int i=0;i<CHAPTERS.size();i++){int index=i;addRenderableWidget(new SpellButton(12,37+i*16,left()-20,15,tr(CHAPTERS.get(i)+".title"),b->{chapter=index;scroll=0;rebuildWidgets();},chapter==i));}
        addRenderableWidget(new SpellButton(12,height-48,left()-20,18,ResearchScreen.tr("title"),b->minecraft.gui.setScreen(new ResearchScreen(this)),true));
        addRenderableWidget(new SpellButton(12,height-26,left()-20,18,Component.translatable("ui.badspell.back"),b->onClose(),false));
        addRenderableWidget(new SpellButton(width-106,10,94,18,Component.translatable("ui.badspell.runes"),b->minecraft.gui.setScreen(new RunePickerScreen(this,null,id->minecraft.gui.setScreen(new RuneReferenceScreen(this,id)))),false));
        String template=switch(CHAPTERS.get(chapter)){case "start","editor"->"light";case "targets"->"self_light";case "combine"->"frost_push";case "logic"->"adaptive_ward";default->null;};
        if(parent instanceof FormulaBookScreen book&&template!=null)addRenderableWidget(new SpellButton(left()+12,height-28,Math.min(210,width-left()-24),18,Component.translatable("ui.badspell.try_template"),b->{minecraft.gui.setScreen(book);book.requestTemplate(template);},true));
        if(CHAPTERS.get(chapter).equals("stations"))addRenderableWidget(new SpellButton(left()+12,height-28,Math.min(210,width-left()-24),18,Component.translatable("ui.badspell.recipes"),b->minecraft.gui.setScreen(new StationRecipeScreen(this,"arcane_workbench")),true));
    }
    private void card(GuiGraphicsExtractor g,int x,int y,int w,Component label,Value.Type type){g.fill(x,y,x+w,y+32,SpellTheme.CARD);g.fill(x,y,x+3,y+32,RunePresentation.color(type));g.text(font,font.plainSubstrByWidth(label.getString(),w-12),x+7,y+6,SpellTheme.INK,false);g.text(font,RunePresentation.type(type),x+7,y+19,SpellTheme.MUTED,false);}
    private void diagram(GuiGraphicsExtractor g,int x,int y,int w,String page){int gap=13,cw=(w-gap*2)/3;String[] ids=switch(page){case "targets"->new String[]{"self","entity_position","sphere"};case "combine"->new String[]{"cold","push","combine"};case "logic"->new String[]{"entity_health","greater","if_effect"};case "energy"->new String[]{"interval","limit","activate"};default->new String[]{"target_position","point","light"};};
        if(page.equals("start")||page.equals("tools")||page.equals("stations")){String[] names=page.equals("stations")?new String[]{"crystal_grinder","arcane_smelter","engraving_station"}:new String[]{"formula_book","rune_glove","field_cell"};for(int i=0;i<3;i++){var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.fromNamespaceAndPath("badspell",names[i]));g.fill(x+i*(cw+gap),y,x+i*(cw+gap)+cw,y+42,SpellTheme.CARD);g.item(new ItemStack(item),x+i*(cw+gap)+cw/2-8,y+5);g.text(font,font.plainSubstrByWidth(new ItemStack(item).getHoverName().getString(),cw-6),x+i*(cw+gap)+3,y+28,SpellTheme.INK,false);}return;}
        if(page.equals("combine")){int half=(w-24)/2;card(g,x,y,half,RunePresentation.name("badspell:cold"),Value.Type.EFFECT);card(g,x,y+40,half,RunePresentation.name("badspell:push"),Value.Type.EFFECT);card(g,x+half+24,y+20,half,RunePresentation.name("badspell:combine"),Value.Type.EFFECT);g.fill(x+half,y+15,x+half+12,y+16,SpellTheme.ACCENT);g.fill(x+half,y+55,x+half+12,y+56,SpellTheme.ACCENT);g.fill(x+half+11,y+15,x+half+12,y+56,SpellTheme.ACCENT);g.fill(x+half+11,y+35,x+half+24,y+36,SpellTheme.ACCENT);return;}
        for(int i=0;i<3;i++){String id="badspell:"+ids[i];card(g,x+i*(cw+gap),y,cw,RunePresentation.name(id),RuneRegistry.standard().get(id).output());if(i<2)g.text(font,"→",x+(i+1)*cw+i*gap+2,y+12,SpellTheme.ACCENT,false);}
        g.text(font,font.plainSubstrByWidth(tr(page+".image").getString(),w),x,y+42,SpellTheme.MUTED,false);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){g.fill(0,0,width,height,SpellTheme.BG);SpellTheme.panel(g,8,8,left()-6,height-16);SpellTheme.panel(g,left()+4,8,width-left()-12,height-16);g.text(font,"BAD SPELL",16,20,SpellTheme.INK,false);String page=CHAPTERS.get(chapter);int x=left()+16,w=width-x-18;
        g.enableScissor(left()+8,36,width-12,height-33);int y=43-scroll;g.text(font,tr(page+".title"),x,y,SpellTheme.ACCENT,false);y+=22;diagram(g,x,y,w,page);y+=page.equals("combine")?91:69;
        for(var line:font.split(tr(page+".body"),w)){g.text(font,line,x,y,SpellTheme.INK,false);y+=13;}maxScroll=Math.max(0,y+scroll-(height-40));g.disableScissor();if(maxScroll>0)g.text(font,"↕",width-24,height-43,SpellTheme.MUTED,false);super.extractRenderState(g,mx,my,partial);}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){scroll=Math.clamp(scroll-(int)(sy*22),0,maxScroll);return true;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
