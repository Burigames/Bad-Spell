#!/usr/bin/env python3
"""Original 16px pixel art and vanilla-referenced cuboid models. No third-party textures copied."""
from pathlib import Path
from PIL import Image, ImageDraw
import json, random, hashlib
ROOT=Path(__file__).resolve().parents[1]; A=ROOT/'src/main/resources/assets/badspell'
def write(path,value):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(value,indent=2)+'\n')
def save(im,kind,name):p=A/f'textures/{kind}/{name}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
outline='#36332c'; iron=['#5d6058','#92968a','#bfc3b4','#e3e5d5']; gold=['#695336','#9c7d48','#bba16a','#e1cf9a']; copper=['#664a35','#9b7350','#bc9367','#d7b186']; crystal=['#304b47','#4e7c72','#87b1a1','#cee0cc']; arcane=['#4b4652','#756b7a','#a298a8','#d9d0d7']; wood=['#423528','#70553b','#9a7950','#c2a77a']
def art(name):
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);rng=random.Random(name)
 pal=arcane if any(k in name for k in ['arcane','resonance']) else crystal
 if 'glove' in name:
  d.polygon([(4,14),(3,10),(1,7),(2,5),(4,7),(4,2),(6,1),(7,6),(7,1),(9,1),(9,6),(10,2),(12,3),(12,8),(13,6),(15,7),(13,12),(11,14)],fill=outline)
  d.rectangle((5,3,6,11),fill=wood[1]);d.rectangle((8,2,8,10),fill=wood[2]);d.rectangle((10,4,11,11),fill=wood[1]);d.rectangle((5,8,11,13),fill=wood[2]);d.line((2,6,5,10),fill=wood[2]);d.rectangle((4,13,11,15),fill=gold[1]);d.line((5,13,10,13),fill=gold[2]);d.rectangle((7,8,10,11),fill=crystal[0]);d.rectangle((8,8,9,10),fill=crystal[2]);d.point((8,8),fill=crystal[3])
 elif 'book' in name or 'codex' in name:
  d.polygon([(2,3),(10,1),(14,4),(14,13),(5,15),(1,12),(1,4)],fill=outline);d.polygon([(3,4),(11,2),(13,4),(5,6)],fill='#d9c9a0');d.polygon([(3,6),(5,7),(5,13),(3,12)],fill='#654934');d.polygon([(5,7),(13,5),(13,12),(5,14)],fill='#71523b');d.line((6,8,12,6),fill='#9b7950');d.rectangle((8,9,10,11),fill=gold[1]);d.line((9,8,9,11),fill=gold[3]);d.point((10,10),fill=crystal[2])
 elif name in ['rune_chisel','resonance_fork']:
  d.line((3,13,9,7),fill=outline,width=4);d.line((3,13,9,7),fill=wood[1],width=2);d.point((3,12),fill=wood[3])
  if name=='rune_chisel':d.polygon([(8,7),(12,2),(14,1),(14,4),(10,9)],fill=iron[0]);d.line((9,7,13,2),fill=iron[3]);d.line((8,8,10,9),fill=gold[2])
  else:d.line((8,7,8,3,11,0),fill=gold[2],width=2);d.line((9,8,13,7,15,4),fill=gold[2],width=2);d.point((11,0),fill=crystal[3]);d.point((15,4),fill=crystal[3])
 elif 'compass' in name or 'monocle' in name:
  d.polygon([(5,2),(10,2),(14,6),(14,10),(10,14),(5,14),(1,10),(1,6)],fill=outline);d.ellipse((2,3,13,14),fill=gold[1],outline=gold[2]);d.ellipse((4,5,11,12),fill=crystal[0] if 'monocle' in name else '#3e4348');d.line((5,6,8,5),fill=crystal[3]);
  if 'compass' in name:d.polygon([(8,4),(10,10),(7,9),(5,11)],fill='#b85c50');d.line((8,6,7,9),fill='#e5c8b2')
  else:d.line((12,11,15,14),fill=gold[2])
 elif 'cell' in name:
  d.rectangle((5,1,10,14),fill=outline);d.rectangle((6,0,9,2),fill=copper[1]);d.rectangle((6,3,9,12),fill=crystal[0]);d.rectangle((6,7 if 'empty' not in name else 11,9,12),fill=crystal[2]);d.line((6,4,6,10),fill=crystal[3]);d.rectangle((5,13,10,14),fill=copper[1]);d.line((6,13,9,13),fill=copper[3])
 elif any(k in name for k in ['ingot','alloy','charge']):
  p=arcane if 'q0' not in name else iron;d.polygon([(2,7),(6,3),(12,3),(15,7),(12,12),(3,12),(0,10)],fill=outline);d.polygon([(2,7),(6,4),(11,4),(13,7),(9,9),(3,9)],fill=p[2]);d.polygon([(3,10),(10,10),(13,8),(12,11),(3,11)],fill=p[1]);d.line((6,4,11,4),fill=p[3]);d.point((3,7),fill=p[3])
 elif 'coil' in name:
  d.line((3,3,12,12),fill=iron[0],width=5)
  for x,y in [(3,5),(5,7),(7,9),(9,11)]:d.line((x,y,x+4,y-4),fill=copper[0],width=3);d.line((x,y-1,x+3,y-4),fill=copper[2],width=2);d.point((x+1,y-2),fill=copper[3])
 elif 'plate' in name or 'seal' in name:
  d.polygon([(3,3),(12,2),(14,11),(11,14),(2,13),(1,6)],fill=outline);d.polygon([(3,4),(11,3),(13,11),(10,13),(3,12),(2,6)],fill=arcane[1]);d.line((3,4,10,3),fill=arcane[3]);d.line((3,5,3,11),fill=arcane[2]);
  if 'inscribed' in name or 'seal' in name:d.line((6,10,6,6,9,6,9,9,7,9),fill=gold[2]);d.point((10,5),fill=crystal[3])
 elif any(k in name for k in ['water','brew','essence','solution','ink']):
  liquid=crystal[1] if 'water' in name else '#6f7592' if 'moon' in name else '#476c54' if 'solution' in name else '#403d5a';d.rectangle((6,1,9,4),fill=outline);d.rectangle((6,1,9,2),fill=wood[2]);d.polygon([(5,4),(10,4),(10,6),(13,8),(13,13),(11,15),(4,15),(2,13),(2,8),(5,6)],fill='#404b51');d.polygon([(5,7),(10,7),(12,9),(12,12),(10,14),(5,14),(3,12),(3,9)],fill=liquid);d.line((4,8,4,11),fill='#d6e0d7');d.point((5,7),fill='#eff4e4');d.line((6,4,9,4),fill='#96a7a8')
 elif any(k in name for k in ['dust','salt','paste','resin','compound']):
  pal=gold if 'resin' in name else arcane if 'salt' in name else crystal
  for x,y in [(3,11),(6,8),(9,11),(12,9),(7,13)]:d.rectangle((x-1,y-1,x+1,y+1),fill=pal[0]);d.point((x,y-1),fill=pal[2]);d.point((x,y),fill=pal[1]);d.point((x+1,y),fill=pal[3])
  d.point((4,6),fill=pal[2]);d.point((11,5),fill=pal[1])
 elif 'sandpaper' in name:
  d.polygon([(2,5),(10,2),(14,10),(6,14)],fill=wood[0]);d.polygon([(3,5),(10,3),(13,10),(6,13)],fill='#bca476')
  for _ in range(15):x,y=rng.randrange(4,12),rng.randrange(5,11);d.point((x,y),fill='#88724f')
 else:
  d.polygon([(7,1),(12,4),(13,10),(8,15),(3,12),(2,6)],fill=pal[0]);d.polygon([(7,2),(8,6),(4,11),(3,6)],fill=pal[2]);d.polygon([(8,6),(11,5),(12,10),(8,13),(5,11)],fill=pal[1]);d.line((7,2,11,4),fill=pal[3]);d.line((7,3,5,7),fill=pal[3]);d.point((8,13),fill=pal[2])
 if name.endswith('_q2'):d.point((12,13),fill=gold[2]);d.point((13,12),fill=gold[2])
 if name.endswith('_q3'):d.line((12,12,14,12),fill=gold[3]);d.line((12,13,14,13),fill=gold[1])
 save(im,'item',name)
