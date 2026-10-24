package com.badspell.client;

import com.badspell.production.StationMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Station-specific work areas, shared inventory and explicit power/help affordances. */
public final class StationScreen extends AbstractContainerScreen<StationMenu> {
    public StationScreen(StationMenu menu,Inventory inventory,Component title){super(menu,inventory,title,248,218);inventoryLabelY=112;}
    @Override protected void init(){super.init();addRenderableWidget(new SpellButton(leftPos+211,topPos+6,28,16,Component.literal("?"),b->minecraft.gui.setScreen(new StationRecipeScreen(this,menu.kind())),false));}
    @Override protected void containerTick(){super.containerTick();if(menu.kind().equals("rune_holder")&&!holderControls){holderControls=true;addRenderableWidget(new SpellButton(leftPos+86,topPos+63,37,16,Component.translatable("ui.badspell.run"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,1),true));addRenderableWidget(new SpellButton(leftPos+127,topPos+63,40,16,Component.translatable("ui.badspell.stop"),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,0),false));}}
    private boolean holderControls;
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial){
        menu.layoutSlots();super.extractBackground(g,mx,my,partial);int x=leftPos,y=topPos;SpellTheme.panel(g,x,y,248,218);g.fill(x,y,x+248,y+25,0xffcbb991);SpellTheme.rule(g,x,y+25,248);
        g.fill(x+9,y+29,x+81,y+84,0xffd5c6a6);g.fill(x+180,y+29,x+235,y+84,0xffd5c6a6);
        int progress=menu.value(0),total=Math.max(1,menu.value(1));int phase=(int)(System.currentTimeMillis()/200%4);
        int cx=x+123,cy=y+51;
        switch(menu.kind()){
            case "crystal_grinder"->{g.fill(cx-19,cy-17,cx+19,cy+17,SpellTheme.EDGE);g.fill(cx-14,cy-12,cx+14,cy+12,SpellTheme.MUTED);g.fill(cx-9,cy-7,cx+9,cy+7,SpellTheme.CARD);if(progress>0)g.fill(cx-2+(phase-2)*4,cy-10,cx+2+(phase-2)*4,cy+10,SpellTheme.ACCENT);}
            case "alchemy_table"->{g.fill(cx-5,cy-19,cx+5,cy-9,SpellTheme.EDGE);g.fill(cx-16,cy-9,cx+16,cy+17,SpellTheme.EDGE);g.fill(cx-13,cy-6,cx+13,cy+14,SpellTheme.CARD);int fill=progress*19/total;g.fill(cx-12,cy+13-fill,cx+12,cy+13,0xff71958a);}
            case "arcane_smelter"->{g.fill(cx-20,cy-20,cx+20,cy+19,SpellTheme.MUTED);g.fill(cx-15,cy-14,cx+15,cy+13,SpellTheme.BG);for(int i=0;i<3;i++)g.fill(cx-11+i*8,cy+10-(progress>0?9+(i+phase)%3*3:3),cx-5+i*8,cy+10,progress>0?0xffc8793e:SpellTheme.EDGE);}
            case "rune_press"->{g.fill(cx-20,cy-18,cx-15,cy+18,SpellTheme.MUTED);g.fill(cx+15,cy-18,cx+20,cy+18,SpellTheme.MUTED);g.fill(cx-20,cy-20,cx+20,cy-15,SpellTheme.EDGE);int down=progress*12/total;g.fill(cx-3,cy-15,cx+3,cy-3+down,SpellTheme.MUTED);g.fill(cx-12,cy-4+down,cx+12,cy+1+down,SpellTheme.MUTED);g.fill(cx-20,cy+17,cx+20,cy+21,SpellTheme.EDGE);}
            case "engraving_station"->{g.fill(cx-20,cy-15,cx+17,cy+18,SpellTheme.EDGE);g.fill(cx-17,cy-12,cx+14,cy+15,SpellTheme.CARD);g.text(font,"◇",cx-6,cy-4,SpellTheme.ACCENT,false);g.fill(cx+10,cy-20,cx+14,cy-2,SpellTheme.MUTED);}
            case "rune_holder"->{g.fill(cx-32,cy+8,cx+32,cy+12,SpellTheme.EDGE);g.fill(cx-17,cy+12,cx-13,cy+19,SpellTheme.MUTED);g.fill(cx+13,cy+12,cx+17,cy+19,SpellTheme.MUTED);}
            default->{for(int i=0;i<3;i++)for(int j=0;j<3;j++)g.fill(cx-16+i*12,cy-16+j*12,cx-6+i*12,cy-6+j*12,SpellTheme.EDGE);}
        }
        for(var slot:menu.slots){g.fill(x+slot.x-1,y+slot.y-1,x+slot.x+17,y+slot.y+17,SpellTheme.EDGE);g.fill(x+slot.x,y+slot.y,x+slot.x+16,y+slot.y+16,0xffc1b394);}
        if(menu.slots.get(6).getItem().isEmpty())g.text(font,"FL",x+97,y+94,SpellTheme.MUTED,false);
        if(menu.slots.get(11).getItem().isEmpty())g.text(font,"↔",x+120,y+94,SpellTheme.MUTED,false);
        if(!menu.kind().equals("rune_holder")){g.fill(x+85,y+78,x+173,y+81,SpellTheme.EDGE);g.fill(x+85,y+78,x+85+Math.clamp(progress*88/total,0,88),y+81,SpellTheme.ACCENT);}
        for(int slotId:new int[]{6,11}){var slot=menu.slots.get(slotId);if(mx>=x+slot.x&&mx<x+slot.x+16&&my>=y+slot.y&&my<y+slot.y+16)g.setTooltipForNextFrame(Component.translatable(slotId==6?"ui.badspell.station_cell":"ui.badspell.station_adapter"),mx,my);}
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){g.text(font,title,10,9,SpellTheme.INK,false);g.text(font,Component.translatable("station.badspell."+menu.kind()+".input"),12,28,SpellTheme.MUTED,false);g.text(font,Component.translatable("gui.badspell.outputs"),185,28,SpellTheme.MUTED,false);g.text(font,Component.translatable("gui.badspell.machine_fl",menu.value(2)),142,94,SpellTheme.ACCENT,false);g.text(font,playerInventoryTitle,43,113,SpellTheme.MUTED,false);g.text(font,font.plainSubstrByWidth(Component.translatable("machine.badspell.status_"+menu.value(3)).getString(),228),10,207,menu.value(3)==0?SpellTheme.ACCENT:SpellTheme.ERROR,false);}
}
