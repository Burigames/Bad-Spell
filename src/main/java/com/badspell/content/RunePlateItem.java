package com.badspell.content;

import com.badspell.formula.*;
import com.badspell.item.GloveFormulas;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public final class RunePlateItem extends Item {
    public RunePlateItem(Properties properties){super(properties);}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
        if(player instanceof ServerPlayer server)try{
            var data=player.getItemInHand(hand).getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var bytes=data.getByteArray("badspell_formula").orElseThrow();if(bytes.length>30000)throw new IllegalArgumentException();var formula=FormulaCodec.decode(bytes);
            if(!formula.owner().equals(player.getUUID())||!new FormulaSnapshot(formula).verifies(data.getStringOr("badspell_checksum",""))){server.sendSystemMessage(Component.translatable("message.badspell.owner"),true);return InteractionResult.SUCCESS;}
            for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i).is(ModItems.RUNE_GLOVE.get())){GloveFormulas.importSnapshot(player.getInventory().getItem(i),formula,player.getUUID());server.sendSystemMessage(Component.translatable("message.badspell.exported"),true);return InteractionResult.SUCCESS;}
            server.sendSystemMessage(Component.translatable("message.badspell.no_glove"),true);
        }catch(java.io.IOException|IllegalArgumentException|java.util.NoSuchElementException error){server.sendSystemMessage(Component.translatable("message.badspell.invalid"),true);}
        return InteractionResult.SUCCESS;
    }
}