for p in sorted((A/'items').glob('*.json')):
 name=p.stem
 if (A/f'models/item/{name}.json').exists() and not (A/f'blockstates/{name}.json').exists():art(name)
# Mineral tiles use neutral stone, compact vein clusters and restrained contrast.
for name in ['field_crystal_ore','arcane_ore','arcane_stone','resonance_crystal','runewood_log','runewood_planks','runewood_leaves']:
 rng=random.Random(name);im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
 base=(65,91,55) if name=='runewood_leaves' else (119,117,113) if 'ore' in name else (78,77,82) if name=='arcane_stone' else (92,74,55) if 'wood' in name else (93,119,129)
 for y in range(16):
  for x in range(16):
   delta=rng.choice([-9,-5,-2,0,0,2,5,8]);im.putpixel((x,y),tuple(max(0,min(255,c+delta)) for c in base)+(255,))
 if 'ore' in name:
  pal=arcane if name=='arcane_ore' else crystal
  for x,y in [(3,3),(10,5),(6,11),(13,12)]:d.rectangle((x-1,y,x+1,y+2),fill='#57565b');d.line((x-1,y,x+1,y),fill=pal[2]);d.point((x,y+1),fill=pal[1]);d.point((x-1,y),fill=pal[3])
 if name=='runewood_planks':
  for y in [3,7,11,15]:d.line((0,y,15,y),fill='#493c30');d.line((0,max(0,y-1),15,max(0,y-1)),fill='#a0805a')
  for x,y in [(6,0),(11,4),(3,8),(9,12)]:d.line((x,y,x,y+2),fill='#5e4934')
 if name=='runewood_log':
  for x in [1,4,8,12,15]:d.line((x,0,x,15),fill='#4e4033');d.line((x+1,0,x+1,15),fill='#a08762')
  d.line((7,5,7,9,9,9),fill='#819b85')
 if name=='runewood_leaves':
  for _ in range(65):x,y=rng.randrange(16),rng.randrange(16);im.putpixel((x,y),(0,0,0,0) if rng.randrange(3)==0 else (67,94,62,255))
 save(im,'block',name)
