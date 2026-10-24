package com.badspell.conduction;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;

/** Passive directed conductor. It has no inventory, buffer, energy generation or block entity. */
public final class FieldCrystalBlock extends Block {
    public static final MapCodec<FieldCrystalBlock> CODEC=simpleCodec(FieldCrystalBlock::new);
    public static final EnumProperty<Direction> FACING=BlockStateProperties.FACING;
    public FieldCrystalBlock(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.SOUTH));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getNearestLookingDirection());}
    @Override protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state,net.minecraft.world.level.Level level,BlockPos pos,net.minecraft.world.entity.player.Player player,net.minecraft.world.phys.BlockHitResult hit){if(player instanceof net.minecraft.server.level.ServerPlayer server){var next=state.cycle(FACING);level.setBlockAndUpdate(pos,next);server.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.badspell.crystal_direction",net.minecraft.network.chat.Component.translatable("direction.badspell."+next.getValue(FACING).getName())),true);}return net.minecraft.world.InteractionResult.SUCCESS;}
    @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context){return net.minecraft.world.phys.shapes.Shapes.box(.25,.25,.25,.75,.75,.75);}
}
