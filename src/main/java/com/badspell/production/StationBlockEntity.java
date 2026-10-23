package com.badspell.production;

import com.badspell.content.*;
import com.badspell.formula.*;
import com.badspell.field.EnergyAccount;
import com.badspell.server.LightFields;
import com.mojang.serialization.JsonOps;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

/** Real timed processing. Inputs and FL remain intact until a single server-thread output commit. */
public final class StationBlockEntity extends BlockEntity implements WorldlyContainer,MenuProvider {
    private NonNullList<ItemStack> items=NonNullList.withSize(12,ItemStack.EMPTY);
    private UUID owner;
    private int progress,total,status,recipeHash;
    private String recipeId="";
    private ProcessingRecipe cachedRecipe;
    private boolean powered;
    public final ContainerData data=new ContainerData(){
        public int get(int i){return switch(i){case 0->progress;case 1->total;case 2->charge();case 3->status;default->0;};}
        public void set(int i,int value){switch(i){case 0->progress=value;case 1->total=value;case 3->status=value;default->{}}}
        public int getCount(){return 4;}
    };
    public StationBlockEntity(BlockPos pos,BlockState state){super(ProductionRegistries.STATION.get(),pos,state);}
    public String station(){return BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()).getPath();}
    public void claim(Player player){if(owner==null){owner=player.getUUID();setChanged();}}
    public boolean canUse(Player player){return owner==null||owner.equals(player.getUUID());}
    private int charge(){var stack=items.get(6);return stack.is(ModItems.FIELD_CELL.get())?Math.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntOr("badspell_fl",100),0,100):0;}
    private void spend(int amount){if(amount>0)CustomData.update(DataComponents.CUSTOM_DATA,items.get(6),t->t.putInt("badspell_fl",charge()-amount));}
    private boolean ready(int energy){if(energy>0&&!items.get(11).is(ModContent.item("cell_adapter"))){status=4;return false;}if(charge()<energy){status=2;return false;}return true;}
    private List<ItemStack> outputsAfter(List<ItemStack> outputs) {
        var slots=new ArrayList<ItemStack>();for(int i=7;i<11;i++)slots.add(items.get(i).copy());
        for(var output:outputs){var left=output.copy();for(var dest:slots)if(!dest.isEmpty()&&ItemStack.isSameItemSameComponents(dest,left)){int count=Math.min(left.getCount(),dest.getMaxStackSize()-dest.getCount());dest.grow(count);left.shrink(count);}
            for(int i=0;i<4&&!left.isEmpty();i++)if(slots.get(i).isEmpty()){int count=Math.min(left.getCount(),left.getMaxStackSize());slots.set(i,left.copyWithCount(count));left.shrink(count);}if(!left.isEmpty())return null;}
        return slots;
    }
    private void commit(int[] used,List<ItemStack> output,int energy){for(int i=0;i<6;i++)items.get(i).shrink(used[i]);spend(energy);for(int i=0;i<4;i++)items.set(7+i,output.get(i));progress=0;status=0;setChanged();}
    public static void tick(Level level,BlockPos pos,BlockState state,StationBlockEntity self) {
        if(!(level instanceof ServerLevel server))return;
        if(self.station().equals("rune_holder")){boolean now=level.hasNeighborSignal(pos);if(now&&!self.powered)self.activatePlate(server);if(now!=self.powered){self.powered=now;self.setChanged();}return;}
        if(level.getGameTime()%5!=0)return;
        if(self.station().equals("engraving_station")&&self.engrave(server))return;
        RecipeHolder<ProcessingRecipe> chosen=null;
        if(!self.recipeId.isBlank()){
            var old=server.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE,Identifier.parse(self.recipeId)));
            if(old.isPresent()&&old.get().value() instanceof ProcessingRecipe recipe&&recipe.matches(new ProcessingRecipe.Inventory(self,self.station()),level))chosen=new RecipeHolder<>(old.get().id(),recipe);
        }
        if(chosen==null)chosen=server.recipeAccess().recipeMap().byType(ProductionRegistries.TYPE.get()).stream().filter(r->r.value().matches(new ProcessingRecipe.Inventory(self,self.station()),level)).sorted(Comparator.comparing(r->r.id().identifier().toString())).findFirst().orElse(null);
        if(chosen==null){if(self.progress!=0)self.setChanged();self.progress=0;self.total=0;self.recipeId="";self.cachedRecipe=null;self.status=1;return;}
        var recipe=chosen.value();String id=chosen.id().identifier().toString();
        if(self.cachedRecipe!=recipe){int hash=ProcessingRecipe.CODEC.codec().encodeStart(server.registryAccess().createSerializationContext(JsonOps.INSTANCE),recipe).getOrThrow().toString().hashCode();if(!id.equals(self.recipeId)||hash!=self.recipeHash)self.progress=0;self.recipeHash=hash;self.recipeId=id;self.cachedRecipe=recipe;}
        self.total=recipe.ticks();var output=self.outputsAfter(recipe.outputs().stream().map(ItemStackTemplate::create).toList());if(output==null){self.status=3;return;}if(!self.ready(recipe.energy()))return;
        self.status=0;self.progress+=5;if(self.progress>=self.total)self.commit(recipe.allocation(self),output,recipe.energy());else if(self.progress%20==0)self.setChanged();
    }
    private boolean engrave(ServerLevel server) {
        int plate=-1,book=-1;for(int i=0;i<6;i++){if(items.get(i).is(ModContent.item("blank_rune_plate")))plate=i;if(items.get(i).is(ModItems.FORMULA_BOOK.get()))book=i;}
        if(plate<0||book<0)return false;
        try {
            var bytes=items.get(book).getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getByteArray("badspell_formula").orElseThrow();
            if(bytes.length>com.badspell.network.Packets.MAX_FORMULA_BYTES)throw new IllegalArgumentException();var formula=FormulaCodec.decode(bytes);
            if(owner==null||!owner.equals(formula.owner())||formula.nodes().size()>24||!new FormulaValidator(RuneRegistry.standard()).validate(formula).isEmpty()){status=5;return true;}
            var output=new ItemStack(ModContent.item("inscribed_rune_plate"));CustomData.update(DataComponents.CUSTOM_DATA,output,t->{t.putByteArray("badspell_formula",bytes);t.putString("badspell_checksum",new FormulaSnapshot(formula).checksum());});
            var slots=outputsAfter(List.of(output));if(slots==null){status=3;return true;}int energy=Math.min(500,30+4*formula.nodes().size());if(!ready(energy))return true;
            String id="engrave:"+new FormulaSnapshot(formula).checksum();if(!recipeId.equals(id)){progress=0;recipeId=id;cachedRecipe=null;}
            total=80;status=0;progress+=5;if(progress>=total){int[] used=new int[6];used[plate]=1;commit(used,slots,energy);}else if(progress%20==0)setChanged();return true;
        }catch(java.io.IOException|IllegalArgumentException|NoSuchElementException error){status=5;return true;}
    }
    public boolean activatePlate(ServerLevel server) {
        if(owner==null||!items.get(0).is(ModContent.item("inscribed_rune_plate"))){status=5;return false;}
        try {
            var tag=items.get(0).getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var bytes=tag.getByteArray("badspell_formula").orElseThrow();if(bytes.length>30000)throw new IllegalArgumentException();
            var formula=FormulaCodec.decode(bytes);if(!formula.owner().equals(owner)||!new FormulaSnapshot(formula).verifies(tag.getStringOr("badspell_checksum",""))){status=5;return false;}
            var target=getBlockPos().above();String dimension=server.dimension().identifier().toString();var position=new Value.Position(dimension,target.getX(),target.getY(),target.getZ());
            var context=new RuneRegistry.Context(position,server.getGameTime());var plan=new FormulaEngine(RuneRegistry.standard()).plan(formula,context);
            if(!ready((int)plan.fieldCost()))return false;
            // Device activations draw FL only; FK is reserved for direct player casting.
            var account=new EnergyAccount(100,Long.MAX_VALUE,charge(),Long.MAX_VALUE);
            var result=new ActivationService(new FormulaEngine(RuneRegistry.standard())).activate(formula,context,position,100,account,effect->{
                if(!effect.id().equals("badspell:light")||effect.region().radius()!=0)return null;var p=effect.region().center();var at=new BlockPos(p.x(),p.y(),p.z());
                if(!p.dimension().equals(dimension)||server.isOutsideBuildHeight(at)||!server.getChunkSource().hasChunk(at.getX()>>4,at.getZ()>>4)||!server.getWorldBorder().isWithinBounds(at)||!server.getBlockState(at).isAir())return null;
                var fields=LightFields.get(server);if(!fields.canAdd(server,at))return null;return ()->fields.add(server,at,effect.strength(),effect.durationTicks());
            });
            if(result==ActivationService.Result.APPLIED){spend(charge()-(int)account.balance().field());status=0;setChanged();return true;}status=5;return false;
        }catch(java.io.IOException|IllegalArgumentException|NoSuchElementException error){status=5;return false;}
    }
    @Override protected void loadAdditional(ValueInput input){super.loadAdditional(input);ContainerHelper.loadAllItems(input,items);owner=input.read("owner",UUIDUtil.CODEC).orElse(null);progress=Math.max(0,input.getIntOr("progress",0));recipeHash=input.getIntOr("recipe_hash",0);recipeId=input.getStringOr("recipe_id","");powered=input.getBooleanOr("powered",false);}
    @Override protected void saveAdditional(ValueOutput output){super.saveAdditional(output);ContainerHelper.saveAllItems(output,items);if(owner!=null)output.store("owner",UUIDUtil.CODEC,owner);output.putInt("progress",progress);output.putInt("recipe_hash",recipeHash);output.putString("recipe_id",recipeId);output.putBoolean("powered",powered);}
    @Override public Component getDisplayName(){return getBlockState().getBlock().getName();}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inventory,Player player){return new StationMenu(id,inventory,this,data);}
    @Override public int getContainerSize(){return 12;}
    @Override public boolean isEmpty(){return items.stream().allMatch(ItemStack::isEmpty);}
    @Override public ItemStack getItem(int slot){return items.get(slot);}
    @Override public ItemStack removeItem(int slot,int amount){var result=ContainerHelper.removeItem(items,slot,amount);if(!result.isEmpty())setChanged();return result;}
    @Override public ItemStack removeItemNoUpdate(int slot){return ContainerHelper.takeItem(items,slot);}
    @Override public void setItem(int slot,ItemStack stack){items.set(slot,stack);stack.limitSize(getMaxStackSize(stack));setChanged();}
    @Override public boolean stillValid(Player player){return level!=null&&level.getBlockEntity(worldPosition)==this&&player.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)<=64&&canUse(player);}
    @Override public void clearContent(){items.clear();setChanged();}
    @Override public int[] getSlotsForFace(Direction side){return side==Direction.DOWN?new int[]{7,8,9,10}:side==Direction.UP?new int[]{0,1,2,3,4,5}:new int[]{6,11};}
    @Override public boolean canPlaceItemThroughFace(int slot,ItemStack stack,Direction side){return slot<6||slot==6&&stack.is(ModItems.FIELD_CELL.get())||slot==11&&stack.is(ModContent.item("cell_adapter"));}
    @Override public boolean canTakeItemThroughFace(int slot,ItemStack stack,Direction side){return slot>=7&&slot<11;}
}
