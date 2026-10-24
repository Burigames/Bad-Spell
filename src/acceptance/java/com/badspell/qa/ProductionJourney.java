package com.badspell.qa;

import com.badspell.content.*;
import com.badspell.formula.*;
import com.badspell.network.Packets;
import com.badspell.production.*;
import com.badspell.server.MagicActions;
import com.google.gson.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;

/** Finite raw-material fixture -> actual recipe matching -> actual timed station processing. */
final class ProductionJourney {
    private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger(ProductionJourney.class);
    private final ServerPlayer player;private final JsonObject plan;private final List<ItemStack> stock=new ArrayList<>(),residualCells=new ArrayList<>();
    private final Map<String,StationBlockEntity> stations=new LinkedHashMap<>();private final Map<String,ItemStack> batteries=new HashMap<>();
    private int index;private StationBlockEntity working;private JsonObject current;private long began;private boolean done;
    ProductionJourney(ServerPlayer player){this.player=player;try(var in=ProductionJourney.class.getResourceAsStream("/m1-journey.json")){if(in==null)throw new IllegalStateException("Missing material itinerary");plan=JsonParser.parseReader(new java.io.InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();}catch(java.io.IOException e){throw new IllegalStateException(e);}
        for(var entry:plan.getAsJsonObject("seed_resources").entrySet()){int count=entry.getValue().getAsInt();if(entry.getKey().startsWith("badspell:")){harvest(entry.getKey(),count);continue;}while(count>0){var item=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(entry.getKey())),Math.min(64,count));if(entry.getKey().equals("minecraft:potion"))item.set(DataComponents.POTION_CONTENTS,new net.minecraft.world.item.alchemy.PotionContents(net.minecraft.world.item.alchemy.Potions.WATER));stock.add(item);count-=item.getCount();}}
        if(player.gameMode.isCreative())throw new IllegalStateException("Journey must run in survival mode");LOG.info("M1_JOURNEY start: {} finite crafting/processing steps; no seeded Bad Spell intermediates",plan.getAsJsonArray("steps").size());
    }
    private void harvest(String resource,int needed){String block=switch(resource){case "badspell:raw_field_crystal"->"field_crystal_ore";case "badspell:raw_arcane_ore"->"arcane_ore";case "badspell:runewood_log"->"runewood_log";case "badspell:moon_blossom"->"moon_blossom";case "badspell:resonance_shard"->"resonance_crystal";default->throw new IllegalStateException("Unverified resource fixture "+resource);};var at=new BlockPos(10,81,10);var old=player.getMainHandItem();player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_PICKAXE));int obtained=0,blocks=0;
        try{player.level().setBlockAndUpdate(at.below(),Blocks.GRASS_BLOCK.defaultBlockState());while(obtained<needed){if(++blocks>needed*4+16)throw new IllegalStateException("No valid survival drop for "+resource);if(player.getMainHandItem().isEmpty())player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.IRON_PICKAXE));var state=BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("badspell",block)).defaultBlockState();player.level().setBlockAndUpdate(at,state);if(!player.gameMode.destroyBlock(at))throw new IllegalStateException("Survival mining refused "+block);for(var entity:player.level().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(at).inflate(1))){var stack=entity.getItem();if(id(stack).equals(resource)){obtained+=stack.getCount();add(stack);entity.discard();}}}LOG.info("M1_JOURNEY harvested {} units of {} from {} real blocks in survival mode",obtained,resource,blocks);}finally{player.setItemInHand(InteractionHand.MAIN_HAND,old);player.level().setBlockAndUpdate(at.below(),Blocks.STONE.defaultBlockState());}
    }
    boolean done(){return done;}
    private static String id(ItemStack stack){return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();}
    private int count(String id){return stock.stream().filter(s->id(s).equals(id)).mapToInt(ItemStack::getCount).sum();}
    private ItemStack take(String item,int amount){for(var stack:stock)if(id(stack).equals(item)&&stack.getCount()>=amount){var out=stack.copyWithCount(amount);stack.shrink(amount);return out;}var collected=ItemStack.EMPTY;int left=amount;for(var stack:stock)if(id(stack).equals(item)&&!stack.isEmpty()){int n=Math.min(left,stack.getCount());if(collected.isEmpty())collected=stack.copyWithCount(n);else collected.grow(n);stack.shrink(n);left-=n;if(left==0)return collected;}throw new IllegalStateException("Material underflow: "+item+" x"+amount+" at step "+index);}
    private void add(ItemStack stack){if(!stack.isEmpty())stock.add(stack.copy());}
    void tick(){if(done)return;
        if(working!=null){boolean complete=true;for(var output:current.getAsJsonArray("outputs")){var expected=output.getAsJsonObject();int n=0;for(int slot=7;slot<11;slot++)if(id(working.getItem(slot)).equals(expected.get("id").getAsString()))n+=working.getItem(slot).getCount();if(n<expected.get("count").getAsInt())complete=false;}
            if(!complete){if(player.level().getGameTime()-began>24000)throw new IllegalStateException("Station stalled: "+current+" status="+working.data.get(3));return;}
            for(int slot=7;slot<11;slot++){add(working.getItem(slot));working.setItem(slot,ItemStack.EMPTY);}for(int slot=0;slot<6;slot++){add(working.getItem(slot));working.setItem(slot,ItemStack.EMPTY);}if(current.has("fuel")&&!current.get("fuel").isJsonNull()){batteries.put(current.get("fuel").getAsString(),working.getItem(6));working.setItem(6,ItemStack.EMPTY);}working=null;index++;if(index%25==0)LOG.info("M1_JOURNEY progress {}/{}",index,plan.getAsJsonArray("steps").size());
        }
        for(int instant=0;instant<20&&working==null;instant++){
            if(index>=plan.getAsJsonArray("steps").size()){for(var goal:plan.getAsJsonArray("goals"))if(count("badspell:"+goal.getAsString())<1)throw new IllegalStateException("Final goal absent: "+goal);done=true;LOG.info("M1_JOURNEY PASS: all {} goals, {} actual recipe steps, {} placed stations; {} residual cells retained without duplication",plan.getAsJsonArray("goals").size(),index,stations.size(),residualCells.size());return;}
            current=plan.getAsJsonArray("steps").get(index).getAsJsonObject();
            if(current.has("place")){String name=current.get("place").getAsString();take("badspell:"+name,1);var at=new BlockPos(16+stations.size()*3,81,16);player.level().setBlockAndUpdate(at.below(),Blocks.STONE.defaultBlockState());player.level().setBlockAndUpdate(at,ModContent.STATIONS.get(name).get().defaultBlockState());var block=(StationBlockEntity)player.level().getBlockEntity(at);block.claim(player);stations.put(name,block);index++;continue;}
            if(current.has("adapter")){stations.get(current.get("adapter").getAsString()).setItem(11,take("badspell:cell_adapter",1));index++;continue;}
            if(current.has("station")&&!current.get("station").isJsonNull()){working=stations.get(current.get("station").getAsString());for(int slot=0;slot<6;slot++)if(!working.getItem(slot).isEmpty())throw new IllegalStateException("Uncollected input");
                int slot=0;for(var entry:current.getAsJsonObject("inputs").entrySet()){var input=take(entry.getKey(),entry.getValue().getAsInt());if(entry.getKey().equals("badspell:formula_book")&&current.has("preserve_book"))prepareSignatureBook(input);working.setItem(slot++,input);}
                if(current.has("fuel")&&!current.get("fuel").isJsonNull()){String fuel=current.get("fuel").getAsString();if(current.get("fuel_new").getAsBoolean()){var old=batteries.remove(fuel);if(old!=null)residualCells.add(old);batteries.put(fuel,take(fuel,1));}var cell=batteries.remove(fuel);if(cell==null)throw new IllegalStateException("Missing charged battery");working.setItem(6,cell);}
                began=player.level().getGameTime();return;
            }
            craft(current.get("id").getAsString());index++;
        }
    }
    private static String ingredient(JsonElement value){return value.isJsonPrimitive()?value.getAsString():value.getAsJsonObject().get("items").getAsString();}
    @SuppressWarnings({"rawtypes","unchecked"}) private void craft(String recipeId){JsonObject json;try(var reader=player.level().getServer().getResourceManager().getResource(Identifier.fromNamespaceAndPath("badspell","recipe/"+recipeId+".json")).orElseThrow().openAsReader()){json=JsonParser.parseReader(reader).getAsJsonObject();}catch(java.io.IOException e){throw new IllegalStateException(e);}RecipeInput input;
        if(json.has("ingredient"))input=new SingleRecipeInput(take(ingredient(json.get("ingredient")),1));
        else {var grid=new ArrayList<ItemStack>();int width,height;if(json.has("pattern")){var rows=json.getAsJsonArray("pattern");width=rows.get(0).getAsString().length();height=rows.size();for(var row:rows)for(char key:row.getAsString().toCharArray())grid.add(key==' '?ItemStack.EMPTY:take(ingredient(json.getAsJsonObject("key").get(String.valueOf(key))),1));}else{var entries=json.getAsJsonArray("ingredients");width=3;height=(entries.size()+2)/3;for(var entry:entries)grid.add(take(ingredient(entry),1));while(grid.size()<width*height)grid.add(ItemStack.EMPTY);}input=CraftingInput.of(width,height,grid);}
        var holder=player.level().recipeAccess().byKey(ResourceKey.create(Registries.RECIPE,Identifier.fromNamespaceAndPath("badspell",recipeId))).orElseThrow();Recipe recipe=holder.value();if(!recipe.matches(input,player.level()))throw new IllegalStateException("Real recipe refused its planned grid: "+recipeId);ItemStack result=(ItemStack)recipe.assemble(input);var expected=current.getAsJsonArray("outputs").get(0).getAsJsonObject();if(!id(result).equals(expected.get("id").getAsString())||result.getCount()!=expected.get("count").getAsInt())throw new IllegalStateException("Recipe result differs from actual data: "+recipeId);add(result);
    }
    private void prepareSignatureBook(ItemStack book){var old=player.getMainHandItem();try{player.setItemInHand(InteractionHand.MAIN_HAND,book);MagicActions.openBook(player,InteractionHand.MAIN_HAND);var tag=book.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var saved=FormulaCodec.decode(tag.getByteArray("badspell_formula").orElseThrow());if(saved.nodes().stream().noneMatch(n->n.rune().equals("badspell:sign"))){var signature=ExampleFormulas.template("signature",player.getUUID(),com.badspell.server.SpellEffects.position(player.level(),player.blockPosition()));var draft=new Formula(saved.id(),player.getUUID(),saved.revision(),"Journey signature",signature.nodes(),signature.edges(),signature.output(),1000);MagicActions.handle(player,new Packets.BookAction(0,0,FormulaCodec.encode(draft)));var actual=FormulaCodec.decode(book.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getByteArray("badspell_formula").orElseThrow());if(actual.nodes().stream().noneMatch(n->n.rune().equals("badspell:sign")))throw new IllegalStateException("Real book save was rejected");}}catch(java.io.IOException e){throw new IllegalStateException(e);}finally{player.setItemInHand(InteractionHand.MAIN_HAND,old);}}
}
