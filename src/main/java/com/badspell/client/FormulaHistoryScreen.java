package com.badspell.client;

import com.badspell.formula.Formula;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Private server history, paged for small windows; restoration is an unsaved, undoable editor draft. */
public final class FormulaHistoryScreen extends Screen {
    private final int hand,ink;
    private final Formula current;
    private final List<Formula> revisions;
    private int offset;
    public FormulaHistoryScreen(int hand,Formula current,int ink,List<Formula> revisions) {
        super(Component.translatable("gui.badspell.history"));
        this.hand=hand;this.current=current;this.ink=ink;this.revisions=List.copyOf(revisions);
    }
    private int pageSize(){return Math.max(1,(height-110)/24);}
    @Override protected void init() {
        int page=pageSize();offset=Math.min(offset,Math.max(0,revisions.size()-1));
        for(int row=0;row<page && offset+row<revisions.size();row++) {
            Formula archived=revisions.get(revisions.size()-1-offset-row);
            var label=Component.translatable("gui.badspell.restore_revision",archived.revision(),archived.title(),archived.nodes().size());
            addRenderableWidget(Button.builder(label,b->{
                var editor=new FormulaBookScreen(hand,current,ink);Minecraft.getInstance().gui.setScreen(editor);editor.restoreDraft(archived);
            }).bounds(20,55+row*24,Math.max(100,width-40),20).build());
        }
        var previous=addRenderableWidget(Button.builder(Component.literal("<"),b->{offset=Math.max(0,offset-page);rebuildWidgets();}).bounds(20,height-30,35,20).build());
        previous.active=offset>0;
        var next=addRenderableWidget(Button.builder(Component.literal(">"),b->{offset+=page;rebuildWidgets();}).bounds(60,height-30,35,20).build());
        next.active=offset+page<revisions.size();
        addRenderableWidget(Button.builder(Component.translatable("gui.badspell.back"),b->onClose()).bounds(Math.max(100,width-110),height-30,90,20).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partialTick) {
        graphics.fill(0,0,width,height,0xff101820);
        graphics.text(font,title,20,12,0xffe0f8ed,false);
        graphics.text(font,Component.translatable(revisions.isEmpty()?"gui.badspell.history_empty":"gui.badspell.history_help"),20,32,0xffb0c0c0,false);
        super.extractRenderState(graphics,mouseX,mouseY,partialTick);
    }
    @Override public void onClose(){Minecraft.getInstance().gui.setScreen(new FormulaBookScreen(hand,current,ink));}
    @Override public boolean isPauseScreen(){return false;}
}