# A separate, compact rune tile is used only on working surfaces.
im=Image.new('RGBA',(16,16),'#77786d');d=ImageDraw.Draw(im);d.rectangle((0,0,15,15),outline='#55594f');d.line((1,1,14,1),fill='#9b9e8e');d.line((5,11,5,4,10,4,10,9,7,9),fill='#b4c6ab');d.point((11,3),fill='#dacb9e');save(im,'block','rune_tile')
def box(a,b,texture,rotation=None):
 e={'from':a,'to':b,'faces':{side:{'texture':'#'+texture} for side in ['down','up','north','south','west','east']}}
 if rotation:e['rotation']=rotation
 return e
def model(name,textures,elements):
 height=max(e['to'][1] for e in elements)
 if height>16:
  for e in elements:e['from'][1]=round(e['from'][1]*16/height,3);e['to'][1]=round(e['to'][1]*16/height,3)
 write(A/f'models/block/{name}.json',{'parent':'minecraft:block/block','textures':{'particle':textures.get('base','minecraft:block/stone'),**textures},'elements':elements})
woodtex='minecraft:block/spruce_planks';metal='minecraft:block/iron_block';stone='minecraft:block/stone';dark='minecraft:block/polished_deepslate';rune='badspell:block/rune_tile';crys='minecraft:block/amethyst_block'
for name in ['arcane_workbench','crystal_grinder','alchemy_table','arcane_smelter','rune_press','engraving_station','rune_holder']:
 textures={'base':woodtex,'metal':metal,'stone':stone,'dark':dark,'rune':rune,'crystal':crys,'glass':'minecraft:block/glass','top':'minecraft:block/crafting_table_top','front':'minecraft:block/furnace_front'};parts=[]
 if name in ['arcane_workbench','alchemy_table','engraving_station']:
  for x,z in [(1,1),(12,1),(1,12),(12,12)]:parts.append(box([x,0,z],[x+3,12,z+3],'base'))
  parts.extend([box([0,11,0],[16,14,16],'base'),box([1,14,1],[15,15,15],'top' if name=='arcane_workbench' else 'dark')])
  if name=='alchemy_table':parts.extend([box([2,15,3],[6,19,7],'glass'),box([3,19,4],[5,21,6],'metal'),box([10,15,9],[14,18,13],'glass'),box([7,15,3],[12,16,7],'rune')])
  if name=='engraving_station':parts.extend([box([4,15,5],[12,16,13],'rune'),box([12,15,2],[14,20,4],'metal'),box([8,19,2],[14,21,4],'metal'),box([7,17,2],[10,19,5],'glass')])
 elif name=='crystal_grinder':
  parts=[box([1,0,1],[15,3,15],'dark'),box([2,3,2],[14,11,14],'stone'),box([3,11,3],[13,14,13],'dark'),box([5,14,5],[11,15,11],'metal'),box([11,11,6],[17,13,8],'base'),box([15,13,6],[17,17,8],'metal')]
 elif name=='arcane_smelter':
  parts=[box([0,0,0],[16,14,16],'dark'),box([0,0,0],[16,2,16],'metal'),box([0,13,0],[16,16,16],'metal'),box([3,4,-.1],[13,12,1],'front'),box([5,16,5],[11,18,11],'dark')]
 elif name=='rune_press':
  parts=[box([0,0,0],[16,3,16],'dark'),box([3,3,3],[13,4,13],'rune'),box([1,3,2],[4,16,14],'metal'),box([12,3,2],[15,16,14],'metal'),box([1,15,2],[15,18,14],'dark'),box([6,9,6],[10,16,10],'metal'),box([4,8,4],[12,10,12],'dark')]
 else:parts=[box([1,0,1],[15,3,15],'dark'),box([4,3,4],[12,11,12],'stone'),box([2,10,2],[14,13,14],'dark'),box([4,13,4],[12,14,12],'rune'),box([6,14,6],[10,19,10],'crystal')]
 model(name,textures,parts)
