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
    public static final java.util.List<String> KINDS=java.util.List.of("arcane_workbench","crystal_grinder","alchemy_table","arcane_smelter","rune_press","engraving_station","rune_holder");
    public final Container station;
    private int laidOut=-1;
    private final ContainerData data;
    public StationMenu(int id,Inventory inventory){this(id,inventory,new SimpleContainer(12),new SimpleContainerData(6));}
    public StationMenu(int id,Inventory inventory,Container station,ContainerData data) {
        super(ProductionRegistries.MENU.get(),id);this.station=station;this.data=data;
        for(int row=0;row<2;row++)for(int col=0;col<3;col++)addSlot(new Slot(station,col+row*3,18+col*18,34+row*18));
        addSlot(new Slot(station,6,90,62){@Override public boolean mayPlace(ItemStack stack){return ModItems.isCell(stack);}});
        for(int row=0;row<2;row++)for(int col=0;col<2;col++)addSlot(new Slot(station,7+col+row*2,134+col*18,34+row*18){@Override public boolean mayPlace(ItemStack stack){return false;}});
        addSlot(new Slot(station,11,90,34){@Override public boolean mayPlace(ItemStack stack){return stack.is(ModContent.item("cell_adapter"));}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inventory,9+col+row*9,43+col*18,124+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,43+col*18,182));addDataSlots(data);layoutSlots();
    }
    public String kind(){return KINDS.get(Math.clamp(data.get(4),0,KINDS.size()-1));}
    /** Only screen coordinates change. Container indices and server transfer rules stay stable. */
    public void layoutSlots(){int kind=Math.clamp(data.get(4),0,KINDS.size()-1);if(laidOut==kind)return;laidOut=kind;
        for(int i=0;i<12;i++){
            int x,y;
            if(i<6){x=18+i%3*18;y=38+i/3*18;
                switch(kind){case 1->{x=22+i%2*18;y=30+i/2*18;}case 2->{x=14+i%3*22;y=46+i/3*20;}case 3->{x=22+i%2*18;y=30+i/2*18;}case 4->{x=16+i%3*18;y=38+i/3*18;}case 5->{x=i<2?22+i*32:14+(i-2)*18;y=i<2?36:65;}case 6->{x=i==0?114:14+(i-1)%3*18;y=i==0?32:40+(i-1)/3*18;}default->{}}
            }else if(i==6){x=94;y=90;}else if(i==11){x=116;y=90;}else{x=190+(i-7)%2*18;y=38+(i-7)/2*18;}
            final int slotId=i;var slot=new Slot(station,i,x,y){@Override public boolean mayPlace(ItemStack stack){return slotId==6?ModItems.isCell(stack):slotId==11?stack.is(ModContent.item("cell_adapter")):slotId<7;}};slot.index=i;slots.set(i,slot);
        }
    }
    public int value(int index){return data.get(index);}
    @Override public boolean clickMenuButton(Player player,int action){if(!(player instanceof net.minecraft.server.level.ServerPlayer server)||!(station instanceof StationBlockEntity block)||!block.station().equals("rune_holder")||!stillValid(player))return false;if(action==0){block.cancelSpell();return true;}if(action==1){block.activatePlate(server.level());return true;}return false;}
    @Override public boolean stillValid(Player player){return station.stillValid(player);}
    @Override public ItemStack quickMoveStack(Player player,int index) {
        var slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;var stack=slot.getItem();var copy=stack.copy();
        if(index<12){if(!moveItemStackTo(stack,12,48,true))return ItemStack.EMPTY;}
        else if(ModItems.isCell(stack)){if(!moveItemStackTo(stack,6,7,false))return ItemStack.EMPTY;}
        else if(stack.is(ModContent.item("cell_adapter"))){if(!moveItemStackTo(stack,11,12,false))return ItemStack.EMPTY;}
        else if(!moveItemStackTo(stack,0,6,false))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();return copy;
    }
}
