#!/usr/bin/env python3
"""Original procedural assets and data, using schemas from the resolved Minecraft 26.2 resources."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,random,hashlib,zipfile,copy
ROOT=Path(__file__).resolve().parents[1]
ASSET=ROOT/'src/main/resources/assets/badspell';DATA=ROOT/'src/main/resources/data'
VANILLA=zipfile.ZipFile(ROOT/'build/moddev/artifacts/minecraft-patched-26.2.0.93.jar')
def vanilla(name):return json.loads(VANILLA.read('data/minecraft/'+name+'.json'))
def write(path,data):
 path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
def bid(name):return name if ':' in name else 'badspell:'+name
def texture(name,block=False):
 seed=int(hashlib.sha256(name.encode()).hexdigest()[:12],16);rng=random.Random(seed)
 color=(96,192,203) if any(x in name for x in ['crystal','resonance','aether']) else (111,159,114) if any(x in name for x in ['rune','wood','resin']) else (192,143,78) if any(x in name for x in ['arcane','coil','paste']) else (159,165,200)
 im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im)
 if block:
  for y in range(16):
   for x in range(16):
    delta=rng.randrange(-20,21);im.putpixel((x,y),tuple(max(0,min(255,v//2+delta)) for v in color)+(255,))
  if name in stations:
   d.rectangle((1,1,14,14),outline=(18,32,43));d.rectangle((3,3,12,12),fill=(22,43,53));d.line([(5,11),(5,5),(10,5),(10,9),(7,9)],fill=color,width=1);d.point((12,2),fill=(230,192,103))
  elif 'ore' in name:
   for x,y in [(3,3),(11,5),(6,11)]:d.polygon([(x,y-2),(x+2,y),(x,y+3),(x-1,y)],fill=color)
  elif 'planks' in name:
   for y in [3,7,11,15]:d.line((0,y,15,y),fill=(28,43,42))
  elif name=='runewood_log':
   for x in [1,5,9,13]:d.line((x,0,x,15),fill=(29,54,44));d.line((6,5,6,11),fill=(91,190,158));d.line((6,5,10,8),fill=(91,190,158))
  elif name in ['moon_blossom','runewood_sapling','glow_moss']:
   im=Image.new('RGBA',(16,16),(0,0,0,0));d=ImageDraw.Draw(im);d.line((7,14,7,5),fill=(65,133,101),width=2);d.polygon([(7,11),(3,7),(3,11),(7,13)],fill=(66,164,132));d.ellipse((4,1,11,7),fill=(158,210,219) if name=='moon_blossom' else (77,161,133));d.point((7,4),fill=(242,223,150))
  elif name=='runewood_leaves':
   for _ in range(30):im.putpixel((rng.randrange(16),rng.randrange(16)),(0,0,0,0))
 else:
  if any(x in name for x in ['water','brew','essence','solution']):
   d.rectangle((6,1,9,3),fill=(150,120,81));d.rectangle((5,4,10,6),outline=(156,194,201));d.ellipse((3,6,12,14),outline=(173,208,215),fill=color+(240,));d.line((5,8,5,11),fill=(219,245,242))
  elif any(x in name for x in ['dust','salt','sandpaper','paste']):
   d.polygon([(2,13),(4,9),(7,7),(11,8),(14,13)],fill=color);d.line((3,13,13,13),fill=tuple(max(0,v-50) for v in color));d.point((6,8),fill=(229,232,215))
  elif 'coil' in name:
   d.ellipse((2,2,13,13),outline=color,width=3);d.ellipse((5,5,10,10),outline=(72,120,135));d.line((12,11,14,14),fill=(220,180,102),width=2)
  elif 'book' in name:
   d.rectangle((3,2,12,13),fill=(45,89,79),outline=(156,204,176));d.rectangle((5,3,11,12),fill=(196,196,157));d.line((6,5,9,5),fill=(47,102,92))
  elif any(x in name for x in ['plate','seal','adapter','cell']):
   d.rectangle((3,2,12,13),fill=(36,57,71),outline=color);d.line([(6,10),(6,5),(10,5),(10,8),(8,8)],fill=(125,236,200),width=1)
  elif 'ingot' in name or 'alloy' in name:
   d.polygon([(2,10),(5,5),(12,4),(14,9),(11,13),(4,13)],fill=color);d.line((3,10,13,9),fill=(239,219,158));d.line((5,5,12,4),fill=(251,229,184))
  else:
   d.polygon([(7,1),(12,5),(11,12),(6,15),(3,9),(4,4)],fill=color);d.line([(7,2),(7,9),(11,12)],fill=(204,237,232));d.line((4,5,7,9),fill=(54,117,126))
 path=ASSET/('textures/block' if block else 'textures/item')/(name+'.png');path.parent.mkdir(parents=True,exist_ok=True);im.save(path)
materials={
'raw_field_crystal':('Roher Feldkristall','Raw field crystal'),'coarse_crystal_shard':('Grober Kristallsplitter','Coarse crystal shard'),'field_crystal_shard_q0':('Handgeschliffener Splitter Q0','Hand-polished shard Q0'),'field_crystal_shard':('Feldkristallsplitter','Field crystal shard'),'crystal_dust':('Kristallstaub','Crystal dust'),'sandpaper':('Sandpapier','Sandpaper'),'raw_arcane_ore':('Arkanerzbrocken','Raw arcane ore'),'impure_arcane_charge':('Unreine Arkanmischung','Impure arcane charge'),'arcane_ingot_q0':('Arkanbarren Q0','Arcane ingot Q0'),'arcane_ingot':('Arkanbarren Q1','Arcane ingot Q1'),'arcane_alloy':('Arkanlegierung Q2','Arcane alloy Q2'),'arcane_dust':('Arkanstaub','Arcane dust'),'raw_crystal_core':('Kristallkernrohling','Raw crystal core'),'crystal_core':('Gereinigter Kristallkern Q1','Purified crystal core Q1'),'resonance_shard':('Resonanzsplitter','Resonance shard'),'resonance_glass':('Resonanzglas','Resonance glass'),'resonance_core_blank':('Resonanzformling','Resonance core blank'),'resonance_core':('Resonanzkern Q2','Resonance core Q2'),'rune_resin':('Runenharz','Rune resin'),'conductive_paste':('Leitpaste','Conductive paste'),'precision_coil':('Präzisionsspule','Precision coil'),'pure_water':('Reinwasser','Purified water'),'moon_brew':('Mondsud','Moon brew'),'moon_essence':('Mondessenz','Moon essence'),'moon_salt':('Mondsalz','Moon salt'),'aether_solution':('Ätherlösung','Aether solution'),'glow_fiber':('Leuchtfaser','Glowing fiber'),'glow_moss_bundle':('Leuchtmoosbündel','Glowing moss bundle'),'binding_compound':('Siegelbindemasse','Seal binding compound'),'stabilizer_seal':('Stabilisatorsiegel','Stabilizer seal'),'blank_rune_plate':('Leere Runenplatte Q1','Blank rune plate Q1'),'precision_lens':('Präzisionslinse','Precision lens'),'raw_formula_book':('Formelbuchrohling','Unfinished formula book'),'raw_rune_glove':('Runenhandschuhrohling','Unfinished rune glove'),'empty_field_cell':('Feldzellengehäuse','Field cell casing'),'cell_adapter':('Startzellenadapter','Starter cell adapter'),'inscribed_rune_plate':('Gravierte Runenplatte Q1','Inscribed rune plate Q1')}
stations={'arcane_workbench':('Arkanwerkbank','Arcane workbench'),'crystal_grinder':('Kristallschleifer','Crystal grinder'),'alchemy_table':('Alchemietisch','Alchemy table'),'arcane_smelter':('Arkaner Schmelzofen','Arcane smelter'),'rune_press':('Runenpresse','Rune press'),'engraving_station':('Gravurstation','Engraving station'),'rune_holder':('Runenhalter','Rune holder')}
blocks={'field_crystal_ore':('Feldkristallerz','Field crystal ore'),'arcane_ore':('Arkanerz','Arcane ore'),'runewood_log':('Runenholzstamm','Runewood log'),'runewood_planks':('Runenholzbretter','Runewood planks'),'runewood_sapling':('Runenholzsetzling','Runewood sapling'),'runewood_leaves':('Runenlaub','Runewood leaves'),'moon_blossom':('Mondblüte','Moon blossom'),'glow_moss':('Leuchtmoos','Glowing moss'),'resonance_crystal':('Resonanzkristall','Resonance crystal'),'arcane_stone':('Arkanstein','Arcane stone'),**stations}
for name in materials:
 texture(name);write(ASSET/'items'/f'{name}.json',{'model':{'type':'minecraft:model','model':'badspell:item/'+name}});write(ASSET/'models/item'/f'{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'badspell:item/'+name}})
for name in blocks:
 texture(name,True);model={'parent':'minecraft:block/cube_all','textures':{'all':'badspell:block/'+name}}
 if name in ['moon_blossom','runewood_sapling']:model={'parent':'minecraft:block/cross','render_type':'minecraft:cutout','textures':{'cross':'badspell:block/'+name}}
 if name=='runewood_leaves':model['render_type']='minecraft:cutout'
 if name=='glow_moss':
  # The verified vanilla multiface blockstate/model geometry is referenced; only the original texture is ours.
  state=vanilla('worldgen/configured_feature/glow_lichen')
  states=json.loads(VANILLA.read('assets/minecraft/blockstates/glow_lichen.json'));states=json.loads(json.dumps(states).replace('minecraft:block/glow_lichen','badspell:block/glow_moss'))
  model=json.loads(VANILLA.read('assets/minecraft/models/block/glow_lichen.json'));model['textures']={k:v.replace('minecraft:block/glow_lichen','badspell:block/glow_moss') for k,v in model.get('textures',{}).items()};model['render_type']='minecraft:cutout'
 elif name=='runewood_log':states={'variants':{f'axis={axis}':{'model':'badspell:block/'+name,**({'x':90,'y':90} if axis=='x' else {'x':90} if axis=='z' else {})} for axis in ['x','y','z']}}
 else:states={'variants':{'':{'model':'badspell:block/'+name}}}
 write(ASSET/'blockstates'/f'{name}.json',states);write(ASSET/'models/block'/f'{name}.json',model);write(ASSET/'models/item'/f'{name}.json',{'parent':'badspell:block/'+name});write(ASSET/'items'/f'{name}.json',{'model':{'type':'minecraft:model','model':'badspell:item/'+name}})
 loot={'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':bid(name)}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]}
 if name in ['field_crystal_ore','arcane_ore','resonance_crystal']:
  loot=vanilla('loot_table/blocks/iron_ore');loot['random_sequence']=bid('blocks/'+name);children=loot['pools'][0]['entries'][0]['children'];children[0]['name']=bid(name);children[1]['name']=bid({'field_crystal_ore':'raw_field_crystal','arcane_ore':'raw_arcane_ore','resonance_crystal':'resonance_shard'}[name]);children[1]['functions'].insert(0,{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':3 if name=='field_crystal_ore' else 2}});children[1]['functions'].append({'function':'minecraft:limit_count','limit':{'max':5}})
 if name=='runewood_leaves':
  loot=vanilla('loot_table/blocks/oak_leaves');loot=json.loads(json.dumps(loot).replace('minecraft:oak_leaves',bid('runewood_leaves')).replace('minecraft:oak_sapling',bid('runewood_sapling')));loot['random_sequence']=bid('blocks/runewood_leaves')
 write(DATA/'badspell/loot_table/blocks'/f'{name}.json',loot)
# Bilingual labels, including actual process states and slot instructions.
ui={'inputs':('Eingaben','Inputs'),'outputs':('Ausgabe','Output'),'machine_fl':('%s FL','%s FL')}
status=[('Verarbeitung läuft','Processing'),('Zutaten einlegen','Insert ingredients'),('Zu wenig Feldladung','Not enough field energy'),('Ausgabe voll – pausiert','Output full – paused'),('Zellenadapter fehlt','Cell adapter missing'),('Formel oder Freigabe prüfen','Check formula or permission')]
for idx,locale in enumerate(['de_de','en_us']):
 path=ASSET/'lang'/f'{locale}.json';lang=json.loads(path.read_text());lang.update({'item.badspell.'+n:v[idx] for n,v in materials.items()});lang.update({'block.badspell.'+n:v[idx] for n,v in blocks.items()});lang.update({'gui.badspell.'+n:v[idx] for n,v in ui.items()});lang.update({'machine.badspell.status_'+str(i):v[idx] for i,v in enumerate(status)});lang['message.badspell.machine_private']=('Diese Station gehört einem anderen Spieler.','This station belongs to another player.')[idx];write(path,lang)
def shaped(name,pattern,key,result,count=1):write(DATA/'badspell/recipe'/f'{name}.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':{k:bid(v) for k,v in key.items()},'result':{'id':bid(result),'count':count}})
def shapeless(name,ingredients,result,count=1):write(DATA/'badspell/recipe'/f'{name}.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':[bid(i) for i in ingredients],'result':{'id':bid(result),'count':count}})
recipes=[]
def process(name,station,inputs,outputs,ticks=80,energy=0):
 r={'type':'badspell:processing','station':station,'inputs':[{'ingredient':bid(i),'count':n} for i,n in inputs],'outputs':[{'id':bid(i),'count':n} for i,n in outputs],'ticks':ticks,'energy':energy};write(DATA/'badspell/recipe'/f'{name}.json',r);recipes.append((name,r))
shapeless('crystal_break',['raw_field_crystal','minecraft:flint'],'coarse_crystal_shard',2)
shapeless('sandpaper',['minecraft:sand','minecraft:sand','minecraft:paper','minecraft:flint'],'sandpaper',3)
shapeless('primitive_shard',['coarse_crystal_shard','coarse_crystal_shard','sandpaper'],'field_crystal_shard_q0')
shapeless('runewood_planks',['runewood_log'],'runewood_planks',4)
shaped('arcane_workbench',['PPP','CTC','PS '],{'P':'runewood_planks','C':'minecraft:copper_ingot','T':'minecraft:crafting_table','S':'field_crystal_shard_q0'},'arcane_workbench')
shapeless('impure_arcane_charge',['raw_arcane_ore']*3+['minecraft:quartz','minecraft:coal'],'impure_arcane_charge')
shapeless('arcane_stone',['minecraft:cobbled_deepslate']*4+['crystal_dust','rune_resin'],'arcane_stone',8)
shapeless('raw_formula_book',['minecraft:book','rune_resin','rune_resin','crystal_dust'],'raw_formula_book')
shapeless('glow_moss_bundle',['glow_moss']*3,'glow_moss_bundle')
shapeless('recover_cell_casing',['field_cell'],'empty_field_cell')
for name,ingredient,result,time in [('arcane_ingot_q0','impure_arcane_charge','arcane_ingot_q0',200),('glow_fiber','glow_moss_bundle','glow_fiber',120)]:
 write(DATA/'badspell/recipe'/f'{name}.json',{'type':'minecraft:smelting','category':'misc','ingredient':bid(ingredient),'result':{'id':bid(result),'count':2 if name=='glow_fiber' else 1},'experience':.1,'cookingtime':time})
AW='arcane_workbench';KS='crystal_grinder';AT='alchemy_table';AS='arcane_smelter';RP='rune_press';GS='engraving_station'
process('cell_casing',AW,[('minecraft:copper_ingot',1),('minecraft:glass',1)],[('empty_field_cell',1)],40)
process('starter_cell',AW,[('minecraft:redstone',2),('field_crystal_shard_q0',1),('minecraft:coal',1),('empty_field_cell',1)],[('field_cell',1)],40)
process('cell_recharge',AW,[('minecraft:redstone',2),('field_crystal_shard',1),('minecraft:coal',1),('empty_field_cell',1)],[('field_cell',1)],40)
process('cell_adapter',AW,[('minecraft:copper_ingot',1),('field_crystal_shard_q0',1),('minecraft:redstone',1)],[('cell_adapter',1)],40)
process('primitive_grinder',AW,[('minecraft:iron_ingot',3),('minecraft:quartz_block',1),('minecraft:copper_ingot',2),('coarse_crystal_shard',2)],[('crystal_grinder',1)],100)
process('crystal_split',KS,[('raw_field_crystal',1)],[('field_crystal_shard',3)],80,20)
process('crystal_dust',KS,[('field_crystal_shard',2)],[('crystal_dust',4)],60,10)
process('raw_core',KS,[('field_crystal_shard',4)],[('raw_crystal_core',1)],100,20)
process('alchemy_table',AW,[('minecraft:brewing_stand',1),('minecraft:glass',2),('runewood_log',2),('minecraft:copper_ingot',1),('crystal_dust',1)],[('alchemy_table',1)],100)
# Exact water-bottle component matching is encoded with the officially supported components ingredient.
r={'type':'badspell:processing','station':AT,'inputs':[{'ingredient':{'neoforge:ingredient_type':'neoforge:components','items':'minecraft:potion','components':{'minecraft:potion_contents':{'potion':'minecraft:water'}},'strict':False},'count':1},{'ingredient':'minecraft:charcoal','count':1}],'outputs':[{'id':'badspell:pure_water','count':1}],'ticks':200,'energy':0};write(DATA/'badspell/recipe/pure_water.json',r);recipes.append(('pure_water',r))
process('rune_resin',AT,[('runewood_log',2),('pure_water',1)],[('rune_resin',2),('minecraft:glass_bottle',1)],100,10)
process('conductive_paste',AT,[('crystal_dust',2),('minecraft:redstone',1),('rune_resin',1)],[('conductive_paste',4)],80,10)
process('precision_coil',AW,[('minecraft:copper_ingot',2),('conductive_paste',2),('field_crystal_shard',1)],[('precision_coil',1)],40)
process('crystal_core',AT,[('raw_crystal_core',1),('pure_water',1)],[('crystal_core',1),('minecraft:glass_bottle',1)],100,25)
process('moon_brew',AT,[('moon_blossom',2),('pure_water',1)],[('moon_brew',2),('minecraft:glass_bottle',1)],160,15)
process('moon_salt',AT,[('moon_brew',2)],[('moon_salt',1)],240,20)
process('moon_essence',AT,[('moon_brew',2)],[('moon_essence',1)],200,20)
process('aether_solution',AT,[('moon_blossom',2),('crystal_dust',1),('pure_water',1)],[('aether_solution',1),('minecraft:glass_bottle',1)],120,20)
process('rune_ink',AT,[('crystal_dust',2),('moon_brew',1),('minecraft:ink_sac',1),('minecraft:glass_bottle',1)],[('rune_ink',1)],100,10)
process('formula_book',AT,[('raw_formula_book',1),('rune_ink',1)],[('formula_book',1)],80,0)
process('arcane_smelter',AW,[('minecraft:blast_furnace',1),('arcane_ingot_q0',4),('conductive_paste',2),('precision_coil',1)],[('arcane_smelter',1)],160)
process('arcane_ingot',AS,[('raw_arcane_ore',2),('minecraft:coal',1)],[('arcane_ingot',1)],160,40)
process('arcane_alloy',AS,[('arcane_ingot',2),('minecraft:gold_ingot',1),('moon_salt',1)],[('arcane_alloy',2)],180,75)
process('arcane_dust',KS,[('arcane_ingot',1)],[('arcane_dust',2)],80,30)
process('resonance_glass',AS,[('resonance_shard',2),('minecraft:glass',2),('arcane_dust',1)],[('resonance_glass',2)],160,50)
process('resonance_blank',KS,[('resonance_shard',4)],[('resonance_core_blank',1)],100,30)
process('resonance_core',AS,[('resonance_core_blank',1),('crystal_core',1)],[('resonance_core',1)],200,100)
process('precision_lens',KS,[('resonance_glass',1),('field_crystal_shard',2)],[('precision_lens',1)],100,45)
process('binding_compound',AT,[('rune_resin',1),('moon_salt',1)],[('binding_compound',1)],100,10)
process('engraving_station',AW,[('arcane_alloy',2),('precision_coil',2),('precision_lens',1),('crystal_core',1)],[('engraving_station',1)],160)
process('stabilizer_seal',GS,[('binding_compound',1),('conductive_paste',1)],[('stabilizer_seal',1)],100,30)
process('rune_press',AW,[('minecraft:iron_block',2),('arcane_ingot',2),('precision_coil',2),('stabilizer_seal',1)],[('rune_press',1)],160)
process('blank_rune_plate',RP,[('arcane_ingot',2),('conductive_paste',2)],[('blank_rune_plate',2)],100,40)
process('raw_rune_glove',AW,[('minecraft:leather',2),('minecraft:gold_ingot',1),('precision_coil',2),('crystal_core',1)],[('raw_rune_glove',1)],100)
process('rune_glove',GS,[('raw_rune_glove',1),('stabilizer_seal',1)],[('rune_glove',1)],160,60)
process('rune_holder',AW,[('arcane_ingot',2),('minecraft:copper_ingot',1),('conductive_paste',1),('precision_coil',1)],[('rune_holder',1)],100)
# Mineability and tree integration use the target version's built-in tags.
for tag,values in {'mineable/pickaxe':['field_crystal_ore','arcane_ore','resonance_crystal','arcane_stone',*stations],'needs_iron_tool':['field_crystal_ore','arcane_ore'],'mineable/axe':['runewood_log','runewood_planks'],'logs':['runewood_log'],'logs_that_burn':['runewood_log'],'planks':['runewood_planks'],'leaves':['runewood_leaves'],'saplings':['runewood_sapling'],'flowers':['moon_blossom'],'small_flowers':['moon_blossom']}.items():write(DATA/'minecraft/tags/block'/f'{tag}.json',{'replace':False,'values':[bid(v) for v in values]})
for tag,values in {'logs':['runewood_log'],'logs_that_burn':['runewood_log'],'planks':['runewood_planks'],'leaves':['runewood_leaves'],'saplings':['runewood_sapling'],'flowers':['moon_blossom'],'small_flowers':['moon_blossom']}.items():write(DATA/'minecraft/tags/item'/f'{tag}.json',{'replace':False,'values':[bid(v) for v in values]})
def feature(name,config,placement,step='vegetal_decoration',biomes='#minecraft:is_overworld'):
 write(DATA/'badspell/worldgen/configured_feature'/f'{name}.json',config);write(DATA/'badspell/worldgen/placed_feature'/f'{name}.json',{'feature':bid(name),'placement':placement});write(DATA/'badspell/neoforge/biome_modifier'/f'{name}.json',{'type':'neoforge:add_features','biomes':biomes,'features':[bid(name)],'step':step})
for name,chance,low,high,size in [('field_crystal_ore',7,-32,96,5),('arcane_ore',4,-60,-24,4)]:
 feature(name,{'type':'minecraft:ore','config':{'discard_chance_on_air_exposure':0,'size':size,'targets':[{'target':{'predicate_type':'minecraft:tag_match','tag':tag},'state':{'Name':bid(name)}} for tag in ['minecraft:stone_ore_replaceables','minecraft:deepslate_ore_replaceables']]}},[{'type':'minecraft:rarity_filter','chance':chance},{'type':'minecraft:in_square'},{'type':'minecraft:height_range','height':{'type':'minecraft:uniform','min_inclusive':{'absolute':low},'max_inclusive':{'absolute':high}}},{'type':'minecraft:biome'}],'underground_ores')
oak=vanilla('worldgen/configured_feature/oak');oak['config']['trunk_provider']['state']['Name']=bid('runewood_log');oak['config']['foliage_provider']['state']['Name']=bid('runewood_leaves')
feature('runewood',oak,[{'type':'minecraft:rarity_filter','chance':8},{'type':'minecraft:in_square'},{'type':'minecraft:heightmap','heightmap':'OCEAN_FLOOR'},{'type':'minecraft:biome'},{'type':'minecraft:block_predicate_filter','predicate':{'type':'minecraft:would_survive','state':{'Name':bid('runewood_sapling'),'Properties':{'stage':'0'}}}}],biomes='#minecraft:is_forest')
flower=vanilla('worldgen/configured_feature/flower_default');flower['config']['to_place']={'type':'minecraft:simple_state_provider','state':{'Name':bid('moon_blossom')}};placement=vanilla('worldgen/placed_feature/flower_default')['placement'];placement[0]['chance']=8;placement[4]['count']=12
feature('moon_blossom',flower,placement)
lichen=vanilla('worldgen/configured_feature/glow_lichen');lichen['config']['block']=bid('glow_moss');placement=vanilla('worldgen/placed_feature/glow_lichen')['placement'];placement[0]['count']=12
feature('glow_moss',lichen,placement,'underground_decoration')
geode=vanilla('worldgen/configured_feature/amethyst_geode')
for key in ['inner_layer_provider','alternate_inner_layer_provider']:geode['config']['blocks'][key]['state']['Name']=bid('resonance_crystal')
placement=vanilla('worldgen/placed_feature/amethyst_geode')['placement'];placement[0]['chance']=200
feature('resonance_geode',geode,placement,'local_modifications')
write(ROOT/'docs/PRODUCTION_RECIPES.json',dict(recipes))
print(f'Generated {len(materials)} material models, {len(blocks)} block models, {len(recipes)} timed station recipes, 6 worldgen sources')
