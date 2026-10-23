package com.badspell.server;

import com.badspell.formula.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.*;

/** Prepared server effects. No arbitrary block replacement, unloaded chunks or unconsenting PvP targets. */
public final class SpellEffects {
    private SpellEffects(){}
    public static RuneRegistry.Context context(ServerPlayer player,BlockPos target,long start){return context(player,target,start,true);}
    public static RuneRegistry.Context context(ServerPlayer player,BlockPos target,long start,boolean followLook) {
        var level=player.level();String dim=level.dimension().identifier().toString();var look=player.getLookAngle();
        var hit=player.pick(12,1,false);var entityHit=com.badspell.magic.SpellTargets.entity(player);
        Value.Entity entity=entityHit==null?null:new Value.Entity(entityHit.getEntity().getUUID(),position(level,entityHit.getEntity().blockPosition()));
        Map<String,Value> environment=new HashMap<>();var observed=followLook&&hit instanceof BlockHitResult block?block.getBlockPos():target;
        environment.put("measurement_position",position(level,observed));
        level.getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(16),e->e.isAlive()).stream().sorted(Comparator.comparing(e->e.getUUID().toString())).limit(64).forEach(e->{environment.put("entity:"+e.getUUID(),new Value.Entity(e.getUUID(),position(level,e.blockPosition())));environment.put("health:"+e.getUUID(),new Value.Number((long)e.getHealth()));});
        if(entity!=null){environment.put("entity:"+entity.id(),entity);environment.put("health:"+entity.id(),new Value.Number((long)((LivingEntity)entityHit.getEntity()).getHealth()));}environment.put("entity:"+player.getUUID(),new Value.Entity(player.getUUID(),position(level,player.blockPosition())));
        environment.put("health:"+player.getUUID(),new Value.Number((long)player.getHealth()));
        environment.put("activation_tick",new Value.Number(start));environment.put("self",new Value.Entity(player.getUUID(),position(level,player.blockPosition())));
        environment.put("biome",new Value.Number(level.getBiome(observed).unwrapKey().map(k->k.identifier().toString().hashCode()).orElse(0)));
        if(level.getBlockEntity(observed) instanceof com.badspell.production.StationBlockEntity station&&station.canUse(player))environment.put("field",new Value.Number(station.data.get(2)));
        environment.put("light",new Value.Number(level.getMaxLocalRawBrightness(observed)));environment.put("rain",new Value.Number(level.isRainingAt(observed)?1:0));environment.put("sky",new Value.Number(level.canSeeSky(observed)?1:0));
        environment.put("health",new Value.Number((long)player.getHealth()));environment.put("focus",new Value.Number(player.getPersistentData().getIntOr("badspell_focus",100)));environment.put("stability",new Value.Number(100));
        return new RuneRegistry.Context(position(level,target),level.getGameTime(),entity,new Value.Vector((int)(look.x*1000),(int)(look.y*1000),(int)(look.z*1000)),environment);
    }
    public static Value.Position position(ServerLevel level,BlockPos at){return new Value.Position(level.dimension().identifier().toString(),at.getX(),at.getY(),at.getZ());}
    public static List<BlockPos> positions(Value.Region region) {
        if(region.shape()==Value.Shape.PATH)return region.points().stream().map(p->new BlockPos(p.x(),p.y(),p.z())).toList();
        var p=region.center();var center=new BlockPos(p.x(),p.y(),p.z());var result=new LinkedHashSet<BlockPos>();
        switch(region.shape()) {
            case POINT->result.add(center);
            case PATH->region.points().forEach(at->result.add(new BlockPos(at.x(),at.y(),at.z())));
            case RAY->{var d=region.direction();double length=Math.sqrt((long)d.x()*d.x()+(long)d.y()*d.y()+(long)d.z()*d.z());if(length==0)break;for(int i=0;i<=region.distance();i++)result.add(BlockPos.containing(p.x()+.5+d.x()/length*i,p.y()+.5+d.y()/length*i,p.z()+.5+d.z()/length*i));}
            case CIRCLE,SPHERE->{int r=region.radius();for(int x=-r;x<=r;x++)for(int y=region.shape()==Value.Shape.CIRCLE?0:-r;y<=(region.shape()==Value.Shape.CIRCLE?0:r);y++)for(int z=-r;z<=r;z++)if(x*x+y*y+z*z<=r*r)result.add(center.offset(x,y,z));}
        }
        return List.copyOf(result);
    }
    public static Runnable prepare(ServerPlayer player,Value.Effect effect) {
        var level=player.level();
        if(!effect.children().isEmpty()){
            var actions=new ArrayList<Runnable>();var identities=new HashSet<String>();int newFields=0;int lightCells=0;
            for(var child:effect.children()){
                if(!Set.of("badspell:heal","badspell:push","badspell:see","badspell:light","badspell:heat","badspell:cold","badspell:shield","badspell:none").contains(child.id()))throw new Failure("combine_unsupported");
                String identity=child.id()+":"+(child.target()!=null?child.target().id():child.region());if(!identities.add(identity))throw new Failure("combine_duplicate");
                if(child.id().equals("badspell:none"))continue;
                if(Set.of("badspell:heat","badspell:cold","badspell:shield").contains(child.id())||child.region().shape()==Value.Shape.PATH)newFields++;
                if(child.id().equals("badspell:light"))lightCells+=positions(child.region()).size();
                var action=prepare(player,child);if(action==null)throw new Failure("denied");actions.add(action);
            }
            if(!SpellFields.get(level).canAdd(newFields)||LightFields.get(level).count()+lightCells>1024)throw new Failure("field_limit");
            return ()->actions.forEach(Runnable::run);
        }
        if(!effect.region().center().dimension().equals(level.dimension().identifier().toString()))return null;
        var positions=positions(effect.region());if(positions.isEmpty()||positions.size()>1024)return null;
        for(var at:positions)if(level.isOutsideBuildHeight(at)||!level.getChunkSource().hasChunk(at.getX()>>4,at.getZ()>>4)||!level.getWorldBorder().isWithinBounds(at)||!MagicPermissions.mayUse(player,at,Direction.UP)||at.distSqr(player.blockPosition())>256)return null;
        if(effect.id().equals("badspell:light")&&effect.region().shape()==Value.Shape.PATH){var fields=SpellFields.get(level);if(!fields.canAdd()||positions.stream().anyMatch(at->!level.getBlockState(at).isAir()))return null;return ()->fields.add(player.getUUID(),effect,level.getGameTime());}
        if(effect.id().equals("badspell:light")) {
            var fields=LightFields.get(level);if(positions.size()>1024||fields.count()+positions.size()>1024)return null;
            if(positions.stream().anyMatch(at->!level.getBlockState(at).isAir()||!fields.canAdd(level,at)))return null;
            return ()->positions.forEach(at->fields.add(level,at,effect.strength(),effect.durationTicks()));
        }
        if(List.of("badspell:bind","badspell:seal","badspell:transfer","badspell:transfer_energy").contains(effect.id()))return WorldBindings.prepare(player,effect);
        if(effect.target()!=null) {
            var raw=effect.target().id().equals(player.getUUID())?player:level.getEntity(effect.target().id());if(!(raw instanceof LivingEntity target)||!target.isAlive()||!permitted(player,target)||target.distanceToSqr(player)>256||(target!=player&&!player.hasLineOfSight(target)))throw new Failure("target_unavailable");
            if(effect.id().equals("badspell:heal")) {
                if(target.getHealth()>=target.getMaxHealth())throw new Failure("full_health");
                if(target.hasEffect(MobEffects.REGENERATION)||level.getGameTime()-target.getPersistentData().getLongOr("badspell_last_heal",-1000)<60)throw new Failure("heal_cooldown");
                return ()->{target.addEffect(new MobEffectInstance(MobEffects.REGENERATION,effect.durationTicks(),0));target.getPersistentData().putLong("badspell_last_heal",level.getGameTime());particles(level,target.blockPosition(),ParticleTypes.HEART,5);};
            }
            if(effect.id().equals("badspell:push")) {
                if(target.getMaxHealth()>100||target.isPassenger()||target.getType().getCategory()==MobCategory.MISC)return null;
                var v=effect.vector();double factor=effect.strength()/1000.0;var motion=new Vec3(v.x()*factor,v.y()*factor,v.z()*factor);if(motion.lengthSqr()>4)return null;
                return ()->{target.setDeltaMovement(target.getDeltaMovement().add(motion));target.hurtMarked=true;particles(level,target.blockPosition(),ParticleTypes.CLOUD,5);};
            }
            if(effect.id().equals("badspell:see"))return ()->{player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.badspell.entity_scan",target.getDisplayName(),Math.round(target.getHealth()),Math.round(target.getMaxHealth()),target.getUUID().toString()),false);particles(level,target.blockPosition(),ParticleTypes.ENCHANT,8);};
            return null;
        }
        if(List.of("badspell:heat","badspell:cold","badspell:shield").contains(effect.id())) {
            var fields=SpellFields.get(level);if(!fields.canAdd())return null;
            return ()->{var field=fields.add(player.getUUID(),effect,level.getGameTime());if(effect.id().equals("badspell:cold"))SpellFields.advance(level,player,field,level.getGameTime());};
        }
        return null;
    }
    public static final class Failure extends IllegalArgumentException {
        private static final long serialVersionUID=1L;
        public final String reason;
        public Failure(String reason){super(reason);this.reason=reason;}
    }
    static boolean permitted(ServerPlayer actor,LivingEntity target){return !(target instanceof net.minecraft.world.entity.player.Player)||target.getUUID().equals(actor.getUUID())||(actor.canHarmPlayer((net.minecraft.world.entity.player.Player)target)&&target.getPersistentData().getBooleanOr("badspell_pvp_consent",false));}
    static void particles(ServerLevel level,BlockPos at,net.minecraft.core.particles.SimpleParticleType type,int count){level.sendParticles(type,at.getX()+.5,at.getY()+.5,at.getZ()+.5,count,.4,.4,.4,.01);}
}
