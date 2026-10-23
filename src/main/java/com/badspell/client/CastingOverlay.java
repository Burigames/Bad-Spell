package com.badspell.client;

import com.badspell.content.ModItems;
import com.badspell.magic.SpellTargets;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Lightweight acquisition feedback, explicitly a preview; the server still validates every cast. */
public final class CastingOverlay {
    private CastingOverlay(){}
    public static void render(net.neoforged.neoforge.client.event.RenderGuiEvent.Post event){var game=Minecraft.getInstance();if(game.player==null||game.gui.screen()!=null||(!ModItems.isGlove(game.player.getMainHandItem())&&!ModItems.isGlove(game.player.getOffhandItem())))return;
        var hit=SpellTargets.entity(game.player);Component label=Component.translatable(hit==null?"ui.badspell.target_none":"ui.badspell.target_preview",hit==null?Component.empty():hit.getEntity().getDisplayName());String line=game.font.plainSubstrByWidth(label.getString(),240);int x=(game.getWindow().getGuiScaledWidth()-game.font.width(line))/2,y=game.getWindow().getGuiScaledHeight()/2+18;var g=event.getGuiGraphics();g.fill(x-5,y-3,x+game.font.width(line)+5,y+11,0xb5211e1a);g.text(game.font,line,x,y,hit==null?0xffc7bda7:0xffb9d3b4,false);
    }
}
