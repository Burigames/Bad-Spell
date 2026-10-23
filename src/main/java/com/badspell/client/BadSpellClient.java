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
    private static final net.minecraft.client.KeyMapping.Category KEYS=new net.minecraft.client.KeyMapping.Category(net.minecraft.resources.Identifier.fromNamespaceAndPath("badspell","controls"));
    private static final net.minecraft.client.KeyMapping CODEX=new net.minecraft.client.KeyMapping("key.badspell.codex",71,KEYS);
    private static final net.minecraft.client.KeyMapping MONOCLE=new net.minecraft.client.KeyMapping("key.badspell.monocle",77,KEYS);
    private static void keys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event){event.registerCategory(KEYS);event.register(MONOCLE);event.register(CODEX);}
    private static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){var game=Minecraft.getInstance();while(CODEX.consumeClick())if(game.player!=null&&game.gui.screen()==null)game.gui.setScreen(new CodexScreen(null));while(MONOCLE.consumeClick())if(game.player!=null&&game.gui.screen()==null&&game.player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).is(com.badspell.content.ModItems.ARCANE_MONOCLE.get()))net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new Packets.MonocleToggle());}
    public BadSpellClient(IEventBus bus) { bus.addListener(BadSpellClient::register);bus.addListener(BadSpellClient::keys);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(BadSpellClient::tick);bus.addListener(BadSpellClient::menus);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(RuneSurfaceRenderer::extract);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(RuneSurfaceRenderer::submit);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(RuneSurfaceRenderer::unload);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(MonocleOverlay::render);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(CastingOverlay::render); net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(BadSpellClient::scroll); }
    private static void menus(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event){event.register(com.badspell.production.ProductionRegistries.MENU.get(),StationScreen::new);}
    private static void scroll(net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent event) {
        var game=Minecraft.getInstance();var player=game.player;
        if(player==null || !game.options.keyShift.isDown() || event.getScrollDeltaY()==0)return;
        int hand=com.badspell.content.ModItems.isGlove(player.getMainHandItem())?0:
            com.badspell.content.ModItems.isGlove(player.getOffhandItem())?1:-1;
        if(hand<0)return;
        event.setCanceled(true);
        if(event.getAccumulatedScrollY()!=0)net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(
            new Packets.GloveSlot(hand,event.getAccumulatedScrollY()>0?-1:1));
    }
    private static void register(RegisterClientPayloadHandlersEvent event) {
        event.register(Packets.OpenCodex.TYPE,(packet,context)->Minecraft.getInstance().gui.setScreen(new CodexScreen(null)));
        event.register(Packets.BoardMarks.TYPE,(packet,context)->RuneSurfaceRenderer.accept(packet));
        event.register(Packets.ScanInfo.TYPE,(packet,context)->MonocleOverlay.accept(packet));
        event.register(Packets.BoardView.TYPE,(packet,context)->{try{var game=Minecraft.getInstance();var formula=FormulaCodec.decode(packet.formula());if(game.gui.screen() instanceof FormulaBookScreen current&&current.isBoard(packet.position(),packet.face()))current.acceptServer(formula,packet.durability());else game.gui.setScreen(FormulaBookScreen.board(packet.hand(),formula,packet.durability(),packet.position(),packet.face()));}catch(IOException error){context.disconnect(net.minecraft.network.chat.Component.translatable("message.badspell.invalid"));}});
        event.register(Packets.LibraryPage.TYPE,(packet,context)->{
            var game=Minecraft.getInstance();if(game.gui.screen() instanceof FormulaLibraryScreen library)library.accept(packet);else if(game.gui.screen() instanceof FormulaBookScreen book)game.gui.setScreen(new FormulaLibraryScreen(book,packet));
        });
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