print('Applied restrained vanilla-style pixel art and seven distinct cuboid station models.')
# Tool geometry stays within Minecraft's cuboid model system, with explicit inventory/hand transforms.
for name in ['rune_chisel','resonance_fork','field_cell','field_cell_q2','field_cell_q3']:
 tex={'wood':'minecraft:block/spruce_planks','metal':'minecraft:block/iron_block','gold':'minecraft:block/gold_block','glass':'minecraft:block/glass','crystal':'minecraft:block/prismarine_bricks','particle':'minecraft:block/iron_block'}
 if name=='rune_chisel':parts=[box([6,0,6],[10,9,10],'wood'),box([6,8,6],[10,10,10],'gold'),box([7,10,7],[9,17,9],'metal')]
 elif name=='resonance_fork':parts=[box([7,0,7],[9,9,9],'wood'),box([4,8,7],[12,10,9],'gold'),box([4,9,7],[6,17,9],'metal'),box([10,9,7],[12,17,9],'metal')]
 else:parts=[box([4,0,4],[12,2,12],'gold'),box([4,2,4],[12,13,12],'glass'),box([6,2,6],[10,13,10],'crystal'),box([4,13,4],[12,15,12],'gold'),box([6,15,6],[10,16,10],'metal')]
 write(A/f'models/item/{name}.json',{'parent':'minecraft:block/block','render_type':'minecraft:cutout','textures':tex,'elements':parts,'display':{'gui':{'rotation':[25,225,0],'translation':[0,0,0],'scale':[.8,.8,.8]},'firstperson_righthand':{'rotation':[0,-90,25],'translation':[1,2,0],'scale':[.55,.55,.55]},'firstperson_lefthand':{'rotation':[0,90,-25],'translation':[1,2,0],'scale':[.55,.55,.55]},'thirdperson_righthand':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.55,.55,.55]},'ground':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.35,.35,.35]}}})

# Native-looking plant silhouettes, without a shared generic flower sprite.
for name in ['moon_blossom','runewood_sapling','glow_moss']:
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
 if name=='moon_blossom':
  d.line((7,15,7,6),fill='#53713d',width=2);d.polygon([(7,12),(3,9),(3,11),(7,14)],fill='#729047');d.polygon([(8,11),(12,7),(12,10),(8,13)],fill='#416534')
  for x,y in [(5,2),(8,1),(10,3),(9,5),(5,5),(3,3)]:d.rectangle((x,y,x+2,y+2),fill='#c9cbb7');d.point((x,y),fill='#e6e2c7')
  d.rectangle((6,3,8,5),fill='#aaad77');d.point((7,3),fill='#e4d99d')
 elif name=='runewood_sapling':
  d.rectangle((7,6,8,15),fill='#775c40');d.line((7,14,3,9),fill='#654a35');d.line((8,11,12,7),fill='#91704d')
  for x,y in [(5,1),(8,2),(2,5),(9,6),(4,8)]:d.rectangle((x,y,x+3,y+3),fill='#496b38');d.line((x,y,x+2,y),fill='#6b884a');d.point((x+2,y+2),fill='#36582d')
 else:
  rng=random.Random(91)
  for x,y in [(1,2),(5,1),(9,3),(13,1),(3,8),(8,9),(13,7),(1,13),(6,14),(12,13)]:
   d.line((x,y,x+1,min(15,y+3)),fill='#566947');d.line((x,y+1,min(15,x+3),y+1),fill='#81965d');d.point((x+1,y),fill='#c0be77')
 save(im,'block',name)
