package com.badspell;

import com.badspell.formula.*;
import com.badspell.field.*;
import com.badspell.machines.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Dependency-free regression runner; checks throw even when JVM assertions are disabled. */
public final class CoreTests {
    private static final UUID OWNER=UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final Value.Position TARGET=new Value.Position("minecraft:overworld",1,64,1);
    private static final RuneRegistry RUNES=RuneRegistry.standard();
    private static final FormulaEngine ENGINE=new FormulaEngine(RUNES);
    private static final ActivationService SERVICE=new ActivationService(ENGINE);
    private static final RuneRegistry.Context CONTEXT=new RuneRegistry.Context(TARGET,100);
    private static int passed;
    @FunctionalInterface interface Test { void run() throws Exception; }
    public static void main(String[] args) throws Exception {
        test("F-01 deterministic graph and Lichtfunke costs",()->{
            var expected=ENGINE.plan(light(),CONTEXT);
            check(expected.fieldCost()==30 && expected.focusCost()==5,"30 FL and 5 FK");
            check(expected.strength()==3 && expected.durationTicks()==600,"light parameters");
            for(int i=0;i<100;i++)check(expected.equals(ENGINE.plan(light(),CONTEXT)),"same result");
        });
        test("F-02 typed edges reject position at effect input",()->{
            var f=changeEdges(light(),new Formula.Edge("target","activate","effect"));
            check(has(f,FormulaValidator.Code.TYPE),"type diagnostic");
            expect(FormulaEngine.InvalidFormula.class,()->ENGINE.plan(f,CONTEXT));
        });
        test("F-03 cyclic graph rejected",()->{
            var f=changeEdges(light(),new Formula.Edge("activate","target","trigger"));
            check(has(f,FormulaValidator.Code.CYCLE),"cycle diagnostic");
        });
        test("missing required port rejected",()->{
            var f=light(); var edges=new ArrayList<>(f.edges()); edges.removeFirst();
            check(has(copy(f,f.nodes(),edges),FormulaValidator.Code.MISSING_PORT),"missing port");
        });
        test("duplicate node and input diagnosed",()->{
            var f=light();var nodes=new ArrayList<>(f.nodes()); nodes.add(nodes.getFirst());
            check(has(copy(f,nodes,f.edges()),FormulaValidator.Code.DUPLICATE_NODE),"duplicate node");
            var edges=new ArrayList<>(f.edges());edges.add(edges.getFirst());
            check(has(copy(f,f.nodes(),edges),FormulaValidator.Code.DUPLICATE_PORT),"duplicate input");
        });
        test("unknown rune, node and port rejected",()->{
            var f=light();var nodes=new ArrayList<>(f.nodes());nodes.set(0,new Formula.Node("impulse","badspell:unknown",0,0,Map.of()));
            check(has(copy(f,nodes,f.edges()),FormulaValidator.Code.UNKNOWN_RUNE),"unknown rune");
            var edges=new ArrayList<>(f.edges());edges.add(new Formula.Edge("ghost","light","bad"));
            check(has(copy(f,f.nodes(),edges),FormulaValidator.Code.UNKNOWN_NODE),"unknown node");
            edges.set(edges.size()-1,new Formula.Edge("point","light","bad"));
            check(has(copy(f,f.nodes(),edges),FormulaValidator.Code.UNKNOWN_PORT),"unknown port");
        });
        test("graph node limit enforced before recursion",()->{
            var f=light();var nodes=new ArrayList<>(f.nodes());
            for(int i=0;i<124;i++)nodes.add(new Formula.Node("extra"+i,"badspell:impulse",0,0,Map.of()));
            check(has(copy(f,nodes,f.edges()),FormulaValidator.Code.LIMIT),"129 nodes");
        });
        test("negative, excessive and missing parameters rejected",()->{
            for(long n:new long[]{-1,0,9,Long.MAX_VALUE})check(has(withStrength(light(),n),FormulaValidator.Code.PARAMETER),"invalid strength");
            var f=light();var nodes=new ArrayList<>(f.nodes());nodes.set(3,new Formula.Node("light","badspell:light",0,0,Map.of()));
            check(has(copy(f,nodes,f.edges()),FormulaValidator.Code.PARAMETER),"missing parameters");
        });
        test("disconnected runes are invalid",()->{
            var f=light();var nodes=new ArrayList<>(f.nodes());nodes.add(new Formula.Node("unused","badspell:impulse",0,0,Map.of()));
            check(has(copy(f,nodes,f.edges()),FormulaValidator.Code.DISCONNECTED),"orphan rune");
        });
        test("F-05 insufficient FK leaves both resources intact",()->{
            var account=new EnergyAccount(2000,100,100,0);var before=account.balance();var effects=new AtomicInteger();
            check(activate(light(),account,100,e->effects::incrementAndGet)==ActivationService.Result.INSUFFICIENT_ENERGY,"denied");
            check(account.balance().equals(before) && effects.get()==0,"no debit/effect");
        });
        test("insufficient FL leaves FK intact",()->{
            var account=new EnergyAccount(2000,100,29,100);
            check(activate(light(),account,100,e->()->{throw new AssertionError("must not apply");})==ActivationService.Result.INSUFFICIENT_ENERGY,"insufficient FL");
            check(account.balance().equals(new EnergyAccount.Balance(29,100)),"no partial debit");
        });
        test("successful activation debits exact resources once",()->{
            var a=new EnergyAccount(2000,100,100,100);var effects=new AtomicInteger();
            check(activate(light(),a,100,e->effects::incrementAndGet)==ActivationService.Result.APPLIED,"activated");
            check(a.balance().equals(new EnergyAccount.Balance(70,95)) && effects.get()==1,"exact debit");
        });
        test("ST thresholds are deterministic and rounded up",()->{
            var a=new EnergyAccount(2000,100,100,100);
            check(activate(light(),a,24,e->()->{})==ActivationService.Result.UNSTABLE,"ST24 denied");
            check(a.balance().field()==100,"unchanged");
            check(activate(light(),a,25,e->()->{})==ActivationService.Result.APPLIED,"ST25 valid");
            check(a.balance().field()==62,"ceil(30*1.25)=38");
            expect(IllegalArgumentException.class,()->activate(light(),a,101,e->()->{}));
        });
        test("F-09 denied world host consumes no energy",()->{
            var a=new EnergyAccount(2000,100,100,100);
            check(activate(light(),a,100,e->null)==ActivationService.Result.DENIED,"host denied");
            check(a.balance().field()==100 && a.balance().focus()==100,"no debit");
        });
        test("range and dimension validation before host action",()->{
            var a=new EnergyAccount(2000,100,100,100);
            for(var origin:List.of(new Value.Position("minecraft:overworld",14,64,1),new Value.Position("minecraft:the_nether",1,64,1),new Value.Position("minecraft:overworld",Integer.MIN_VALUE,64,Integer.MAX_VALUE))) {
                check(SERVICE.activate(light(),CONTEXT,origin,100,a,e->{throw new AssertionError("host must not run");})==ActivationService.Result.OUT_OF_RANGE,"range");
            }
            check(a.balance().field()==100,"unchanged");
        });
        test("formula budget cannot be exceeded",()->{
            var f=light();f=new Formula(f.id(),f.owner(),f.revision(),f.title(),f.nodes(),f.edges(),f.output(),29);
            check(activate(f,new EnergyAccount(2000,100,100,100),100,e->()->{})==ActivationService.Result.BUDGET,"formula budget");
        });
        test("effect failure before mutation rolls back debit",()->{
            var a=new EnergyAccount(2000,100,100,100);
            expect(IllegalStateException.class,()->activate(light(),a,100,e->()->{throw new IllegalStateException("adapter refused before mutation");}));
            check(a.balance().equals(new EnergyAccount.Balance(100,100)),"rollback");
        });
        test("concurrent resource debits cannot overdraw",()->{
            var a=new EnergyAccount(2000,100,1000,100);var successes=new AtomicInteger();
            try(var pool=Executors.newFixedThreadPool(8)) {
                var futures=new ArrayList<Future<?>>();
                for(int i=0;i<200;i++)futures.add(pool.submit(()->{if(a.spend(10,1))successes.incrementAndGet();}));
                for(var future:futures)future.get();
            }
            check(successes.get()==100 && a.balance().equals(new EnergyAccount.Balance(0,0)),"atomic two-resource spend");
            expect(IllegalArgumentException.class,()->a.spend(-1,0));
            check(a.chargeField(Long.MAX_VALUE)==2000 && a.restoreFocus(200)==100,"capacity clamps");
        });
        test("conditional evaluates only selected effect branch",()->{
            var standard=RuneRegistry.standard();var entries=new HashMap<String,RuneRegistry.Rune>();
            standard.ids().forEach(id->entries.put(id,standard.get(id)));
            entries.put("badspell:forbidden",new RuneRegistry.Rune(Map.of(),Map.of(),Value.Type.EFFECT,(i,p,c)->{throw new AssertionError("unselected branch executed");}));
            var f=light();var nodes=new ArrayList<>(f.nodes());
            nodes.add(new Formula.Node("condition","badspell:boolean",0,80,Map.of("value",new Value.Bool(true))));
            nodes.add(new Formula.Node("forbidden","badspell:forbidden",0,160,Map.of()));
            nodes.add(new Formula.Node("choose","badspell:if_effect",200,80,Map.of()));
            var edges=new ArrayList<>(f.edges());edges.removeIf(e->e.to().equals("activate")&&e.port().equals("effect"));
            edges.addAll(List.of(new Formula.Edge("condition","choose","condition"),new Formula.Edge("light","choose","yes"),new Formula.Edge("forbidden","choose","no"),new Formula.Edge("choose","activate","effect")));
            check(new FormulaEngine(new RuneRegistry(entries)).plan(copy(f,nodes,edges),CONTEXT).fieldCost()==30,"selected branch");
        });
        test("F-10 immutable snapshot independent of source revisions",()->{
            var f=light();var snap=new FormulaSnapshot(f);var changed=new FormulaSnapshot(withStrength(f,6));
            check(!snap.checksum().equals(changed.checksum()),"new checksum");
            check(ENGINE.plan(snap.formula(),CONTEXT).strength()==3,"old snapshot unchanged");
            expect(UnsupportedOperationException.class,()->f.nodes().clear());
        });
        test("canonical bounded codec roundtrip and corruption checks",()->{
            var f=light();byte[] bytes=FormulaCodec.encode(f);var restored=FormulaCodec.decode(bytes);
            check(ENGINE.plan(restored,CONTEXT).equals(ENGINE.plan(f,CONTEXT)),"roundtrip semantics");
            check(new FormulaSnapshot(f).checksum().equals(new FormulaSnapshot(restored).checksum()),"canonical checksum");
            var reversed=new ArrayList<>(f.nodes());Collections.reverse(reversed);
            check(Arrays.equals(bytes,FormulaCodec.encode(copy(f,reversed,f.edges()))),"canonical node order");
            byte[] bad=bytes.clone();bad[0]=0;expect(IOException.class,()->FormulaCodec.decode(bad));
            expect(IOException.class,()->FormulaCodec.decode(Arrays.copyOf(bytes,bytes.length-1)));
            expect(IOException.class,()->FormulaCodec.decode(Arrays.copyOf(bytes,bytes.length+1)));
            expect(IOException.class,()->FormulaCodec.decode(new byte[FormulaCodec.MAX_BYTES+1]));
        });
        test("disk save reload, ACL and optimistic revisions",()->{
            Path directory=Files.createTempDirectory("badspell-formulas-");
            try {
                var store=new FormulaStore(directory,RUNES);var f=light();store.save(OWNER,f,0);
                check(ENGINE.plan(store.load(f.id()),CONTEXT).fieldCost()==30,"disk reload");
                expect(SecurityException.class,()->store.save(UUID.randomUUID(),f,1));
                expect(IllegalStateException.class,()->store.save(OWNER,f,0));
                var next=new Formula(f.id(),f.owner(),2,f.title(),f.nodes(),f.edges(),f.output(),f.energyBudget());
                store.save(OWNER,next,1);check(store.load(f.id()).revision()==2,"new revision");
                Files.write(directory.resolve(f.id()+".bspell"),new byte[]{1,2});
                expect(IOException.class,()->store.load(f.id()));
            } finally { try(var files=Files.list(directory)){for(Path path:files.toList())Files.delete(path);}Files.delete(directory); }
        });
        test("parameterized reusable module snapshot expansion",()->{
            var module=new FormulaModules.Definition(new FormulaSnapshot(light()),Map.of());
            UUID rootId=UUID.fromString("00000000-0000-0000-0000-000000000020");
            var root=new Formula(rootId,OWNER,1,"Module use",List.of(new Formula.Node("call","badspell:call",50,70,Map.of())),List.of(),"call",100);
            var use=new FormulaModules.Use(module.snapshot().formula().id(),Map.of(new FormulaModules.ParameterAddress("light","strength"),new Value.Number(6)));
            var expanded=FormulaModules.expand(new FormulaModules.Definition(new FormulaSnapshot(root),Map.of("call",use)),Map.of(module.snapshot().formula().id(),module));
            check(ENGINE.plan(expanded,CONTEXT).fieldCost()==60,"bound parameter propagated");
            check(ENGINE.plan(module.snapshot().formula(),CONTEXT).strength()==3,"template immutable");
        });
        test("F-04 module depth 9 and recursive calls rejected",()->{
            var library=new HashMap<UUID,FormulaModules.Definition>();
            var leaf=new FormulaModules.Definition(new FormulaSnapshot(light()),Map.of());library.put(light().id(),leaf);
            FormulaModules.Definition current=leaf;
            for(int i=0;i<9;i++) {
                var id=new UUID(2,i);var f=new Formula(id,OWNER,1,"Nested",List.of(new Formula.Node("call","badspell:call",0,0,Map.of())),List.of(),"call",100);
                current=new FormulaModules.Definition(new FormulaSnapshot(f),Map.of("call",new FormulaModules.Use(current.snapshot().formula().id(),Map.of())));library.put(id,current);
                if(i==7)check(ENGINE.plan(FormulaModules.expand(current,library),CONTEXT).fieldCost()==30,"8 levels accepted");
            }
            var root=current;expect(IllegalArgumentException.class,()->FormulaModules.expand(root,library));
            var recursive=new FormulaModules.Definition(root.snapshot(),Map.of("call",new FormulaModules.Use(root.snapshot().formula().id(),Map.of())));
            library.put(root.snapshot().formula().id(),recursive);
            expect(IllegalArgumentException.class,()->FormulaModules.expand(recursive,library));
        });
        test("C-03 full output leaves inputs and FL intact",()->{
            var recipe=new Recipe("split","grinder",Map.of("crystal",1),Map.of("shard",3),20,40);
            var machine=new MachineInventory("grinder",2,Map.of("crystal",1));var a=new EnergyAccount(100,0,100,0);
            check(!machine.complete(UUID.randomUUID(),recipe,a),"no output capacity");
            check(machine.contents().equals(Map.of("crystal",1)) && a.balance().field()==100,"untouched");
        });
        test("machine commit is atomic and job IDs are idempotent in memory",()->{
            var recipe=new Recipe("split","grinder",Map.of("crystal",1),Map.of("shard",3),20,40);
            var machine=new MachineInventory("grinder",64,Map.of("crystal",2));var a=new EnergyAccount(100,0,100,0);var job=UUID.randomUUID();
            check(machine.complete(job,recipe,a),"first commit");check(!machine.complete(job,recipe,a),"replay denied");
            check(machine.contents().equals(Map.of("crystal",1,"shard",3)) && a.balance().field()==80,"one result");
            var empty=new EnergyAccount(100,0,0,0);check(!machine.complete(UUID.randomUUID(),recipe,empty),"no FL");
            check(machine.contents().get("crystal")==1,"input retained");
        });
        test("production planner preserves batch leftovers and totals",()->{
            var paste=new Recipe("paste","alchemy",Map.of("dust",2,"redstone",1,"resin",1),Map.of("paste",4),10,40);
            var coil=new Recipe("coil","workbench",Map.of("copper",2,"paste",2,"shard",1),Map.of("coil",1),0,40);
            var connector=new Recipe("connectors","workbench",Map.of("glass",2,"coil",1,"paste",2),Map.of("connector",2),50,40);
            var planner=new ProductionPlanner(Map.of("paste",paste,"coil",coil,"connector",connector),Set.of("dust","redstone","resin","copper","shard","glass"));
            var plan=planner.plan("connector",2,Map.of());
            check(plan.rawInputs().equals(Map.of("dust",2L,"redstone",1L,"resin",1L,"copper",2L,"shard",1L,"glass",2L)),"transitive quantities");
            check(plan.fieldCost()==60 && plan.ticks()==120 && plan.leftovers().isEmpty(),"costs and no discarded paste");
            var one=planner.plan("connector",1,Map.of());check(one.leftovers().equals(Map.of("connector",1L)),"batch leftover");
            check(planner.plan("connector",2,Map.of("connector",2L)).batches().isEmpty(),"stock reused");
        });
        test("C-07 unbootstrapped production cycle and missing material rejected",()->{
            var a=new Recipe("a","w",Map.of("b",1),Map.of("a",1),0,1);
            var b=new Recipe("b","w",Map.of("a",1),Map.of("b",1),0,1);
            var planner=new ProductionPlanner(Map.of("a",a,"b",b),Set.of());
            expect(IllegalArgumentException.class,()->planner.plan("a",1,Map.of()));
            expect(IllegalArgumentException.class,()->planner.plan("missing",1,Map.of()));
        });
        System.out.println("PASS: "+passed+" tests; no Minecraft integration tests executed.");
    }
    private static Formula light() { return ExampleFormulas.light(OWNER); }
    private static Formula copy(Formula f,List<Formula.Node> nodes,List<Formula.Edge> edges) { return new Formula(f.id(),f.owner(),f.revision(),f.title(),nodes,edges,f.output(),f.energyBudget()); }
    private static Formula withStrength(Formula f,long strength) {
        var nodes=new ArrayList<>(f.nodes());var n=nodes.get(3);var params=new HashMap<>(n.parameters());params.put("strength",new Value.Number(strength));
        nodes.set(3,new Formula.Node(n.id(),n.rune(),n.x(),n.y(),params));return copy(f,nodes,f.edges());
    }
    private static Formula changeEdges(Formula f,Formula.Edge replacement) {
        var edges=new ArrayList<>(f.edges());edges.removeIf(e->e.to().equals(replacement.to())&&e.port().equals(replacement.port()));edges.add(replacement);return copy(f,f.nodes(),edges);
    }
    private static boolean has(Formula f,FormulaValidator.Code code) { return new FormulaValidator(RUNES).validate(f).stream().anyMatch(d->d.code()==code); }
    private static ActivationService.Result activate(Formula f,EnergyAccount account,int stability,ActivationService.EffectHost host) { return SERVICE.activate(f,CONTEXT,TARGET,stability,account,host); }
    private static void check(boolean condition,String message) { if(!condition)throw new AssertionError(message); }
    private static void expect(Class<? extends Throwable> type,Test test) throws Exception {
        try { test.run(); } catch(Throwable error) { if(type.isInstance(error))return;throw new AssertionError("Expected "+type+", got "+error,error); }
        throw new AssertionError("Expected "+type.getSimpleName());
    }
    private static void test(String name,Test test) throws Exception { test.run();passed++;System.out.println("PASS "+name); }
}
