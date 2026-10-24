package com.badspell.qa;

import com.badspell.content.*;
import com.badspell.progression.*;
import java.util.*;
import java.nio.file.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Real packaged clients and real player files; opt-in only, excluded from release. */
@Mod(value="badspell",dist=Dist.DEDICATED_SERVER)
public final class ResearchAcceptance {
    private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger(ResearchAcceptance.class);
    private final Properties journal=new Properties();private final Path file=Path.of("research-acceptance.properties");private final Set<UUID> joined=new HashSet<>();private final int phase=Integer.getInteger("badspell.acceptance.research",1);
    public ResearchAcceptance(){if(System.getProperty("badspell.acceptance.research")==null)return;try{if(Files.exists(file))try(var in=Files.newInputStream(file)){journal.load(in);}}catch(java.io.IOException e){throw new IllegalStateException(e);}NeoForge.EVENT_BUS.addListener(this::started);NeoForge.EVENT_BUS.addListener(this::login);NeoForge.EVENT_BUS.addListener(this::tick);NeoForge.EVENT_BUS.addListener(this::stopping);NeoForge.EVENT_BUS.addListener(this::respawn);}
    private void record(String key,String value){if(value.equals(journal.getProperty(key)))return;journal.setProperty(key,value);LOG.info("M2_ACCEPTANCE {}={}",key,value);try(var out=Files.newOutputStream(file)){journal.store(out,"Actual two-client research acceptance");}catch(java.io.IOException e){throw new IllegalStateException(e);}}
    private void started(net.neoforged.neoforge.event.server.ServerStartedEvent event){record("phase",Integer.toString(phase));var level=event.getServer().overworld();if(phase==1)for(int x=0;x<18;x++)for(int z=0;z<18;z++)level.setBlockAndUpdate(new BlockPos(x,80,z),Blocks.STONE.defaultBlockState());}
    private void login(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event){if(!(event.getEntity() instanceof ServerPlayer p)||p instanceof net.neoforged.neoforge.common.util.FakePlayer)return;String name=p.getGameProfile().name();joined.add(p.getUUID());record("login."+name,p.getUUID().toString());if(phase==1&&!journal.containsKey("initialized."+name)){p.getInventory().clearContent();p.getInventory().setItem(0,new ItemStack(ModItems.FORMULA_BOOK.get()));p.getInventory().setItem(1,new ItemStack(ModItems.FIELD_CELL_Q3.get()));p.getInventory().setItem(2,new ItemStack(ModContent.item("field_crystal_shard"),8));p.getInventory().setItem(3,new ItemStack(Items.PAPER,12));p.getInventory().setItem(4,new ItemStack(ModItems.RUNE_GLOVE.get()));p.getInventory().setItem(5,new ItemStack(ModItems.CODEX.get()));p.getInventory().setItem(6,new ItemStack(Items.REDSTONE,8));p.getInventory().setItem(7,new ItemStack(ModContent.item("moon_blossom"),8));p.getInventory().setItem(8,new ItemStack(ModContent.item("rune_resin"),8));p.getInventory().setItem(9,new ItemStack(ModContent.item("arcane_ingot"),8));com.badspell.server.MagicActions.openBook(p,net.minecraft.world.InteractionHand.MAIN_HAND);record("initialized."+name,"8 bootstrap runes; physical test materials");}
        p.level().getServer().getCommands().performPrefixedCommand(p.level().getServer().createCommandSourceStack(),"tp "+name+" "+(name.endsWith("1")?8:11)+" 81 8 0 0");if(phase==2){if(!ResearchProgress.knows(p,"badspell:circle"))throw new IllegalStateException("Learned rune missing after restart");if(!snapshot(p).equals(journal.getProperty("profile."+name)))throw new IllegalStateException("Knowledge profile changed on restart");record("restart."+name,"PASS");}ResearchProgress.send(p,false,"");}
    private void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event){if(joined.size()==2)record("two_clients_phase"+phase,"PASS");var players=event.getServer().getPlayerList().getPlayers();ServerPlayer teacher=null,pupil=null;for(var p:players){if(p.getGameProfile().name().equals("M2Research1"))teacher=p;if(p.getGameProfile().name().equals("M2Research2"))pupil=p;}
        if(teacher!=null&&ResearchProgress.knows(teacher,"badspell:circle")){record("network_analysis","PASS");if(pupil!=null&&!ResearchProgress.knows(pupil,"badspell:circle"))record("independent_knowledge","PASS");}
        if(teacher!=null&&has(teacher,ModItems.TEACHING_FOLIO.get()))record("network_folio_written","PASS");if(pupil!=null&&has(pupil,ModItems.TEACHING_FOLIO.get())&&!ResearchProgress.knows(pupil,"badspell:circle"))record("carrying_does_not_teach","PASS");if(pupil!=null&&ResearchProgress.knows(pupil,"badspell:circle"))record("network_folio_read","PASS");}
    private void respawn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event){if(event.getEntity() instanceof ServerPlayer p&&ResearchProgress.knows(p,"badspell:circle")){record("death_preserves_knowledge","PASS");ResearchProgress.send(p,false,"");}}
    private static boolean has(ServerPlayer p,net.minecraft.world.item.Item item){for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(item))return true;return false;}
    private static String snapshot(ServerPlayer p){var d=ResearchProgress.data(p);return new TreeSet<>(ResearchProgress.keys(d,"known"))+"|"+new TreeSet<>(ResearchProgress.keys(d,"decoded"))+"|"+new TreeSet<>(ResearchProgress.keys(d,"stations"));}
    private void stopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event){for(var p:event.getServer().getPlayerList().getPlayers())record("profile."+p.getGameProfile().name(),snapshot(p));record("stopped_phase"+phase,"clean");}
}
