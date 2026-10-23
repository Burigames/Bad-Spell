package com.badspell.content;

import com.badspell.BadSpell;
import com.badspell.server.MagicActions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(BadSpell.MOD_ID);
    public static final DeferredItem<Item> FORMULA_BOOK=ITEMS.registerItem("formula_book",p->new MagicItem(p,0),p->p.stacksTo(1));
    public static final DeferredItem<Item> RUNE_GLOVE=ITEMS.registerItem("rune_glove",p->new MagicItem(p,1),p->p.stacksTo(1));
    public static final DeferredItem<Item> FIELD_CELL=ITEMS.registerItem("field_cell",InfoItem::new,p->p.stacksTo(1));
    public static final DeferredItem<Item> RUNE_INK=ITEMS.registerItem("rune_ink",InfoItem::new,p->p.stacksTo(16));
    public static final DeferredItem<Item> RUNE_GLOVE_Q2=ITEMS.registerItem("rune_glove_q2",p->new MagicItem(p,1),p->p.stacksTo(1));
    public static final DeferredItem<Item> RUNE_GLOVE_Q3=ITEMS.registerItem("rune_glove_q3",p->new MagicItem(p,1),p->p.stacksTo(1));
    public static final DeferredItem<Item> FIELD_CELL_Q2=ITEMS.registerItem("field_cell_q2",InfoItem::new,p->p.stacksTo(1));
    public static final DeferredItem<Item> FIELD_CELL_Q3=ITEMS.registerItem("field_cell_q3",InfoItem::new,p->p.stacksTo(1));
    public static final DeferredItem<Item> RUNE_CHISEL=ITEMS.registerItem("rune_chisel",p->new UtilityItem(p,0),p->p.durability(256));
    public static final DeferredItem<Item> RESONANCE_FORK=ITEMS.registerItem("resonance_fork",p->new UtilityItem(p,1),p->p.durability(256));
    public static final DeferredItem<Item> FIELD_COMPASS=ITEMS.registerItem("field_compass",p->new UtilityItem(p,2),p->p.stacksTo(1));
    public static final DeferredItem<Item> FIELD_COMPASS_Q3=ITEMS.registerItem("field_compass_q3",p->new UtilityItem(p,2),p->p.stacksTo(1));
    public static final DeferredItem<Item> ARCANE_MONOCLE=ITEMS.registerItem("arcane_monocle",p->new UtilityItem(p,3),p->p.stacksTo(1).equippable(net.minecraft.world.entity.EquipmentSlot.HEAD));
    public static boolean isGlove(ItemStack stack){return stack.is(RUNE_GLOVE.get())||stack.is(RUNE_GLOVE_Q2.get())||stack.is(RUNE_GLOVE_Q3.get());}
    public static int cellCapacity(ItemStack stack){return stack.is(FIELD_CELL.get())?100:stack.is(FIELD_CELL_Q2.get())?300:stack.is(FIELD_CELL_Q3.get())?600:0;}
    public static boolean isCell(ItemStack stack){return cellCapacity(stack)>0;}
    private static final DeferredRegister<CreativeModeTab> TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,BadSpell.MOD_ID);
    static {
        TABS.register("badspell",()->CreativeModeTab.builder().title(Component.translatable("itemGroup.badspell"))
            .icon(()->FORMULA_BOOK.get().getDefaultInstance()).displayItems((parameters,output)->{
                ITEMS.getEntries().forEach(holder->output.accept(holder.get()));
            }).build());
    }
    private ModItems() {}
    public static void register(IEventBus bus) { ITEMS.register(bus);TABS.register(bus); }
    private static class InfoItem extends Item {
        InfoItem(Properties properties){super(properties);}
        @Override public void appendHoverText(ItemStack stack,Item.TooltipContext context,net.minecraft.world.item.component.TooltipDisplay display,java.util.function.Consumer<Component> text,TooltipFlag flag) {
            var data=stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            if(isCell(stack))text.accept(Component.translatable("tooltip.badspell.cell",Math.clamp(data.getIntOr("badspell_fl",cellCapacity(stack)),0,cellCapacity(stack))));
            if(stack.is(FORMULA_BOOK.get()))text.accept(Component.translatable("tooltip.badspell.ink",data.getIntOr("badspell_ink",64)));
            if(isGlove(stack))text.accept(Component.translatable("tooltip.badspell.slots",com.badspell.item.GloveFormulas.selected(stack)+1,com.badspell.item.GloveFormulas.capacity(stack)));
            var payload=data.getByteArray("badspell_formula");
            if(payload.isPresent()) {
                try {
                    if(payload.get().length>com.badspell.network.Packets.MAX_FORMULA_BYTES)throw new java.io.IOException("Snapshot limit");
                    var f=com.badspell.formula.FormulaCodec.decode(payload.get());
                    text.accept(Component.translatable("tooltip.badspell.snapshot",f.title(),f.revision(),f.nodes().size()));
                    if(isGlove(stack))try{var at=new com.badspell.formula.Value.Position("minecraft:overworld",0,64,0);var entity=new com.badspell.formula.Value.Entity(f.owner(),at);var previewContext=new com.badspell.formula.RuneRegistry.Context(at,0,entity,new com.badspell.formula.Value.Vector(0,0,1000),java.util.Map.of("self",entity));var effect=new com.badspell.formula.FormulaEngine(com.badspell.formula.RuneRegistry.standard()).plan(f,previewContext);text.accept(Component.translatable("tooltip.badspell.cast_cost",effect.fieldCost(),effect.focusCost()));}catch(IllegalArgumentException error){}
                } catch(java.io.IOException error){text.accept(Component.translatable("message.badspell.invalid"));}
            }
        }
    }
    private static final class MagicItem extends InfoItem {
        private final int action;
        MagicItem(Properties properties,int action) { super(properties);this.action=action; }
        @Override public InteractionResult use(Level level,Player player,InteractionHand hand) {
            if(player instanceof ServerPlayer serverPlayer) {
                if(action==0)MagicActions.openBook(serverPlayer,hand);
                else MagicActions.cast(serverPlayer,hand);
            }
            return InteractionResult.SUCCESS;
        }
    }
}
