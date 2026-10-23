package com.badspell.client;

import com.badspell.formula.*;
import com.badspell.network.Packets;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class FormulaLibraryScreen extends Screen {
    private final FormulaBookScreen parent;
    private Packets.LibraryPage page;
    private UUID selected;private int preset;
    private int offset;
    private String query="";
    private EditBox search,tags;
    public FormulaLibraryScreen(FormulaBookScreen parent,Packets.LibraryPage page){super(Component.translatable("gui.badspell.library"));this.parent=parent;this.page=page;}
    public void accept(Packets.LibraryPage update){page=update;offset=0;rebuildWidgets();}
    private Component tr(String key){return Component.translatable("gui.badspell."+key);}
    private void button(int x,int y,int w,Component label,Runnable action,boolean primary){addRenderableWidget(new SpellButton(x,y,w,20,label,b->action.run(),primary));}
    private void send(int action,UUID id,String title,String tags,int targetPage){ClientPacketDistributor.sendToServer(new Packets.LibraryAction(page.hand(),action,id,title,tags,targetPage));}
    private int visible(){return Math.max(1,(height-140)/29);}
    private Formula selected(){for(var entry:page.entries())try{var formula=FormulaCodec.decode(entry.formula());if(formula.id().equals(selected))return formula;}catch(java.io.IOException ignored){}return null;}
    @Override protected void init() {
        search=addRenderableWidget(new EditBox(font,16,33,Math.max(80,width-172),20,tr("search")));search.setMaxLength(96);search.setValue(query);search.setHint(tr("library_query"));search.setResponder(value->query=value);
        button(width-162,33,64,Component.translatable("ui.badspell.find"),()->send(0,new UUID(0,0),query,"",0),false);
        button(width-92,33,76,Component.translatable("ui.badspell.templates"),()->net.minecraft.client.Minecraft.getInstance().gui.setScreen(new TemplatePickerScreen(this,parent)),true);
        int count=visible();offset=Math.clamp(offset,0,Math.max(0,page.entries().size()-count));
        for(int row=0;row<count&&row+offset<page.entries().size();row++) {
            var entry=page.entries().get(row+offset);try {
                var formula=FormulaCodec.decode(entry.formula());String label=formula.title()+" · r"+formula.revision()+" · "+formula.nodes().size();
                var item=addRenderableWidget(new SpellButton(16,66+row*29,width-32,25,Component.literal(label),b->{selected=formula.id();rebuildWidgets();},formula.id().equals(selected)));item.setTooltip(Tooltip.create(Component.literal(entry.tags())));
            }catch(java.io.IOException ignored){}
        }
        var current=selected();int y=height-68;
        tags=addRenderableWidget(new EditBox(font,16,y,Math.max(70,width-238),20,tr("tags")));tags.setMaxLength(256);tags.setHint(tr("tags"));
        if(current!=null)for(var entry:page.entries())try{if(FormulaCodec.decode(entry.formula()).id().equals(selected))tags.setValue(entry.tags());}catch(java.io.IOException ignored){}
        button(width-214,y,62,tr("tags"),()->{if(current!=null)send(4,current.id(),query,tags.getValue(),page.page());},false);
        button(width-146,y,62,tr("load"),()->{if(current!=null)send(2,current.id(),"","",page.page());},true);
        button(width-78,y,62,tr("module"),()->{if(current!=null){Minecraft.getInstance().gui.setScreen(parent);parent.addModule(current);}},false);
        button(16,height-35,32,Component.literal("<"),()->send(0,new UUID(0,0),query,"",Math.max(0,page.page()-1)),false);
        button(52,height-35,32,Component.literal(">"),()->send(0,new UUID(0,0),query,"",Math.min(31,page.page()+1)),false);
        button(94,height-35,66,tr("delete"),()->{if(current!=null&&!current.id().equals(parent.formulaId()))Minecraft.getInstance().gui.setScreen(new net.minecraft.client.gui.screens.ConfirmScreen(yes->{Minecraft.getInstance().gui.setScreen(this);if(yes)send(3,current.id(),query,"",page.page());},tr("delete"),Component.literal(current.title())));},false);
        button(170,height-35,82,tr("history"),()->{Minecraft.getInstance().gui.setScreen(parent);parent.requestHistory();},false);
        button(width-90,height-35,74,tr("back"),this::onClose,false);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){g.fill(0,0,width,height,SpellTheme.BG);SpellTheme.panel(g,8,8,width-16,height-16);g.text(font,title,16,12,SpellTheme.INK,false);g.text(font,Component.literal((page.page()+1)+" / "+Math.max(1,(page.total()+7)/8)+" · "+page.total()),width-130,12,SpellTheme.MUTED,false);super.extractRenderState(g,mx,my,partial);}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){offset-=(int)Math.signum(sy);rebuildWidgets();return true;}
    @Override public void onClose(){Minecraft.getInstance().gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