im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.line((4,13,4,3,11,3,11,10,7,10),fill='#e4ddbd',width=2);d.point((12,2),fill='#f0e8d0');save(im,'block','rune_mark')
# Eight-pixel glyphs form a separate writing atlas; block material textures remain 16×16.
im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im)
for code in range(1,50):
 x=(code-1)%8*8;y=(code-1)//8*8;color='#e4ddbd';d.line((x+2,y+1,x+2,y+6,x+4,y+6),fill=color)
 if code&1:d.line((x+2,y+1,x+5,y+1),fill=color)
 if code&2:d.line((x+2,y+3,x+5,y+3),fill=color)
 if code&4:d.line((x+3,y+2,x+5,y),fill=color)
 if code&8:d.line((x+5,y+2,x+5,y+5),fill=color)
 if code&16:d.line((x+4,y+6,x+6,y+5),fill=color)
 if code&32:d.point((x+6,y+1),fill=color)
save(im,'block','rune_glyphs')
# Unified workshop set: original compact material tiles, restrained brass and jade accents.
for name,base in {'workshop_wood':(91,72,51),'workshop_top':(142,118,78),'workshop_stone':(88,91,86),'workshop_metal':(151,128,80),'workshop_crystal':(71,120,117),'workshop_glass':(131,164,158),'workshop_ink':(60,76,70)}.items():
 im=Image.new('RGBA',(16,16),base+(255,));d=ImageDraw.Draw(im)
 for y in range(16):
  for x in range(16):
   v=1 if (x*7+y*3)%17==0 else -2 if (x+y*5)%19==0 else 0
   im.putpixel((x,y),tuple(c+v for c in base)+(255,))
 if name=='workshop_wood':
  for y in [3,7,11,15]:d.line((0,y,15,y),fill='#4a3c2b');d.line((1,y-1,14,y-1),fill='#655037')
 elif name=='workshop_top':
  d.rectangle((0,0,15,15),outline='#685336');d.line((1,1,14,1),fill='#aa8d5d');d.line((1,2,1,14),fill='#9a7e51')
 elif name=='workshop_metal':d.line((0,0,15,0),fill='#bdab7c');d.line((0,1,0,15),fill='#aa955f');d.line((0,15,15,15),fill='#6e6042')
 elif name=='workshop_crystal':d.line((3,2,10,2),fill='#a9c5b9');d.line((3,3,3,11),fill='#7ca99b');d.line((5,13,12,13),fill='#355e5c')
 elif name=='workshop_glass':d.line((2,2,2,12),fill='#d7e2d1');d.point((3,2),fill='#edf0df')
 save(im,'block',name)
