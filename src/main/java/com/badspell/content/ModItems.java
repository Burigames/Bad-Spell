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
    private static final DeferredRegister<CreativeModeTab> TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,BadSpell.MOD_ID);
    static {
        TABS.register("badspell",()->CreativeModeTab.builder().title(Component.translatable("itemGroup.badspell"))
            .icon(()->FORMULA_BOOK.get().getDefaultInstance()).displayItems((parameters,output)->{
                output.accept(FORMULA_BOOK);output.accept(RUNE_GLOVE);output.accept(FIELD_CELL);output.accept(RUNE_INK);
            }).build());
    }
    private ModItems() {}
    public static void register(IEventBus bus) { ITEMS.register(bus);TABS.register(bus); }
    private static class InfoItem extends Item {
        InfoItem(Properties properties){super(properties);}
        @Override public void appendHoverText(ItemStack stack,Item.TooltipContext context,net.minecraft.world.item.component.TooltipDisplay display,java.util.function.Consumer<Component> text,TooltipFlag flag) {
            var data=stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            if(stack.is(FIELD_CELL.get()))text.accept(Component.translatable("tooltip.badspell.cell",Math.clamp(data.getIntOr("badspell_fl",100),0,100)));
            if(stack.is(FORMULA_BOOK.get()))text.accept(Component.translatable("tooltip.badspell.ink",data.getIntOr("badspell_ink",64)));
            if(stack.is(RUNE_GLOVE.get()))text.accept(Component.translatable("tooltip.badspell.slots",com.badspell.item.GloveFormulas.selected(stack)+1,com.badspell.item.GloveFormulas.CAPACITY));
            var payload=data.getByteArray("badspell_formula");
            if(payload.isPresent()) {
                try {
                    if(payload.get().length>com.badspell.network.Packets.MAX_FORMULA_BYTES)throw new java.io.IOException("Snapshot limit");
                    var f=com.badspell.formula.FormulaCodec.decode(payload.get());
                    text.accept(Component.translatable("tooltip.badspell.snapshot",f.title(),f.revision(),f.nodes().size()));
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
