package com.badspell.production;

import com.badspell.BadSpell;
import com.badspell.content.ModContent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class ProductionRegistries {
    private static final DeferredRegister<RecipeType<?>> TYPES=DeferredRegister.create(Registries.RECIPE_TYPE,BadSpell.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS=DeferredRegister.create(Registries.RECIPE_SERIALIZER,BadSpell.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,BadSpell.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,BadSpell.MOD_ID);
    public static final DeferredHolder<RecipeType<?>,RecipeType<ProcessingRecipe>> TYPE=TYPES.register("processing",()->new RecipeType<>(){});
    public static final DeferredHolder<RecipeSerializer<?>,RecipeSerializer<ProcessingRecipe>> SERIALIZER=SERIALIZERS.register("processing",()->new RecipeSerializer<>(ProcessingRecipe.CODEC,ByteBufCodecs.fromCodecWithRegistries(ProcessingRecipe.CODEC.codec())));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<StationBlockEntity>> STATION=ENTITIES.register("station",()->new BlockEntityType<>(StationBlockEntity::new,ModContent.STATIONS.values().stream().map(h->h.get()).collect(java.util.stream.Collectors.toSet())));
    public static final DeferredHolder<MenuType<?>,MenuType<StationMenu>> MENU=MENUS.register("station",()->new MenuType<>(StationMenu::new,FeatureFlags.VANILLA_SET));
    private ProductionRegistries(){}
    public static void register(IEventBus bus){TYPES.register(bus);SERIALIZERS.register(bus);ENTITIES.register(bus);MENUS.register(bus);}
}
