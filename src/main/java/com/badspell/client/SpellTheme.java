package com.badspell.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Shared parchment, ink and aged brass palette. No textures or game state in UI chrome. */
public final class SpellTheme {
    private SpellTheme(){}
    public static final int BG=0xff211e1a,PAPER=0xffe5d9bb,CARD=0xfff0e6ce,EDGE=0xff9c8967,INK=0xff382f25,MUTED=0xff786a54,ACCENT=0xff376e67,GOLD=0xffb79857,ERROR=0xffa84436;
    public static void panel(GuiGraphicsExtractor g,int x,int y,int w,int h){g.fill(x-1,y-1,x+w+1,y+h+1,EDGE);g.fill(x,y,x+w,y+h,PAPER);g.fill(x,y,x+w,y+1,CARD);}
    public static void rule(GuiGraphicsExtractor g,int x,int y,int w){g.fill(x,y,x+w,y+1,EDGE);}
}
