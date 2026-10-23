package com.badspell.client;

import com.badspell.BadSpell;
import com.badspell.formula.FormulaCodec;
import com.badspell.network.Packets;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@Mod(value=BadSpell.MOD_ID,dist=Dist.CLIENT)
public final class BadSpellClient {
    public BadSpellClient(IEventBus bus) { bus.addListener(BadSpellClient::register);bus.addListener(BadSpellClient::menus); net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(BadSpellClient::scroll); }
    private static void menus(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event){event.register(com.badspell.production.ProductionRegistries.MENU.get(),StationScreen::new);}
    private static void scroll(net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent event) {
        var game=Minecraft.getInstance();var player=game.player;
        if(player==null || !game.options.keyShift.isDown() || event.getScrollDeltaY()==0)return;
        int hand=player.getMainHandItem().is(com.badspell.content.ModItems.RUNE_GLOVE.get())?0:
            player.getOffhandItem().is(com.badspell.content.ModItems.RUNE_GLOVE.get())?1:-1;
        if(hand<0)return;
        event.setCanceled(true);
        if(event.getAccumulatedScrollY()!=0)net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(
            new Packets.GloveSlot(hand,event.getAccumulatedScrollY()>0?-1:1));
    }
    private static void register(RegisterClientPayloadHandlersEvent event) {
        event.register(Packets.OpenBook.TYPE,(packet,context)->{
            try {
                var game=Minecraft.getInstance();var formula=FormulaCodec.decode(packet.formula());
                if(game.gui.screen() instanceof FormulaBookScreen current && current.accepts(packet.hand(),formula))current.acceptServer(formula,packet.ink());
                else game.gui.setScreen(new FormulaBookScreen(packet.hand(),formula,packet.ink()));
            } catch(IOException error) { context.disconnect(net.minecraft.network.chat.Component.translatable("message.badspell.invalid")); }
        });
        event.register(Packets.BookHistory.TYPE,(packet,context)->{
            try {
                var current=FormulaCodec.decode(packet.current());var history=new java.util.ArrayList<com.badspell.formula.Formula>();
                for(byte[] bytes:packet.revisions()) {
                    var archived=FormulaCodec.decode(bytes);
                    if(!archived.id().equals(current.id()) || !archived.owner().equals(current.owner()) || archived.revision()>=current.revision())throw new IOException("Invalid history identity");
                    history.add(archived);
                }
                Minecraft.getInstance().gui.setScreen(new FormulaHistoryScreen(packet.hand(),current,packet.ink(),history));
            } catch(IOException error){context.disconnect(net.minecraft.network.chat.Component.translatable("message.badspell.invalid"));}
        });
        event.register(Packets.LightUpdate.TYPE,(packet,context)->{
            var level=Minecraft.getInstance().level;
            if(level!=null && level.getChunkSource().hasChunk((packet.position()).getX() >> 4,(packet.position()).getZ() >> 4) && packet.strength()>=0 && packet.strength()<=8) {
                com.badspell.lighting.FieldLightSources.set(level,packet.position(),packet.strength());
                com.badspell.lighting.FieldLightSources.syncSection(level,packet.position());
                level.getChunkAt(packet.position()).getAuxLightManager(ChunkPos.containing(packet.position())).setLightAt(packet.position(),packet.strength());
            }
        });
    }
}
