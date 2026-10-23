package com.badspell.server;

import com.badspell.content.ModItems;
import com.badspell.item.GloveFormulas;
import com.badspell.formula.*;
import com.badspell.field.EnergyAccount;
import com.badspell.network.Packets;
import java.io.IOException;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class MagicActions {
    private static final String FORMULA="badspell_formula",INK="badspell_ink",FOCUS="badspell_focus",LAST_CAST="badspell_last_cast";
    private static final RuneRegistry RUNES=RuneRegistry.standard();
    private static final ActivationService ACTIVATIONS=new ActivationService(new FormulaEngine(RUNES));
    private MagicActions() {}
    private static net.minecraft.nbt.CompoundTag tag(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag(); }
    static Formula read(ItemStack stack) throws IOException {
        byte[] bytes=tag(stack).getByteArray(FORMULA).orElseThrow(()->new IOException("No formula snapshot"));
        if(bytes.length>Packets.MAX_FORMULA_BYTES)throw new IOException("Oversized book snapshot");
        Formula formula=FormulaCodec.decode(bytes);
        if(!new FormulaValidator(RUNES).validateDefinition(formula).isEmpty())throw new IOException("Stored graph is invalid");
        return formula;
    }
    static void write(ItemStack stack,Formula formula) throws IOException {
        byte[] bytes=FormulaCodec.encode(formula);
        if(bytes.length>Packets.MAX_FORMULA_BYTES)throw new IOException("Formula exceeds network snapshot limit");
        CustomData.update(DataComponents.CUSTOM_DATA,stack,t->{t.putByteArray(FORMULA,bytes);t.putString("badspell_checksum",new FormulaSnapshot(formula).checksum());});
    }
    public static void openBook(ServerPlayer player,InteractionHand hand) {
        ItemStack stack=player.getItemInHand(hand);
        if(!stack.is(ModItems.FORMULA_BOOK.get()))return;
        try {
            if(!tag(stack).contains(FORMULA)) {
                var example=ExampleFormulas.light(player.getUUID());
                Formula first=new Formula(UUID.randomUUID(),player.getUUID(),1,example.title(),example.nodes(),example.edges(),example.output(),example.energyBudget());
                write(stack,first);CustomData.update(DataComponents.CUSTOM_DATA,stack,t->t.putInt(INK,64));
            }
            var f=read(stack);
            if(!f.owner().equals(player.getUUID())){message(player,"owner");return;}
            FormulaLibrary.track(player,f,stack);
            PacketDistributor.sendToPlayer(player,new Packets.OpenBook(hand.ordinal(),FormulaCodec.encode(f),tag(stack).getIntOr(INK,0)));
        } catch(IOException|IllegalArgumentException error){message(player,"invalid");}
    }
    public static void handle(ServerPlayer player,Packets.BookAction packet) {
        if(packet.hand()<0 || packet.hand()>1 || packet.action()<0 || packet.action()>3)return;
        long second=player.level().getGameTime()/20;var throttle=player.getPersistentData();
        int requests=throttle.getLongOr("badspell_request_second",-1)==second?throttle.getIntOr("badspell_request_count",0):0;
        if(requests>=20)return;
        throttle.putLong("badspell_request_second",second);throttle.putInt("badspell_request_count",requests+1);
        InteractionHand hand=InteractionHand.values()[packet.hand()];ItemStack book=player.getItemInHand(hand);
        if(!book.is(ModItems.FORMULA_BOOK.get()))return;
        try {
            Formula current=read(book),proposed=FormulaCodec.decode(packet.formula());
            if(!current.owner().equals(player.getUUID()) || !proposed.owner().equals(player.getUUID()) || !current.id().equals(proposed.id()) || current.revision()!=proposed.revision()) {message(player,"conflict");return;}
            if(packet.action()==0) {
                var authoritative=player.getPersistentData().getCompoundOrEmpty(FormulaLibrary.KEY).getCompoundOrEmpty(current.id().toString()).getByteArray("snapshot");
                if(authoritative.isPresent()&&FormulaCodec.decode(authoritative.get()).revision()!=current.revision()){message(player,"conflict");return;}

                long tick=player.level().getGameTime();var state=player.getPersistentData();long window=state.getLongOr("badspell_save_window",-1200);
                int saves=tick-window>=1200?0:state.getIntOr("badspell_saves",0);
                if(saves>=10){message(player,"rate_limit");return;}
                var diagnostics=new FormulaValidator(RUNES).validateDefinition(proposed);
                if(!diagnostics.isEmpty()){player.sendSystemMessage(Component.literal(diagnostics.getFirst().toString()),false);return;}
                int changes=0;Map<String,Formula.Node> old=new HashMap<>();current.nodes().forEach(n->old.put(n.id(),n));
                for(var node:proposed.nodes()){var previous=old.remove(node.id());if(!node.equals(previous))changes+=previous!=null&&node.rune().equals("badspell:call")?2:1;}
                changes+=old.size();if(!new HashSet<>(current.edges()).equals(new HashSet<>(proposed.edges())))changes++;
                int cost=Math.min(8,changes);int ink=tag(book).getIntOr(INK,0);if(cost>ink){message(player,"no_ink");return;}
                Formula saved=new Formula(current.id(),current.owner(),Math.addExact(current.revision(),1),proposed.title(),proposed.nodes(),proposed.edges(),proposed.output(),proposed.energyBudget());
                var history=tag(book).getListOrEmpty("badspell_history");
                var revisionTag=new net.minecraft.nbt.CompoundTag();revisionTag.putByteArray("snapshot",FormulaCodec.encode(current));history.add(revisionTag);
                while(history.size()>20)history.remove(0);
                write(book,saved);CustomData.update(DataComponents.CUSTOM_DATA,book,t->t.put("badspell_history",history));CustomData.update(DataComponents.CUSTOM_DATA,book,t->t.putInt(INK,ink-cost));
                state.putLong("badspell_save_window",saves==0?tick:window);state.putInt("badspell_saves",saves+1);message(player,"saved");
            } else if(packet.action()==1) {
                ItemStack glove=findGlove(player);if(glove.isEmpty()){message(player,"no_glove");return;}
                if(!GloveFormulas.current(player,glove)||!GloveFormulas.ownedBy(glove,player.getUUID())){message(player,"owner");return;}
                // Export only the authoritative saved graph, never unsaved client edits.
                if(!new FormulaValidator(RUNES).validate(current).isEmpty()){message(player,"invalid");return;}
                GloveFormulas.importSnapshot(glove,current,player.getUUID());GloveFormulas.backup(player,glove);message(player,"exported");
            } else if(packet.action()==3) {
                long tick=player.level().getGameTime();var state=player.getPersistentData();
                if(tick-state.getLongOr("badspell_last_history",-1000)<20){message(player,"rate_limit");return;}
                state.putLong("badspell_last_history",tick);
                java.util.List<byte[]> revisions=new java.util.ArrayList<>();
                var history=tag(book).getListOrEmpty("badspell_history");
                for(int i=Math.max(0,history.size()-20);i<history.size();i++) {
                    var entry=history.getCompoundOrEmpty(i);var bytes=entry.getByteArray("snapshot").orElseThrow(()->new IOException("Invalid history"));
                    if(bytes.length>Packets.MAX_FORMULA_BYTES)throw new IOException("Oversized history");
                    var archived=FormulaCodec.decode(bytes);
                    if(!archived.id().equals(current.id()) || !archived.owner().equals(current.owner()) || archived.revision()>=current.revision())throw new IOException("Invalid history identity");
                    revisions.add(bytes);
                }
                PacketDistributor.sendToPlayer(player,new Packets.BookHistory(hand.ordinal(),FormulaCodec.encode(current),tag(book).getIntOr(INK,0),java.util.List.copyOf(revisions)));
                return;
            } else {
                ItemStack ink=find(player,ModItems.RUNE_INK.get());
                if(ink.isEmpty() || tag(book).getIntOr(INK,0)>=64){message(player,"no_ink");return;}
                ink.shrink(1);CustomData.update(DataComponents.CUSTOM_DATA,book,t->t.putInt(INK,64));message(player,"refilled");
            }
            openBook(player,hand);
        } catch(IOException|IllegalArgumentException|ArithmeticException error){message(player,"invalid");}
    }
    private static ItemStack find(ServerPlayer player,net.minecraft.world.item.Item item) {
        for(int i=0;i<player.getInventory().getContainerSize();i++){ItemStack stack=player.getInventory().getItem(i);if(stack.is(item))return stack;}
        return ItemStack.EMPTY;
    }
    private static ItemStack findGlove(ServerPlayer player){for(int i=0;i<player.getInventory().getContainerSize();i++){var stack=player.getInventory().getItem(i);if(ModItems.isGlove(stack))return stack;}return ItemStack.EMPTY;}
    public static void cast(ServerPlayer player,InteractionHand hand) {
        var glove=player.getItemInHand(hand);if(!ModItems.isGlove(glove))return;
        if(player.isShiftKeyDown()){ScheduledSpells.cancel(player);try{if(!GloveFormulas.current(player,glove))GloveFormulas.restore(player,glove);}catch(IOException error){message(player,"invalid");}message(player,"cancelled");return;}
        long tick=player.level().getGameTime();if(tick-player.getPersistentData().getLongOr(LAST_CAST,-1000)<1){message(player,"rate_limit");return;}
        try {
            if(!GloveFormulas.current(player,glove)){message(player,"conflict");return;}
            if(!GloveFormulas.occupied(glove,GloveFormulas.selected(glove))){message(player,"empty_slot");return;}
            Formula formula=GloveFormulas.read(glove);if(!formula.owner().equals(player.getUUID())){message(player,"owner");return;}
            var hit=player.pick(12,1,false);BlockPos target=hit instanceof BlockHitResult block?block.getBlockPos().relative(block.getDirection()):BlockPos.containing(hit.getLocation());
            var context=SpellEffects.context(player,target,tick);var plan=new FormulaEngine(RUNES).plan(formula,context);
            if((plan.event().interval()>0||plan.event().tick()>tick)&&!ScheduledSpells.canAdd(player)){message(player,"schedule_limit");return;}
            var result=execute(player,formula,context);
            if((result==ActivationService.Result.WAITING||result==ActivationService.Result.APPLIED||result==ActivationService.Result.SKIPPED)&&plan.event().remaining()!=0&&(plan.event().tick()>tick||plan.event().interval()>0))ScheduledSpells.add(player,formula,context,plan.event(),result==ActivationService.Result.APPLIED||result==ActivationService.Result.SKIPPED);
            else if(result==ActivationService.Result.APPLIED)GloveFormulas.backup(player,glove);
        }catch(IOException|IllegalArgumentException|IllegalStateException error){message(player,"invalid");}
    }
    public static ActivationService.Result execute(ServerPlayer player,Formula formula,RuneRegistry.Context context) {
        var state=player.getPersistentData();var cells=new ArrayList<ItemStack>();int fl=0,capacity=0;
        for(int i=0;i<player.getInventory().getContainerSize();i++){var cell=player.getInventory().getItem(i);int cap=ModItems.cellCapacity(cell);if(cap>0){cells.add(cell);fl+=Math.clamp(tag(cell).getIntOr("badspell_fl",cap),0,cap);capacity+=cap;}}
        if(cells.isEmpty()){message(player,"no_cell");return ActivationService.Result.INSUFFICIENT_ENERGY;}
        int focus=Math.clamp(state.getIntOr(FOCUS,100),0,100);var account=new EnergyAccount(capacity,100,fl,focus);
        String dim=player.level().dimension().identifier().toString();var origin=SpellEffects.position(player.level(),player.blockPosition());
        int stability=context.environment().get("stability") instanceof Value.Number n?(int)Math.clamp(n.value(),0,100):100;
        var result=ACTIVATIONS.activate(formula,context,origin,stability,account,effect->SpellEffects.prepare(player,effect));
        if(result==ActivationService.Result.APPLIED||result==ActivationService.Result.SKIPPED){int spent=fl-(int)account.balance().field();for(var cell:cells){int charge=Math.clamp(tag(cell).getIntOr("badspell_fl",ModItems.cellCapacity(cell)),0,ModItems.cellCapacity(cell));int amount=Math.min(charge,spent);spent-=amount;int left=charge-amount;CustomData.update(DataComponents.CUSTOM_DATA,cell,t->t.putInt("badspell_fl",left));}state.putInt(FOCUS,(int)account.balance().focus());if(result==ActivationService.Result.APPLIED)state.putLong(LAST_CAST,player.level().getGameTime());}
        player.sendSystemMessage(Component.translatable("message.badspell.cast",Component.translatable("result.badspell."+result.name().toLowerCase(Locale.ROOT)),account.balance().field(),account.balance().focus()),true);return result;
    }
    public static void selectSlot(ServerPlayer player,int handIndex,int direction) {
        if(handIndex<0 || handIndex>1 || (direction!=-1 && direction!=1))return;
        ItemStack glove=player.getItemInHand(InteractionHand.values()[handIndex]);
        if(!ModItems.isGlove(glove))return;
        long tick=player.level().getGameTime();var state=player.getPersistentData();
        if(state.getLongOr("badspell_last_slot_tick",Long.MIN_VALUE)==tick)return;
        state.putLong("badspell_last_slot_tick",tick);
        try {
            if(!GloveFormulas.current(player,glove)||!GloveFormulas.ownedBy(glove,player.getUUID())){message(player,"owner");return;}
            GloveFormulas.cycle(glove,direction,player.getUUID());GloveFormulas.backup(player,glove);
            Component title=GloveFormulas.occupied(glove,GloveFormulas.selected(glove))
                ?Component.literal(GloveFormulas.read(glove).title()):Component.translatable("message.badspell.empty_slot");
            player.sendSystemMessage(Component.translatable("message.badspell.selected_slot",GloveFormulas.selected(glove)+1,GloveFormulas.capacity(glove),title),true);
        } catch(IOException error){message(player,"invalid");}
    }
    public static void tick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        ScheduledSpells.tick(player);
        long tick=player.level().getGameTime();var state=player.getPersistentData();
        if(tick%20==0 && tick-state.getLongOr(LAST_CAST,-1000)>40)state.putInt(FOCUS,Math.min(100,state.getIntOr(FOCUS,100)+3));
    }
    private static void message(ServerPlayer player,String key) { player.sendSystemMessage(Component.translatable("message.badspell."+key),true); }
}
