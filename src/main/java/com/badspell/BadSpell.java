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
        ModItems.register(bus);
        if(Boolean.getBoolean("badspell.tests"))IntegrationTests.register(bus);bus.addListener(Packets::register);
        NeoForge.EVENT_BUS.addListener(LightFields::tick);
        NeoForge.EVENT_BUS.addListener(LightFields::load);
        NeoForge.EVENT_BUS.addListener(LightFields::sendChunk);
        NeoForge.EVENT_BUS.addListener(com.badspell.lighting.FieldLightSources::unloadClientChunk);
        NeoForge.EVENT_BUS.addListener(MagicActions::tick);
    }
}
