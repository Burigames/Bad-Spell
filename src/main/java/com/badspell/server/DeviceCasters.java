package com.badspell.server;

import com.badspell.production.StationBlockEntity;
import com.badspell.production.StationBlock;
import com.mojang.authlib.GameProfile;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.neoforged.neoforge.common.util.FakePlayer;

/** An unspawned permission actor; never teleports or borrows a real player or the global FakePlayer cache. */
public final class DeviceCasters {
    private static final Map<ServerLevel,Map<UUID,FakePlayer>> ACTORS=new WeakHashMap<>();
    private DeviceCasters(){}
    public static boolean isDevice(ServerPlayer player){return player instanceof FakePlayer&&player.getPersistentData().getBooleanOr("badspell_device",false);}
    public static BlockPos anchor(ServerPlayer player){var tag=player.getPersistentData();return new BlockPos(tag.getIntOr("badspell_device_x",0),tag.getIntOr("badspell_device_y",0),tag.getIntOr("badspell_device_z",0));}
    public static ServerPlayer at(ServerLevel level,UUID owner,BlockPos pos){
        var player=ACTORS.computeIfAbsent(level,k->new HashMap<>()).computeIfAbsent(owner,id->new FakePlayer(level,new GameProfile(id,"BadSpellDevice")));
        player.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);player.setXRot(0);
        var state=level.getBlockState(pos);var face=state.hasProperty(StationBlock.FACING)?state.getValue(StationBlock.FACING):net.minecraft.core.Direction.SOUTH;
        float yaw=switch(face){case SOUTH->0;case WEST->90;case NORTH->180;default->270;};player.setYRot(yaw);player.setYHeadRot(yaw);
        var tag=player.getPersistentData();tag.putBoolean("badspell_device",true);tag.putInt("badspell_device_x",pos.getX());tag.putInt("badspell_device_y",pos.getY());tag.putInt("badspell_device_z",pos.getZ());return player;
    }
    public static ServerPlayer resolve(ServerLevel level,UUID owner,BlockPos pos){
        if(!level.getChunkSource().hasChunk(pos.getX()>>4,pos.getZ()>>4))return null;
        return level.getBlockEntity(pos) instanceof StationBlockEntity station&&owner.equals(station.ownerId())?at(level,owner,pos):null;
    }
}
