package com.badspell.progression;

import com.badspell.research.ResearchRules;

import com.badspell.formula.*;
import com.badspell.content.ModItems;
import com.badspell.network.Packets;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.PacketDistributor;

/** Authoritative per-world, per-player knowledge. No static player state; survives logout and death. */
public final class ResearchProgress {
    public static final String KEY="badspell_research";
    private ResearchProgress(){}
    public static CompoundTag data(ServerPlayer p){initialize(p);return p.getPersistentData().getCompoundOrEmpty(KEY);}
    public static Set<String> keys(CompoundTag tag,String key){return Set.copyOf(tag.getCompoundOrEmpty(key).keySet());}
    private static void add(CompoundTag tag,String key,String id){var entries=tag.getCompoundOrEmpty(key);entries.putBoolean(id,true);tag.put(key,entries);}
    public static void save(ServerPlayer p,CompoundTag data){p.getPersistentData().put(KEY,data);}
    public static void initialize(ServerPlayer p){
        if(p.getPersistentData().contains(KEY))return;
        var d=new CompoundTag();d.putInt("version",1);ResearchRules.BASICS.forEach(id->add(d,"known",id));
        // One-time migration of server-owned M1 snapshots. Never runs after this profile exists.
        var library=p.getPersistentData().getCompoundOrEmpty(com.badspell.server.FormulaLibrary.KEY);
        for(String id:library.keySet())migrate(p,d,library.getCompoundOrEmpty(id).getByteArray("snapshot").orElse(new byte[0]));
        for(int i=0;i<p.getInventory().getContainerSize();i++){
            var s=p.getInventory().getItem(i);var tag=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
            migrate(p,d,tag.getByteArray("badspell_formula").orElse(new byte[0]));
            var slots=tag.getCompoundOrEmpty("badspell_slots");for(String k:slots.keySet())migrate(p,d,slots.getCompoundOrEmpty(k).getByteArray("badspell_formula").orElse(new byte[0]));
        }
        save(p,d);
    }
    private static void migrate(ServerPlayer p,CompoundTag d,byte[] bytes){if(bytes.length==0||bytes.length>30_000)return;try{var f=FormulaCodec.decode(bytes);if(f.owner().equals(p.getUUID())&&new FormulaValidator(ResearchRules.RUNES).validateDefinition(f).isEmpty())for(String id:ResearchRules.used(f)){add(d,"known",id);if(!ResearchRules.BASICS.contains(id))add(d,"decoded",id);}}catch(java.io.IOException|IllegalArgumentException ignored){/* Invalid old data never grants knowledge. */}}
    public static boolean knows(ServerPlayer p,String rune){return p.isCreative()||keys(data(p),"known").contains(rune);}
    public static boolean allows(ServerPlayer p,Formula formula){return p.isCreative()||ResearchRules.missing(formula,keys(data(p),"known")).isEmpty();}
    public static void authorize(net.minecraft.world.item.ItemStack book,Formula formula){CustomData.update(DataComponents.CUSTOM_DATA,book,t->t.putString("badspell_known_snapshot",new FormulaSnapshot(formula).checksum()));}
    public static boolean authorized(net.minecraft.world.item.ItemStack book,Formula formula){return book.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getStringOr("badspell_known_snapshot","").equals(new FormulaSnapshot(formula).checksum());}
    public static boolean require(ServerPlayer p,Formula f){if(allows(p,f))return true;p.sendSystemMessage(net.minecraft.network.chat.Component.translatable("research.badspell.unknown_formula"),false);return false;}
    public static void learn(ServerPlayer p,String id){if(ResearchRules.RUNES.get(id)==null&&!id.equals("badspell:call"))throw new IllegalArgumentException();var d=data(p);add(d,"known",id);add(d,"decoded",id);save(p,d);}
    public static void formula(ServerPlayer p){var d=data(p);d.putBoolean("formula",true);save(p,d);}
    public static void station(ServerPlayer p,String id){var d=data(p);add(d,"stations",id);save(p,d);}
    public static void observe(ServerPlayer p){var d=data(p);var observed=d.getCompoundOrEmpty("discovered");boolean changed=false;
        for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(s.isEmpty())continue;String id=BuiltInRegistries.ITEM.getKey(s.getItem()).toString();if(observed.keySet().size()<4096&&!observed.contains(id)){observed.putBoolean(id,true);changed=true;}}
        if(changed){d.put("discovered",observed);save(p,d);}
    }
    public static void send(ServerPlayer p,boolean open,String message){send(p,open,message,"");}
    public static void send(ServerPlayer p,boolean open,String message,String focus){observe(p);var d=data(p);var known=new TreeSet<>(keys(d,"known"));if(p.isCreative()){known.addAll(ResearchRules.RUNES.ids());known.add("badspell:call");}
        PacketDistributor.sendToPlayer(p,new Packets.ResearchState(open,ResearchActions.seed(p),List.copyOf(known),List.copyOf(new TreeSet<>(keys(d,"discovered"))),List.copyOf(new TreeSet<>(keys(d,"stations"))),keys(d,"decoded").size(),d.getBooleanOr("formula",false),focus,message));
    }
    public static void login(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event){if(event.getEntity() instanceof ServerPlayer p)send(p,false,"");}
    public static void clone(net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone event){if(event.getOriginal().getPersistentData().contains(KEY))event.getEntity().getPersistentData().put(KEY,event.getOriginal().getPersistentData().getCompoundOrEmpty(KEY).copy());}
    public static void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event){if(event.getEntity() instanceof ServerPlayer p&&p.level().getGameTime()%40==0)observe(p);}
}
