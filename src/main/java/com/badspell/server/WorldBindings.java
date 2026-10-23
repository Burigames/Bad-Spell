package com.badspell.server;

import com.badspell.formula.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.*;
import com.badspell.production.StationBlockEntity;

/** Finite local bindings and owner seals. Resource moves are planned completely before any mutation. */
public final class WorldBindings extends SavedData {
    record Binding(UUID owner,String kind,BlockPos source,BlockPos target,long expiry){
        static final Codec<Binding> CODEC=RecordCodecBuilder.create(i->i.group(UUIDUtil.CODEC.fieldOf("owner").forGetter(Binding::owner),Codec.STRING.fieldOf("kind").forGetter(Binding::kind),BlockPos.CODEC.fieldOf("source").forGetter(Binding::source),BlockPos.CODEC.fieldOf("target").forGetter(Binding::target),Codec.LONG.fieldOf("expiry").forGetter(Binding::expiry)).apply(i,Binding::new));
    }
    private final List<Binding> bindings;
    private static final Codec<WorldBindings> CODEC=Binding.CODEC.sizeLimitedListOf(4096).xmap(WorldBindings::new,data->List.copyOf(data.bindings));
    public static final SavedDataType<WorldBindings> TYPE=new SavedDataType<>(Identifier.fromNamespaceAndPath("badspell","bindings"),()->new WorldBindings(List.of()),CODEC);
    private WorldBindings(List<Binding> stored){bindings=new ArrayList<>(stored);}
    private static WorldBindings get(ServerLevel level){var data=level.getDataStorage().computeIfAbsent(TYPE);if(data.bindings.removeIf(b->b.expiry()<=level.getGameTime()))data.setDirty();return data;}
    public static boolean sealed(ServerLevel level,BlockPos at,UUID actor){return get(level).bindings.stream().anyMatch(b->b.kind().equals("seal")&&b.source().equals(at)&&!b.owner().equals(actor));}
    public static void interact(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event){if(event.getEntity() instanceof ServerPlayer player&&sealed(player.level(),event.getPos(),player.getUUID())){event.setCanceled(true);player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.badspell.private"),true);}}
    public static void breakBlock(net.neoforged.neoforge.event.level.block.BreakBlockEvent event){if(event.getPlayer() instanceof ServerPlayer player&&sealed(player.level(),event.getPos(),player.getUUID())){event.setCanceled(true);event.setNotifyClient(true);}}
    public static Runnable prepare(ServerPlayer player,Value.Effect effect) {
        var level=player.level();var p=effect.region().center();var source=new BlockPos(p.x(),p.y(),p.z());var v=effect.vector();var target=source.offset(v.x()/1000,v.y()/1000,v.z()/1000);
        if(!level.getChunkSource().hasChunk(target.getX()>>4,target.getZ()>>4)||target.distSqr(player.blockPosition())>256||!level.getWorldBorder().isWithinBounds(target)||!MagicPermissions.mayUse(player,target,Direction.UP)||sealed(level,source,player.getUUID())||sealed(level,target,player.getUUID()))return null;
        var self=get(level);if(effect.id().equals("badspell:seal")){
            if(level.getBlockState(source).isAir()||self.bindings.size()>=4096)return null;
            return ()->{self.bindings.removeIf(b->b.kind().equals("seal")&&b.source().equals(source)&&b.owner().equals(player.getUUID()));if(effect.strength()==1)self.bindings.add(new Binding(player.getUUID(),"seal",source,source,level.getGameTime()+effect.durationTicks()));self.setDirty();};
        }
        if(source.equals(target))return null;
        if(effect.id().equals("badspell:bind")){
            if(self.bindings.size()>=4096||level.getBlockState(source).isAir()||level.getBlockState(target).isAir())return null;
            if(level.getBlockEntity(source) instanceof StationBlockEntity a&&!a.canUse(player)||level.getBlockEntity(target) instanceof StationBlockEntity b&&!b.canUse(player))return null;
            return ()->{self.bindings.removeIf(b->b.kind().equals("bind")&&b.source().equals(source)&&b.owner().equals(player.getUUID()));self.bindings.add(new Binding(player.getUUID(),"bind",source,target,level.getGameTime()+effect.durationTicks()));self.setDirty();};
        }
        if(self.bindings.stream().noneMatch(b->b.kind().equals("bind")&&b.owner().equals(player.getUUID())&&b.source().equals(source)&&b.target().equals(target)))return null;
        var a=level.getBlockEntity(source);var b=level.getBlockEntity(target);
        if(effect.id().equals("badspell:transfer_energy")){
            if(!(a instanceof StationBlockEntity from)||!(b instanceof StationBlockEntity to)||!from.canUse(player)||!to.canUse(player)||from.energyStored()<effect.strength()||to.energyCapacity()-to.energyStored()<effect.strength())return null;
            return ()->{from.changeEnergy(-effect.strength());to.changeEnergy(effect.strength());};
        }
        if(!(a instanceof Container from)||!(b instanceof Container to)||a==b)return null;
        if(a instanceof StationBlockEntity fromStation&&!fromStation.canUse(player)||b instanceof StationBlockEntity toStation&&!toStation.canUse(player))return null;
        int input=-1;for(int slot=0;slot<from.getContainerSize();slot++)if(!from.getItem(slot).isEmpty()&&(!(from instanceof WorldlyContainer sided)||sided.canTakeItemThroughFace(slot,from.getItem(slot),Direction.DOWN))){input=slot;break;}if(input<0)return null;
        var original=from.getItem(input);int amount=Math.min(effect.strength(),original.getCount());var outputs=new ArrayList<ItemStack>();for(int slot=0;slot<to.getContainerSize();slot++)outputs.add(to.getItem(slot).copy());int left=amount;
        for(int slot=0;slot<outputs.size()&&left>0;slot++)if(to.canPlaceItem(slot,original)&&(!(to instanceof WorldlyContainer sided)||sided.canPlaceItemThroughFace(slot,original,Direction.UP))){var dest=outputs.get(slot);if(!dest.isEmpty()&&!ItemStack.isSameItemSameComponents(dest,original))continue;int capacity=Math.min(original.getMaxStackSize(),to.getMaxStackSize(original))-dest.getCount();int count=Math.min(left,capacity);if(count>0){outputs.set(slot,original.copyWithCount(dest.getCount()+count));left-=count;}}
        if(left!=0)return null;final int selected=input;return ()->{from.getItem(selected).shrink(amount);for(int slot=0;slot<outputs.size();slot++)to.setItem(slot,outputs.get(slot));from.setChanged();to.setChanged();};
    }
}
