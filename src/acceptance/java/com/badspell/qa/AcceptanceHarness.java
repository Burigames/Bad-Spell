package com.badspell.qa;

import com.badspell.content.*;
import com.badspell.formula.*;
import com.badspell.item.GloveFormulas;
import com.badspell.production.*;
import com.badspell.server.*;
import java.util.*;
import java.nio.file.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/** Only compiled into the opt-in qaM1 development run, never the release JAR. */
@Mod(value="badspell",dist=Dist.DEDICATED_SERVER)
public final class AcceptanceHarness {
    private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger(AcceptanceHarness.class);
    private final Properties journal=new Properties();private final int phase=Integer.getInteger("badspell.acceptance.phase",1);
    private final Path file=Path.of("acceptance.properties");private final Set<UUID> joined=new HashSet<>();
    private final BlockPos holderPos=new BlockPos(8,81,12),boardPos=new BlockPos(6,81,12);
    private boolean twoClients,checkedRestart;private ProductionJourney journey;private boolean journeyRecorded;
    public AcceptanceHarness(){if(System.getProperty("badspell.acceptance.phase")==null)return;try{if(Files.exists(file))try(var in=Files.newInputStream(file)){journal.load(in);}}catch(java.io.IOException e){throw new IllegalStateException(e);}NeoForge.EVENT_BUS.addListener(this::started);NeoForge.EVENT_BUS.addListener(this::login);NeoForge.EVENT_BUS.addListener(this::tick);NeoForge.EVENT_BUS.addListener(this::stopping);}
    private void record(String key,String value){journal.setProperty(key,value);LOG.info("M1_ACCEPTANCE {}={}",key,value);save();}
    private void save(){try(var out=Files.newOutputStream(file)){journal.store(out,"Actual dedicated-server/two-client M1 acceptance");}catch(java.io.IOException e){throw new IllegalStateException(e);}}
    private void started(net.neoforged.neoforge.event.server.ServerStartedEvent event){record("phase",Integer.toString(phase));var level=event.getServer().overworld();if(phase==1){for(int x=0;x<24;x++)for(int z=0;z<24;z++)level.setBlockAndUpdate(new BlockPos(x,80,z),Blocks.STONE.defaultBlockState());}else {level.getChunkAt(holderPos);verifyRestart(level);}event.getServer().getCommands().performPrefixedCommand(event.getServer().createCommandSourceStack(),"gamerule minecraft:advance_time false");}
    private static ItemStack cell(int charge){var item=new ItemStack(ModItems.FIELD_CELL_Q3.get());CustomData.update(DataComponents.CUSTOM_DATA,item,t->t.putInt("badspell_fl",charge));return item;}
    private void login(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event){if(!(event.getEntity() instanceof ServerPlayer p))return;if(p instanceof net.neoforged.neoforge.common.util.FakePlayer)throw new IllegalStateException("Acceptance requires actual network players");joined.add(p.getUUID());record("login."+p.getGameProfile().name(),p.getUUID().toString());var level=p.level();
        if(phase==1&&p.getGameProfile().name().equals("BadSpellQA1")&&!journal.containsKey("owner")){
            for(int x=0;x<24;x++)for(int z=0;z<24;z++)level.setBlockAndUpdate(new BlockPos(x,80,z),Blocks.STONE.defaultBlockState());
            for(String rune:com.badspell.research.ResearchRules.ids())com.badspell.progression.ResearchProgress.learn(p,rune); // M1 factory fixture; M2 has a separate fresh-knowledge harness.
            p.getInventory().clearContent();p.getInventory().setItem(0,new ItemStack(ModItems.FORMULA_BOOK.get()));var glove=new ItemStack(ModItems.RUNE_GLOVE_Q3.get());p.getInventory().setItem(1,glove);p.getInventory().setItem(2,cell(600));p.getInventory().setItem(3,new ItemStack(ModItems.RUNE_CHISEL.get()));p.getInventory().setItem(4,new ItemStack(ModItems.RESONANCE_FORK.get()));p.getInventory().setItem(5,new ItemStack(ModItems.FIELD_COMPASS.get()));p.getInventory().setItem(6,new ItemStack(ModItems.ARCANE_MONOCLE.get()));p.getInventory().setItem(7,new ItemStack(ModItems.RUNE_INK.get(),8));p.getInventory().setItem(8,new ItemStack(ModItems.CODEX.get()));
            try{GloveFormulas.importSnapshot(glove,ExampleFormulas.light(p.getUUID()),p.getUUID());GloveFormulas.backup(p,glove);}catch(java.io.IOException e){throw new IllegalStateException(e);}
            level.setBlockAndUpdate(holderPos,ModContent.STATIONS.get("rune_holder").get().defaultBlockState());var holder=(StationBlockEntity)level.getBlockEntity(holderPos);holder.claim(p);holder.setItem(6,cell(600));holder.setItem(11,new ItemStack(ModContent.item("cell_adapter")));var formula=ExampleFormulas.template("night_lamp",p.getUUID(),SpellEffects.position(level,holderPos.above()));
            try{var plate=new ItemStack(ModContent.item("inscribed_rune_plate"));byte[] bytes=FormulaCodec.encode(formula);CustomData.update(DataComponents.CUSTOM_DATA,plate,t->{t.putByteArray("badspell_formula",bytes);t.putString("badspell_checksum",new FormulaSnapshot(formula).checksum());});holder.setItem(0,plate);holder.activatePlate(level);
                level.setBlockAndUpdate(boardPos,Blocks.STONE.defaultBlockState());var light=ExampleFormulas.light(p.getUUID());var nodes=new ArrayList<Formula.Node>();for(int i=0;i<light.nodes().size();i++){var n=light.nodes().get(i);nodes.add(new Formula.Node(n.id(),n.rune(),i%4*154,i/4*100,n.parameters()));}var board=new Formula(light.id(),p.getUUID(),1,"Acceptance engraving",nodes,light.edges(),light.output(),100);RuneBoards.get(level).put(boardPos,Direction.NORTH.ordinal(),board);record("board_checksum",new FormulaSnapshot(board).checksum());
            }catch(java.io.IOException e){throw new IllegalStateException(e);}
            record("owner",p.getUUID().toString());level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack(),"tp "+p.getGameProfile().name()+" 8 81 8");journey=new ProductionJourney(p);level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack(),"tick rate 200");
        }
        p.level().getServer().getCommands().performPrefixedCommand(p.level().getServer().createCommandSourceStack(),"tp "+p.getGameProfile().name()+" "+(p.getGameProfile().name().equals("BadSpellQA1")?8:10)+" 81 8 0 0");
        if(phase==2&&p.getGameProfile().name().equals("BadSpellQA1")){
            try{var glove=p.getInventory().getItem(1);if(!ModItems.isGlove(glove)||!new FormulaSnapshot(GloveFormulas.read(glove)).checksum().equals(journal.getProperty("glove_checksum")))throw new IllegalStateException("Player snapshot changed on restart");if(GloveFormulas.selected(glove)!=Integer.parseInt(journal.getProperty("glove_slot")))throw new IllegalStateException("Selected slot changed");record("restart_player","PASS");}catch(java.io.IOException e){throw new IllegalStateException(e);}
        }
    }
    private void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event){var server=event.getServer();if(journey!=null&&!journeyRecorded){try{journey.tick();if(journey.done()){journeyRecorded=true;record("survival_material_journey","PASS");server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),"tick rate 20");}}catch(RuntimeException failure){journeyRecorded=true;record("survival_material_journey","FAIL: "+failure.getMessage());LOG.error("M1_JOURNEY failed",failure);server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),"tick rate 20");}}
        if(server.getTickCount()%20!=0)return;var level=server.overworld();
        if(server.getPlayerList().getPlayerCount()>=2&&!twoClients){twoClients=true;record("two_real_clients_phase"+phase,"PASS");}
        if(phase==1){var p=server.getPlayerList().getPlayerByName("BadSpellQA1");if(p!=null){try{var glove=p.getInventory().getItem(1);if(ModItems.isGlove(glove)&&GloveFormulas.occupied(glove,GloveFormulas.selected(glove))){journal.setProperty("glove_checksum",new FormulaSnapshot(GloveFormulas.read(glove)).checksum());journal.setProperty("glove_slot",Integer.toString(GloveFormulas.selected(glove)));}var book=p.getInventory().getItem(0);var tag=book.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();if(tag.getByteArray("badspell_formula").isPresent()){var f=FormulaCodec.decode(tag.getByteArray("badspell_formula").orElseThrow());journal.setProperty("book_revision",Integer.toString(f.revision()));if(f.revision()>1&&!journal.containsKey("network_book_save"))record("network_book_save","PASS");}}catch(java.io.IOException e){throw new IllegalStateException(e);}save();}}
    }
    private void verifyRestart(net.minecraft.server.level.ServerLevel level){
            level.getChunkAt(holderPos);
            if(!(level.getBlockEntity(holderPos) instanceof StationBlockEntity holder)||!holder.scheduled()||!holder.ownerId().toString().equals(journal.getProperty("owner")))throw new IllegalStateException("Holder job/owner did not survive restart");
            if(SpellFields.get(level).forOwner(UUID.fromString(journal.getProperty("owner"))).stream().noneMatch(f->f.effect().equals("badspell:cold")&&f.device().isPresent()&&f.expiry()>level.getGameTime()))throw new IllegalStateException("Paid cold field did not survive restart");record("restart_paid_field","PASS");var board=RuneBoards.get(level).find(boardPos,Direction.NORTH.ordinal());if(board==null||!new FormulaSnapshot(board.formula()).checksum().equals(journal.getProperty("board_checksum")))throw new IllegalStateException("Physical graph changed on restart");record("restart_holder_and_board","PASS");checkedRestart=true;

    }

    private void stopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event){if(phase==1&&journal.containsKey("owner")){var level=event.getServer().overworld();var owner=UUID.fromString(journal.getProperty("owner"));var pos=new BlockPos(12,81,12);level.setBlockAndUpdate(pos,ModContent.STATIONS.get("rune_holder").get().defaultBlockState());var station=(StationBlockEntity)level.getBlockEntity(pos);station.claim(DeviceCasters.at(level,owner,pos));station.setItem(6,cell(600));station.setItem(11,new ItemStack(ModContent.item("cell_adapter")));var base=ExampleFormulas.template("cold",owner,SpellEffects.position(level,pos.above()));var nodes=new ArrayList<Formula.Node>();for(var n:base.nodes())nodes.add(n.id().equals("effect")?new Formula.Node(n.id(),n.rune(),n.x(),n.y(),Map.of("strength",new Value.Number(1),"duration_ticks",new Value.Number(1200))):n);var formula=new Formula(base.id(),owner,1,"Restart field",nodes,base.edges(),base.output(),1000);try{var plate=new ItemStack(ModContent.item("inscribed_rune_plate"));var bytes=FormulaCodec.encode(formula);CustomData.update(DataComponents.CUSTOM_DATA,plate,t->{t.putByteArray("badspell_formula",bytes);t.putString("badspell_checksum",new FormulaSnapshot(formula).checksum());});station.setItem(0,plate);if(!station.activatePlate(level))throw new IllegalStateException("Cannot arm restart field");record("paid_field_armed","PASS");}catch(java.io.IOException e){throw new IllegalStateException(e);}}save();record("stopped_phase"+phase,"clean");}
}
