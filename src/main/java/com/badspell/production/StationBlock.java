package com.badspell.production;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class StationBlock extends BaseEntityBlock {
    public static final MapCodec<StationBlock> CODEC=simpleCodec(StationBlock::new);
    public StationBlock(Properties properties){super(properties);}
    @Override public MapCodec<StationBlock> codec(){return CODEC;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new StationBlockEntity(pos,state);}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        if(level instanceof ServerLevel&&level.getBlockEntity(pos) instanceof StationBlockEntity station){if(station.canUse(player)){station.claim(player);player.openMenu(station);}else if(player instanceof net.minecraft.server.level.ServerPlayer server)server.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.badspell.machine_private"),true);}
        return InteractionResult.SUCCESS;
    }
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,LivingEntity placer,ItemStack stack){if(placer instanceof Player player&&level.getBlockEntity(pos) instanceof StationBlockEntity station)station.claim(player);}
    @Override protected void affectNeighborsAfterRemoval(BlockState state,ServerLevel level,BlockPos pos,boolean moved){Containers.updateNeighboursAfterDestroy(state,level,pos);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level.isClientSide()?null:createTickerHelper(type,ProductionRegistries.STATION.get(),StationBlockEntity::tick);}
}
