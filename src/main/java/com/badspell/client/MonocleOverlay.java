package com.badspell.client;

import com.badspell.network.Packets;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/** Short-lived server-approved diagnosis; never reads foreign formula NBT on the client. */
public final class MonocleOverlay {
    private static net.minecraft.core.BlockPos target;private static String text="";private static long until;private static int intensity;
    private MonocleOverlay(){}
    public static void accept(Packets.ScanInfo packet){var game=Minecraft.getInstance();if(game.level==null)return;target=packet.position();text=packet.text();intensity=packet.intensity();until=game.level.getGameTime()+110;}
    public static void render(net.neoforged.neoforge.client.event.RenderGuiEvent.Post event){
        var game=Minecraft.getInstance();if(game.player==null||game.level==null||game.gui.screen()!=null||game.level.getGameTime()>until||!game.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(com.badspell.content.ModItems.ARCANE_MONOCLE.get())||text.isBlank())return;
        var hit=game.player.pick(12,1,false);if(!(hit instanceof net.minecraft.world.phys.BlockHitResult block)||!block.getBlockPos().equals(target))return;
        int mode=game.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getIntOr("badspell_intensity",1);if(mode==0)return;
        var g=event.getGuiGraphics();String line=game.font.plainSubstrByWidth(text,Math.min(310,game.getWindow().getGuiScaledWidth()-32));g.fill(10,12,26+game.font.width(line),39,mode>1?0xe51a242d:0x991a242d);g.text(game.font,Component.translatable("item.badspell.arcane_monocle"),18,17,0xffd4bd79,false);g.text(game.font,line,18,29,0xffc6d6d7,false);
    }
}
