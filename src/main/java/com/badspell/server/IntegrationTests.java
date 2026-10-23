package com.badspell.server;

import com.badspell.content.ModItems;
import com.badspell.field.EnergyAccount;
import com.badspell.formula.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Executed only in the explicit gameTestServer run, never registered for normal play. */
public final class IntegrationTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS=DeferredRegister.create(Registries.TEST_FUNCTION,"badspell");
    static {
        FUNCTIONS.register("registered_items",()->helper->{
            helper.assertTrue(ModItems.FORMULA_BOOK.get().getDefaultInstance().getMaxStackSize()==1,"Formula book must not stack");
            helper.assertTrue(ModItems.RUNE_GLOVE.get().getDefaultInstance().getMaxStackSize()==1,"Glove must not stack");
            helper.assertTrue(ModItems.RUNE_INK.get().getDefaultInstance().getMaxStackSize()==16,"Ink stack limit");helper.succeed();
        });
        FUNCTIONS.register("light_lifecycle",()->helper->{
            var level=helper.getLevel();var pos=helper.absolutePos(new BlockPos(2,2,2));
            var original=level.getBlockState(pos);var fields=LightFields.get(level);
            var target=new Value.Position(level.dimension().identifier().toString(),pos.getX(),pos.getY(),pos.getZ());
            var template=ExampleFormulas.light(new UUID(0,1));var nodes=new ArrayList<>(template.nodes());
            var light=nodes.get(3);nodes.set(3,new Formula.Node(light.id(),light.rune(),light.x(),light.y(),Map.of("strength",new Value.Number(3),"duration_ticks",new Value.Number(20))));
            var formula=new Formula(template.id(),template.owner(),1,template.title(),nodes,template.edges(),template.output(),100);
            var account=new EnergyAccount(100,100,100,100);
            var result=new ActivationService(new FormulaEngine(RuneRegistry.standard())).activate(formula,new RuneRegistry.Context(target,level.getGameTime()),target,100,account,
                effect->()->fields.add(level,pos,effect.strength(),effect.durationTicks()));
            helper.assertTrue(result==ActivationService.Result.APPLIED,"Activation must apply in a real server world");
            helper.assertTrue(level.getChunkAt(pos).getAuxLightManager(ChunkPos.containing(pos)).getLightAt(pos)==3,"Auxiliary light is present");
            helper.assertTrue(level.getBlockState(pos).equals(original),"Light must not replace a world block");
            helper.assertTrue(account.balance().equals(new EnergyAccount.Balance(99,95)),"Exactly 1 FL and 5 FK for 20 tick test light");
            helper.runAfterDelay(21,()->{
                helper.assertTrue(level.getChunkAt(pos).getAuxLightManager(ChunkPos.containing(pos)).getLightAt(pos)==0,"Expired light must be removed");
                helper.assertTrue(level.getBlockState(pos).equals(original),"Block remains unchanged after expiry");helper.succeed();
            });
        });
        FUNCTIONS.register("light_illuminates_air",()->helper->{
            var level=helper.getLevel();var pos=helper.absolutePos(new BlockPos(2,2,2));
            LightFields.get(level).add(level,pos,8,40);
            boolean[] observed={false};java.util.concurrent.CompletableFuture<?>[] cleared={null};
            // Server light work is asynchronous. Observe actual illumination before asserting its removal.
            helper.succeedWhen(()->{
                yieldForLighting(level);
                if(!observed[0]) {
                    helper.assertTrue(level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,pos)==8,"Spell must produce real block light at an air position, not only an auxiliary entry");
                    helper.assertTrue(level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,pos.above())>=7,"Spell light must propagate to adjacent air");
                    observed[0]=true;
                }
                helper.assertTrue(com.badspell.lighting.FieldLightSources.emission(level,pos)==0,"Wait for field expiry");
                if(cleared[0]==null)cleared[0]=level.getChunkSource().getLightEngine().waitForPendingTasks(pos.getX()>>4,pos.getZ()>>4);
                helper.assertTrue(cleared[0].isDone(),"Wait for asynchronous removal lighting work");
                helper.assertTrue(level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,pos)==0,"Real light must disappear after expiry; actual="+level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,pos));
            });
        });
        FUNCTIONS.register("light_starter_cast",()->helper->{
            var level=helper.getLevel();var player=new net.neoforged.neoforge.common.util.FakePlayer(level,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"BadSpell_Cast"));
            var origin=helper.absolutePos(new BlockPos(2,1,2));var wall=helper.absolutePos(new BlockPos(2,2,5));var target=wall.north();
            level.setBlockAndUpdate(wall,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(target,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            player.setPos(origin.getX()+.5,origin.getY(),origin.getZ()+.5);player.setYRot(0);player.setXRot(0);
            var glove=ModItems.RUNE_GLOVE.get().getDefaultInstance();var cell=ModItems.FIELD_CELL.get().getDefaultInstance();
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,glove);player.getInventory().setItem(1,cell);
            try {com.badspell.item.GloveFormulas.importSnapshot(glove,ExampleFormulas.light(player.getUUID()),player.getUUID());}
            catch(java.io.IOException error){throw new IllegalStateException(error);}
            glove.getItem().use(level,player,net.minecraft.world.InteractionHand.MAIN_HAND);
            helper.assertTrue(bookTag(cell).getIntOr("badspell_fl",100)==70,"Real glove use spends exactly 30 FL");
            helper.assertTrue(player.getPersistentData().getIntOr("badspell_focus",100)==95,"Real glove use spends exactly 5 FK");
            helper.succeedWhen(()->{
                yieldForLighting(level);
                helper.assertTrue(level.getBlockState(target).isAir(),"Spell must not replace target air with a light block");
                helper.assertTrue(level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,target)==3,"Starter glove spell must actually illuminate the world");
            });
        });
        FUNCTIONS.register("light_empty_section",()->helper->{
            var level=helper.getLevel();var pos=helper.absolutePos(new BlockPos(2,90,2));
            helper.assertTrue(level.getChunkAt(pos).getSection(level.getSectionIndex(pos.getY())).hasOnlyAir(),"Fixture must be an empty air section");
            LightFields.get(level).add(level,pos,8,40);boolean[] observed={false};java.util.concurrent.CompletableFuture<?>[] cleared={null};
            helper.succeedWhen(()->{
                yieldForLighting(level);
                if(!observed[0]) {
                    helper.assertTrue(level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,pos)==8,"A field must illuminate a completely empty section");
                    helper.assertTrue(level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,pos.above())>=7,"Light must propagate through empty air");observed[0]=true;
                }
                helper.assertTrue(com.badspell.lighting.FieldLightSources.emission(level,pos)==0,"Wait for empty-section expiry");
                if(cleared[0]==null)cleared[0]=level.getChunkSource().getLightEngine().waitForPendingTasks(pos.getX()>>4,pos.getZ()>>4);
                helper.assertTrue(cleared[0].isDone(),"Wait for asynchronous empty-section removal work");
                helper.assertTrue(level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,pos)==0,"Empty-section light must disappear after expiry; actual="+level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK,pos));
            });
        });
        FUNCTIONS.register("item_snapshot_persistence",()->helper->{
            var book=ModItems.FORMULA_BOOK.get().getDefaultInstance();
            try {
                var formula=ExampleFormulas.light(new UUID(0,1));var bytes=FormulaCodec.encode(formula);
                net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,book,tag->tag.putByteArray("badspell_formula",bytes));
                var stored=book.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag().getByteArray("badspell_formula").orElseThrow();
                helper.assertTrue(new FormulaSnapshot(formula).checksum().equals(new FormulaSnapshot(FormulaCodec.decode(stored)).checksum()),"Item component retains exact graph snapshot");
                helper.succeed();
            } catch(java.io.IOException error){throw new IllegalStateException(error);}
        });

        FUNCTIONS.register("book_server_workflow",()->helper->{
            var player=new net.neoforged.neoforge.common.util.FakePlayer(helper.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"BadSpell_QA"));
            var hand=net.minecraft.world.InteractionHand.MAIN_HAND;
            var book=ModItems.FORMULA_BOOK.get().getDefaultInstance();
            var glove=ModItems.RUNE_GLOVE.get().getDefaultInstance();
            player.setItemInHand(hand,book);player.getInventory().setItem(1,glove);
            player.getInventory().setItem(2,new net.minecraft.world.item.ItemStack(ModItems.RUNE_INK.get(),2));
            MagicActions.openBook(player,hand);
            try {
                Formula original=bookFormula(book);
                helper.assertTrue(original.owner().equals(player.getUUID()),"Server owns initial formula identity");
                helper.assertTrue(bookTag(book).getIntOr("badspell_ink",0)==64,"Initial book ink");
                Formula edited=editStrength(original,4);
                MagicActions.handle(player,new com.badspell.network.Packets.BookAction(0,0,FormulaCodec.encode(edited)));
                Formula saved=bookFormula(book);
                helper.assertTrue(saved.revision()==2,"Save advances authoritative revision");
                helper.assertTrue(bookTag(book).getIntOr("badspell_ink",0)==63,"One changed rune costs one writing point");
                helper.assertTrue(bookTag(book).getListOrEmpty("badspell_history").size()==1,"Previous revision is retained");
                MagicActions.handle(player,new com.badspell.network.Packets.BookAction(0,1,FormulaCodec.encode(saved)));
                String imported=new FormulaSnapshot(bookFormula(glove)).checksum();
                helper.assertTrue(imported.equals(new FormulaSnapshot(saved).checksum()),"Glove gets exact saved snapshot");
                MagicActions.handle(player,new com.badspell.network.Packets.BookAction(0,0,FormulaCodec.encode(editStrength(saved,5))));
                helper.assertTrue(bookFormula(book).revision()==3,"Second book revision saved");
                helper.assertTrue(imported.equals(new FormulaSnapshot(bookFormula(glove)).checksum()),"Editing book does not mutate glove");
                MagicActions.handle(player,new com.badspell.network.Packets.BookAction(0,0,FormulaCodec.encode(saved)));
                helper.assertTrue(bookFormula(book).revision()==3,"Stale client revision rejected");
                MagicActions.handle(player,new com.badspell.network.Packets.BookAction(0,2,FormulaCodec.encode(bookFormula(book))));
                helper.assertTrue(player.getInventory().getItem(2).getCount()==1 && bookTag(book).getIntOr("badspell_ink",0)==64,"Ink refill consumes one real item");
                helper.succeed();
            }catch(java.io.IOException error){throw new IllegalStateException(error);}
        });
        FUNCTIONS.register("book_rejects_forgery",()->helper->{
            var player=new net.neoforged.neoforge.common.util.FakePlayer(helper.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"BadSpell_QA"));
            var book=ModItems.FORMULA_BOOK.get().getDefaultInstance();player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,book);
            MagicActions.openBook(player,net.minecraft.world.InteractionHand.MAIN_HAND);
            try {
                Formula original=bookFormula(book);String checksum=new FormulaSnapshot(original).checksum();
                var forged=new Formula(original.id(),UUID.randomUUID(),original.revision(),original.title(),original.nodes(),original.edges(),original.output(),100);
                MagicActions.handle(player,new com.badspell.network.Packets.BookAction(0,0,FormulaCodec.encode(forged)));
                MagicActions.handle(player,new com.badspell.network.Packets.BookAction(0,0,FormulaCodec.encode(editStrength(original,-100))));
                helper.assertTrue(checksum.equals(new FormulaSnapshot(bookFormula(book)).checksum()),"Forged owner and negative strength cannot change book");
                helper.assertTrue(bookTag(book).getIntOr("badspell_ink",0)==64,"Invalid graph consumes no ink");
                helper.succeed();
            }catch(java.io.IOException error){throw new IllegalStateException(error);}
        });
        FUNCTIONS.register("book_history_restore",()->helper->{
            var player=new net.neoforged.neoforge.common.util.FakePlayer(helper.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"BadSpell_History"));
            var hand=net.minecraft.world.InteractionHand.MAIN_HAND;var book=ModItems.FORMULA_BOOK.get().getDefaultInstance();player.setItemInHand(hand,book);MagicActions.openBook(player,hand);
            try {
                var first=bookFormula(book);
                MagicActions.handle(player,new com.badspell.network.Packets.BookAction(0,0,FormulaCodec.encode(editStrength(first,5))));
                var current=bookFormula(book);int ink=bookTag(book).getIntOr("badspell_ink",0);
                MagicActions.handle(player,new com.badspell.network.Packets.BookAction(0,3,FormulaCodec.encode(current)));
                helper.assertTrue(bookTag(book).getIntOr("badspell_ink",0)==ink && bookFormula(book).revision()==2,"Reading history spends no ink or revision");
                var archived=FormulaCodec.decode(bookTag(book).getListOrEmpty("badspell_history").getCompoundOrEmpty(0).getByteArray("snapshot").orElseThrow());
                var restored=new Formula(current.id(),current.owner(),current.revision(),archived.title(),archived.nodes(),archived.edges(),archived.output(),archived.energyBudget());
                MagicActions.handle(player,new com.badspell.network.Packets.BookAction(0,0,FormulaCodec.encode(restored)));
                helper.assertTrue(bookFormula(book).revision()==3,"Restore advances revision instead of rewinding it");
                helper.assertTrue(bookFormula(book).nodes().equals(first.nodes()),"Restored graph matches archived content");
                helper.assertTrue(bookTag(book).getIntOr("badspell_ink",0)==ink-1,"Restore pays ordinary changed-rune ink cost");
                helper.assertTrue(bookTag(book).getListOrEmpty("badspell_history").size()==2,"Restore retains replaced revision");
                helper.succeed();
            }catch(java.io.IOException error){throw new IllegalStateException(error);}
        });
        FUNCTIONS.register("glove_slots",()->helper->{
            var player=new net.neoforged.neoforge.common.util.FakePlayer(helper.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"BadSpell_Slots"));
            var glove=ModItems.RUNE_GLOVE.get().getDefaultInstance();
            var book=ModItems.FORMULA_BOOK.get().getDefaultInstance();
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,book);player.getInventory().setItem(1,glove);
            MagicActions.openBook(player,net.minecraft.world.InteractionHand.MAIN_HAND);
            try {
                var original=bookFormula(book);var first=new FormulaSnapshot(original).checksum();
                MagicActions.handle(player,new com.badspell.network.Packets.BookAction(0,1,FormulaCodec.encode(original)));
                com.badspell.item.GloveFormulas.cycle(glove,1,player.getUUID());
                helper.assertTrue(com.badspell.item.GloveFormulas.selected(glove)==1,"Select second slot");
                helper.assertTrue(!com.badspell.item.GloveFormulas.occupied(glove,1),"New slot is empty; no fallback spell");
                helper.assertTrue(!bookTag(glove).contains("badspell_formula"),"Empty selection clears legacy mirror");
                var edited=editStrength(original,4);
                MagicActions.handle(player,new com.badspell.network.Packets.BookAction(0,0,FormulaCodec.encode(edited)));
                var saved=bookFormula(book);
                MagicActions.handle(player,new com.badspell.network.Packets.BookAction(0,1,FormulaCodec.encode(saved)));
                helper.assertTrue(com.badspell.item.GloveFormulas.read(glove).revision()==2,"Export writes selected second slot");
                com.badspell.item.GloveFormulas.cycle(glove,-1,player.getUUID());
                helper.assertTrue(first.equals(new FormulaSnapshot(com.badspell.item.GloveFormulas.read(glove)).checksum()),"First slot remains unchanged");
                com.badspell.item.GloveFormulas.cycle(glove,-1,player.getUUID());
                helper.assertTrue(com.badspell.item.GloveFormulas.selected(glove)==3,"Backward wrap stays within four slots");
                com.badspell.item.GloveFormulas.cycle(glove,1,player.getUUID());
                helper.assertTrue(com.badspell.item.GloveFormulas.selected(glove)==0,"Forward wrap");
                var copied=glove.copy();com.badspell.item.GloveFormulas.cycle(copied,1,player.getUUID());
                helper.assertTrue(com.badspell.item.GloveFormulas.read(copied).revision()==2,"Item copy retains second snapshot");
                helper.assertTrue(com.badspell.item.GloveFormulas.selected(glove)==0,"Copied selection cannot mutate original");
                helper.succeed();
            }catch(java.io.IOException error){throw new IllegalStateException(error);}
        });
        FUNCTIONS.register("glove_legacy_and_security",()->helper->{
            var owner=UUID.randomUUID();var glove=ModItems.RUNE_GLOVE.get().getDefaultInstance();
            try {
                var formula=ExampleFormulas.light(owner);var checksum=new FormulaSnapshot(formula).checksum();
                var bytes=FormulaCodec.encode(formula);
                net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,glove,t->{t.putByteArray("badspell_formula",bytes);t.putString("badspell_checksum",checksum);});
                helper.assertTrue(checksum.equals(new FormulaSnapshot(com.badspell.item.GloveFormulas.read(glove)).checksum()),"Legacy snapshot reads unchanged");
                com.badspell.item.GloveFormulas.cycle(glove,1,owner);
                helper.assertTrue(com.badspell.item.GloveFormulas.occupied(glove,0),"Migration retains legacy first slot");
                helper.assertTrue(!com.badspell.item.GloveFormulas.ownedBy(glove,UUID.randomUUID()),"Empty selected slot does not bypass private occupied slots");
                var before=bookTag(glove);
                try {com.badspell.item.GloveFormulas.importSnapshot(glove,ExampleFormulas.light(UUID.randomUUID()),owner);throw new AssertionError("Foreign import accepted");}
                catch(java.io.IOException expected) {helper.assertTrue(before.equals(bookTag(glove)),"Denied import leaves glove unchanged");}
                try {com.badspell.item.GloveFormulas.cycle(glove,100,owner);throw new AssertionError("Invalid direction accepted");}
                catch(IllegalArgumentException expected){helper.assertTrue(before.equals(bookTag(glove)),"Invalid direction changes no data");}
                com.badspell.item.GloveFormulas.cycle(glove,-1,owner);
                helper.assertTrue(checksum.equals(new FormulaSnapshot(com.badspell.item.GloveFormulas.read(glove)).checksum()),"Migration roundtrip keeps exact original");
                helper.succeed();
            }catch(java.io.IOException error){throw new IllegalStateException(error);}
        });
        FUNCTIONS.register("glove_server_intents",()->helper->{
            var player=new net.neoforged.neoforge.common.util.FakePlayer(helper.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"BadSpell_Intents"));
            var hand=net.minecraft.world.InteractionHand.MAIN_HAND;var glove=ModItems.RUNE_GLOVE.get().getDefaultInstance();
            player.setItemInHand(hand,glove);
            MagicActions.selectSlot(player,-1,1);MagicActions.selectSlot(player,0,200);
            helper.assertTrue(com.badspell.item.GloveFormulas.selected(glove)==0,"Malformed intents rejected");
            MagicActions.selectSlot(player,0,1);MagicActions.selectSlot(player,0,1);
            helper.assertTrue(com.badspell.item.GloveFormulas.selected(glove)==1,"At most one switch per server tick");
            var cell=ModItems.FIELD_CELL.get().getDefaultInstance();player.getInventory().setItem(1,cell);
            var before=bookTag(cell);MagicActions.cast(player,hand);
            helper.assertTrue(before.equals(bookTag(cell)),"Empty slot spends no cell charge");
            helper.assertTrue(!player.getPersistentData().contains("badspell_last_cast"),"Empty slot spends no focus or cast cooldown");
            player.setItemInHand(hand,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK));
            MagicActions.openBook(player,hand);MagicActions.cast(player,hand);
            helper.assertTrue(!bookTag(player.getItemInHand(hand)).contains("badspell_formula"),"Wrong item cannot become a formula book");
            helper.succeed();
        });
    }
    private static void yieldForLighting(net.minecraft.server.level.ServerLevel level) {
        // GameTestServer runs ticks without real-time pacing. Let its asynchronous light worker run too.
        level.getChunkSource().pollTask();level.getChunkSource().getLightEngine().tryScheduleUpdate();
        try {Thread.sleep(1);}
        catch(InterruptedException error){Thread.currentThread().interrupt();throw new IllegalStateException("Lighting test interrupted",error);}
    }
    private static net.minecraft.nbt.CompoundTag bookTag(net.minecraft.world.item.ItemStack item) {
        return item.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
    }
    private static Formula bookFormula(net.minecraft.world.item.ItemStack item) throws java.io.IOException {
        return FormulaCodec.decode(bookTag(item).getByteArray("badspell_formula").orElseThrow());
    }
    private static Formula editStrength(Formula formula,long strength) {
        var nodes=new ArrayList<>(formula.nodes());var node=nodes.stream().filter(n->n.rune().equals("badspell:light")).findFirst().orElseThrow();var parameters=new HashMap<>(node.parameters());parameters.put("strength",new Value.Number(strength));
        nodes.set(nodes.indexOf(node),new Formula.Node(node.id(),node.rune(),node.x(),node.y(),parameters));
        return new Formula(formula.id(),formula.owner(),formula.revision(),formula.title(),nodes,formula.edges(),formula.output(),formula.energyBudget());
    }
    private IntegrationTests() {}
    public static void register(IEventBus bus){FUNCTIONS.register(bus);bus.addListener(IntegrationTests::tests);}
    private static void tests(RegisterGameTestsEvent event) {
        var environment=event.registerEnvironment(Identifier.fromNamespaceAndPath("badspell","test_environment"));
        for(String path:List.of("registered_items","light_lifecycle","light_illuminates_air","light_starter_cast","light_empty_section","item_snapshot_persistence","book_server_workflow","book_rejects_forgery","glove_slots","glove_legacy_and_security","glove_server_intents","book_history_restore")) {
            Identifier id=Identifier.fromNamespaceAndPath("badspell",path);
            event.registerTest(id,new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION,id),
                new TestData<>(environment,Identifier.fromNamespaceAndPath("badspell","test_room"),path.startsWith("light_")?1000:100,0,true)));
        }
    }
}
