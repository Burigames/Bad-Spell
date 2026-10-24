package com.badspell;

import com.badspell.content.ModItems;
import com.badspell.network.Packets;
import com.badspell.server.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Common entrypoint; no references to client classes. */
@Mod(BadSpell.MOD_ID)
public final class BadSpell {
    public static final String MOD_ID="badspell";
    public BadSpell(IEventBus bus) {
        com.badspell.content.ModContent.bootstrap();
        ModItems.register(bus);com.badspell.content.ModContent.register(bus);com.badspell.production.ProductionRegistries.register(bus);
        if(Boolean.getBoolean("badspell.tests")){IntegrationTests.register(bus);M1Tests.register(bus);M1CompletionTests.register(bus);}bus.addListener(Packets::register);
        NeoForge.EVENT_BUS.addListener(LightFields::tick);NeoForge.EVENT_BUS.addListener(SpellFields::tick);NeoForge.EVENT_BUS.addListener(SpellFields::incoming);
        NeoForge.EVENT_BUS.addListener(LightFields::load);
        NeoForge.EVENT_BUS.addListener(LightFields::sendChunk);
        NeoForge.EVENT_BUS.addListener(com.badspell.lighting.FieldLightSources::unloadClientChunk);
        NeoForge.EVENT_BUS.addListener(MagicActions::tick);NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,MagicActions::entityInteract);NeoForge.EVENT_BUS.addListener(FormulaLibrary::clone);NeoForge.EVENT_BUS.addListener(ToolActions::tick);NeoForge.EVENT_BUS.addListener(RuneBoards::broken);NeoForge.EVENT_BUS.addListener(RuneBoards::tick);NeoForge.EVENT_BUS.addListener(RuneBoards::load);NeoForge.EVENT_BUS.addListener(RuneBoards::sendChunk);NeoForge.EVENT_BUS.addListener(WorldBindings::interact);NeoForge.EVENT_BUS.addListener(WorldBindings::breakBlock);
    }
}
