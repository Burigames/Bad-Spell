package com.badspell.content;

import com.badspell.BadSpell;
import com.badspell.production.StationBlock;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/** Only materials with actual processing recipes and working blocks enter the catalogue. */
public final class ModContent {
    public static final List<String> MATERIAL_IDS=List.of("raw_field_crystal","coarse_crystal_shard","field_crystal_shard_q0","field_crystal_shard","crystal_dust","sandpaper","raw_arcane_ore","impure_arcane_charge","arcane_ingot_q0","arcane_ingot","arcane_alloy","arcane_dust","raw_crystal_core","crystal_core","resonance_shard","resonance_glass","resonance_core_blank","resonance_core","rune_resin","conductive_paste","precision_coil","pure_water","moon_brew","moon_essence","moon_salt","aether_solution","glow_fiber","glow_moss_bundle","binding_compound","stabilizer_seal","blank_rune_plate","precision_lens","raw_formula_book","raw_rune_glove","empty_field_cell","cell_adapter");
    public static final Map<String,DeferredItem<Item>> MATERIALS=new LinkedHashMap<>();
    public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(BadSpell.MOD_ID);
    public static final Map<String,DeferredBlock<StationBlock>> STATIONS=new LinkedHashMap<>();
    public static final DeferredBlock<Block> FIELD_ORE=BLOCKS.registerSimpleBlock("field_crystal_ore",p->p.strength(3).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> ARCANE_ORE=BLOCKS.registerSimpleBlock("arcane_ore",p->p.strength(4).requiresCorrectToolForDrops());
    public static final DeferredBlock<RotatedPillarBlock> RUNEWOOD=BLOCKS.registerBlock("runewood_log",RotatedPillarBlock::new,p->p.strength(2).sound(SoundType.WOOD));
    public static final DeferredBlock<Block> RUNEWOOD_PLANKS=BLOCKS.registerSimpleBlock("runewood_planks",p->p.strength(2).sound(SoundType.WOOD));
    public static final DeferredBlock<SaplingBlock> RUNEWOOD_SAPLING=BLOCKS.registerBlock("runewood_sapling",p->new SaplingBlock(new net.minecraft.world.level.block.grower.TreeGrower("badspell_runewood",Optional.empty(),Optional.of(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.CONFIGURED_FEATURE,Identifier.fromNamespaceAndPath("badspell","runewood"))),Optional.empty()),p),p->p.noCollision().randomTicks().instabreak().sound(SoundType.GRASS));
    public static final DeferredBlock<TintedParticleLeavesBlock> RUNEWOOD_LEAVES=BLOCKS.registerBlock("runewood_leaves",p->new TintedParticleLeavesBlock(.01f,p),p->p.strength(.2f).randomTicks().noOcclusion().isSuffocating((s,l,at)->false).isViewBlocking((s,l,at)->false).sound(SoundType.GRASS));
    public static final DeferredBlock<Block> RESONANCE_CRYSTAL=BLOCKS.registerSimpleBlock("resonance_crystal",p->p.strength(1.5f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST).lightLevel(s->2));
    public static final DeferredBlock<FlowerBlock> MOON_BLOSSOM=BLOCKS.registerBlock("moon_blossom",p->new FlowerBlock(MobEffects.NIGHT_VISION,3,p),p->p.noCollision().instabreak().sound(SoundType.GRASS));
    public static final DeferredBlock<GlowLichenBlock> GLOW_MOSS=BLOCKS.registerBlock("glow_moss",GlowLichenBlock::new,p->p.noCollision().instabreak().lightLevel(s->5).sound(SoundType.GLOW_LICHEN));
    public static final DeferredBlock<Block> ARCANE_STONE=BLOCKS.registerSimpleBlock("arcane_stone",p->p.strength(3,12));
    static {
        for(String id:MATERIAL_IDS)MATERIALS.put(id,ModItems.ITEMS.registerSimpleItem(id));
        ModItems.ITEMS.registerItem("inscribed_rune_plate",RunePlateItem::new,p->p.stacksTo(1));
        for(String id:List.of("arcane_workbench","crystal_grinder","alchemy_table","arcane_smelter","rune_press","engraving_station","rune_holder"))STATIONS.put(id,BLOCKS.registerBlock(id,StationBlock::new,p->p.strength(3.5f).requiresCorrectToolForDrops()));
        BLOCKS.getEntries().forEach(block->ModItems.ITEMS.registerSimpleBlockItem(block));
    }
    private ModContent(){}
    public static Item item(String id){return BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(BadSpell.MOD_ID,id));}
    public static void register(IEventBus bus){BLOCKS.register(bus);}
    public static void bootstrap(){}
}
