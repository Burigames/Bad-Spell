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
    private final java.util.List<ItemStack> recipeInputs=new ArrayList<>();
    private boolean powered;
    private byte[] scheduled=new byte[0];private long startTick,nextTick;private int remaining,interval;
    public UUID ownerId(){return owner;}
    public boolean hasAdapter(){return items.get(11).is(ModContent.item("cell_adapter"));}
    public boolean scheduled(){return scheduled.length>0;}
    public void cancelSpell(){scheduled=new byte[0];remaining=0;interval=0;status=0;setChanged();}

    public final ContainerData data=new ContainerData(){
        public int get(int i){return switch(i){case 0->progress;case 1->total;case 2->charge();case 3->status;case 4->StationMenu.KINDS.indexOf(station());case 5->energyCapacity();default->0;};}
        public void set(int i,int value){switch(i){case 0->progress=value;case 1->total=value;case 3->status=value;default->{}}}
        public int getCount(){return 6;}
    };
    public StationBlockEntity(BlockPos pos,BlockState state){super(ProductionRegistries.STATION.get(),pos,state);}
    public String station(){return BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()).getPath();}
    public void claim(Player player){if(player instanceof net.minecraft.server.level.ServerPlayer p&&!com.badspell.server.DeviceCasters.isDevice(p))com.badspell.progression.ResearchProgress.station(p,station());if(owner==null){owner=player.getUUID();setChanged();}}
    public boolean canUse(Player player){return owner==null||owner.equals(player.getUUID());}
    private int charge(){var stack=items.get(6);return ModItems.isCell(stack)?Math.clamp(stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntOr("badspell_fl",ModItems.cellCapacity(stack)),0,ModItems.cellCapacity(stack)):0;}
    public int energyStored(){return charge();}
    public int energyCapacity(){return ModItems.cellCapacity(items.get(6));}
    public void changeEnergy(int delta){int next=charge()+delta;if(next<0||next>energyCapacity())throw new IllegalArgumentException("Energy bounds");CustomData.update(DataComponents.CUSTOM_DATA,items.get(6),t->t.putInt("badspell_fl",next));setChanged();}
    private void spend(int amount){if(amount>0)CustomData.update(DataComponents.CUSTOM_DATA,items.get(6),t->t.putInt("badspell_fl",charge()-amount));}
    private boolean ready(int energy){if(energy>0&&!items.get(11).is(ModContent.item("cell_adapter"))){status=4;return false;}if(charge()<energy)com.badspell.conduction.CrystalConduction.pull(this,energy);if(charge()<energy){status=2;return false;}return true;}
    private List<ItemStack> outputsAfter(List<ItemStack> outputs) {
        var slots=new ArrayList<ItemStack>();for(int i=7;i<11;i++)slots.add(items.get(i).copy());
        for(var output:outputs){var left=output.copy();for(var dest:slots)if(!dest.isEmpty()&&ItemStack.isSameItemSameComponents(dest,left)){int count=Math.min(left.getCount(),dest.getMaxStackSize()-dest.getCount());dest.grow(count);left.shrink(count);}
            for(int i=0;i<4&&!left.isEmpty();i++)if(slots.get(i).isEmpty()){int count=Math.min(left.getCount(),left.getMaxStackSize());slots.set(i,left.copyWithCount(count));left.shrink(count);}if(!left.isEmpty())return null;}
        return slots;
    }
    private void commit(int[] used,List<ItemStack> output,int energy){for(int i=0;i<6;i++)items.get(i).shrink(used[i]);spend(energy);for(int i=0;i<4;i++)items.set(7+i,output.get(i));progress=0;status=0;setChanged();}
    public static void tick(Level level,BlockPos pos,BlockState state,StationBlockEntity self) {
        if(!(level instanceof ServerLevel server))return;
        if(self.station().equals("rune_holder")){self.tickSpell(server);boolean now=level.hasNeighborSignal(pos);if(now&&!self.powered)self.activatePlate(server);if(now!=self.powered){self.powered=now;self.setChanged();}return;}
        if(level.getGameTime()%5!=0)return;
        if(self.station().equals("engraving_station")&&self.engrave(server))return;
        boolean inputChanged=self.recipeInputs.size()==6&&java.util.stream.IntStream.range(0,6).anyMatch(slot->!ItemStack.matches(self.items.get(slot),self.recipeInputs.get(slot)));
        boolean initialized=self.recipeInputs.size()==6;
        if(inputChanged||!initialized){self.recipeInputs.clear();for(int slot=0;slot<6;slot++)self.recipeInputs.add(self.items.get(slot).copy());if(inputChanged)self.progress=0;}
        RecipeHolder<ProcessingRecipe> chosen=null;
        if(initialized&&!inputChanged&&!self.recipeId.isBlank()){
            var old=server.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE,Identifier.parse(self.recipeId)));
            if(old.isPresent()&&old.get().value() instanceof ProcessingRecipe recipe&&recipe.matches(new ProcessingRecipe.Inventory(self,self.station()),level))chosen=new RecipeHolder<>(old.get().id(),recipe);
        }
        if(chosen==null)chosen=server.recipeAccess().recipeMap().byType(ProductionRegistries.TYPE.get()).stream().filter(r->r.value().matches(new ProcessingRecipe.Inventory(self,self.station()),level)).sorted(Comparator.<RecipeHolder<ProcessingRecipe>>comparingInt(r->r.value().inputs().stream().mapToInt(ProcessingRecipe.Input::count).sum()).reversed().thenComparing(r->r.id().identifier().toString())).findFirst().orElse(null);
        if(chosen==null){if(self.progress!=0)self.setChanged();self.progress=0;self.total=0;self.recipeId="";self.cachedRecipe=null;self.status=1;return;}
        var recipe=chosen.value();String id=chosen.id().identifier().toString();
        if(self.cachedRecipe!=recipe){int hash=ProcessingRecipe.CODEC.codec().encodeStart(server.registryAccess().createSerializationContext(JsonOps.INSTANCE),recipe).getOrThrow().toString().hashCode();if(!id.equals(self.recipeId)||hash!=self.recipeHash)self.progress=0;self.recipeHash=hash;self.recipeId=id;self.cachedRecipe=recipe;}
        self.total=recipe.ticks();var products=recipe.outputs().stream().map(ItemStackTemplate::create).toList();
        for(var product:products)if(ModItems.isGlove(product))for(int slot=0;slot<6;slot++)if(ModItems.isGlove(self.items.get(slot))){if(self.owner==null){self.status=5;return;}try{if(!com.badspell.item.GloveFormulas.ownedBy(self.items.get(slot),self.owner)){self.status=5;return;}}catch(java.io.IOException error){self.status=5;return;}product.set(DataComponents.CUSTOM_DATA,self.items.get(slot).getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY));break;}
        for(var product:products)if(product.is(ModItems.FIELD_COMPASS_Q3.get()))for(int slot=0;slot<6;slot++)if(self.items.get(slot).is(ModItems.FIELD_COMPASS.get())){product.set(DataComponents.CUSTOM_DATA,self.items.get(slot).getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY));break;}
        for(var product:products)if(product.is(ModItems.FIELD_COMPASS.get())||product.is(ModItems.RESONANCE_FORK.get())&&self.items.stream().anyMatch(stack->stack.is(ModContent.item("raw_resonance_fork")))){
            boolean qualified=false;for(int slot=0;slot<6;slot++)if(self.items.get(slot).getItem() instanceof RunePlateItem)try{var formula=FormulaCodec.decode(self.items.get(slot).getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getByteArray("badspell_formula").orElseThrow());if(self.owner!=null&&formula.owner().equals(self.owner)&&formula.nodes().stream().anyMatch(n->product.is(ModItems.FIELD_COMPASS.get())?n.rune().equals("badspell:sign"):n.rune().equals("badspell:check")||n.rune().equals("badspell:impulse")))qualified=true;}catch(java.io.IOException|java.util.NoSuchElementException error){}if(!qualified){self.status=5;return;}
        }
        var output=self.outputsAfter(products);if(output==null){self.status=3;return;}if(!self.ready(recipe.energy()))return;
        self.status=0;self.progress+=5;if(self.progress>=self.total)self.commit(recipe.allocation(self),output,recipe.energy());else if(self.progress%20==0)self.setChanged();
    }
    private boolean engrave(ServerLevel server) {
        int plate=-1,book=-1;for(int i=0;i<6;i++){if(items.get(i).is(ModContent.item("blank_rune_plate"))||items.get(i).is(ModContent.item("blank_rune_plate_q2"))||items.get(i).is(ModContent.item("blank_rune_plate_q3")))plate=i;if(items.get(i).is(ModItems.FORMULA_BOOK.get()))book=i;}
        if(plate<0||book<0)return false;
        try {
            var bytes=items.get(book).getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getByteArray("badspell_formula").orElseThrow();
            if(bytes.length>com.badspell.network.Packets.MAX_FORMULA_BYTES)throw new IllegalArgumentException();var formula=FormulaCodec.decode(bytes);
            if(owner==null||!owner.equals(formula.owner())||formula.nodes().size()>(items.get(plate).is(ModContent.item("blank_rune_plate_q3"))?128:items.get(plate).is(ModContent.item("blank_rune_plate_q2"))?64:24)||!new FormulaValidator(RuneRegistry.standard()).validate(formula).isEmpty()){status=5;return true;}
            var researcher=server.getServer().getPlayerList().getPlayer(owner);if(researcher!=null&&!com.badspell.progression.ResearchProgress.allows(researcher,formula)||researcher==null&&!com.badspell.progression.ResearchProgress.authorized(items.get(book),formula)){status=5;return true;}
            var output=new ItemStack(ModContent.item(items.get(plate).is(ModContent.item("blank_rune_plate_q3"))?"inscribed_rune_plate_q3":items.get(plate).is(ModContent.item("blank_rune_plate_q2"))?"inscribed_rune_plate_q2":"inscribed_rune_plate"));CustomData.update(DataComponents.CUSTOM_DATA,output,t->{t.putByteArray("badspell_formula",bytes);t.putString("badspell_checksum",new FormulaSnapshot(formula).checksum());});
            var slots=outputsAfter(List.of(output));if(slots==null){status=3;return true;}int energy=Math.min(500,30+4*formula.nodes().size());if(!ready(energy))return true;
            String id="engrave:"+new FormulaSnapshot(formula).checksum();if(!recipeId.equals(id)){progress=0;recipeId=id;cachedRecipe=null;}
            total=80;status=0;progress+=5;if(progress>=total){int[] used=new int[6];used[plate]=1;commit(used,slots,energy);}else if(progress%20==0)setChanged();return true;
        }catch(java.io.IOException|IllegalArgumentException|NoSuchElementException error){status=5;return true;}
    }
    public boolean activatePlate(ServerLevel server){
        if(owner==null||!(items.get(0).getItem() instanceof RunePlateItem)){status=5;return false;}
        try{
            var tag=items.get(0).getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var bytes=tag.getByteArray("badspell_formula").orElseThrow();if(bytes.length>30000)throw new IllegalArgumentException();var formula=FormulaCodec.decode(bytes);
            if(!formula.owner().equals(owner)||!new FormulaSnapshot(formula).verifies(tag.getStringOr("badspell_checksum","")))throw new IllegalArgumentException();
            var actor=com.badspell.server.DeviceCasters.at(server,owner,worldPosition);long now=server.getGameTime();var context=com.badspell.server.SpellEffects.context(actor,worldPosition.above(),now);var plan=new FormulaEngine(RuneRegistry.standard()).plan(formula,context);
            var result=executePlate(server,formula,now);boolean applied=result==ActivationService.Result.APPLIED||result==ActivationService.Result.SKIPPED;
            if(plan.event().remaining()!=0&&(result==ActivationService.Result.WAITING||result==ActivationService.Result.INSUFFICIENT_ENERGY||applied&&plan.event().interval()>0)){
                scheduled=bytes.clone();startTick=now;interval=plan.event().interval();remaining=plan.event().remaining()<0?-1:plan.event().remaining()-(applied?1:0);nextTick=applied?now+interval:Math.max(now+20,plan.event().tick());if(remaining==0)cancelSpell();else{status=result==ActivationService.Result.INSUFFICIENT_ENERGY?7:6;setChanged();}
            }else if(applied)cancelSpell();return applied;
        }catch(java.io.IOException|IllegalArgumentException|IllegalStateException|NoSuchElementException error){status=5;return false;}
    }
    private ActivationService.Result executePlate(ServerLevel server,Formula formula,long start){
        var actor=com.badspell.server.DeviceCasters.at(server,owner,worldPosition);var context=com.badspell.server.SpellEffects.context(actor,worldPosition.above(),start);var engine=new FormulaEngine(RuneRegistry.standard());var plan=engine.plan(formula,context);
        if(!ready((int)Math.min(Integer.MAX_VALUE,plan.fieldCost())))return ActivationService.Result.INSUFFICIENT_ENERGY;
        var origin=com.badspell.server.SpellEffects.position(server,worldPosition);var account=new EnergyAccount(600,Long.MAX_VALUE,charge(),Long.MAX_VALUE);
        var result=new ActivationService(engine).activate(formula,context,origin,100,account,effect->com.badspell.server.SpellEffects.prepare(actor,effect));
        if(result==ActivationService.Result.APPLIED||result==ActivationService.Result.SKIPPED){spend(charge()-(int)account.balance().field());status=0;setChanged();}else status=result==ActivationService.Result.WAITING?6:5;return result;
    }
    private void tickSpell(ServerLevel server){
        if(scheduled.length==0||server.getGameTime()<nextTick)return;
        if(owner==null||!(items.get(0).getItem() instanceof RunePlateItem)){cancelSpell();return;}
        try{var formula=FormulaCodec.decode(scheduled);if(!formula.owner().equals(owner))throw new IllegalArgumentException();var current=items.get(0).getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getByteArray("badspell_formula").orElseThrow();if(!java.util.Arrays.equals(current,scheduled)){cancelSpell();return;}
            var result=executePlate(server,formula,startTick);
            if(result==ActivationService.Result.INSUFFICIENT_ENERGY||result==ActivationService.Result.DENIED||result==ActivationService.Result.UNSTABLE){nextTick=server.getGameTime()+20;status=7;setChanged();return;}
            if(result==ActivationService.Result.WAITING){nextTick=server.getGameTime()+1;return;}
            if(result!=ActivationService.Result.APPLIED&&result!=ActivationService.Result.SKIPPED){cancelSpell();return;}
            if(remaining>0)remaining--;if(remaining==0||interval==0)cancelSpell();else{nextTick=server.getGameTime()+interval;status=6;setChanged();}
        }catch(java.io.IOException|IllegalArgumentException|IllegalStateException|NoSuchElementException error){cancelSpell();status=5;}
    }
    @Override protected void loadAdditional(ValueInput input){super.loadAdditional(input);ContainerHelper.loadAllItems(input,items);owner=input.read("owner",UUIDUtil.CODEC).orElse(null);progress=Math.max(0,input.getIntOr("progress",0));recipeHash=input.getIntOr("recipe_hash",0);recipeId=input.getStringOr("recipe_id","");powered=input.getBooleanOr("powered",false);scheduled=input.read("scheduled",com.mojang.serialization.Codec.BYTE_BUFFER).map(buffer->{if(buffer.remaining()>30000)return new byte[0];byte[] bytes=new byte[buffer.remaining()];buffer.get(bytes);return bytes;}).orElse(new byte[0]);if(scheduled.length>30000)scheduled=new byte[0];startTick=input.getLongOr("spell_start",0);nextTick=input.getLongOr("spell_next",0);remaining=input.getIntOr("spell_remaining",0);interval=input.getIntOr("spell_interval",0);if(remaining< -1||remaining>64||interval<0||interval>1200)scheduled=new byte[0];}
    @Override protected void saveAdditional(ValueOutput output){super.saveAdditional(output);ContainerHelper.saveAllItems(output,items);if(owner!=null)output.store("owner",UUIDUtil.CODEC,owner);output.putInt("progress",progress);output.putInt("recipe_hash",recipeHash);output.putString("recipe_id",recipeId);output.putBoolean("powered",powered);if(scheduled.length>0){output.store("scheduled",com.mojang.serialization.Codec.BYTE_BUFFER,java.nio.ByteBuffer.wrap(scheduled));output.putLong("spell_start",startTick);output.putLong("spell_next",nextTick);output.putInt("spell_remaining",remaining);output.putInt("spell_interval",interval);}}
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
    @Override public boolean canPlaceItemThroughFace(int slot,ItemStack stack,Direction side){return slot<6||slot==6&&ModItems.isCell(stack)||slot==11&&stack.is(ModContent.item("cell_adapter"));}
    @Override public boolean canTakeItemThroughFace(int slot,ItemStack stack,Direction side){return slot>=7&&slot<11;}
}
