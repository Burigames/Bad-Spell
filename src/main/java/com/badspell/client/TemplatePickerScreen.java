package com.badspell.client;

import com.badspell.formula.ExampleFormulas;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Discoverable template list instead of cycling an unlabeled preset button. */
public final class TemplatePickerScreen extends Screen {
    private final FormulaBookScreen book;private final Screen parent;private int offset;
    public TemplatePickerScreen(Screen parent,FormulaBookScreen book){super(Component.translatable("ui.badspell.templates"));this.parent=parent;this.book=book;}
    private int rows(){return Math.max(1,(height-94)/31);}
    @Override protected void init(){offset=Math.clamp(offset,0,Math.max(0,ExampleFormulas.TEMPLATES.size()-rows()));for(int row=0;row<rows()&&offset+row<ExampleFormulas.TEMPLATES.size();row++){String id=ExampleFormulas.TEMPLATES.get(offset+row);var button=addRenderableWidget(new SpellButton(20,59+row*31,width-40,27,Component.translatable("template.badspell."+id),b->{minecraft.gui.setScreen(book);book.requestTemplate(id);},false));button.setTooltip(Tooltip.create(Component.translatable("template_help.badspell."+id)));}addRenderableWidget(new SpellButton(width-90,height-28,70,18,Component.translatable("ui.badspell.back"),b->onClose(),false));}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){g.fill(0,0,width,height,SpellTheme.BG);SpellTheme.panel(g,10,8,width-20,height-16);g.text(font,title,20,20,SpellTheme.INK,false);g.text(font,font.plainSubstrByWidth(Component.translatable("ui.badspell.template_tip").getString(),width-40),20,40,SpellTheme.MUTED,false);super.extractRenderState(g,mx,my,partial);}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){offset=Math.clamp(offset-(int)Math.signum(sy),0,Math.max(0,ExampleFormulas.TEMPLATES.size()-rows()));rebuildWidgets();return true;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
