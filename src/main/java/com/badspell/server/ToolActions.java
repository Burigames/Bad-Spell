package com.badspell.server;

import com.badspell.content.*;
import com.badspell.formula.*;
import com.badspell.network.Packets;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ToolActions {
    private ToolActions(){}
    public static boolean utility(ItemStack stack){return stack.getItem() instanceof UtilityItem;}
    private static net.minecraft.nbt.CompoundTag data(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();}
    public static void useAir(ServerPlayer player,InteractionHand hand,int kind) {
        var stack=player.getItemInHand(hand);
        if(kind==1){int mode=1-data(stack).getIntOr("badspell_mode",0);CustomData.update(DataComponents.CUSTOM_DATA,stack,t->t.putInt("badspell_mode",mode));player.sendSystemMessage(Component.translatable(mode==0?"message.badspell.fork_diagnose":"message.badspell.fork_activate"),true);}
        if(kind==2)locate(player,stack);
        if(kind==3){int mode=(data(stack).getIntOr("badspell_intensity",1)+1)%3;CustomData.update(DataComponents.CUSTOM_DATA,stack,t->t.putInt("badspell_intensity",mode));player.sendSystemMessage(Component.translatable("message.badspell.monocle_intensity",mode),true);}
    }
    private static boolean allowed(ServerPlayer player,BlockPos at){return player.blockPosition().distSqr(at)<=64&&player.level().getChunkSource().hasChunk(at.getX()>>4,at.getZ()>>4)&&player.level().getWorldBorder().isWithinBounds(at)&&MagicPermissions.mayUse(player,at,Direction.UP);}
    public static void useBlock(ServerPlayer player,InteractionHand hand,BlockPos at,Direction face,int kind) {
        if(!allowed(player,at))return;var stack=player.getItemInHand(hand);var level=player.level();
        if(kind==0) {
            if(!level.getBlockState(at).isCollisionShapeFullBlock(level,at)||!level.getBlockState(at).isFaceSturdy(level,at,face)){message(player,"surface");return;}
            var stored=RuneBoards.get(level).find(at,face.ordinal());Formula formula=stored==null?new Formula(UUID.randomUUID(),player.getUUID(),1,"Runenfeld",List.of(),List.of(),"",1000):stored.formula();if(!formula.owner().equals(player.getUUID())){message(player,"owner");return;}
            try{PacketDistributor.sendToPlayer(player,new Packets.BoardView(hand.ordinal(),at,face.ordinal(),FormulaCodec.encode(formula),stack.getMaxDamage()-stack.getDamageValue()));}catch(java.io.IOException error){message(player,"invalid");}
        }
        if(kind==1) {
            var board=RuneBoards.get(level).find(at,face.ordinal());var machine=level.getBlockEntity(at);
            if(data(stack).getIntOr("badspell_mode",0)==0){
                if(machine instanceof com.badspell.production.StationBlockEntity station){player.sendSystemMessage(Component.translatable("message.badspell.machine_scan",station.getDisplayName(),station.canUse(player)?station.data.get(2):0,Component.translatable(station.canUse(player)?"machine.badspell.status_"+station.data.get(3):"message.badspell.private")),false);return;}
                if(board==null){message(player,"no_board");return;}var formula=board.formula();if(!formula.owner().equals(player.getUUID())){message(player,"private");return;}var errors=new FormulaValidator(RuneRegistry.standard()).validate(formula);player.sendSystemMessage(Component.translatable("message.badspell.board_scan",formula.nodes().size(),errors.size()),false);
            } else {
                boolean applied=false;
                if(machine instanceof com.badspell.production.StationBlockEntity station&&station.canUse(player))applied=station.activatePlate(level);
                else if(board!=null&&board.formula().owner().equals(player.getUUID())){var formula=board.formula();var raw=SpellEffects.context(player,at.relative(face),level.getGameTime());var environment=new HashMap<>(raw.environment());float hardness=level.getBlockState(at).getDestroySpeed(level,at);environment.put("stability",new Value.Number(hardness<0?100:Math.clamp((int)(20+hardness*20),0,100)));var context=new RuneRegistry.Context(raw.target(),raw.tick(),raw.entity(),raw.look(),environment);applied=MagicActions.execute(player,formula,context)==ActivationService.Result.APPLIED;}
                if(applied)stack.hurtAndBreak(1,player,hand);else message(player,"activation_denied");
            }
        }
        if(kind==2) {
            if(player.isShiftKeyDown()){
                UUID signature=UUID.randomUUID();var old=data(stack).read("badspell_signature",UUIDUtil.CODEC).orElse(null);
                CustomData.update(DataComponents.CUSTOM_DATA,stack,t->{t.store("badspell_signature",UUIDUtil.CODEC,signature);t.putString("badspell_dimension",level.dimension().identifier().toString());t.putInt("badspell_x",at.getX());t.putInt("badspell_y",at.getY());t.putInt("badspell_z",at.getZ());});message(player,"signature_bound");
            }else locate(player,stack);
        }
        if(kind==3)useAir(player,hand,kind);
    }
    private static void locate(ServerPlayer player,ItemStack stack) {
        var tag=data(stack);if(!tag.contains("badspell_signature")){message(player,"signature_missing");return;}
        if(!tag.getStringOr("badspell_dimension","").equals(player.level().dimension().identifier().toString())){message(player,"signature_far");return;}
        double x=tag.getIntOr("badspell_x",0)+.5-player.getX(),z=tag.getIntOr("badspell_z",0)+.5-player.getZ(),y=tag.getIntOr("badspell_y",0)-player.getY(),distance=Math.sqrt(x*x+y*y+z*z);int range=stack.is(ModItems.FIELD_COMPASS_Q3.get())?4096:1024;if(distance>range){message(player,"signature_far");return;}
        double target=Math.toDegrees(Math.atan2(-x,z)),angle=((target-player.getYRot()+540)%360)-180;String direction=Math.abs(angle)<15?"forward":Math.abs(angle)>165?"back":angle>0?"right":"left";
        player.sendSystemMessage(Component.translatable("message.badspell.compass",Component.translatable("direction.badspell."+direction),Math.max(1,5-(int)(distance/range*5))),true);
    }
    public static void handle(ServerPlayer player,Packets.BoardAction packet) {
        if(packet.hand()<0||packet.hand()>1||packet.face()<0||packet.face()>5||!allowed(player,packet.position()))return;var hand=InteractionHand.values()[packet.hand()];var tool=player.getItemInHand(hand);if(!tool.is(ModItems.RUNE_CHISEL.get()))return;
        long tick=player.level().getGameTime();if(tick-player.getPersistentData().getLongOr("badspell_board_request",-1000)<5)return;player.getPersistentData().putLong("badspell_board_request",tick);
        try {
            var formula=FormulaCodec.decode(packet.formula());var boards=RuneBoards.get(player.level());var old=boards.find(packet.position(),packet.face());
            if(!formula.owner().equals(player.getUUID())||formula.nodes().size()>16||!new FormulaValidator(RuneRegistry.standard()).validate(formula).isEmpty()||!player.level().getBlockState(packet.position()).isCollisionShapeFullBlock(player.level(),packet.position())||!player.level().getBlockState(packet.position()).isFaceSturdy(player.level(),packet.position(),Direction.values()[packet.face()]))return;
            if(old!=null&&(!old.formula().owner().equals(player.getUUID())||!old.formula().id().equals(formula.id())||old.formula().revision()!=formula.revision()))return;
            var occupied=new HashSet<String>();for(var node:formula.nodes())if(node.x()<0||node.y()<0||node.x()>462||node.y()>300||node.x()%154!=0||node.y()%100!=0||!occupied.add(node.x()+":"+node.y()))return;
            Map<String,Formula.Node> previous=new HashMap<>();if(old!=null)old.formula().nodes().forEach(node->previous.put(node.id(),node));int cost=0;for(var node:formula.nodes())if(!node.equals(previous.get(node.id())))cost++;if(cost>tool.getMaxDamage()-tool.getDamageValue())return;
            var saved=new Formula(formula.id(),formula.owner(),formula.revision()+1,formula.title(),formula.nodes(),formula.edges(),formula.output(),formula.energyBudget());boards.put(packet.position(),packet.face(),saved);tool.hurtAndBreak(cost,player,hand);RuneBoards.sync(player.level(),packet.position(),packet.face(),saved);
            if(!tool.isEmpty())PacketDistributor.sendToPlayer(player,new Packets.BoardView(packet.hand(),packet.position(),packet.face(),FormulaCodec.encode(saved),tool.getMaxDamage()-tool.getDamageValue()));
        }catch(java.io.IOException|IllegalArgumentException error){message(player,"invalid");}
    }
    public static void toggleMonocle(ServerPlayer player){var stack=player.getItemBySlot(EquipmentSlot.HEAD);if(!stack.is(ModItems.ARCANE_MONOCLE.get()))return;long tick=player.level().getGameTime();if(tick-player.getPersistentData().getLongOr("badspell_monocle_toggle",-1000)<5)return;player.getPersistentData().putLong("badspell_monocle_toggle",tick);int mode=(data(stack).getIntOr("badspell_intensity",1)+1)%3;CustomData.update(DataComponents.CUSTOM_DATA,stack,t->t.putInt("badspell_intensity",mode));player.sendSystemMessage(Component.translatable("message.badspell.monocle_intensity",mode),true);}
    public static void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event){if(event.getEntity() instanceof ServerPlayer player&&player.level().getGameTime()%100==0)scan(player);}
    public static void scan(ServerPlayer player) {
        var monocle=player.getItemBySlot(EquipmentSlot.HEAD);if(!monocle.is(ModItems.ARCANE_MONOCLE.get()))return;int intensity=data(monocle).getIntOr("badspell_intensity",1);if(intensity==0)return;
        int focus=player.getPersistentData().getIntOr("badspell_focus",100);if(focus<1)return;player.getPersistentData().putInt("badspell_focus",focus-1);var hit=player.pick(12,1,false);if(!(hit instanceof net.minecraft.world.phys.BlockHitResult block)||!allowed(player,block.getBlockPos()))return;
        String text="";if(player.level().getBlockEntity(block.getBlockPos()) instanceof com.badspell.production.StationBlockEntity station)text=station.canUse(player)?station.getDisplayName().getString()+" · "+station.data.get(2)+" FL · "+Component.translatable("machine.badspell.status_"+station.data.get(3)).getString():Component.translatable("message.badspell.private").getString();
        else {var board=RuneBoards.get(player.level()).find(block.getBlockPos(),block.getDirection().ordinal());if(board!=null)text=board.formula().owner().equals(player.getUUID())?board.formula().title()+" · "+board.formula().nodes().size()+" "+Component.translatable("gui.badspell.runes_short").getString():Component.translatable("message.badspell.private").getString();}
        PacketDistributor.sendToPlayer(player,new Packets.ScanInfo(block.getBlockPos(),text,intensity));
    }
    private static void message(ServerPlayer player,String key){player.sendSystemMessage(Component.translatable("message.badspell."+key),true);}
}
