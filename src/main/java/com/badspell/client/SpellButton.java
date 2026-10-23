package com.badspell.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Flat, keyboard-focusable controls shared by Bad Spell screens. */
public final class SpellButton extends Button {
    private final boolean primary;
    public SpellButton(int x,int y,int width,int height,Component label,OnPress action,boolean primary) {
        super(x,y,width,height,label,action,DEFAULT_NARRATION);this.primary=primary;
    }
    @Override protected void extractContents(GuiGraphicsExtractor g,int mouseX,int mouseY,float partial) {
        int color=!active?0xffc8bda4:primary?(isHoveredOrFocused()?0xff4b8277:SpellTheme.ACCENT):(isHoveredOrFocused()?0xffe0d0ad:0xffd2c19d);
        g.fill(getX(),getY(),getX()+getWidth(),getY()+getHeight(),SpellTheme.EDGE);g.fill(getX()+1,getY()+1,getX()+getWidth()-1,getY()+getHeight()-1,color);
        if(isFocused())g.fill(getX(),getY()+getHeight()-2,getX()+getWidth(),getY()+getHeight(),SpellTheme.GOLD);
        var font=Minecraft.getInstance().font;
        String label=font.plainSubstrByWidth(getMessage().getString(),Math.max(0,getWidth()-8));
        g.text(font,label,getX()+(getWidth()-font.width(label))/2,getY()+(getHeight()-8)/2,!active?0xff8b7e67:primary?0xfff5edda:SpellTheme.INK,false);
    }
}
