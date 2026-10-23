package com.badspell.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import com.badspell.server.MagicActions;

public final class Packets {
    private Packets() {}
    public static final int MAX_FORMULA_BYTES=30_000;
    public record OpenBook(int hand,byte[] formula,int ink) implements CustomPacketPayload {
        public static final Type<OpenBook> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","open_book"));
        public static final StreamCodec<RegistryFriendlyByteBuf,OpenBook> CODEC=CustomPacketPayload.codec(
            (p,b)->{b.writeVarInt(p.hand);b.writeByteArray(p.formula);b.writeVarInt(p.ink);},
            b->new OpenBook(b.readVarInt(),b.readByteArray(MAX_FORMULA_BYTES),b.readVarInt()));
        public Type<OpenBook> type(){return TYPE;}
    }
    /** action 0=save, 1=export authoritative book snapshot, 2=refill ink, 3=read private revision history. */
    public record BookAction(int hand,int action,byte[] formula) implements CustomPacketPayload {
        public static final Type<BookAction> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","book_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf,BookAction> CODEC=CustomPacketPayload.codec(
            (p,b)->{b.writeVarInt(p.hand);b.writeVarInt(p.action);b.writeByteArray(p.formula);},
            b->new BookAction(b.readVarInt(),b.readVarInt(),b.readByteArray(MAX_FORMULA_BYTES)));
        public Type<BookAction> type(){return TYPE;}
    }
    public record BookHistory(int hand,byte[] current,int ink,java.util.List<byte[]> revisions) implements CustomPacketPayload {
        public static final Type<BookHistory> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","book_history"));
        public static final StreamCodec<RegistryFriendlyByteBuf,BookHistory> CODEC=CustomPacketPayload.codec(
            (p,b)->{
                if(p.revisions.size()>20)throw new IllegalArgumentException("History limit");
                b.writeVarInt(p.hand);b.writeByteArray(p.current);b.writeVarInt(p.ink);b.writeVarInt(p.revisions.size());
                for(byte[] revision:p.revisions)b.writeByteArray(revision);
            },b->{
                int hand=b.readVarInt();byte[] current=b.readByteArray(MAX_FORMULA_BYTES);int ink=b.readVarInt(),count=b.readVarInt();
                if(count<0 || count>20)throw new IllegalArgumentException("History limit");
                java.util.List<byte[]> revisions=new java.util.ArrayList<>();
                for(int i=0;i<count;i++)revisions.add(b.readByteArray(MAX_FORMULA_BYTES));
                return new BookHistory(hand,current,ink,java.util.List.copyOf(revisions));
            });
        public Type<BookHistory> type(){return TYPE;}
    }
    /** Only a hand and a one-step intent cross the trust boundary; no client snapshot or capacity. */
    public record GloveSlot(int hand,int direction) implements CustomPacketPayload {
        public static final Type<GloveSlot> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","glove_slot"));
        public static final StreamCodec<RegistryFriendlyByteBuf,GloveSlot> CODEC=CustomPacketPayload.codec(
            (p,b)->{b.writeVarInt(p.hand);b.writeVarInt(p.direction);},b->new GloveSlot(b.readVarInt(),b.readVarInt()));
        public Type<GloveSlot> type(){return TYPE;}
    }
    public record LightUpdate(BlockPos position,int strength) implements CustomPacketPayload {
        public static final Type<LightUpdate> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","light_update"));
        public static final StreamCodec<RegistryFriendlyByteBuf,LightUpdate> CODEC=CustomPacketPayload.codec(
            (p,b)->{b.writeBlockPos(p.position);b.writeVarInt(p.strength);},b->new LightUpdate(b.readBlockPos(),b.readVarInt()));
        public Type<LightUpdate> type(){return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar=event.registrar("1");
        registrar.playToClient(OpenBook.TYPE,OpenBook.CODEC);
        registrar.playToClient(BookHistory.TYPE,BookHistory.CODEC);
        registrar.playToServer(GloveSlot.TYPE,GloveSlot.CODEC,(packet,context)->{
            if(context.player() instanceof net.minecraft.server.level.ServerPlayer player)MagicActions.selectSlot(player,packet.hand(),packet.direction());
        });
        registrar.playToClient(LightUpdate.TYPE,LightUpdate.CODEC);
        registrar.playToServer(BookAction.TYPE,BookAction.CODEC,(packet,context)->{
            if(context.player() instanceof net.minecraft.server.level.ServerPlayer player)MagicActions.handle(player,packet);
        });
    }
}
