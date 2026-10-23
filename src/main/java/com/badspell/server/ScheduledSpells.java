package com.badspell.server;

import com.badspell.formula.*;
import com.badspell.network.Packets;
import java.io.IOException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;

/** Four bounded, persisted player schedules. Every pulse rechecks energy, rights, range and targets. */
public final class ScheduledSpells {
    private static final String KEY="badspell_schedules";
    private ScheduledSpells(){}
    public static boolean canAdd(ServerPlayer player){return player.getPersistentData().getListOrEmpty(KEY).size()<4;}
    public static void cancel(ServerPlayer player){player.getPersistentData().remove(KEY);}
    public static void add(ServerPlayer player,Formula formula,RuneRegistry.Context context,Value.Event event,boolean firstApplied) throws IOException {
        var jobs=player.getPersistentData().getListOrEmpty(KEY);if(jobs.size()>=4)return;int remaining=event.remaining()<0?-1:event.remaining()-(firstApplied?1:0);if(remaining==0)return;
        var job=new CompoundTag();byte[] bytes=FormulaCodec.encode(formula);if(bytes.length>Packets.MAX_FORMULA_BYTES)throw new IOException("Schedule snapshot limit");job.putByteArray("formula",bytes);
        job.putLong("start",context.start());job.putLong("next",firstApplied?player.level().getGameTime()+event.interval():event.tick());job.putInt("remaining",remaining);job.putInt("interval",event.interval());
        job.putString("dimension",context.target().dimension());job.putInt("x",context.target().x());job.putInt("y",context.target().y());job.putInt("z",context.target().z());
        job.putInt("vx",context.look().x());job.putInt("vy",context.look().y());job.putInt("vz",context.look().z());if(context.entity()!=null)job.store("target",net.minecraft.core.UUIDUtil.CODEC,context.entity().id());
        jobs.add(job);player.getPersistentData().put(KEY,jobs);
    }
    public static void tick(ServerPlayer player) {
        long tick=player.level().getGameTime();var jobs=player.getPersistentData().getListOrEmpty(KEY);if(jobs.isEmpty())return;
        if(jobs.size()>4){cancel(player);return;}
        for(int index=jobs.size()-1;index>=0;index--) {
            var job=jobs.getCompoundOrEmpty(index);if(tick<job.getLongOr("next",Long.MAX_VALUE))continue;
            try {
                byte[] bytes=job.getByteArray("formula").orElseThrow();if(bytes.length>Packets.MAX_FORMULA_BYTES)throw new IllegalArgumentException("Schedule size");var formula=FormulaCodec.decode(bytes);if(!formula.owner().equals(player.getUUID()))throw new IllegalArgumentException("Schedule owner");
                var level=player.level();var target=new BlockPos(job.getIntOr("x",0),job.getIntOr("y",0),job.getIntOr("z",0));
                if(!job.getStringOr("dimension","").equals(level.dimension().identifier().toString())||!level.getChunkSource().hasChunk(target.getX()>>4,target.getZ()>>4)){jobs.remove(index);continue;}
                var fresh=SpellEffects.context(player,target,job.getLongOr("start",tick),false);Value.Entity entity=job.read("target",net.minecraft.core.UUIDUtil.CODEC).map(id->{var raw=level.getEntity(id);return raw==null?null:new Value.Entity(id,SpellEffects.position(level,raw.blockPosition()));}).orElse(null);
                var context=new RuneRegistry.Context(fresh.target(),tick,entity,new Value.Vector(job.getIntOr("vx",0),job.getIntOr("vy",0),job.getIntOr("vz",0)),fresh.environment());
                int remaining=job.getIntOr("remaining",1);if(remaining< -1||remaining>64)throw new IllegalArgumentException("Invalid invocation count");
                var result=MagicActions.execute(player,formula,context);
                if(result==ActivationService.Result.INSUFFICIENT_ENERGY||result==ActivationService.Result.DENIED||result==ActivationService.Result.UNSTABLE){job.putLong("next",tick+20);jobs.set(index,job);continue;}
                int left=remaining<0?-1:remaining-1,interval=job.getIntOr("interval",0);
                if((result!=ActivationService.Result.APPLIED&&result!=ActivationService.Result.SKIPPED)||left==0||interval<20||interval>1200)jobs.remove(index);else{job.putInt("remaining",left);job.putLong("next",tick+interval);jobs.set(index,job);}
            }catch(IOException|IllegalArgumentException|IllegalStateException|java.util.NoSuchElementException error){jobs.remove(index);}
        }
        player.getPersistentData().put(KEY,jobs);
    }
}
