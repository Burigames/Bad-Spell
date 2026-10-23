package com.badspell.formula;

import java.io.*;
import java.util.*;

/** Canonical, versioned, bounded format. No Java object deserialization. */
public final class FormulaCodec {
    public static final int SCHEMA_VERSION=1, MAX_BYTES=1_048_576;
    private FormulaCodec() {}
    public static byte[] encode(Formula f) throws IOException {
        if(f.nodes().size()>128 || f.edges().size()>512) throw new IOException("Graph limit");
        var bytes=new ByteArrayOutputStream();
        try(var out=new DataOutputStream(bytes)) {
            out.writeInt(0x4253504c); out.writeInt(SCHEMA_VERSION);
            uuid(out,f.id()); uuid(out,f.owner()); out.writeInt(f.revision()); out.writeUTF(f.title());
            out.writeUTF(f.output()); out.writeLong(f.energyBudget());
            var nodes=f.nodes().stream().sorted(Comparator.comparing(Formula.Node::id)).toList();
            out.writeInt(nodes.size());
            for(var n:nodes) {
                out.writeUTF(n.id()); out.writeUTF(n.rune()); out.writeInt(n.x()); out.writeInt(n.y());
                if(n.parameters().size()>32) throw new IOException("Parameter limit");
                out.writeInt(n.parameters().size());
                for(var e:new TreeMap<>(n.parameters()).entrySet()) { out.writeUTF(e.getKey()); value(out,e.getValue()); }
            }
            var edges=f.edges().stream().sorted(Comparator.comparing(Formula.Edge::to).thenComparing(Formula.Edge::port).thenComparing(Formula.Edge::from)).toList();
            out.writeInt(edges.size());
            for(var e:edges) { out.writeUTF(e.from()); out.writeUTF(e.to()); out.writeUTF(e.port()); }
        }
        if(bytes.size()>MAX_BYTES) throw new IOException("Formula payload limit");
        return bytes.toByteArray();
    }
    public static Formula decode(byte[] payload) throws IOException {
        if(payload.length>MAX_BYTES) throw new IOException("Formula payload limit");
        try(var in=new DataInputStream(new ByteArrayInputStream(payload))) {
            if(in.readInt()!=0x4253504c || in.readInt()!=SCHEMA_VERSION) throw new IOException("Unknown formula schema; quarantine instead of importing");
            UUID id=uuid(in),owner=uuid(in); int revision=in.readInt();
            String title=text(in,256),output=text(in,64); long budget=in.readLong();
            int count=count(in,128); List<Formula.Node> nodes=new ArrayList<>();
            for(int j=0;j<count;j++) {
                String node=text(in,64),rune=text(in,128); int x=in.readInt(),y=in.readInt();
                int params=count(in,32); Map<String,Value> p=new HashMap<>();
                for(int k=0;k<params;k++) {
                    String name=text(in,64); Value v=value(in);
                    if(p.putIfAbsent(name,v)!=null) throw new IOException("Duplicate parameter");
                }
                nodes.add(new Formula.Node(node,rune,x,y,p));
            }
            count=count(in,512); List<Formula.Edge> edges=new ArrayList<>();
            for(int j=0;j<count;j++) edges.add(new Formula.Edge(text(in,64),text(in,64),text(in,64)));
            if(in.read()!=-1) throw new IOException("Trailing formula data");
            return new Formula(id,owner,revision,title,nodes,edges,output,budget);
        } catch(IllegalArgumentException ex) { throw new IOException("Invalid formula data",ex); }
    }
    private static int count(DataInputStream in,int limit) throws IOException { int n=in.readInt(); if(n<0 || n>limit) throw new IOException("Collection limit"); return n; }
    private static String text(DataInputStream in,int limit) throws IOException { String s=in.readUTF(); if(s.length()>limit) throw new IOException("String limit"); return s; }
    private static void uuid(DataOutputStream out,UUID id) throws IOException { out.writeLong(id.getMostSignificantBits()); out.writeLong(id.getLeastSignificantBits()); }
    private static UUID uuid(DataInputStream in) throws IOException { return new UUID(in.readLong(),in.readLong()); }
    private static void value(DataOutputStream out,Value v) throws IOException {
        if(v instanceof Value.Number n) { out.writeByte(0); out.writeLong(n.value()); }
        else if(v instanceof Value.Bool b) { out.writeByte(1); out.writeBoolean(b.value()); }
        else if(v instanceof Value.Position p) { out.writeByte(2); out.writeUTF(p.dimension()); out.writeInt(p.x()); out.writeInt(p.y()); out.writeInt(p.z()); }
        else if(v instanceof Value.Vector vector){out.writeByte(3);out.writeInt(vector.x());out.writeInt(vector.y());out.writeInt(vector.z());}
        else if(v instanceof Value.Text text){out.writeByte(4);out.writeUTF(text.value());}
        else throw new IOException("Only literal parameters may be persisted");
    }
    private static Value value(DataInputStream in) throws IOException {
        return switch(in.readUnsignedByte()) {
            case 0 -> new Value.Number(in.readLong()); case 1 -> new Value.Bool(in.readBoolean());
            case 2 -> new Value.Position(text(in,256),in.readInt(),in.readInt(),in.readInt());
            case 3 -> new Value.Vector(in.readInt(),in.readInt(),in.readInt());
            case 4 -> new Value.Text(text(in,40000));
            default -> throw new IOException("Unknown value type");
        };
    }
}
