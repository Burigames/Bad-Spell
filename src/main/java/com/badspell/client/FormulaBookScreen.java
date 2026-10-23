package com.badspell.client;

import com.badspell.formula.*;
import com.badspell.network.Packets;
import java.io.IOException;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.*;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** Responsive editor: one inspector, readable canvas, explicit validity and immutable server snapshots. */
public final class FormulaBookScreen extends Screen {
    private static final RuneRegistry RUNES=RuneRegistry.standard();
    private static final int BG=0xff0b131c,PANEL=0xff121f2a,LINE=0xff223743,TEXT=0xffe2eef1,MUTED=0xff8ba6ae,ACCENT=0xff65dcba;
    private static final int NODE_W=110,TOP=60;
    private final int hand;
    private int ink,sideTab,paletteOffset,nextId=1;
    private Formula formula;
    private String savedChecksum,status="",search="",selected,connecting,dragging;
    private final Deque<Formula> undo=new ArrayDeque<>(),redo=new ArrayDeque<>();
    private Formula beforeDrag;
    private double zoom=.85,panX=16,panY=18;
    private boolean fitted,updatingFields;
    private EditBox searchBox,titleBox,budgetBox;
    private SpellButton saveButton,exportButton;
    private List<FormulaValidator.Diagnostic> diagnostics=List.of();
    private Value.Effect preview;
    private int canvasX,canvasY,canvasW,canvasH,sideW;
    public FormulaBookScreen(int hand,Formula formula,int ink) {
        super(tr("screen.badspell.formula_book"));this.hand=hand;this.formula=formula;this.ink=ink;this.savedChecksum=new FormulaSnapshot(formula).checksum();validate();
    }
    private static Component tr(String key,Object... args){return Component.translatable(key,args);}
    private static String runeName(String rune){return tr("rune.badspell."+rune.substring(rune.indexOf(':')+1)).getString();}
    private boolean dirty(){return !savedChecksum.equals(new FormulaSnapshot(formula).checksum());}
    public boolean accepts(int nextHand,Formula next){return nextHand==hand&& formula.id().equals(next.id())&&formula.owner().equals(next.owner());}
    public void acceptServer(Formula next,int ink) {
        formula=next;this.ink=ink;savedChecksum=new FormulaSnapshot(next).checksum();status=tr("gui.badspell.synced").getString();validate();rebuildWidgets();
    }
    public void restoreDraft(Formula archived) {
        selected=null;fitted=false;
        change(new Formula(formula.id(),formula.owner(),formula.revision(),archived.title(),archived.nodes(),archived.edges(),archived.output(),archived.energyBudget()));fit();
    }
    private SpellButton button(Component label,int x,int y,int w,int h,Runnable action,boolean primary) {
        return addRenderableWidget(new SpellButton(x,y,w,h,label,b->action.run(),primary));
    }
    private SpellButton button(String key,int x,int y,int w,Runnable action){return button(tr(key),x,y,w,18,action,false);}
    @Override protected void init() {
        sideW=Math.clamp(width/3,114,170);canvasX=sideW+20;canvasY=TOP+28;canvasW=Math.max(90,width-canvasX-10);canvasH=Math.max(50,height-canvasY-30);
        int x=10;
        var save=button(tr("gui.badspell.save"),x,32,64,20,()->send(0),true);saveButton=save;save.active=diagnostics.isEmpty();x+=68;
        var export=button("gui.badspell.export",x,33,80,()->send(1));exportButton=export;export.active=diagnostics.isEmpty()&&!dirty();export.setTooltip(Tooltip.create(tr("gui.badspell.export_tip")));x+=84;
        button("gui.badspell.check",x,33,60,()->{sideTab=2;validate();rebuildWidgets();});x+=64;
        button("gui.badspell.history",x,33,64,()->send(3));
        button(Component.literal("<"),width-(width<365?162:66),width<365?5:33,25,18,()->history(undo,redo),false).active=!undo.isEmpty();
        button(Component.literal(">"),width-(width<365?133:37),width<365?5:33,25,18,()->history(redo,undo),false).active=!redo.isEmpty();
        int tab=(sideW-8)/3;
        for(int i=0;i<3;i++) {final int selectedTab=i;button(tr("gui.badspell.tab_"+i),14+i*tab,TOP+5,tab-2,17,()->{sideTab=selectedTab;rebuildWidgets();},sideTab==i);}
        if(sideTab==0) {
            searchBox=addRenderableWidget(new EditBox(font,16,TOP+29,sideW-12,18,tr("gui.badspell.search")));searchBox.setMaxLength(40);searchBox.setHint(tr("gui.badspell.search"));searchBox.setValue(search);
            searchBox.setResponder(value->{search=value;paletteOffset=0;refreshPalette();});
            refreshPalette();
        } else if(sideTab==1) {
            var selectedNode=node(selected);
            if(selectedNode==null) {
                updatingFields=true;
                titleBox=addRenderableWidget(new EditBox(font,16,TOP+41,sideW-12,18,tr("gui.badspell.name")));titleBox.setMaxLength(96);titleBox.setValue(formula.title());
                titleBox.setResponder(value->{if(!updatingFields&&!value.isBlank())changeMetadata(value,formula.energyBudget());});
                budgetBox=addRenderableWidget(new EditBox(font,16,TOP+79,sideW-12,18,tr("gui.badspell.budget_label")));budgetBox.setMaxLength(7);budgetBox.setValue(Long.toString(formula.energyBudget()));
                budgetBox.setResponder(value->{if(!updatingFields){boolean valid=value.matches("[0-9]{1,7}");budgetBox.setTextColor(valid?TEXT:0xffe68c8c);if(valid)changeMetadata(formula.title(),Long.parseLong(value));}});
                updatingFields=false;
            } else {
                int y=TOP+64;
                for(var entry:new TreeMap<>(selectedNode.parameters()).entrySet()) {
                    final String parameter=entry.getKey();if(y+18>height-50)break;
                    if(entry.getValue() instanceof Value.Number number) {
                        button(Component.literal("−"),16,y,20,18,()->adjust(parameter,-1),false);
                        button(Component.literal("+"),sideW-16,y,20,18,()->adjust(parameter,1),false);
                        var input=addRenderableWidget(new EditBox(font,40,y,sideW-60,18,Component.literal(parameter)));input.setMaxLength(10);input.setValue(Long.toString(number.value()));
                        var bounds=RUNES.get(selectedNode.rune()).parameters().get(parameter);
                        input.setResponder(value->{try {long numeric=Long.parseLong(value);boolean valid=numeric>=bounds.min()&&numeric<=bounds.max();input.setTextColor(valid?TEXT:0xffe68c8c);if(valid)changeParameter(parameter,new Value.Number(numeric));}catch(NumberFormatException error){input.setTextColor(0xffe68c8c);}});
                    } else if(entry.getValue() instanceof Value.Bool bool) {
                        button(tr(bool.value()?"gui.yes":"gui.no"),16,y,sideW-12,18,()->adjust(parameter,1),false);
                    }
                    y+=36;
                }
                if(y+20<=height-30)button("gui.badspell.output",16,y,sideW-12,()->change(copy(formula.nodes(),formula.edges(),selectedNode.id())));
            }
        } else if(!diagnostics.isEmpty()) {
            button("gui.badspell.find_error",16,height-55,sideW-12,()->{selected=diagnostics.getFirst().node();centerSelected();});
        }
        int tx=canvasX+8;
        button("gui.badspell.layout",tx,TOP+4,60,this::layout);
        button("gui.badspell.fit",tx+64,TOP+4,60,this::fit);
        if(canvasW>205)button("gui.badspell.delete",tx+128,TOP+4,62,this::delete).active=selected!=null;
        if(canvasW>280)button("gui.badspell.output",tx+194,TOP+4,70,()->{if(selected!=null)change(copy(formula.nodes(),formula.edges(),selected));});
        if(!fitted){fitted=true;panX=14;panY=18;}
    }
    private final List<SpellButton> paletteButtons=new ArrayList<>();
    private List<String> palette(){return RUNES.ids().stream().filter(id->runeName(id).toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))||id.contains(search.toLowerCase(Locale.ROOT))).sorted(Comparator.comparing(FormulaBookScreen::runeName)).toList();}
    private void refreshPalette() {
        for(var b:paletteButtons)removeWidget(b);paletteButtons.clear();
        if(sideTab!=0)return;
        var runes=palette();int visible=Math.max(1,(height-TOP-98)/23);paletteOffset=Math.clamp(paletteOffset,0,Math.max(0,runes.size()-visible));
        for(int row=0;row<visible&&row+paletteOffset<runes.size();row++) {
            String id=runes.get(row+paletteOffset);var rune=RUNES.get(id);
            var b=button(Component.literal("+ "+runeName(id)),16,TOP+54+row*23,sideW-12,20,()->add(id),false);
            b.setTooltip(Tooltip.create(tr("gui.badspell.rune_type",tr("type.badspell."+rune.output().name().toLowerCase(Locale.ROOT)))));paletteButtons.add(b);
        }
    }
    private void send(int action) {
        if((action==1||action==3)&&dirty()){status=tr("gui.badspell.history_save_first").getString();return;}
        try {ClientPacketDistributor.sendToServer(new Packets.BookAction(hand,action,FormulaCodec.encode(formula)));status=tr("gui.badspell.waiting").getString();}
        catch(IOException error){status=error.getMessage();}
    }
    private Formula copy(List<Formula.Node> nodes,List<Formula.Edge> edges,String output){return new Formula(formula.id(),formula.owner(),formula.revision(),formula.title(),nodes,edges,output,formula.energyBudget());}
    private Formula rebase(Formula graph){return new Formula(formula.id(),formula.owner(),formula.revision(),graph.title(),graph.nodes(),graph.edges(),graph.output(),graph.energyBudget());}
    private void remember(){undo.addLast(formula);while(undo.size()>100)undo.removeFirst();redo.clear();}
    private void change(Formula next){remember();formula=next;validate();rebuildWidgets();}
    private void changeMetadata(String title,long budget){remember();formula=new Formula(formula.id(),formula.owner(),formula.revision(),title,formula.nodes(),formula.edges(),formula.output(),budget);validate();updateActions();}
    private void updateActions(){if(saveButton!=null)saveButton.active=diagnostics.isEmpty();if(exportButton!=null)exportButton.active=diagnostics.isEmpty()&&!dirty();}
    private void changeParameter(String key,Value value){var n=node(selected);if(n==null||value.equals(n.parameters().get(key)))return;remember();var p=new HashMap<>(n.parameters());p.put(key,value);var nodes=new ArrayList<>(formula.nodes());nodes.set(nodes.indexOf(n),new Formula.Node(n.id(),n.rune(),n.x(),n.y(),p));formula=copy(nodes,formula.edges(),formula.output());validate();updateActions();}
    private void history(Deque<Formula> source,Deque<Formula> target){if(source.isEmpty())return;target.addLast(formula);formula=rebase(source.removeLast());selected=null;validate();rebuildWidgets();}
    private Formula.Node node(String id){return id==null?null:formula.nodes().stream().filter(n->n.id().equals(id)).findFirst().orElse(null);}
    private void add(String rune) {
        if(formula.nodes().size()>=128)return;
        String id;do{id="r"+nextId++;}while(node(id)!=null);
        Map<String,Value> parameters=new HashMap<>();
        RUNES.get(rune).parameters().forEach((name,type)->parameters.put(name,switch(type.type()) {
            case NUMBER->new Value.Number(Math.max(type.min(),Math.min(type.max(),name.equals("duration_ticks")?600:3)));
            case BOOLEAN->new Value.Bool(true);case POSITION->new Value.Position("minecraft:overworld",0,64,0);
            default->throw new IllegalStateException("Unsupported parameter type");
        }));
        var nodes=new ArrayList<>(formula.nodes());nodes.add(new Formula.Node(id,rune,(int)((24-panX)/zoom),(int)((24-panY)/zoom),parameters));selected=id;change(copy(nodes,formula.edges(),formula.output()));
    }
    private void layout() {
        if(diagnostics.stream().anyMatch(d->d.code()==FormulaValidator.Code.CYCLE))return;
        Map<String,Integer> columns=new HashMap<>(),rows=new HashMap<>();formula.nodes().forEach(n->columns.put(n.id(),0));
        for(int i=0;i<formula.nodes().size();i++)for(var e:formula.edges())if(columns.containsKey(e.from())&&columns.containsKey(e.to()))columns.put(e.to(),Math.max(columns.get(e.to()),columns.get(e.from())+1));
        var nodes=new ArrayList<Formula.Node>();
        for(var n:formula.nodes().stream().sorted(Comparator.comparing(Formula.Node::id)).toList()) {
            int column=columns.get(n.id()),row=rows.merge(Integer.toString(column),1,Integer::sum)-1;
            // Fold the long reference chain into two columns, with space for labeled input ports.
            nodes.add(new Formula.Node(n.id(),n.rune(),(column%2)*154,(column/2)*100+row*90,n.parameters()));
        }
        change(copy(nodes,formula.edges(),formula.output()));fit();
    }
    private void fit() {
        if(formula.nodes().isEmpty())return;
        int minX=formula.nodes().stream().mapToInt(Formula.Node::x).min().orElse(0),minY=formula.nodes().stream().mapToInt(Formula.Node::y).min().orElse(0);
        int maxX=formula.nodes().stream().mapToInt(n->n.x()+NODE_W).max().orElse(NODE_W),maxY=formula.nodes().stream().mapToInt(n->n.y()+nodeHeight(n)).max().orElse(60);
        zoom=Math.clamp(Math.min((canvasW-24.0)/(maxX-minX),(canvasH-24.0)/(maxY-minY)),.25,1.3);
        panX=(canvasW-(maxX-minX)*zoom)/2-minX*zoom;panY=(canvasH-(maxY-minY)*zoom)/2-minY*zoom;
    }
    private void centerSelected(){var n=node(selected);if(n!=null){panX=canvasW/2.0-(n.x()+NODE_W/2.0)*zoom;panY=canvasH/2.0-(n.y()+nodeHeight(n)/2.0)*zoom;}}
    private void delete(){if(selected==null)return;String id=selected;selected=null;change(copy(formula.nodes().stream().filter(n->!n.id().equals(id)).toList(),formula.edges().stream().filter(e->!e.from().equals(id)&&!e.to().equals(id)).toList(),formula.output()));}
    private void adjust(String parameter,long delta){var n=node(selected);if(n==null)return;var p=new HashMap<>(n.parameters());var old=p.get(parameter);var bounds=RUNES.get(n.rune()).parameters().get(parameter);
        if(old instanceof Value.Number v)p.put(parameter,new Value.Number(Math.clamp(v.value()+delta,bounds.min(),bounds.max())));if(old instanceof Value.Bool v)p.put(parameter,new Value.Bool(!v.value()));
        var nodes=new ArrayList<>(formula.nodes());nodes.set(nodes.indexOf(n),new Formula.Node(n.id(),n.rune(),n.x(),n.y(),p));change(copy(nodes,formula.edges(),formula.output()));}
    private void validate(){diagnostics=new FormulaValidator(RUNES).validate(formula);preview=null;if(diagnostics.isEmpty())try{preview=new FormulaEngine(RUNES).plan(formula,new RuneRegistry.Context(new Value.Position("minecraft:overworld",0,64,0),0));}catch(IllegalArgumentException ignored){}}
    private int nodeHeight(Formula.Node n){return 34+RUNES.get(n.rune()).inputs().size()*16;}
    private List<String> ports(Formula.Node n){return new TreeSet<>(RUNES.get(n.rune()).inputs().keySet()).stream().toList();}
    private int typeColor(Value.Type type){return switch(type){case EVENT->0xffe8bd73;case POSITION,REGION->0xff77bde8;case BOOLEAN->0xffd8a5eb;case NUMBER->0xff9ad484;case EFFECT->ACCENT;};}
    private void text(GuiGraphicsExtractor g,Component value,int x,int y,int max,int color){g.text(font,font.plainSubstrByWidth(value.getString(),Math.max(0,max)),x,y,color,false);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
        g.fill(0,0,width,height,BG);g.fill(0,0,width,25,PANEL);g.fill(0,24,width,25,LINE);
        g.text(font,"BAD SPELL",12,8,ACCENT,false);text(g,Component.literal(formula.title()),88,8,width-(width<365?250:190),TEXT);
        text(g,tr("gui.badspell.ink",ink),width-92,8,82,0xffe4c68c);
        g.fill(10,TOP,sideW+10,height-28,PANEL);g.fill(canvasX,TOP,width-10,height-28,PANEL);
        g.fill(canvasX,canvasY,width-10,height-28,0xff0d1923);g.enableScissor(canvasX,canvasY,canvasX+canvasW,canvasY+canvasH);
        int spacing=Math.max(12,(int)(24*zoom));for(int x=canvasX+(int)(panX%spacing);x<canvasX+canvasW;x+=spacing)for(int y=canvasY+(int)(panY%spacing);y<canvasY+canvasH;y+=spacing)g.fill(x,y,x+1,y+1,0xff233441);
        g.pose().pushMatrix();g.pose().translate((float)(canvasX+panX),(float)(canvasY+panY));g.pose().scale((float)zoom,(float)zoom);
        for(var e:formula.edges()) {var from=node(e.from());var to=node(e.to());if(from==null||to==null)continue;int index=ports(to).indexOf(e.port());if(index<0)continue;
            int x1=from.x()+NODE_W,y1=from.y()+14,x2=to.x(),y2=to.y()+39+index*16,mid=(x1+x2)/2,color=typeColor(RUNES.get(from.rune()).output());
            line(g,x1,y1,mid,y1,color);line(g,mid,y1,mid,y2,color);line(g,mid,y2,x2,y2,color);
        }
        for(var n:formula.nodes()) {
            boolean error=diagnostics.stream().anyMatch(d->d.node().equals(n.id()));int border=error?0xffe68c8c:n.id().equals(selected)?ACCENT:LINE;
            g.fill(n.x()-1,n.y()-1,n.x()+NODE_W+1,n.y()+nodeHeight(n)+1,border);g.fill(n.x(),n.y(),n.x()+NODE_W,n.y()+nodeHeight(n),0xff172b37);
            g.fill(n.x(),n.y(),n.x()+NODE_W,n.y()+28,0xff213c49);g.text(font,font.plainSubstrByWidth(runeName(n.rune()),NODE_W-18),n.x()+7,n.y()+6,TEXT,false);
            g.text(font,n.id(),n.x()+7,n.y()+18,MUTED,false);if(n.id().equals(formula.output()))g.text(font,"*",n.x()+NODE_W-16,n.y()+6,ACCENT,false);
            int color=typeColor(RUNES.get(n.rune()).output());g.fill(n.x()+NODE_W-3,n.y()+10,n.x()+NODE_W+4,n.y()+18,n.id().equals(connecting)?0xffffffff:color);
            int y=n.y()+35;for(String port:ports(n)){g.fill(n.x()-4,y,n.x()+3,y+8,typeColor(RUNES.get(n.rune()).inputs().get(port)));g.text(font,port,n.x()+8,y,MUTED,false);y+=16;}
        }
        g.pose().popMatrix();g.disableScissor();
        if(sideTab==0)text(g,tr("gui.badspell.rune_count",RUNES.ids().size()),16,height-42,sideW-12,MUTED);
        if(sideTab==1) {
            var n=node(selected);
            if(n==null){text(g,tr("gui.badspell.name"),16,TOP+29,sideW-12,MUTED);text(g,tr("gui.badspell.budget_label"),16,TOP+67,sideW-12,MUTED);}
            else {text(g,Component.literal(runeName(n.rune())),16,TOP+30,sideW-12,ACCENT);text(g,Component.literal(n.id()),16,TOP+43,sideW-12,MUTED);int y=TOP+64;
                for(var entry:new TreeMap<>(n.parameters()).entrySet()){if(y+18>height-50)break;text(g,tr("parameter.badspell."+entry.getKey()),16,y-11,sideW-12,MUTED);y+=36;}}
        }
        if(sideTab==2) {
            int y=TOP+32;boolean valid=diagnostics.isEmpty();text(g,tr(valid?"gui.badspell.valid":"gui.badspell.errors",diagnostics.size()),16,y,sideW-12,valid?ACCENT:0xffe68c8c);y+=20;
            if(preview!=null){text(g,tr("gui.badspell.cost",preview.fieldCost(),preview.focusCost()),16,y,sideW-12,TEXT);y+=18;text(g,tr("gui.badspell.duration",preview.durationTicks()/20.0),16,y,sideW-12,MUTED);y+=18;
                boolean supported=preview.id().equals("badspell:light")&&preview.region().radius()==0;text(g,tr(supported?"gui.badspell.point_ready":"gui.badspell.effect_unavailable"),16,y,sideW-12,supported?ACCENT:0xffe8bd73);}
            for(var d:diagnostics){if(y>height-72)break;text(g,tr("diagnostic.badspell."+d.code().name().toLowerCase(Locale.ROOT)),16,y,sideW-12,0xffe68c8c);y+=12;text(g,Component.literal(d.node()+" · "+d.port()),16,y,sideW-12,MUTED);y+=20;}
        }
        String footer=!status.isBlank()?status:diagnostics.isEmpty()?tr("gui.badspell.canvas_status",formula.nodes().size(),formula.revision(),(int)(zoom*100)).getString():tr("gui.badspell.errors",diagnostics.size()).getString();
        text(g,Component.literal((dirty()?"● ":"")+footer),12,height-21,width-24,diagnostics.isEmpty()?MUTED:0xffe68c8c);
        super.extractRenderState(g,mx,my,partial);
    }
    private static void line(GuiGraphicsExtractor g,int x1,int y1,int x2,int y2,int color){g.fill(Math.min(x1,x2),Math.min(y1,y2),Math.max(x1,x2)+1,Math.max(y1,y2)+1,color);}
    private boolean onCanvas(double x,double y){return x>=canvasX&&x<=canvasX+canvasW&&y>=canvasY&&y<=canvasY+canvasH;}
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean twice) {
        if(super.mouseClicked(e,twice))return true;if(!onCanvas(e.x(),e.y()))return false;
        if(e.button()==2){dragging="@pan";return true;}
        double x=(e.x()-canvasX-panX)/zoom,y=(e.y()-canvasY-panY)/zoom;
        for(var n:formula.nodes().reversed())if(x>=n.x()-5&&x<=n.x()+NODE_W+5&&y>=n.y()&&y<=n.y()+nodeHeight(n)) {
            selected=n.id();
            if(e.button()==1){int index=(int)((y-n.y()-33)/16);if(x<n.x()+8&&y>=n.y()+33&&index>=0&&index<ports(n).size()){String port=ports(n).get(index);var edges=new ArrayList<>(formula.edges());edges.removeIf(edge->edge.to().equals(n.id())&&edge.port().equals(port));change(copy(formula.nodes(),edges,formula.output()));}else {connecting=null;sideTab=1;rebuildWidgets();}return true;}
            if(x>=n.x()+NODE_W-7&&y<=n.y()+24){connecting=n.id();rebuildWidgets();return true;}
            int portIndex=(int)((y-n.y()-33)/16);
            if(x<n.x()+8&&y>=n.y()+33&&portIndex>=0&&portIndex<ports(n).size()) {
                String port=ports(n).get(portIndex);
                if(connecting!=null){var from=node(connecting);if(from!=null&&RUNES.get(from.rune()).output()==RUNES.get(n.rune()).inputs().get(port)){
                    var edges=new ArrayList<>(formula.edges());edges.removeIf(edge->edge.to().equals(n.id())&&edge.port().equals(port));edges.add(new Formula.Edge(connecting,n.id(),port));connecting=null;change(copy(formula.nodes(),edges,formula.output()));
                }else status=tr("gui.badspell.type_error").getString();return true;}
            }
            sideTab=1;dragging=n.id();beforeDrag=formula;rebuildWidgets();return true;
        }
        selected=null;connecting=null;rebuildWidgets();return true;
    }
    @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){if("@pan".equals(dragging)){panX+=dx;panY+=dy;return true;}var n=node(dragging);if(n!=null){var nodes=new ArrayList<>(formula.nodes());nodes.set(nodes.indexOf(n),new Formula.Node(n.id(),n.rune(),n.x()+(int)Math.round(dx/zoom),n.y()+(int)Math.round(dy/zoom),n.parameters()));formula=copy(nodes,formula.edges(),formula.output());return true;}return super.mouseDragged(e,dx,dy);}
    @Override public boolean mouseReleased(MouseButtonEvent e){if(beforeDrag!=null&&!beforeDrag.equals(formula)){undo.addLast(beforeDrag);while(undo.size()>100)undo.removeFirst();redo.clear();validate();updateActions();}beforeDrag=null;dragging=null;return super.mouseReleased(e);}
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy){if(x<sideW+10&&y>TOP&&sideTab==0){paletteOffset-=(int)Math.signum(sy);refreshPalette();return true;}if(onCanvas(x,y)){double old=zoom;zoom=Math.clamp(zoom*Math.pow(1.15,sy),.25,4);panX=x-canvasX-(x-canvasX-panX)*zoom/old;panY=y-canvasY-(y-canvasY-panY)*zoom/old;return true;}return super.mouseScrolled(x,y,sx,sy);}
    @Override public boolean keyPressed(KeyEvent e){if(e.hasControlDown()&&e.key()==83){send(0);return true;}if(super.keyPressed(e))return true;if(e.hasControlDown()&&e.key()==90){history(undo,redo);return true;}if(e.hasControlDown()&&e.key()==89){history(redo,undo);return true;}if(e.key()==261){delete();return true;}return false;}
    @Override public void onClose(){if(dirty())minecraft.gui.setScreen(new ConfirmScreen(yes->{if(yes)minecraft.gui.setScreen(null);else minecraft.gui.setScreen(this);},tr("gui.badspell.unsaved"),tr("gui.badspell.discard_question")));else super.onClose();}
    @Override public boolean isPauseScreen(){return false;}
}
