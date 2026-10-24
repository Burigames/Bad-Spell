package com.badspell.progression;

import com.badspell.research.ResearchRules;

import com.badspell.content.ModItems;
import com.badspell.formula.*;
import com.badspell.network.Packets;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;

/** Bounded server intents; all charges, fragments, signatures and port solutions are checked here. */
public final class ResearchActions {
    private ResearchActions(){}
    public static long seed(ServerPlayer p){return java.nio.ByteBuffer.wrap(digest("badspell.research.v1:"+p.level().getServer().overworld().getSeed())).getLong();}
    private static byte[] digest(String value){try{return java.security.MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));}catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
    private static String seal(ServerPlayer p,String rune,String author){return HexFormat.of().formatHex(digest(seed(p)+":"+rune+":"+author));}
    private static net.minecraft.nbt.CompoundTag tag(ItemStack s){return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();}
    public static ItemStack artifact(ServerPlayer p,String rune,boolean lesson){var s=new ItemStack(lesson?ModItems.TEACHING_FOLIO.get():ModItems.RUNE_FRAGMENT.get());String author=lesson?p.getUUID().toString():"fragment";CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putString("rune",rune);t.putString("author",author);t.putString("seal",seal(p,rune,author));});return s;}
    private static boolean valid(ServerPlayer p,ItemStack s,String rune,boolean lesson){var t=tag(s);String author=t.getStringOr("author","");return s.is(lesson?ModItems.TEACHING_FOLIO.get():ModItems.RUNE_FRAGMENT.get())&&t.getStringOr("rune","").equals(rune)&&(!lesson?author.equals("fragment"):!author.isEmpty())&&t.getStringOr("seal","").equals(seal(p,rune,author));}
    private static ItemStack find(ServerPlayer p,java.util.function.Predicate<ItemStack> predicate){for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(predicate.test(s))return s;}return ItemStack.EMPTY;}
    private static ItemStack book(ServerPlayer p){return find(p,s->{if(!s.is(ModItems.FORMULA_BOOK.get()))return false;try{return com.badspell.server.MagicActions.read(s).owner().equals(p.getUUID());}catch(java.io.IOException e){return false;}});}
    private static int energy(ServerPlayer p){int total=0;for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);if(ModItems.isCell(s))total+=Math.clamp(tag(s).getIntOr("badspell_fl",ModItems.cellCapacity(s)),0,ModItems.cellCapacity(s));}return total;}
    private static void pay(ServerPlayer p,ItemStack book,int fl,int ink){
        for(int i=0;i<p.getInventory().getContainerSize()&&fl>0;i++){var s=p.getInventory().getItem(i);if(!ModItems.isCell(s))continue;int charge=Math.clamp(tag(s).getIntOr("badspell_fl",ModItems.cellCapacity(s)),0,ModItems.cellCapacity(s));int debit=Math.min(charge,fl);fl-=debit;int left=charge-debit;CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putInt("badspell_fl",left));}
        int left=tag(book).getIntOr("badspell_ink",0)-ink;CustomData.update(DataComponents.CUSTOM_DATA,book,t->t.putInt("badspell_ink",left));p.getInventory().setChanged();
    }
    private static boolean funded(ServerPlayer p,ItemStack book,int fl,int ink){return !book.isEmpty()&&tag(book).getIntOr("badspell_ink",0)>=ink&&energy(p)>=fl;}
    /** 0 refresh; 1 prepare fragment; 2 validate; 3 deliberately write a teaching folio. */
    public static String perform(ServerPlayer p,int action,String rune,List<Integer> answer){
        ResearchProgress.observe(p);if(action==0)return "";
        if(!ResearchRules.ids().contains(rune))return "invalid";
        var book=book(p);if(book.isEmpty())return "need_book";
        if(action==1){
            if(ResearchProgress.knows(p,rune))return "already";
            var sample=BuiltInRegistries.ITEM.getValue(Identifier.parse(ResearchRules.family(rune).sample()));
            var source=find(p,s->s.is(sample));var paper=find(p,s->s.is(Items.PAPER));
            if(source.isEmpty()||paper.isEmpty())return "need_sample";
            if(p.getInventory().getFreeSlot()<0)return "full";
            var fragment=artifact(p,rune,false);source.shrink(1);paper.shrink(1);p.getInventory().add(fragment);return "prepared";
        }
        if(action==2){
            if(ResearchProgress.knows(p,rune))return "already";
            var fragment=find(p,s->valid(p,s,rune,false));if(fragment.isEmpty())return "need_fragment";
            var solution=ResearchRules.solution(seed(p),rune);if(answer.size()!=solution.size()||answer.stream().anyMatch(n->n<0||n>=Value.Type.values().length))return "invalid";
            int fl=ResearchRules.fieldCost(rune),ink=ResearchRules.inkCost(rune);if(!funded(p,book,fl,ink))return "need_energy";
            pay(p,book,fl,ink);for(int i=0;i<solution.size();i++)if(solution.get(i).ordinal()!=answer.get(i))return "mismatch";
            fragment.shrink(1);ResearchProgress.learn(p,rune);return "learned";
        }
        if(action==3){
            if(!ResearchProgress.knows(p,rune))return "unknown_formula";
            var paper=find(p,s->s.is(Items.PAPER));if(paper.isEmpty())return "need_paper";
            if(p.getInventory().getFreeSlot()<0)return "full";
            if(!funded(p,book,10,2))return "need_energy";
            var folio=artifact(p,rune,true);pay(p,book,10,2);paper.shrink(1);p.getInventory().add(folio);return "shared";
        }
        return "invalid";
    }
    public static void handle(ServerPlayer p,Packets.ResearchAction packet){
        long tick=p.level().getGameTime();var state=p.getPersistentData();if(tick-state.getLongOr("badspell_research_request",-1000)<4)return;state.putLong("badspell_research_request",tick);
        String result;try{result=perform(p,packet.action(),packet.rune(),packet.answer());}catch(IllegalArgumentException|IllegalStateException e){result="invalid";}
        ResearchProgress.send(p,packet.open(),result);
    }
    public static void use(ServerPlayer p,InteractionHand hand,boolean lesson){var stack=p.getItemInHand(hand);String rune=tag(stack).getStringOr("rune","");
        if(!ResearchRules.ids().contains(rune)||!valid(p,stack,rune,lesson)){p.sendSystemMessage(Component.translatable("research.badspell.invalid"),true);return;}
        if(lesson){if(ResearchProgress.knows(p,rune)){p.sendSystemMessage(Component.translatable("research.badspell.already"),true);return;}ResearchProgress.learn(p,rune);stack.shrink(1);ResearchProgress.send(p,false,"learned");p.sendSystemMessage(Component.translatable("research.badspell.learned"),true);}
        else ResearchProgress.send(p,true,"",rune);
    }
}
