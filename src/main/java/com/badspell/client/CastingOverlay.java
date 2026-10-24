package com.badspell.client;

import com.badspell.content.ModItems;
import com.badspell.item.GloveFormulas;
import com.badspell.magic.SpellTargets;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.CustomData;

/** Preview follows the selected graph's target type; only the server can authorize a cast. */
public final class CastingOverlay {
    private static CustomData previous;private static int mode;
    private CastingOverlay(){}
    public static void render(net.neoforged.neoforge.client.event.RenderGuiEvent.Post event){var game=Minecraft.getInstance();if(game.player==null||game.gui.screen()!=null)return;
        var glove=ModItems.isGlove(game.player.getMainHandItem())?game.player.getMainHandItem():game.player.getOffhandItem();if(!ModItems.isGlove(glove))return;
        var data=glove.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY);if(data!=previous){previous=data;mode=0;try{var formula=GloveFormulas.read(glove);if(formula.nodes().stream().anyMatch(n->n.rune().startsWith("badspell:target_entity")))mode=1;else if(formula.nodes().stream().anyMatch(n->n.rune().equals("badspell:self")))mode=2;}catch(java.io.IOException error){mode=3;}}
        var hit=mode==1?SpellTargets.entity(game.player):null;Component label;
        if(mode==1)label=Component.translatable(hit==null?"ui.badspell.target_none":"ui.badspell.target_preview",hit==null?Component.empty():hit.getEntity().getDisplayName());else if(mode==2)label=Component.translatable("ui.badspell.target_self");else if(mode==3)label=Component.translatable("message.badspell.empty_slot");else{var at=SpellTargets.position(game.player);label=Component.translatable("ui.badspell.target_position",at.getX(),at.getY(),at.getZ());}
        String line=game.font.plainSubstrByWidth(label.getString(),240);int x=(game.getWindow().getGuiScaledWidth()-game.font.width(line))/2,y=game.getWindow().getGuiScaledHeight()/2+18;var g=event.getGuiGraphics();g.fill(x-5,y-3,x+game.font.width(line)+5,y+11,0xb5211e1a);g.text(game.font,line,x,y,mode==1&&hit==null?0xffc7bda7:0xffb9d3b4,false);
    }
}
