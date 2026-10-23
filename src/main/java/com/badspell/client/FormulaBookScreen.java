package com.badspell.client;

import com.badspell.formula.*;
import com.badspell.network.Packets;
import java.io.IOException;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.*;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/** First in-game graph editor: real nodes, typed ports, editable values, zoom/pan and bounded undo.
 * Book library, named runtime parameters, accessibility remain pending.
 */
public final class FormulaBookScreen extends Screen {
    private static final RuneRegistry RUNES=RuneRegistry.standard();
    private final int hand,ink;
    private Formula formula;
    private final String savedChecksum;
    private final Deque<Formula> undo=new ArrayDeque<>(),redo=new ArrayDeque<>();
    private double zoom=.8,panX=20,panY=30;
    private String selected,connecting,dragging;
    private Formula beforeDrag;
    private String status="";
    private int nextId=1,paletteOffset;
    private boolean fitted;
    public FormulaBookScreen(int hand,Formula formula,int ink) {
        super(Component.translatable("screen.badspell.formula_book"));this.hand=hand;this.formula=formula;this.ink=ink;this.savedChecksum=new FormulaSnapshot(formula).checksum();
    }
    public void restoreDraft(Formula archived) {
        // Keep the live identity/revision. Saving creates a new revision through normal server checks and ink costs.
        selected=null;fitted=false;
        change(new Formula(formula.id(),formula.owner(),formula.revision(),archived.title(),archived.nodes(),archived.edges(),archived.output(),archived.energyBudget()));
    }
    private void button(String key,int x,int y,int w,Runnable action) {
        addRenderableWidget(Button.builder(Component.translatable(key),b->action.run()).bounds(x,y,w,18).build());
    }
    @Override protected void init() {
        if(!fitted && !formula.nodes().isEmpty()) {
            int min=formula.nodes().stream().mapToInt(Formula.Node::x).min().orElse(0);
            int max=formula.nodes().stream().mapToInt(Formula.Node::x).max().orElse(0)+106;
            zoom=Math.clamp((width-264.0)/Math.max(106,max-min),.25,1.0);
            panX=10-min*zoom;panY=30;fitted=true;
        }
        button("gui.badspell.save",4,4,65,()->send(0));
        button("gui.badspell.export",72,4,75,()->send(1));
        button("gui.badspell.refill",150,4,70,()->send(2));
        button("gui.badspell.check",223,4,65,this::validate);
        button("gui.badspell.undo",4,25,65,()->history(undo,redo));
        button("gui.badspell.redo",72,25,65,()->history(redo,undo));
        button("gui.badspell.delete",140,25,75,this::delete);
        button("gui.badspell.output",218,25,70,()->{if(selected!=null)change(copy(formula.nodes(),formula.edges(),selected));});
        button("gui.badspell.layout",291,25,65,this::layout);
        button("gui.badspell.history",291,4,65,()->send(3));
        int y=65;
        var palette=new TreeSet<>(RUNES.ids()).stream().toList();
        int visible=Math.max(1,(height-105)/17);
        for(String id:palette.subList(Math.min(paletteOffset,palette.size()-1),Math.min(palette.size(),paletteOffset+visible))) {
            String label=id.substring(id.indexOf(':')+1);
            addRenderableWidget(Button.builder(Component.literal("+ "+label),b->add(id)).bounds(4,y,110,16).build());y+=17;
        }
        var node=node(selected);
        if(node!=null) {
            y=86;
            for(var entry:new TreeMap<>(node.parameters()).entrySet()) {
                final String parameter=entry.getKey();
                if(entry.getValue() instanceof Value.Number || entry.getValue() instanceof Value.Bool) {
                    button("gui.badspell.minus",width-112,y,24,()->adjust(parameter,-1));
                    button("gui.badspell.plus",width-28,y,24,()->adjust(parameter,1));
                }
                y+=38;
            }
        }
    }
    private void send(int action) {
        if(action==3 && !savedChecksum.equals(new FormulaSnapshot(formula).checksum())) {
            status=Component.translatable("gui.badspell.history_save_first").getString();return;
        }
        try { ClientPacketDistributor.sendToServer(new Packets.BookAction(hand,action,FormulaCodec.encode(formula)));status=Component.translatable("gui.badspell.waiting").getString(); }
        catch(IOException error) { status=error.getMessage(); }
    }
    private Formula copy(List<Formula.Node> nodes,List<Formula.Edge> edges,String output) {
        return new Formula(formula.id(),formula.owner(),formula.revision(),formula.title(),nodes,edges,output,formula.energyBudget());
    }
    private void change(Formula next) {
        undo.addLast(formula);while(undo.size()>100)undo.removeFirst();redo.clear();formula=next;validate();rebuildWidgets();
    }
    private void history(Deque<Formula> source,Deque<Formula> target) {
        if(source.isEmpty())return;target.addLast(formula);formula=source.removeLast();selected=null;validate();rebuildWidgets();
    }
    private Formula.Node node(String id) { return id==null?null:formula.nodes().stream().filter(n->n.id().equals(id)).findFirst().orElse(null); }
    private void add(String rune) {
        if(formula.nodes().size()>=128)return;
        String id;do{id="r"+nextId++;}while(node(id)!=null);
        Map<String,Value> parameters=new HashMap<>();
        RUNES.get(rune).parameters().forEach((name,type)->{
            Value value=switch(type.type()) {
                case NUMBER->new Value.Number(Math.max(type.min(),Math.min(type.max(),name.equals("duration_ticks")?600:3)));
                case BOOLEAN->new Value.Bool(true);
                case POSITION->new Value.Position("minecraft:overworld",0,64,0);
                default->throw new IllegalStateException("Unsupported editor parameter type");
            };parameters.put(name,value);
        });
        var nodes=new ArrayList<>(formula.nodes());nodes.add(new Formula.Node(id,rune,(int)((140-panX)/zoom),(int)((90-panY)/zoom),parameters));selected=id;
        change(copy(nodes,formula.edges(),formula.output()));
    }
    private void layout() {
        if(new FormulaValidator(RUNES).validate(formula).stream().anyMatch(d->d.code()==FormulaValidator.Code.CYCLE)) { validate();return; }
        Map<String,Integer> column=new HashMap<>(),row=new HashMap<>();
        formula.nodes().forEach(n->column.put(n.id(),0));
        for(int step=0;step<formula.nodes().size();step++) {
            boolean changed=false;
            for(var edge:formula.edges())if(column.containsKey(edge.from())&&column.containsKey(edge.to())) {
                int next=column.get(edge.from())+1;
                if(next>column.get(edge.to())){column.put(edge.to(),next);changed=true;}
            }
            if(!changed)break;
        }
        var nodes=new ArrayList<Formula.Node>();
        for(var n:formula.nodes().stream().sorted(Comparator.comparing(Formula.Node::id)).toList()) {
            int col=column.get(n.id()),line=row.getOrDefault(Integer.toString(col),0);row.put(Integer.toString(col),line+1);
            nodes.add(new Formula.Node(n.id(),n.rune(),col*150,line*110,n.parameters()));
        }
        fitted=false;change(copy(nodes,formula.edges(),formula.output()));
    }
    private void delete() {
        if(selected==null)return;
        String deleted=selected;var nodes=formula.nodes().stream().filter(n->!n.id().equals(deleted)).toList();
        var edges=formula.edges().stream().filter(e->!e.from().equals(deleted)&&!e.to().equals(deleted)).toList();selected=null;
        change(copy(nodes,edges,formula.output()));
    }
    private void adjust(String parameter,long delta) {
        var n=node(selected);if(n==null)return;var params=new HashMap<>(n.parameters());Value old=params.get(parameter);
        var bounds=RUNES.get(n.rune()).parameters().get(parameter);
        if(old instanceof Value.Number number)params.put(parameter,new Value.Number(Math.clamp(number.value()+delta,bounds.min(),bounds.max())));
        if(old instanceof Value.Bool bool)params.put(parameter,new Value.Bool(!bool.value()));
        var nodes=new ArrayList<>(formula.nodes());nodes.set(nodes.indexOf(n),new Formula.Node(n.id(),n.rune(),n.x(),n.y(),params));change(copy(nodes,formula.edges(),formula.output()));
    }
    private void validate() {
        var diagnostics=new FormulaValidator(RUNES).validate(formula);
        status=diagnostics.isEmpty()?Component.translatable("gui.badspell.valid").getString():diagnostics.getFirst().toString();
    }
    private int nodeHeight(Formula.Node n) { return 28+RUNES.get(n.rune()).inputs().size()*14; }
    private List<String> ports(Formula.Node n) { return new TreeSet<>(RUNES.get(n.rune()).inputs().keySet()).stream().toList(); }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float partial) {
        g.fill(0,0,width,height,0xff101922);g.fill(116,60,width-118,height-36,0xff172833);
        g.text(font,Component.translatable("gui.badspell.ink",ink),width-116,7,0xffe9d49b);
        g.text(font,(int)(zoom*100)+"%",width-60,29,0xffb0d8d8);
        g.enableScissor(117,61,width-118,height-36);
        g.pose().pushMatrix();g.pose().translate((float)(116+panX),(float)(60+panY));g.pose().scale((float)zoom,(float)zoom);
        for(var e:formula.edges()) {
            var from=node(e.from());var to=node(e.to());if(from==null||to==null)continue;
            int index=ports(to).indexOf(e.port());if(index<0)continue;
            int x1=from.x()+105,y1=from.y()+10,x2=to.x(),y2=to.y()+29+index*14,mid=(x1+x2)/2;
            line(g,x1,y1,mid,y1,0xff7bd3c5);line(g,mid,y1,mid,y2,0xff7bd3c5);line(g,mid,y2,x2,y2,0xff7bd3c5);
        }
        for(var n:formula.nodes()) {
            g.fill(n.x(),n.y(),n.x()+106,n.y()+nodeHeight(n),n.id().equals(selected)?0xff516a7c:0xff2e4556);
            g.text(font,n.rune().substring(n.rune().indexOf(':')+1),n.x()+4,n.y()+3,0xffffffff);
            g.fill(n.x()+100,n.y()+6,n.x()+108,n.y()+14,n.id().equals(connecting)?0xffffd15c:0xffa6e3a1);
            int y=n.y()+24;
            for(String port:ports(n)) {
                g.fill(n.x()-3,y,n.x()+3,y+8,0xffffd15c);g.text(font,port,n.x()+6,y,0xffeff8f0);y+=14;
            }
            if(n.id().equals(formula.output()))g.text(font,"*",n.x()+90,n.y()+3,0xffffd15c);
        }
        g.pose().popMatrix();g.disableScissor();
        var n=node(selected);
        if(n!=null) {
            g.text(font,n.id(),width-113,65,0xffffffff);int y=80;
            for(var entry:new TreeMap<>(n.parameters()).entrySet()) {
                g.text(font,entry.getKey(),width-114,y,0xffa2d7d2);
                String value=entry.getValue() instanceof Value.Number num?Long.toString(num.value()):entry.getValue().toString();
                g.text(font,value,width-82,y+12,0xffffffff);y+=38;
            }
        }
        g.text(font,status.length()>100?status.substring(0,100):status,4,height-30,0xfff6d68d);
        g.text(font,Component.translatable("gui.badspell.help"),4,height-16,0xffa9c2ca);
        super.extractRenderState(g,mouseX,mouseY,partial);
    }
    private static void line(GuiGraphicsExtractor g,int x1,int y1,int x2,int y2,int color) { g.fill(Math.min(x1,x2),Math.min(y1,y2),Math.max(x1,x2)+2,Math.max(y1,y2)+2,color); }
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean twice) {
        if(super.mouseClicked(event,twice))return true;
        if(event.x()<117||event.x()>width-118||event.y()<60||event.y()>height-36)return false;
        if(event.button()==2){dragging="@pan";return true;}
        double x=(event.x()-116-panX)/zoom,y=(event.y()-60-panY)/zoom;
        for(var n:formula.nodes().reversed()) {
            if(x>=n.x()-4&&x<=n.x()+109&&y>=n.y()&&y<=n.y()+nodeHeight(n)) {
                selected=n.id();
                if(event.button()==1) { connecting=null;rebuildWidgets();return true; }
                if(x>=n.x()+98&&y<=n.y()+16){connecting=n.id();rebuildWidgets();return true;}
                int portIndex=(int)((y-n.y()-22)/14);
                if(x<n.x()+8&&y>=n.y()+22&&portIndex>=0&&portIndex<ports(n).size()&&connecting!=null) {
                    String port=ports(n).get(portIndex);var from=node(connecting);
                    if(from!=null&&RUNES.get(from.rune()).output()==RUNES.get(n.rune()).inputs().get(port)) {
                        var edges=new ArrayList<>(formula.edges());edges.removeIf(e->e.to().equals(n.id())&&e.port().equals(port));edges.add(new Formula.Edge(from.id(),n.id(),port));
                        connecting=null;change(copy(formula.nodes(),edges,formula.output()));
                    } else { status=Component.translatable("gui.badspell.type_error").getString();rebuildWidgets(); }
                    return true;
                }
                dragging=n.id();beforeDrag=formula;rebuildWidgets();return true;
            }
        }
        selected=null;connecting=null;rebuildWidgets();return true;
    }
    @Override public boolean mouseDragged(MouseButtonEvent event,double dx,double dy) {
        if("@pan".equals(dragging)){panX+=dx;panY+=dy;return true;}
        var n=node(dragging);if(n!=null) {
            var nodes=new ArrayList<>(formula.nodes());nodes.set(nodes.indexOf(n),new Formula.Node(n.id(),n.rune(),n.x()+(int)Math.round(dx/zoom),n.y()+(int)Math.round(dy/zoom),n.parameters()));
            formula=copy(nodes,formula.edges(),formula.output());return true;
        }
        return super.mouseDragged(event,dx,dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent event) {
        if(beforeDrag!=null&&!beforeDrag.equals(formula)){undo.addLast(beforeDrag);while(undo.size()>100)undo.removeFirst();redo.clear();}
        beforeDrag=null;dragging=null;return super.mouseReleased(event);
    }
    @Override public boolean mouseScrolled(double x,double y,double sx,double sy) {
        if(x<115&&y>60) {
            int visible=Math.max(1,(height-105)/17);
            paletteOffset=Math.clamp(paletteOffset-(int)Math.signum(sy),0,Math.max(0,RUNES.ids().size()-visible));
            rebuildWidgets();return true;
        }
        if(x>116&&x<width-118&&y>60&&y<height-36) {
            double old=zoom;zoom=Math.clamp(zoom*Math.pow(1.15,sy),.25,4);
            panX=(x-116)-(x-116-panX)*zoom/old;panY=(y-60)-(y-60-panY)*zoom/old;return true;
        }
        return super.mouseScrolled(x,y,sx,sy);
    }
    @Override public boolean keyPressed(KeyEvent event) {
        if(event.key()==261){delete();return true;}
        if(event.hasControlDown()&&event.key()==90){history(undo,redo);return true;}
        if(event.hasControlDown()&&event.key()==89){history(redo,undo);return true;}
        return super.keyPressed(event);
    }
    @Override public boolean isPauseScreen() { return false; }
}
