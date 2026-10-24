package com.badspell.client;

import com.badspell.network.Packets;
import com.badspell.research.ResearchRules;
import java.util.*;

/** Client mirror only. It never grants server knowledge. */
public final class ClientResearch {
    private ClientResearch(){}
    public static Packets.ResearchState state=new Packets.ResearchState(false,0,List.copyOf(ResearchRules.BASICS),List.of(),List.of(),0,false,"","");
    public static boolean knows(String id){var p=net.minecraft.client.Minecraft.getInstance().player;return p!=null&&p.isCreative()||state.known().contains(id);}
    public static void accept(Packets.ResearchState packet){state=packet;var game=net.minecraft.client.Minecraft.getInstance();if(packet.open()&&!packet.focus().isBlank()){game.gui.setScreen(new ResearchScreen(game.gui.screen(),false,packet.focus()));return;}if(game.gui.screen() instanceof ResearchAnalysisScreen analysis){analysis.accept(packet);return;}if(game.gui.screen() instanceof ResearchScreen screen){screen.refresh();return;}if(packet.open())game.gui.setScreen(new ResearchScreen(game.gui.screen(),false));}
    public static void request(boolean open){net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new Packets.ResearchAction(0,"",List.of(),open));}
}
