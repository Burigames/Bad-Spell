package com.badspell.content;

import com.badspell.server.ToolActions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class UtilityItem extends Item {
    private final int kind;
    public UtilityItem(Properties properties,int kind){super(properties);this.kind=kind;}
    @Override public InteractionResult useOn(UseOnContext context){if(context.getPlayer() instanceof ServerPlayer player){ToolActions.useBlock(player,context.getHand(),context.getClickedPos(),context.getClickedFace(),kind);return InteractionResult.SUCCESS;}return InteractionResult.SUCCESS;}
    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){
        if(player instanceof ServerPlayer server){if(kind==3&&!player.isShiftKeyDown())return super.use(level,player,hand);ToolActions.useAir(server,hand,kind);}return InteractionResult.SUCCESS;
    }
}
