package com.badspell.production;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** Datapack recipes. Ingredient counts are assigned by bounded max flow, including overlapping tags. */
public record ProcessingRecipe(String station,List<Input> inputs,List<ItemStackTemplate> outputs,int ticks,int energy) implements Recipe<ProcessingRecipe.Inventory> {
    public record Input(Ingredient ingredient,int count) {
        public static final Codec<Input> CODEC=RecordCodecBuilder.create(i->i.group(Ingredient.CODEC.fieldOf("ingredient").forGetter(Input::ingredient),Codec.intRange(1,64).fieldOf("count").forGetter(Input::count)).apply(i,Input::new));
    }
    public record Inventory(Container container,String station) implements RecipeInput {
        public ItemStack getItem(int index){return container.getItem(index);}
        public int size(){return 6;}
    }
    private static final MapCodec<ProcessingRecipe> RAW_CODEC=RecordCodecBuilder.mapCodec(i->i.group(
        Codec.STRING.fieldOf("station").forGetter(ProcessingRecipe::station),Input.CODEC.sizeLimitedListOf(6).fieldOf("inputs").forGetter(ProcessingRecipe::inputs),
        ItemStackTemplate.CODEC.sizeLimitedListOf(4).fieldOf("outputs").forGetter(ProcessingRecipe::outputs),Codec.intRange(1,24000).fieldOf("ticks").forGetter(ProcessingRecipe::ticks),Codec.intRange(0,600).fieldOf("energy").forGetter(ProcessingRecipe::energy)).apply(i,ProcessingRecipe::new));
    public static final MapCodec<ProcessingRecipe> CODEC=RAW_CODEC.validate(recipe->recipe.inputs().isEmpty()||recipe.inputs().size()>6||recipe.outputs().isEmpty()||recipe.outputs().size()>4?com.mojang.serialization.DataResult.error(()->"Recipe needs 1..6 inputs and 1..4 outputs"):com.mojang.serialization.DataResult.success(recipe));
    public ProcessingRecipe {
        inputs=List.copyOf(inputs);outputs=List.copyOf(outputs);

    }
    public int[] allocation(Container inventory) {
        int n=inputs.size(),sink=n+7,nodes=sink+1;int[][] residual=new int[nodes][nodes];int required=0;
        for(int i=0;i<n;i++){residual[0][i+1]=inputs.get(i).count();required+=inputs.get(i).count();for(int slot=0;slot<6;slot++)if(inputs.get(i).ingredient().test(inventory.getItem(slot)))residual[i+1][n+1+slot]=64;}
        for(int slot=0;slot<6;slot++)residual[n+1+slot][sink]=inventory.getItem(slot).getCount();
        int flow=0;
        while(flow<required) {
            int[] parent=new int[nodes];Arrays.fill(parent,-1);parent[0]=0;var queue=new ArrayDeque<Integer>();queue.add(0);
            while(!queue.isEmpty()&&parent[sink]<0){int at=queue.removeFirst();for(int to=1;to<nodes;to++)if(parent[to]<0&&residual[at][to]>0){parent[to]=at;queue.add(to);}}
            if(parent[sink]<0)return null;
            int amount=required-flow;for(int at=sink;at!=0;at=parent[at])amount=Math.min(amount,residual[parent[at]][at]);
            for(int at=sink;at!=0;at=parent[at]){residual[parent[at]][at]-=amount;residual[at][parent[at]]+=amount;}flow+=amount;
        }
        int[] used=new int[6];for(int slot=0;slot<6;slot++)used[slot]=inventory.getItem(slot).getCount()-residual[n+1+slot][sink];return used;
    }
    @Override public boolean matches(Inventory input,Level level){return station.equals(input.station())&&allocation(input.container())!=null;}
    @Override public ItemStack assemble(Inventory input){return outputs.getFirst().create();}
    @Override public boolean isSpecial(){return true;}
    @Override public boolean showNotification(){return false;}
    @Override public String group(){return "badspell";}
    @Override public RecipeSerializer<ProcessingRecipe> getSerializer(){return ProductionRegistries.SERIALIZER.get();}
    @Override public RecipeType<ProcessingRecipe> getType(){return ProductionRegistries.TYPE.get();}
    @Override public PlacementInfo placementInfo(){return PlacementInfo.NOT_PLACEABLE;}
    @Override public RecipeBookCategory recipeBookCategory(){return RecipeBookCategories.CRAFTING_MISC;}
}
