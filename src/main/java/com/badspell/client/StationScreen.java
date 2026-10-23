package com.badspell.client;

import com.badspell.production.StationMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class StationScreen extends AbstractContainerScreen<StationMenu> {
    public StationScreen(StationMenu menu,Inventory inventory,Component title){super(menu,inventory,title,176,208);inventoryLabelY=96;}
    @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float partial) {
        super.extractBackground(g,mx,my,partial);int x=leftPos,y=topPos;
        g.fill(x-3,y-3,x+179,y+211,0xff263f4b);g.fill(x,y,x+176,y+208,0xff101f2b);g.fill(x,y,x+176,y+23,0xff1b3541);
        for(var slot:menu.slots){g.fill(x+slot.x-1,y+slot.y-1,x+slot.x+17,y+slot.y+17,0xff35515c);g.fill(x+slot.x,y+slot.y,x+slot.x+16,y+slot.y+16,0xff09151d);}
        int total=Math.max(1,menu.value(1)),filled=Math.clamp(menu.value(0)*64/total,0,64);
        g.fill(x+17,y+86,x+159,y+90,0xff263d49);g.fill(x+17,y+86,x+17+filled*142/64,y+90,0xff64d8b6);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my) {
        g.text(font,title,10,8,0xffdceef0,false);g.text(font,Component.translatable("gui.badspell.inputs"),17,24,0xff93acb3,false);
        g.text(font,Component.translatable("gui.badspell.adapter_label"),77,24,0xff93acb3,false);
        g.text(font,Component.translatable("gui.badspell.cell_label"),83,53,0xff93acb3,false);
        g.text(font,Component.translatable("gui.badspell.outputs"),132,24,0xff93acb3,false);
        g.text(font,Component.translatable("gui.badspell.machine_fl",menu.value(2)),112,76,0xffe3c487,false);
        g.text(font,Component.translatable("machine.badspell.status_"+menu.value(3)),10,194,menu.value(3)==0?0xff64d8b6:0xffe3c487,false);
        g.text(font,playerInventoryTitle,8,96,0xff93acb3,false);
    }
}
