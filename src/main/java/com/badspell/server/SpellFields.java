package com.badspell.server;

import com.badspell.formula.Value;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.saveddata.*;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Paid, finite-duration fields; never load chunks. Expiry and owner survive world saves. */
public final class SpellFields extends SavedData {
    public record Field(UUID owner,String effect,List<BlockPos> cells,int strength,long expiry,long started,boolean moving) {
        static final Codec<Field> CODEC=RecordCodecBuilder.create(i->i.group(UUIDUtil.CODEC.fieldOf("owner").forGetter(Field::owner),Codec.STRING.fieldOf("effect").forGetter(Field::effect),BlockPos.CODEC.sizeLimitedListOf(1024).fieldOf("cells").forGetter(Field::cells),Codec.intRange(1,8).fieldOf("strength").forGetter(Field::strength),Codec.LONG.fieldOf("expiry").forGetter(Field::expiry),Codec.LONG.optionalFieldOf("started",0L).forGetter(Field::started),Codec.BOOL.optionalFieldOf("moving",false).forGetter(Field::moving)).apply(i,Field::new));
    }
    private final List<Field> fields;
    private static final Codec<SpellFields> CODEC=Field.CODEC.sizeLimitedListOf(128).xmap(SpellFields::new,data->List.copyOf(data.fields));
    public static final SavedDataType<SpellFields> TYPE=new SavedDataType<>(Identifier.fromNamespaceAndPath("badspell","spell_fields"),()->new SpellFields(List.of()),CODEC);
    private SpellFields(List<Field> fields){this.fields=new ArrayList<>(fields);}
    public static SpellFields get(ServerLevel level){return level.getDataStorage().computeIfAbsent(TYPE);}
    public boolean canAdd(){return fields.size()<128;}
    public void add(UUID owner,Value.Effect effect,long tick){fields.add(new Field(owner,effect.id(),SpellEffects.positions(effect.region()),effect.strength(),tick+effect.durationTicks(),tick,effect.region().shape()==Value.Shape.PATH));setDirty();}
    public static void incoming(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event){
        if(!(event.getEntity().level() instanceof ServerLevel level)||!(event.getSource().getDirectEntity() instanceof Projectile))return;
        var at=event.getEntity().blockPosition();for(var field:get(level).fields)if(field.effect().equals("badspell:shield")&&field.owner().equals(event.getEntity().getUUID())&&field.expiry()>level.getGameTime()&&(field.cells().contains(at)||field.cells().contains(at.below()))){event.setCanceled(true);return;}
    }
    public static void tick(LevelTickEvent.Post event) {
        if(!(event.getLevel() instanceof ServerLevel level))return;var self=get(level);long tick=level.getGameTime();
        if(self.fields.removeIf(f->f.expiry()<=tick))self.setDirty();if(tick%10!=0)return;
        for(var field:self.fields) {
            var owner=level.getServer().getPlayerList().getPlayer(field.owner());if(owner==null||owner.level()!=level)continue;
            advance(level,owner,field,tick);
        }
    }
    public List<Field> forOwner(UUID owner){return fields.stream().filter(f->f.owner().equals(owner)).toList();}
    static void advance(ServerLevel level,net.minecraft.server.level.ServerPlayer owner,Field field,long tick){
        if(field.expiry()<=tick)return;
            var active=field.moving()?List.of(field.cells().get(Math.clamp((int)((tick-field.started())*field.cells().size()/Math.max(1,field.expiry()-field.started())),0,field.cells().size()-1))):field.cells();
            var cells=active.stream().filter(at->level.getChunkSource().hasChunk(at.getX()>>4,at.getZ()>>4)).toList();if(cells.isEmpty())return;
            if(field.effect().equals("badspell:light")){var lights=LightFields.get(level);var at=cells.getFirst();if(level.getBlockState(at).isAir()&&level.mayInteract(owner,at)&&lights.canAdd(level,at))lights.add(level,at,field.strength(),(int)Math.min(20,field.expiry()-tick));return;}
            int minX=cells.stream().mapToInt(BlockPos::getX).min().orElseThrow(),maxX=cells.stream().mapToInt(BlockPos::getX).max().orElseThrow();int minY=cells.stream().mapToInt(BlockPos::getY).min().orElseThrow(),maxY=cells.stream().mapToInt(BlockPos::getY).max().orElseThrow();int minZ=cells.stream().mapToInt(BlockPos::getZ).min().orElseThrow(),maxZ=cells.stream().mapToInt(BlockPos::getZ).max().orElseThrow();
            var box=new AABB(minX,minY,minZ,maxX+1,maxY+2,maxZ+1);var occupied=new HashSet<>(cells);
            if(field.effect().equals("badspell:shield")) {
                for(var projectile:level.getEntitiesOfClass(Projectile.class,box,p->p.isAlive()&&p.getOwner()!=owner))if(occupied.contains(projectile.blockPosition())||occupied.contains(projectile.blockPosition().below())){projectile.discard();SpellEffects.particles(level,projectile.blockPosition(),ParticleTypes.ENCHANT,3);}
                for(var target:level.getEntitiesOfClass(LivingEntity.class,box,e->e!=owner&&e.isAlive()))if(SpellEffects.permitted(owner,target)&&(occupied.contains(target.blockPosition())||occupied.contains(target.blockPosition().below()))){double x=target.getX()-(minX+maxX+1)/2.0,z=target.getZ()-(minZ+maxZ+1)/2.0;double length=Math.max(.1,Math.sqrt(x*x+z*z));target.push(x/length*.15*field.strength(),.05,z/length*.15*field.strength());}
            } else for(var target:level.getEntitiesOfClass(LivingEntity.class,box,e->e.isAlive()))if(SpellEffects.permitted(owner,target)&&(occupied.contains(target.blockPosition())||occupied.contains(target.blockPosition().below()))) {
                if(field.effect().equals("badspell:cold")){target.clearFire();target.setTicksFrozen(Math.min(140,target.getTicksFrozen()+20*field.strength()));target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS,30,field.strength()-1));}
                else if(tick%20==0&&target!=owner){target.hurtServer(level,level.damageSources().playerAttack(owner),field.strength());}
            }
            if(tick%20==0)for(int index=0;index<cells.size();index+=Math.max(1,cells.size()/8))SpellEffects.particles(level,cells.get(index),field.effect().equals("badspell:cold")?ParticleTypes.SNOWFLAKE:field.effect().equals("badspell:heat")?ParticleTypes.FLAME:ParticleTypes.ENCHANT,1);
    }
}
