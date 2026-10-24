package com.badspell.client;

import com.badspell.research.ResearchRules;
import com.badspell.formula.Value;
import com.badspell.network.Packets;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Each socket is explicit; stable hints explain the measurement instead of relying on trial and error. */
public final class ResearchAnalysisScreen extends Screen {
    private final ResearchScreen parent;private final String rune;private final List<Value.Type> answers=new ArrayList<>();private int scroll;
    public ResearchAnalysisScreen(ResearchScreen parent,String rune){super(ResearchScreen.tr("analyze"));this.parent=parent;this.rune=rune;for(int i=0;i<ResearchRules.solution(ClientResearch.state.seed(),rune).size();i++)answers.add(Value.Type.NUMBER);}
    public void accept(Packets.ResearchState p){if(ClientResearch.knows(rune))minecraft.gui.setScreen(parent);else rebuildWidgets();}
    private List<String> slots(){return ResearchRules.slots(ClientResearch.state.seed(),rune);}
    @Override protected void init(){var ports=slots();int available=Math.max(1,(height-142)/23);scroll=Math.clamp(scroll,0,Math.max(0,answers.size()-available));
        for(int i=0;i<available&&i+scroll<answers.size();i++){int index=i+scroll;String port=index<ports.size()?ports.get(index):"→";addRenderableWidget(new SpellButton(width/2,96+i*23,width/2-23,20,Component.literal(port+": "+RunePresentation.type(answers.get(index)).getString()),b->{answers.set(index,Value.Type.values()[(answers.get(index).ordinal()+1)%Value.Type.values().length]);rebuildWidgets();},false));}
        addRenderableWidget(new SpellButton(20,height-49,width-40,20,ResearchScreen.tr("validate",ResearchRules.fieldCost(rune),ResearchRules.inkCost(rune)),b->ClientPacketDistributor.sendToServer(new Packets.ResearchAction(2,rune,answers.stream().map(Enum::ordinal).toList(),false)),true));
        addRenderableWidget(new SpellButton(width-90,height-25,70,18,Component.translatable("ui.badspell.back"),b->onClose(),false));
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){g.fill(0,0,width,height,SpellTheme.BG);SpellTheme.panel(g,10,8,width-20,height-16);g.text(font,title,20,18,SpellTheme.INK,false);g.text(font,RunePresentation.name(rune),20,36,SpellTheme.ACCENT,false);
        int y=56;for(var line:font.split(ResearchScreen.tr("analysis_body"),width-40)){g.text(font,line,20,y,SpellTheme.INK,false);y+=12;}
        g.enableScissor(18,92,width/2-8,height-55);int at=97-scroll*23;var ports=slots();var solution=ResearchRules.solution(ClientResearch.state.seed(),rune);
        var hints=new ArrayList<Component>();hints.add(ResearchScreen.tr("hint_inputs",ports.size()));hints.add(ResearchScreen.tr("hint_output",RunePresentation.type(solution.getLast())));hints.add(ResearchScreen.tr("hint_ports",ports.isEmpty()?ResearchScreen.tr("no_inputs"):Component.literal(String.join(", ",ports.stream().map(p->p+" = "+RunePresentation.type(ResearchRules.definition(rune).inputs().get(p)).getString()).toList()))));
        for(var hint:hints){for(var line:font.split(hint,width/2-43)){g.text(font,line,22,at,SpellTheme.MUTED,false);at+=12;}at+=7;}g.disableScissor();
        if(!ClientResearch.state.message().isEmpty())g.text(font,font.plainSubstrByWidth(ResearchScreen.tr(ClientResearch.state.message()).getString(),width-114),20,height-20,ClientResearch.state.message().equals("mismatch")||ClientResearch.state.message().startsWith("need_")?SpellTheme.ERROR:SpellTheme.ACCENT,false);super.extractRenderState(g,mx,my,partial);}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){scroll-=(int)Math.signum(sy);rebuildWidgets();return true;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
