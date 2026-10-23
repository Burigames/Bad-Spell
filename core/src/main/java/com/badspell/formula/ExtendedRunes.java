package com.badspell.formula;

import java.util.*;
import static com.badspell.formula.Value.Type.*;

/** Pure rune operations. All world access is supplied as a bounded context snapshot. */
final class ExtendedRunes {
    private static RuneRegistry.Parameter n(long min,long max){return new RuneRegistry.Parameter(NUMBER,min,max);}
    private static long number(Map<String,Value> map,String key){return ((Value.Number)map.get(key)).value();}
    static void register(Map<String,RuneRegistry.Rune> r) {
        r.put("badspell:entity_position",new RuneRegistry.Rune(Map.of("target",ENTITY),Map.of(),POSITION,(i,p,c)->((Value.Entity)i.get("target")).position()));
        r.put("badspell:entity_health",new RuneRegistry.Rune(Map.of("target",ENTITY),Map.of(),NUMBER,(i,p,c)->{var e=(Value.Entity)i.get("target");var health=c.environment().get("health:"+e.id());if(health==null)throw new IllegalArgumentException("Entity health unavailable");return health;}));
        r.put("badspell:has_target",new RuneRegistry.Rune(Map.of(),Map.of(),BOOLEAN,(i,p,c)->new Value.Bool(c.entity()!=null)));
        r.put("badspell:offset",new RuneRegistry.Rune(Map.of("position",POSITION,"vector",VECTOR),Map.of(),POSITION,(i,p,c)->{var a=(Value.Position)i.get("position");var v=(Value.Vector)i.get("vector");return new Value.Position(a.dimension(),Math.addExact(a.x(),v.x()/1000),Math.addExact(a.y(),v.y()/1000),Math.addExact(a.z(),v.z()/1000));}));
        for(String operation:List.of("add","subtract","multiply"))r.put("badspell:"+operation,new RuneRegistry.Rune(Map.of("a",NUMBER,"b",NUMBER),Map.of(),NUMBER,(i,p,c)->new Value.Number(switch(operation){case "add"->Math.addExact(number(i,"a"),number(i,"b"));case "subtract"->Math.subtractExact(number(i,"a"),number(i,"b"));default->Math.multiplyExact(number(i,"a"),number(i,"b"));})));
        r.put("badspell:combine",new RuneRegistry.Rune(Map.of("first",EFFECT,"second",EFFECT),Map.of(),EFFECT,(i,p,c)->{
            var a=(Value.Effect)i.get("first");var b=(Value.Effect)i.get("second");var parts=new ArrayList<Value.Effect>();parts.addAll(a.children().isEmpty()?List.of(a):a.children());parts.addAll(b.children().isEmpty()?List.of(b):b.children());
            return new Value.Effect("badspell:combine",a.region(),1,Math.max(a.durationTicks(),b.durationTicks()),Math.addExact(a.fieldCost(),b.fieldCost()),Math.addExact(a.focusCost(),b.focusCost()),null,new Value.Vector(0,0,0),new Value.Event(c.start()),parts);
        }));
        r.put("badspell:vector",new RuneRegistry.Rune(Map.of(),Map.of("x",n(-1000,1000),"y",n(-1000,1000),"z",n(-1000,1000)),VECTOR,(i,p,c)->new Value.Vector((int)number(p,"x"),(int)number(p,"y"),(int)number(p,"z"))));
        r.put("badspell:direction",new RuneRegistry.Rune(Map.of(),Map.of(),VECTOR,(i,p,c)->c.look()));
        r.put("badspell:target_entity",new RuneRegistry.Rune(Map.of("trigger",EVENT),Map.of(),ENTITY,(i,p,c)->{if(c.entity()==null)throw new IllegalArgumentException("No living entity targeted");return c.entity();}));
        r.put("badspell:target_entity_id",new RuneRegistry.Rune(Map.of(),Map.of("id",new RuneRegistry.Parameter(TEXT,0,0)),ENTITY,(i,p,c)->{String id=java.util.UUID.fromString(((Value.Text)p.get("id")).value()).toString();var entity=c.environment().get("entity:"+id);if(!(entity instanceof Value.Entity))throw new IllegalArgumentException("Entity is unavailable in the local context");return entity;}));
        r.put("badspell:self",new RuneRegistry.Rune(Map.of(),Map.of(),ENTITY,(i,p,c)->{if(!(c.environment().get("self") instanceof Value.Entity self))throw new IllegalArgumentException("No actor");return self;}));
        for(Value.Type type:List.of(NUMBER,BOOLEAN,POSITION,VECTOR)) {
            String suffix=type.name().toLowerCase(Locale.ROOT);
            r.put("badspell:input_"+suffix,new RuneRegistry.Rune(Map.of(),Map.of("name",new RuneRegistry.Parameter(TEXT,0,0),"value",new RuneRegistry.Parameter(type,-1000000,1000000)),type,(i,p,c)->{
                String name=((Value.Text)p.get("name")).value();Value value=c.environment().getOrDefault("input:"+name,p.get("value"));if(value.type()!=type)throw new IllegalArgumentException("Module input type");return value;
            }));
        }
        for(Value.Type type:List.of(NUMBER,BOOLEAN,POSITION,VECTOR,ENTITY,TEXT,REGION,EVENT)) {
            String suffix=type.name().toLowerCase(Locale.ROOT);
            r.put("badspell:equal_"+suffix,new RuneRegistry.Rune(Map.of("a",type,"b",type),Map.of(),BOOLEAN,(i,p,c)->new Value.Bool(i.get("a").equals(i.get("b")))));
            r.put("badspell:if_"+suffix,new RuneRegistry.Rune(Map.of("condition",BOOLEAN,"yes",type,"no",type),Map.of(),type,(i,p,c)->((Value.Bool)i.get("condition")).value()?i.get("yes"):i.get("no")));
        }
        r.put("badspell:equal_entity",new RuneRegistry.Rune(Map.of("a",ENTITY,"b",ENTITY),Map.of(),BOOLEAN,(i,p,c)->new Value.Bool(((Value.Entity)i.get("a")).id().equals(((Value.Entity)i.get("b")).id()))));
        for(String sample:List.of("light","rain","sky","health","focus","field","stability","biome"))r.put("badspell:environment_"+sample,new RuneRegistry.Rune(Map.of(),Map.of(),NUMBER,(i,p,c)->c.environment().getOrDefault(sample,new Value.Number(0))));
        r.put("badspell:sphere",new RuneRegistry.Rune(Map.of("position",POSITION),Map.of("radius",n(1,6)),REGION,(i,p,c)->new Value.Region((Value.Position)i.get("position"),(int)number(p,"radius"),Value.Shape.SPHERE,List.of(),c.look(),0)));
        r.put("badspell:ray",new RuneRegistry.Rune(Map.of("position",POSITION,"direction",VECTOR),Map.of("distance",n(1,16)),REGION,(i,p,c)->new Value.Region((Value.Position)i.get("position"),0,Value.Shape.RAY,List.of(),(Value.Vector)i.get("direction"),(int)number(p,"distance"))));
        r.put("badspell:path_start",new RuneRegistry.Rune(Map.of("position",POSITION),Map.of(),REGION,(i,p,c)->{var start=(Value.Position)i.get("position");return new Value.Region(start,0,Value.Shape.PATH,List.of(start),c.look(),0);}));
        r.put("badspell:path_append",new RuneRegistry.Rune(Map.of("path",REGION,"position",POSITION),Map.of(),REGION,(i,p,c)->{var path=(Value.Region)i.get("path");var next=(Value.Position)i.get("position");if(path.shape()!=Value.Shape.PATH||path.points().size()>=16)throw new IllegalArgumentException("Expected path below 16 waypoints");var points=new ArrayList<>(path.points());points.add(next);return new Value.Region(path.center(),0,Value.Shape.PATH,points,path.direction(),0);}));
        r.put("badspell:target_block",new RuneRegistry.Rune(Map.of("trigger",EVENT),Map.of(),POSITION,(i,p,c)->c.environment().getOrDefault("measurement_position",c.target())));
        r.put("badspell:path",new RuneRegistry.Rune(Map.of("start",POSITION,"end",POSITION),Map.of("points",n(2,16)),REGION,(i,p,c)->{
            var start=(Value.Position)i.get("start");var end=(Value.Position)i.get("end");int count=(int)number(p,"points");var points=new ArrayList<Value.Position>();
            long dx=(long)end.x()-start.x(),dy=(long)end.y()-start.y(),dz=(long)end.z()-start.z();if(dx*dx+dy*dy+dz*dz>256)throw new IllegalArgumentException("Path exceeds 16 blocks");
            if(!start.dimension().equals(end.dimension()))throw new IllegalArgumentException("Path dimension");
            for(int index=0;index<count;index++)points.add(new Value.Position(start.dimension(),start.x()+(int)(dx*index/(count-1)),start.y()+(int)(dy*index/(count-1)),start.z()+(int)(dz*index/(count-1))));
            return new Value.Region(start,0,Value.Shape.PATH,points,c.look(),0);
        }));
        r.put("badspell:delay",new RuneRegistry.Rune(Map.of("trigger",EVENT),Map.of("ticks",n(1,1200)),EVENT,(i,p,c)->{var event=(Value.Event)i.get("trigger");return new Value.Event(Math.addExact(event.tick(),number(p,"ticks")),event.interval(),event.remaining(),event.cancelled());}));
        r.put("badspell:interval",new RuneRegistry.Rune(Map.of(),Map.of("ticks",n(20,1200)),EVENT,(i,p,c)->new Value.Event(c.start(),(int)number(p,"ticks"),-1,false)));
        r.put("badspell:limit",new RuneRegistry.Rune(Map.of("trigger",EVENT),Map.of("count",n(1,64)),EVENT,(i,p,c)->{var event=(Value.Event)i.get("trigger");return new Value.Event(event.tick(),event.interval(),event.remaining()<0?(int)number(p,"count"):Math.min(event.remaining(),(int)number(p,"count")),event.cancelled());}));
        r.put("badspell:abort",new RuneRegistry.Rune(Map.of("trigger",EVENT),Map.of(),EVENT,(i,p,c)->{var e=(Value.Event)i.get("trigger");return new Value.Event(e.tick(),e.interval(),0,true);}));
        r.put("badspell:none",new RuneRegistry.Rune(Map.of(),Map.of(),EFFECT,(i,p,c)->new Value.Effect("badspell:none",new Value.Region(c.target(),0),1,1,0,0)));
        r.put("badspell:check",new RuneRegistry.Rune(Map.of("effect",EFFECT),Map.of(),EFFECT,(i,p,c)->i.get("effect")));
        r.put("badspell:return",new RuneRegistry.Rune(Map.of("effect",EFFECT),Map.of(),EFFECT,(i,p,c)->i.get("effect")));
        for(String id:List.of("heat","cold","shield"))r.put("badspell:"+id,new RuneRegistry.Rune(Map.of("region",REGION),Map.of("strength",n(1,4),"duration_ticks",n(20,1200)),EFFECT,(i,p,c)->{
            var region=(Value.Region)i.get("region");int strength=(int)number(p,"strength"),ticks=(int)number(p,"duration_ticks");long fl=Math.max(1,(long)strength*ticks/20)*(1+region.radius());return new Value.Effect("badspell:"+id,region,strength,ticks,fl,id.equals("shield")?15:8);
        }));
        for(Value.Type type:Value.Type.values())r.put("badspell:return_"+type.name().toLowerCase(Locale.ROOT),new RuneRegistry.Rune(Map.of("value",type),Map.of(),type,(i,p,c)->i.get("value")));
        r.put("badspell:sign",new RuneRegistry.Rune(Map.of("position",POSITION),Map.of("frequency",n(1,4)),TEXT,(i,p,c)->new Value.Text(java.util.UUID.nameUUIDFromBytes(i.get("position").toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString()+":"+number(p,"frequency"))));
        r.put("badspell:sign_entity",new RuneRegistry.Rune(Map.of("target",ENTITY),Map.of("frequency",n(1,4)),TEXT,(i,p,c)->new Value.Text(((Value.Entity)i.get("target")).id()+":"+number(p,"frequency"))));
        r.put("badspell:measure",new RuneRegistry.Rune(Map.of("position",POSITION),Map.of(),NUMBER,(i,p,c)->{if(!i.get("position").equals(c.target())&&!i.get("position").equals(c.environment().get("measurement_position")))throw new IllegalArgumentException("Measurement target unavailable");return c.environment().getOrDefault("field",new Value.Number(0));}));
        for(String kind:List.of("bind","transfer","transfer_energy"))r.put("badspell:"+kind,new RuneRegistry.Rune(Map.of("source",POSITION,"target",POSITION),kind.equals("bind")?Map.of():Map.of("count",n(1,16)),EFFECT,(i,p,c)->{
            var a=(Value.Position)i.get("source");var b=(Value.Position)i.get("target");if(!a.dimension().equals(b.dimension()))throw new IllegalArgumentException("Binding dimensions");long x=(long)b.x()-a.x(),y=(long)b.y()-a.y(),z=(long)b.z()-a.z();if(x*x+y*y+z*z>256)throw new IllegalArgumentException("Binding range");int count=kind.equals("bind")?1:(int)number(p,"count");return new Value.Effect("badspell:"+kind,new Value.Region(a,0),count,kind.equals("bind")?1200:1,kind.equals("bind")?30:count*5L,8,null,new Value.Vector((int)x*1000,(int)y*1000,(int)z*1000),new Value.Event(c.start()));
        }));
        r.put("badspell:seal",new RuneRegistry.Rune(Map.of("position",POSITION),Map.of("locked",new RuneRegistry.Parameter(BOOLEAN,0,0),"duration_ticks",n(20,1200)),EFFECT,(i,p,c)->new Value.Effect("badspell:seal",new Value.Region((Value.Position)i.get("position"),0),((Value.Bool)p.get("locked")).value()?1:2,(int)number(p,"duration_ticks"),20,8)));
        r.put("badspell:heal",new RuneRegistry.Rune(Map.of("target",ENTITY),Map.of("points",n(1,8)),EFFECT,(i,p,c)->{var target=(Value.Entity)i.get("target");int points=(int)number(p,"points");return new Value.Effect("badspell:heal",new Value.Region(target.position(),0),points,points*50,points*15L,12,target,new Value.Vector(0,0,0),new Value.Event(c.start()));}));
        r.put("badspell:push",new RuneRegistry.Rune(Map.of("target",ENTITY,"vector",VECTOR),Map.of("strength",n(1,4)),EFFECT,(i,p,c)->{var target=(Value.Entity)i.get("target");int strength=(int)number(p,"strength");return new Value.Effect("badspell:push",new Value.Region(target.position(),0),strength,1,45L*strength,12,target,(Value.Vector)i.get("vector"),new Value.Event(c.start()));}));
        r.put("badspell:see",new RuneRegistry.Rune(Map.of("target",ENTITY),Map.of(),EFFECT,(i,p,c)->{var target=(Value.Entity)i.get("target");return new Value.Effect("badspell:see",new Value.Region(target.position(),0),1,100,10,3,target,new Value.Vector(0,0,0),new Value.Event(c.start()));}));
    }
    private ExtendedRunes(){}
}
