package com.badspell.client;

import com.badspell.formula.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Search by meaning AND real input/output types. An input socket can lock the required result. */
public final class RunePickerScreen extends Screen {
    private static final RuneRegistry RUNES=RuneRegistry.standard();
    private final Screen parent;private final Consumer<String> choose;private final Value.Type required;
    private Value.Type input,output;private int offset;private String query="";private List<String> found=List.of();
    public RunePickerScreen(Screen parent,Value.Type required,Consumer<String> choose){super(Component.translatable("ui.badspell.runes"));this.parent=parent;this.required=required;this.output=required;this.choose=choose;}
    private Component tr(String key,Object... args){return Component.translatable("ui.badspell."+key,args);}
    private Value.Type next(Value.Type value){return value==null?Value.Type.values()[0]:value.ordinal()+1==Value.Type.values().length?null:Value.Type.values()[value.ordinal()+1];}
    private void filter(){found=RUNES.ids().stream().filter(id->{var r=RUNES.get(id);return (input==null||r.inputs().containsValue(input))&&(output==null||r.output()==output)&&RunePresentation.matches(id,r,query);}).sorted(Comparator.<String>comparingInt(id->RunePresentation.relevance(id,RUNES.get(id),query)).thenComparing(id->RunePresentation.name(id).getString())).toList();offset=Math.clamp(offset,0,Math.max(0,found.size()-rows()));}
    private int rows(){return Math.max(1,(height-112)/40);}
    @Override protected void init(){filter();var search=addRenderableWidget(new EditBox(font,18,34,width-36,20,tr("search")));search.setMaxLength(120);search.setValue(query);search.setHint(tr("search_hint"));search.setResponder(value->{query=value;offset=0;filter();rebuildRows();});
        addRenderableWidget(new SpellButton(18,60,(width-42)/2,20,tr("filter_in",input==null?tr("any"):RunePresentation.type(input)),b->{input=next(input);offset=0;rebuildWidgets();},false));
        var out=addRenderableWidget(new SpellButton(24+(width-42)/2,60,(width-42)/2,20,tr("filter_out",output==null?tr("any"):RunePresentation.type(output)),b->{output=next(output);offset=0;rebuildWidgets();},false));out.active=required==null;
        addRenderableWidget(new SpellButton(width-88,height-25,70,18,tr("back"),b->onClose(),false));rebuildRows();setInitialFocus(search);
    }
    private final List<SpellButton> rowWidgets=new ArrayList<>();
    private void rebuildRows(){for(var widget:rowWidgets)removeWidget(widget);rowWidgets.clear();for(int row=0;row<rows()&&row+offset<found.size();row++){String id=found.get(row+offset);int y=88+row*40;
        var add=addRenderableWidget(new SpellButton(width-100,y+7,36,22,Component.literal("+"),b->{minecraft.gui.setScreen(parent);choose.accept(id);},true));add.active=ClientResearch.knows(id);add.setTooltip(Tooltip.create(ClientResearch.knows(id)?tr("add_rune",RunePresentation.name(id)):ResearchScreen.tr("unknown_formula")));rowWidgets.add(add);
        var help=addRenderableWidget(new SpellButton(width-58,y+7,38,22,Component.literal("?"),b->minecraft.gui.setScreen(new RuneReferenceScreen(this,id)),false));rowWidgets.add(help);
    }}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial){g.fill(0,0,width,height,SpellTheme.BG);SpellTheme.panel(g,10,8,width-20,height-16);g.text(font,title,18,17,SpellTheme.INK,false);
        for(int row=0;row<rows()&&row+offset<found.size();row++){String id=found.get(row+offset);int y=88+row*40;g.fill(18,y,width-18,y+39,SpellTheme.CARD);g.fill(18,y,21,y+39,RunePresentation.color(RUNES.get(id).output()));g.text(font,font.plainSubstrByWidth(RunePresentation.name(id).getString(),width-132),28,y+5,SpellTheme.INK,false);g.text(font,font.plainSubstrByWidth(RunePresentation.contract(RUNES.get(id)),width-132),28,y+18,SpellTheme.ACCENT,false);g.text(font,font.plainSubstrByWidth(RunePresentation.description(id).getString(),width-132),28,y+29,SpellTheme.MUTED,false);if(mx>=18&&mx<width-108&&my>=y&&my<y+39)g.setTooltipForNextFrame(font.split(RunePresentation.description(id),240),mx,my);}
        g.text(font,tr("results",found.size(),offset+1),18,height-20,SpellTheme.MUTED,false);super.extractRenderState(g,mx,my,partial);}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){offset=Math.clamp(offset-(int)Math.signum(sy),0,Math.max(0,found.size()-rows()));rebuildRows();return true;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
