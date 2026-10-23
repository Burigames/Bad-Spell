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
        int color=!active?0xff17232c:primary?(isHoveredOrFocused()?0xff77e5c3:0xff53c8a5):(isHoveredOrFocused()?0xff304752:0xff20333d);
        g.fill(getX(),getY(),getX()+getWidth(),getY()+getHeight(),color);
        if(isFocused())g.fill(getX(),getY()+getHeight()-2,getX()+getWidth(),getY()+getHeight(),0xffb9ffe5);
        var font=Minecraft.getInstance().font;
        String label=font.plainSubstrByWidth(getMessage().getString(),Math.max(0,getWidth()-8));
        g.text(font,label,getX()+(getWidth()-font.width(label))/2,getY()+(getHeight()-8)/2,!active?0xff607981:primary?0xff09231e:0xffd8e8ec,false);
    }
}
