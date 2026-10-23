package com.badspell.client;

import com.badspell.network.Packets;
import java.util.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.*;

/** Public glyph masks only. Private graphs are never sent to observers or read from render callbacks. */
public final class RuneSurfaceRenderer {
    private record Key(BlockPos position,int face){}
    private record Glyph(float x,float y,float z,int face,byte[] glyphs,int light){}
    private static final Map<ClientLevel,Map<Key,byte[]>> CACHE=Collections.synchronizedMap(new WeakHashMap<>());
    private static final ContextKey<List<Glyph>> DATA=new ContextKey<>(Identifier.fromNamespaceAndPath("badspell","rune_surfaces"));
    private static final Identifier TEXTURE=Identifier.fromNamespaceAndPath("badspell","textures/block/rune_glyphs.png");
    private RuneSurfaceRenderer(){}
    public static void accept(Packets.BoardMarks packet){var level=net.minecraft.client.Minecraft.getInstance().level;if(level==null||packet.face()<0||packet.face()>5||packet.glyphs().length!=16)return;boolean empty=true;for(byte code:packet.glyphs()){if(code<0||code>64)return;if(code!=0)empty=false;}var entries=CACHE.computeIfAbsent(level,k->new HashMap<>());var key=new Key(packet.position(),packet.face());if(empty)entries.remove(key);else if(entries.size()<4096)entries.put(key,packet.glyphs().clone());}
    public static void unload(net.neoforged.neoforge.event.level.ChunkEvent.Unload event){if(event.getLevel() instanceof ClientLevel level){var entries=CACHE.get(level);if(entries!=null)entries.keySet().removeIf(k->net.minecraft.world.level.ChunkPos.containing(k.position()).equals(event.getChunk().getPos()));}}
    public static void extract(ExtractLevelRenderStateEvent event){
        var entries=CACHE.get(event.getLevel());var glyphs=new ArrayList<Glyph>();var camera=event.getCamera().position();
        if(entries!=null)for(var entry:entries.entrySet()){
            var at=entry.getKey().position();if(at.distToCenterSqr(camera)>1024||!event.getFrustum().isVisible(new AABB(at)))continue;
            int light=net.minecraft.util.LightCoordsUtil.getLightCoords(event.getLevel(),at.relative(Direction.values()[entry.getKey().face()]));
            glyphs.add(new Glyph((float)(at.getX()+.5-camera.x),(float)(at.getY()+.5-camera.y),(float)(at.getZ()+.5-camera.z),entry.getKey().face(),entry.getValue(),light));if(glyphs.size()>=128)break;
        }
        event.getRenderState().setRenderData(DATA,List.copyOf(glyphs));
    }
    public static void submit(SubmitCustomGeometryEvent event){
        var glyphs=event.getLevelRenderState().getRenderData(DATA);if(glyphs==null||glyphs.isEmpty())return;
        event.getSubmitNodeCollector().submitCustomGeometry(event.getPoseStack(),net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(TEXTURE,false),(pose,buffer)->{
            for(var glyph:glyphs){var normal=Direction.values()[glyph.face()];float nx=normal.getStepX(),ny=normal.getStepY(),nz=normal.getStepZ();
                float ux=normal.getAxis()==Direction.Axis.X?0:1,uz=normal.getAxis()==Direction.Axis.X?1:0;float vy=normal.getAxis()==Direction.Axis.Y?0:-1,vz=normal==Direction.UP?1:normal==Direction.DOWN?-1:0;
                for(int cell=0;cell<16;cell++)if(glyph.glyphs()[cell]!=0){float centerU=(cell%4+.5f)/4-.5f,centerV=(cell/4+.5f)/4-.5f;
                    for(int vertex=0;vertex<4;vertex++){float u=vertex==0||vertex==3?0:1,v=vertex<2?0:1;float a=centerU+(u-.5f)*.19f,b=centerV+(v-.5f)*.19f;
                        buffer.addVertex(pose,glyph.x()+nx*.502f+ux*a,glyph.y()+ny*.502f+vy*b,glyph.z()+nz*.502f+uz*a+vz*b).setColor(0xffcad1b1).setUv(((glyph.glyphs()[cell]-1)%8+u)/8f,((glyph.glyphs()[cell]-1)/8+v)/8f).setOverlay(0).setLight(glyph.light()).setNormal(nx,ny,nz);
                    }
                }
            }
        });
    }
}