textures={key:'badspell:block/workshop_'+value for key,value in {'base':'wood','top':'top','stone':'stone','metal':'metal','crystal':'crystal','glass':'glass','ink':'ink'}.items()};textures['rune']='badspell:block/rune_tile';textures['front']='minecraft:block/furnace_front'
for name in ['arcane_workbench','crystal_grinder','alchemy_table','arcane_smelter','rune_press','engraving_station','rune_holder']:
 if name in ['arcane_workbench','alchemy_table','engraving_station']:
  parts=[box([x,0,z],[x+2,10,z+2],'base') for x,z in [(1,1),(13,1),(1,13),(13,13)]]
  parts.extend([box([1,4,2],[15,5,3],'base'),box([1,4,13],[15,5,14],'base'),box([0,9,0],[16,11,16],'base'),box([1,11,1],[15,12,15],'top')])
  if name=='arcane_workbench':parts.extend([box([2,12,2],[9,12.5,9],'rune'),box([11,12,3],[13,13,9],'metal')])
  elif name=='alchemy_table':parts.extend([box([2,12,3],[5,15,6],'glass'),box([3,15,4],[4,16,5],'metal'),box([10,12,8],[14,15,12],'crystal'),box([11,15,9],[13,16,11],'metal'),box([6,12,3],[9,12.5,9],'ink')])
  else:parts.extend([box([3,12,5],[11,12.5,13],'rune'),box([12,12,2],[13,16,3],'metal'),box([8,15,2],[13,16,3],'metal'),box([7,14,2],[10,15,5],'glass')])
 elif name=='crystal_grinder':parts=[box([1,0,1],[15,2,15],'base'),box([3,2,3],[13,10,13],'stone'),box([2,10,2],[14,12,14],'stone'),box([4,12,4],[12,14,12],'metal'),box([11,11,6],[16,12,8],'base'),box([14,12,6],[16,15,8],'metal')]
 elif name=='arcane_smelter':parts=[box([1,0,1],[15,15,15],'stone'),box([0,0,0],[16,2,16],'metal'),box([0,13,0],[16,15,16],'metal'),box([3,4,0],[13,12,1],'front'),box([5,15,5],[11,16,11],'stone')]
 elif name=='rune_press':parts=[box([0,0,0],[16,2,16],'base'),box([3,2,3],[13,3,13],'rune'),box([1,2,3],[3,15,13],'metal'),box([13,2,3],[15,15,13],'metal'),box([1,14,3],[15,16,13],'base'),box([7,8,7],[9,15,9],'metal'),box([4,7,4],[12,9,12],'stone')]
 else:parts=[box([1,0,1],[15,2,15],'base'),box([4,2,4],[12,9,12],'stone'),box([2,9,2],[14,11,14],'metal'),box([3,11,3],[13,12,13],'rune'),box([6,12,6],[10,16,10],'crystal')]
 model(name,textures,parts)
# Matching workshop materials on tools and cells; tier rings are visible in-world.
for name in ['rune_chisel','resonance_fork','field_cell','field_cell_q2','field_cell_q3']:
 p=A/f'models/item/{name}.json';m=json.loads(p.read_text());m['textures'].update({'wood':textures['base'],'metal':textures['metal'],'gold':textures['metal'],'glass':textures['glass'],'crystal':textures['crystal']})
 if name.endswith('_q2'):m['elements'].append(box([3.5,7,3.5],[12.5,8,12.5],'gold'))
 if name.endswith('_q3'):
  for y in [5,10]:m['elements'].append(box([3.5,y,3.5],[12.5,y+1,12.5],'gold'))
 write(p,m)
# The codex is identifiable beside the formula book, with a jade cover and pale bookmark.
im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.polygon([(2,2),(12,1),(14,3),(14,13),(4,15),(1,12),(1,3)],fill='#302d27');d.polygon([(3,3),(12,2),(13,3),(4,5)],fill='#e2d5b4');d.polygon([(4,5),(13,4),(13,13),(4,14)],fill='#416e62');d.line((2,4,2,12),fill='#997847');d.line((5,6,12,5),fill='#7ba18b');d.line((7,8,10,8,10,11,7,11,7,8),fill='#d3be87');d.line((11,2,11,6),fill='#c5a362');save(im,'item','arcane_codex')
print('Applied coherent workshop materials, bounded models and quality markings.')
# Collision/selection geometry is generated from the same cuboids as the visible stations.
rows=[]
for name in ['arcane_workbench','crystal_grinder','alchemy_table','arcane_smelter','rune_press','engraving_station','rune_holder']:
 elements=json.loads((A/f'models/block/{name}.json').read_text())['elements'];boxes=[]
 for e in elements:
  coords=[max(0,min(16,v))/16 for v in e['from']+e['to']];boxes.append('Shapes.box('+','.join(str(round(v,6)) for v in coords)+')')
 rows.append('case "'+name+'" -> Shapes.or(Shapes.empty(),'+','.join(boxes)+');')
source='''package com.badspell.production;\n\nimport net.minecraft.world.phys.shapes.*;\n\n/** Generated by style-assets.py from the visible cuboids; no runtime asset reads on dedicated servers. */\npublic final class StationShapes {\n private StationShapes(){}\n public static VoxelShape shape(String station){return switch(station){\n'''+ '\n'.join(rows)+'''\ndefault -> Shapes.block();\n};}\n}\n'''
(ROOT/'src/main/java/com/badspell/production/StationShapes.java').write_text(source)
