package com.badspell.content;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** World-bound fragment or intentionally authored, one-use lesson. */
public final class ResearchItem extends Item {
    private final boolean lesson;
    public ResearchItem(Properties p,boolean lesson){super(p);this.lesson=lesson;}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){if(player instanceof ServerPlayer p)com.badspell.progression.ResearchActions.use(p,hand,lesson);return InteractionResult.SUCCESS;}
    @Override public void appendHoverText(ItemStack s,TooltipContext context,net.minecraft.world.item.component.TooltipDisplay display,java.util.function.Consumer<Component> text,TooltipFlag flag){var d=s.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();String id=d.getStringOr("rune","");if(com.badspell.research.ResearchRules.ids().contains(id))text.accept(Component.translatable("rune.badspell."+id.substring(9)));text.accept(Component.translatable("research.badspell."+(lesson?"folio_tip":"fragment_tip")));}
}
