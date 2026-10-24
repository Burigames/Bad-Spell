package com.badspell.conduction;

import com.badspell.production.StationBlockEntity;
import com.badspell.server.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;

/** Demand-driven, bounded upstream walk. Only the cells at the two endpoints store FL. */
public final class CrystalConduction {
    private record Step(BlockPos pos,List<BlockPos> path){}
    private static final class Usage {long second;int spent;Usage(long second){this.second=second;}}
    private static final Map<ServerLevel,Map<Long,Usage>> USAGE=new WeakHashMap<>();
    private CrystalConduction(){}
    public static int pull(StationBlockEntity receiver,int required){
        if(!(receiver.getLevel() instanceof ServerLevel level)||receiver.ownerId()==null||!receiver.hasAdapter()||receiver.energyCapacity()==0)return 0;
        int missing=Math.min(required,receiver.energyCapacity())-receiver.energyStored();if(missing<=0)return 0;
        long second=level.getGameTime()/20;var usage=USAGE.computeIfAbsent(level,k->new HashMap<>());usage.entrySet().removeIf(e->e.getValue().second!=second);
        var actor=DeviceCasters.at(level,receiver.ownerId(),receiver.getBlockPos());if(!MagicPermissions.mayUse(actor,receiver.getBlockPos(),Direction.UP))return 0;
        var queue=new ArrayDeque<Step>();var visited=new HashSet<BlockPos>();queue.add(new Step(receiver.getBlockPos(),List.of()));int moved=0;
        while(!queue.isEmpty()&&visited.size()<128&&missing>0){var step=queue.removeFirst();if(step.path().size()>=16)continue;
            for(var face:Direction.values()){
                var at=step.pos().relative(face);if(!level.getChunkSource().hasChunk(at.getX()>>4,at.getZ()>>4)||visited.contains(at))continue;var state=level.getBlockState(at);
                if(state.getBlock() instanceof FieldCrystalBlock&&at.relative(state.getValue(FieldCrystalBlock.FACING)).equals(step.pos())){visited.add(at);var path=new ArrayList<>(step.path());path.add(at.immutable());queue.addLast(new Step(at,List.copyOf(path)));continue;}
                if(step.path().isEmpty()||!(level.getBlockEntity(at) instanceof StationBlockEntity source)||source==receiver||!receiver.ownerId().equals(source.ownerId())||!source.hasAdapter()||source.energyStored()==0)continue;
                if(!MagicPermissions.mayUse(actor,at,face.getOpposite()))continue;int available=Math.min(missing,source.energyStored());
                for(var conductor:step.path()){var budget=usage.get(conductor.asLong());available=Math.min(available,80-(budget==null?0:budget.spent));}
                if(available<=0)continue;
                source.changeEnergy(-available);receiver.changeEnergy(available);for(var conductor:step.path())usage.computeIfAbsent(conductor.asLong(),k->new Usage(second)).spent+=available;missing-=available;moved+=available;
            }
        }
        return moved;
    }
}
