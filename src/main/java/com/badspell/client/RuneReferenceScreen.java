package com.badspell.client;

import com.badspell.formula.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class RuneReferenceScreen extends Screen {
    private final Screen parent;private final String id;private int scroll;
    public RuneReferenceScreen(Screen parent,String id){super(RunePresentation.name(id));this.parent=parent;this.id=id;}
    @Override protected void init(){addRenderableWidget(new SpellButton(width-88,height-26,70,18,Component.translatable("ui.badspell.back"),b->onClose(),false));}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){g.fill(0,0,width,height,SpellTheme.BG);SpellTheme.panel(g,12,10,width-24,height-20);g.text(font,title,24,22,SpellTheme.INK,false);g.enableScissor(20,43,width-20,height-34);int y=48-scroll;for(var line:font.split(RunePresentation.description(id),width-54)){g.text(font,line,26,y,SpellTheme.INK,false);y+=12;}y+=16;var rune=RuneRegistry.standard().get(id);
        g.text(font,Component.translatable("ui.badspell.inputs"),26,y,SpellTheme.ACCENT,false);y+=16;
        if(rune.inputs().isEmpty()){g.text(font,Component.translatable("ui.badspell.no_inputs"),26,y,SpellTheme.MUTED,false);y+=16;}
        for(var port:new TreeMap<>(rune.inputs()).entrySet()){g.fill(26,y,32,y+6,RunePresentation.color(port.getValue()));g.text(font,RunePresentation.port(port.getKey()).getString()+"  ·  "+RunePresentation.type(port.getValue()).getString(),40,y-1,SpellTheme.INK,false);y+=18;}
        y+=6;g.text(font,Component.translatable("ui.badspell.output",RunePresentation.type(rune.output())),26,y,SpellTheme.ACCENT,false);y+=25;
        for(var parameter:new TreeMap<>(rune.parameters()).entrySet()){var p=parameter.getValue();g.text(font,Component.translatable("parameter.badspell."+parameter.getKey()).getString()+"  ·  "+RunePresentation.type(p.type()).getString()+(p.type()==Value.Type.NUMBER?" ["+p.min()+" … "+p.max()+"]":""),26,y,SpellTheme.INK,false);y+=16;}g.disableScissor();super.extractRenderState(g,mx,my,partial);}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){scroll=Math.clamp(scroll-(int)(sy*18),0,300);return true;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
