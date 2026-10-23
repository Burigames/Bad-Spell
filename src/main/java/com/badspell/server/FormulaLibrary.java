package com.badspell.server;

import com.badspell.formula.*;
import com.badspell.network.Packets;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.PacketDistributor;

/** Private player library; books keep active copies, calls embed immutable copies rather than live references. */
public final class FormulaLibrary {
    public static final String KEY="badspell_library";
    private FormulaLibrary(){}
    public static void track(ServerPlayer player,Formula formula,net.minecraft.world.item.ItemStack book) throws java.io.IOException {
        var library=player.getPersistentData().getCompoundOrEmpty(KEY);String key=formula.id().toString();var old=library.getCompoundOrEmpty(key);
        if(library.keySet().size()>=256&&!library.contains(key))throw new java.io.IOException("Library limit 256");
        if(old.contains("snapshot")){var existing=FormulaCodec.decode(old.getByteArray("snapshot").orElseThrow());if(existing.revision()>formula.revision())return;if(existing.revision()==formula.revision()&&!new FormulaSnapshot(existing).checksum().equals(new FormulaSnapshot(formula).checksum()))throw new java.io.IOException("Conflicting library revision");}
        old.putByteArray("snapshot",FormulaCodec.encode(formula));old.put("history",book.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getListOrEmpty("badspell_history"));library.put(key,old);player.getPersistentData().put(KEY,library);
    }
    public static void handle(ServerPlayer player,Packets.LibraryAction packet) {
        if(packet.hand()<0||packet.hand()>1||packet.action()<0||packet.action()>4)return;
        var hand=InteractionHand.values()[packet.hand()];var book=player.getItemInHand(hand);if(!book.is(com.badspell.content.ModItems.FORMULA_BOOK.get()))return;
        long tick=player.level().getGameTime();if(tick-player.getPersistentData().getLongOr("badspell_library_request",-1000)<2)return;player.getPersistentData().putLong("badspell_library_request",tick);
        try {
            var current=MagicActions.read(book);if(!current.owner().equals(player.getUUID()))return;track(player,current,book);var library=player.getPersistentData().getCompoundOrEmpty(KEY);
            if(packet.action()==1) {
                if(library.keySet().size()>=256)throw new IllegalArgumentException("Library full");int ink=book.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntOr("badspell_ink",0);if(ink<5)throw new IllegalArgumentException("Ink");
                var example=ExampleFormulas.template(packet.tags(),player.getUUID(),SpellEffects.position(player.level(),player.blockPosition()));int creationCost=Math.min(8,example.nodes().size());if(ink<creationCost)throw new IllegalArgumentException("Ink");var created=new Formula(UUID.randomUUID(),player.getUUID(),1,packet.title().isBlank()?example.title():packet.title(),example.nodes(),example.edges(),example.output(),example.energyBudget());
                CustomData.update(DataComponents.CUSTOM_DATA,book,t->{t.remove("badspell_history");t.putInt("badspell_ink",ink-creationCost);});MagicActions.write(book,created);track(player,created,book);MagicActions.openBook(player,hand);return;
            }
            if(packet.action()==2) {
                var entry=library.getCompoundOrEmpty(packet.id().toString());var bytes=entry.getByteArray("snapshot").orElseThrow();if(bytes.length>Packets.MAX_FORMULA_BYTES)throw new IllegalArgumentException();var selected=FormulaCodec.decode(bytes);
                if(!selected.owner().equals(player.getUUID())||!selected.id().equals(packet.id()))return;
                MagicActions.write(book,selected);CustomData.update(DataComponents.CUSTOM_DATA,book,t->t.put("badspell_history",entry.getListOrEmpty("history")));MagicActions.openBook(player,hand);return;
            }
            if(packet.action()==3){if(current.id().equals(packet.id()))return;library.remove(packet.id().toString());player.getPersistentData().put(KEY,library);}
            if(packet.action()==4){var entry=library.getCompoundOrEmpty(packet.id().toString());if(!entry.contains("snapshot"))return;entry.putString("tags",packet.tags());library.put(packet.id().toString(),entry);player.getPersistentData().put(KEY,library);}
            int page=Math.clamp(packet.page(),0,31);String query=packet.title().toLowerCase(java.util.Locale.ROOT);var ids=library.keySet().stream().filter(id->{if(query.isBlank())return true;var entry=library.getCompoundOrEmpty(id);try{return (FormulaCodec.decode(entry.getByteArray("snapshot").orElseThrow()).title()+" "+entry.getStringOr("tags","")).toLowerCase(java.util.Locale.ROOT).contains(query);}catch(java.io.IOException error){return false;}}).sorted().toList();var entries=new ArrayList<Packets.LibraryEntry>();
            for(String id:ids.stream().skip(page*8L).limit(8).toList()) {
                var entry=library.getCompoundOrEmpty(id);var bytes=entry.getByteArray("snapshot").orElseThrow();if(bytes.length>Packets.MAX_FORMULA_BYTES)continue;var formula=FormulaCodec.decode(bytes);
                if(!formula.owner().equals(player.getUUID()))continue;entries.add(new Packets.LibraryEntry(bytes,entry.getStringOr("tags","")));
            }
            PacketDistributor.sendToPlayer(player,new Packets.LibraryPage(packet.hand(),page,ids.size(),List.copyOf(entries)));
        }catch(java.io.IOException|IllegalArgumentException|NoSuchElementException error){player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.badspell.invalid"),true);}
    }
    public static void clone(net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone event) {
        var old=event.getOriginal().getPersistentData();for(String key:List.of(KEY,"badspell_glove_backups"))if(old.contains(key))event.getEntity().getPersistentData().put(key,old.getCompoundOrEmpty(key).copy());
    }
}
