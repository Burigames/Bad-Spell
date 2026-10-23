package com.badspell.item;

import com.badspell.formula.*;
import com.badspell.network.Packets;
import java.io.IOException;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Four bounded, independent Q1 snapshots. Item components provide vanilla save and inventory sync. */
public final class GloveFormulas {
    public static final int CAPACITY=4;
    public static int capacity(ItemStack stack){return stack.is(com.badspell.content.ModItems.RUNE_GLOVE_Q3.get())?12:stack.is(com.badspell.content.ModItems.RUNE_GLOVE_Q2.get())?8:4;}
    private static final String SLOTS="badspell_slots", SELECTED="badspell_selected_slot";
    private static final String FORMULA="badspell_formula", CHECKSUM="badspell_checksum";
    private GloveFormulas() {}
    private static CompoundTag tag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
    }
    public static int selected(ItemStack stack) { return Math.clamp(tag(stack).getIntOr(SELECTED,0),0,capacity(stack)-1); }
    private static CompoundTag slot(CompoundTag data,int index) {
        if(data.contains(SLOTS))return data.getCompoundOrEmpty(SLOTS).getCompoundOrEmpty("slot_"+index);
        // Legacy one-slot gloves migrate only on successful writes, never on tooltip/read.
        return index==0?data:new CompoundTag();
    }
    public static boolean occupied(ItemStack stack,int index) {
        return index>=0 && index<capacity(stack) && slot(tag(stack),index).contains(FORMULA);
    }
    public static Formula read(ItemStack stack) throws IOException { return decode(slot(tag(stack),selected(stack))); }
    private static Formula decode(CompoundTag data) throws IOException {
        byte[] bytes=data.getByteArray(FORMULA).orElseThrow(()->new IOException("Empty formula slot"));
        if(bytes.length>Packets.MAX_FORMULA_BYTES)throw new IOException("Oversized formula slot");
        Formula formula=FormulaCodec.decode(bytes);
        if(!new FormulaSnapshot(formula).verifies(data.getStringOr(CHECKSUM,"")) || !new FormulaValidator(RuneRegistry.standard()).validate(formula).isEmpty())
            throw new IOException("Invalid formula slot");
        return formula;
    }
    public static boolean ownedBy(ItemStack stack,UUID owner) throws IOException {
        CompoundTag data=tag(stack);
        for(int i=0;i<capacity(stack);i++) {
            var entry=slot(data,i);
            if(entry.contains(FORMULA) && !decode(entry).owner().equals(owner))return false;
        }
        return true;
    }
    private static CompoundTag migrated(CompoundTag data) {
        if(data.contains(SLOTS))return data.getCompoundOrEmpty(SLOTS).copy();
        var slots=new CompoundTag();
        if(data.contains(FORMULA)) {
            var first=new CompoundTag();first.putByteArray(FORMULA,data.getByteArray(FORMULA).orElseThrow());
            first.putString(CHECKSUM,data.getStringOr(CHECKSUM,""));slots.put("slot_0",first);
        }
        return slots;
    }
    private static void mirror(CompoundTag data,CompoundTag slots,int selected) {
        var entry=slots.getCompoundOrEmpty("slot_"+selected);
        data.remove(FORMULA);data.remove(CHECKSUM);
        if(entry.contains(FORMULA)) {
            data.putByteArray(FORMULA,entry.getByteArray(FORMULA).orElseThrow());
            data.putString(CHECKSUM,entry.getStringOr(CHECKSUM,""));
        }
        data.put(SLOTS,slots);data.putInt(SELECTED,selected);data.putInt("badspell_device_revision",data.getIntOr("badspell_device_revision",0)+1);
    }
    public static void importSnapshot(ItemStack stack,Formula formula,UUID owner) throws IOException {
        if(!formula.owner().equals(owner) || !ownedBy(stack,owner))throw new IOException("Private glove");
        byte[] bytes=FormulaCodec.encode(formula);
        if(bytes.length>Packets.MAX_FORMULA_BYTES || !new FormulaValidator(RuneRegistry.standard()).validate(formula).isEmpty())throw new IOException("Invalid import");
        var data=tag(stack);var slots=migrated(data);var entry=new CompoundTag();
        entry.putByteArray(FORMULA,bytes);entry.putString(CHECKSUM,new FormulaSnapshot(formula).checksum());
        slots.put("slot_"+selected(stack),entry);mirror(data,slots,selected(stack));
        stack.set(DataComponents.CUSTOM_DATA,CustomData.of(data));
    }
    public static boolean current(net.minecraft.server.level.ServerPlayer player,ItemStack stack){var data=tag(stack);var id=data.read("badspell_device",net.minecraft.core.UUIDUtil.CODEC);if(id.isEmpty())return true;var mirror=player.getPersistentData().getCompoundOrEmpty("badspell_glove_backups").getCompoundOrEmpty(id.get().toString());return mirror.getIntOr("badspell_device_revision",0)<=data.getIntOr("badspell_device_revision",0);}
    public static boolean restore(net.minecraft.server.level.ServerPlayer player,ItemStack stack) throws IOException {if(!ownedBy(stack,player.getUUID()))return false;var data=tag(stack);var id=data.read("badspell_device",net.minecraft.core.UUIDUtil.CODEC);if(id.isEmpty())return false;var mirror=player.getPersistentData().getCompoundOrEmpty("badspell_glove_backups").getCompoundOrEmpty(id.get().toString());if(mirror.isEmpty())return false;stack.set(DataComponents.CUSTOM_DATA,CustomData.of(mirror.copy()));return true;}
    public static void backup(net.minecraft.server.level.ServerPlayer player,ItemStack stack){
        var data=tag(stack);var id=data.read("badspell_device",net.minecraft.core.UUIDUtil.CODEC).orElseGet(UUID::randomUUID);data.store("badspell_device",net.minecraft.core.UUIDUtil.CODEC,id);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(data));
        var backups=player.getPersistentData().getCompoundOrEmpty("badspell_glove_backups");backups.put(id.toString(),data.copy());player.getPersistentData().put("badspell_glove_backups",backups);
    }
    public static void cycle(ItemStack stack,int direction,UUID owner) throws IOException {
        if(direction!=-1 && direction!=1)throw new IllegalArgumentException("Slot step must be -1 or 1");
        if(!ownedBy(stack,owner))throw new IOException("Private glove");
        var data=tag(stack);var slots=migrated(data);
        mirror(data,slots,Math.floorMod(selected(stack)+direction,capacity(stack)));
        stack.set(DataComponents.CUSTOM_DATA,CustomData.of(data));
    }
}
