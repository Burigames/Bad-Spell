package com.badspell.production;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import com.badspell.content.ModContent;
import com.badspell.content.ModItems;

public final class StationMenu extends AbstractContainerMenu {
    public final Container station;
    private final ContainerData data;
    public StationMenu(int id,Inventory inventory){this(id,inventory,new SimpleContainer(12),new SimpleContainerData(4));}
    public StationMenu(int id,Inventory inventory,Container station,ContainerData data) {
        super(ProductionRegistries.MENU.get(),id);this.station=station;this.data=data;
        for(int row=0;row<2;row++)for(int col=0;col<3;col++)addSlot(new Slot(station,col+row*3,18+col*18,34+row*18));
        addSlot(new Slot(station,6,90,62){@Override public boolean mayPlace(ItemStack stack){return stack.is(ModItems.FIELD_CELL.get());}});
        for(int row=0;row<2;row++)for(int col=0;col<2;col++)addSlot(new Slot(station,7+col+row*2,134+col*18,34+row*18){@Override public boolean mayPlace(ItemStack stack){return false;}});
        addSlot(new Slot(station,11,90,34){@Override public boolean mayPlace(ItemStack stack){return stack.is(ModContent.item("cell_adapter"));}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,9+col+row*9,8+col*18,108+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,8+col*18,166));addDataSlots(data);
    }
    public int value(int index){return data.get(index);}
    @Override public boolean stillValid(Player player){return station.stillValid(player);}
    @Override public ItemStack quickMoveStack(Player player,int index) {
        var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var copy=stack.copy();
        if(index<12){if(!moveItemStackTo(stack,12,48,true))return ItemStack.EMPTY;}
        else if(stack.is(ModItems.FIELD_CELL.get())){if(!moveItemStackTo(stack,6,7,false))return ItemStack.EMPTY;}
        else if(stack.is(ModContent.item("cell_adapter"))){if(!moveItemStackTo(stack,11,12,false))return ItemStack.EMPTY;}
        else if(!moveItemStackTo(stack,0,6,false))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();return copy;
    }
}
