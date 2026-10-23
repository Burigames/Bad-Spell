package com.badspell.formula;

import java.util.*;

/** Closed, immutable data values. Vectors use thousandths of a block, never floating point inputs. */
public sealed interface Value {
    enum Type { NUMBER, BOOLEAN, POSITION, REGION, EVENT, EFFECT, VECTOR, ENTITY, TEXT }
    Type type();
    record Number(long value) implements Value { public Type type(){return Type.NUMBER;} }
    record Bool(boolean value) implements Value { public Type type(){return Type.BOOLEAN;} }
    record Text(String value) implements Value {
        public Text {Objects.requireNonNull(value);if(value.length()>40000)throw new IllegalArgumentException("text limit");}
        public Type type(){return Type.TEXT;}
    }
    record Vector(int x,int y,int z) implements Value {
        public Vector {if(Math.abs((long)x)>16000||Math.abs((long)y)>16000||Math.abs((long)z)>16000)throw new IllegalArgumentException("vector bounds");}
        public Type type(){return Type.VECTOR;}
    }
    record Position(String dimension,int x,int y,int z) implements Value {
        public Position {Objects.requireNonNull(dimension);if(dimension.isBlank())throw new IllegalArgumentException("dimension");}
        public Type type(){return Type.POSITION;}
    }
    record Entity(UUID id,Position position) implements Value {
        public Entity {Objects.requireNonNull(id);Objects.requireNonNull(position);}
        public Type type(){return Type.ENTITY;}
    }
    enum Shape { POINT,CIRCLE,SPHERE,RAY,PATH }
    record Region(Position center,int radius,Shape shape,List<Position> points,Vector direction,int distance) implements Value {
        public Region(Position center,int radius){this(center,radius,radius==0?Shape.POINT:Shape.CIRCLE,List.of(),new Vector(0,0,1000),0);}
        public Region {Objects.requireNonNull(center);Objects.requireNonNull(shape);Objects.requireNonNull(direction);points=List.copyOf(points);if(radius<0||radius>8||points.size()>16||distance<0||distance>16)throw new IllegalArgumentException("region bounds");if(shape==Shape.SPHERE&&radius>6)throw new IllegalArgumentException("sphere radius");if(points.stream().anyMatch(p->!p.dimension().equals(center.dimension())))throw new IllegalArgumentException("path dimension");}
        public Type type(){return Type.REGION;}
    }
    record Event(long tick,int interval,int remaining,boolean cancelled) implements Value {
        public Event(long tick){this(tick,0,1,false);}
        public Event {if(interval<0||interval>1200||remaining< -1||remaining>64)throw new IllegalArgumentException("event bounds");}
        public Type type(){return Type.EVENT;}
    }
    record Effect(String id,Region region,int strength,int durationTicks,long fieldCost,long focusCost,
                  Entity target,Vector vector,Event event) implements Value {
        public Effect(String id,Region region,int strength,int durationTicks,long fieldCost,long focusCost){this(id,region,strength,durationTicks,fieldCost,focusCost,null,new Vector(0,0,0),new Event(0));}
        public Effect {Objects.requireNonNull(id);Objects.requireNonNull(region);Objects.requireNonNull(vector);Objects.requireNonNull(event);if(strength<1||durationTicks<1||fieldCost<0||focusCost<0)throw new IllegalArgumentException("effect bounds");}
        public Effect scheduled(Event trigger){return new Effect(id,region,strength,durationTicks,fieldCost,focusCost,target,vector,trigger);}
        public Type type(){return Type.EFFECT;}
    }
}
