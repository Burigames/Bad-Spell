#!/usr/bin/env python3
"""M1 conductor, device guide and original readable equipment models. Run after base asset generators."""
from pathlib import Path
from PIL import Image,ImageDraw
import json
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/badspell';D=ROOT/'src/main/resources/data/badspell'
def write(p,data):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
def box(a,b,texture):return {'from':a,'to':b,'faces':{side:{'texture':'#'+texture} for side in ['up','down','north','south','east','west']}}
tex={'leather':'badspell:block/equipment_tool_leather','brass':'badspell:block/workshop_metal','jade':'badspell:block/equipment_crystal_facet','lens':'badspell:block/equipment_tool_lens','stone':'badspell:block/workshop_stone','paper':'badspell:block/equipment_book_pages','cover':'badspell:block/equipment_book_cover','formula':'badspell:block/equipment_formula_cover','dark':'badspell:block/workshop_ink','rune':'badspell:block/rune_tile'}
display={'gui':{'rotation':[24,225,0],'translation':[0,0,0],'scale':[.85,.85,.85]},'ground':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.35,.35,.35]},'firstperson_righthand':{'rotation':[0,-80,18],'translation':[1,2,0],'scale':[.6,.6,.6]},'firstperson_lefthand':{'rotation':[0,80,-18],'translation':[1,2,0],'scale':[.6,.6,.6]},'thirdperson_righthand':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.5,.5,.5]}}
def model(name,parts):write(A/f'models/item/{name}.json',{'parent':'minecraft:block/block','render_type':'minecraft:cutout','textures':{'particle':tex['brass'],**tex},'elements':parts,'display':display})
# Calm low-noise cover and layered page edges, with purposeful pixel detail.
for name,color in [('book_pages','#dfd5b5'),('book_cover','#416659')]:
 im=Image.new('RGBA',(16,16),color);g=ImageDraw.Draw(im)
 if name=='book_pages':
  for y in [3,6,9,12]:g.line((0,y,15,y),fill='#b7a987')
 else:
  g.rectangle((1,1,14,14),outline='#a58e55');g.line((6,11,6,4,10,4,10,8,8,8),fill='#d2bf87')
 im.save(A/f'textures/block/equipment_{name}.png')
# Equipment materials are leather and faceted crystal, not scaled wood/stone tiles.
im=Image.new('RGBA',(16,16),'#6d5137');g=ImageDraw.Draw(im)
for y in range(16):
 for x in range(16):
  if (x*7+y*11)%23==0:g.point((x,y),fill='#785b3e')
for y in range(2,15,3):g.point((2,y),fill='#af9266');g.point((13,y),fill='#af9266')
g.line((0,0,15,0),fill='#92734b');g.line((0,15,15,15),fill='#493a2a');im.save(A/'textures/block/equipment_tool_leather.png')
im=Image.new('RGBA',(16,16),'#4c7c6d');g=ImageDraw.Draw(im);g.polygon([(1,1),(11,1),(7,7),(1,13)],fill='#97b8a0');g.polygon([(12,2),(14,4),(14,13),(8,14),(8,8)],fill='#345e55');g.line((2,1,10,1),fill='#d3debf');g.line((2,2,2,9),fill='#b3ceaf');im.save(A/'textures/block/equipment_crystal_facet.png')
im=Image.new('RGBA',(16,16));g=ImageDraw.Draw(im);g.rectangle((0,0,15,15),outline='#789b91');g.line((2,2,6,2),fill='#d4e2d0');g.line((2,3,2,6),fill='#d4e2d0');im.save(A/'textures/block/equipment_tool_lens.png')
im=Image.new('RGBA',(16,16),'#929a90');g=ImageDraw.Draw(im);g.line((0,0,15,0),fill='#d4d9c9');g.line((0,1,0,15),fill='#bdc4b4');g.line((0,15,15,15),fill='#626e64');im.save(A/'textures/block/equipment_tool_steel.png')
im=Image.new('RGBA',(16,16),'#5a4d61');g=ImageDraw.Draw(im);g.rectangle((1,1,14,14),outline='#8f7b64');g.line((6,12,6,4,10,4,10,8,8,8),fill='#ddcda6');g.point((11,3),fill='#91b09b');im.save(A/'textures/block/equipment_formula_cover.png')
for name in ['formula_book','arcane_codex','raw_formula_book']:
 cover='leather' if name=='raw_formula_book' else 'formula' if name=='formula_book' else 'cover'
 model(name,[box([2,2,3],[14,3,13],cover),box([3,3,4],[13,6,12],'paper'),box([2,6,3],[14,7,13],cover),box([2,3,3],[3,6,13],'leather'),box([11,6.5,6],[14.5,8,9],'brass')])
for name in ['rune_glove','rune_glove_q2','rune_glove_q3','raw_rune_glove']:
 tier=3 if name.endswith('q3') else 2 if name.endswith('q2') else 1
 parts=[box([4,1,5],[12,4,10],'leather'),box([4,4,5],[12,10,9],'leather'),box([2,5,5],[4,9,9],'leather')]
 for x in [4,6,8,10]:parts.append(box([x,10,5],[x+1.7,14-(1 if x in [4,10] else 0),8.5],'leather'))
 if name!='raw_rune_glove':parts.extend([box([4,2,4.5],[12,3.2,5],'brass'),box([6,5,4],[10,9,5],'brass'),box([7,6,3.5],[9,8,4],'jade')])
 for t in range(tier-1):parts.append(box([4,4+t*1.5,4.5],[12,4.6+t*1.5,5],'brass'))
 model(name,parts)
for name in ['field_compass','field_compass_q3','raw_field_compass']:
 parts=[box([4,3,4],[12,5,12],'brass'),box([3,3,5],[13,5,11],'brass'),box([5,5,5],[11,5.5,11],'dark'),box([7,5.5,5],[9,6,9],'jade'),box([7.5,5.5,9],[8.5,6,11],'paper')]
 if name=='raw_field_compass':parts=parts[:3]
 if name.endswith('q3'):parts.extend([box([5,5,3],[11,6,4],'jade'),box([5,5,12],[11,6,13],'jade')])
 model(name,parts)
model('arcane_monocle',[box([4,3,7],[12,4.5,9],'brass'),box([4,10.5,7],[12,12,9],'brass'),box([3,4.5,7],[4.5,10.5,9],'brass'),box([11.5,4.5,7],[13,10.5,9],'brass'),box([4.5,4.5,7.5],[11.5,10.5,8.5],'lens'),box([11,2,7],[12,4,8],'brass'),box([12,1,7],[14,2,8],'brass')])
# Open glass cages keep the core visible; Q2/Q3 have one/two recognizable metal rings.
for name in ['field_cell','field_cell_q2','field_cell_q3']:
 tier=3 if name.endswith('q3') else 2 if name.endswith('q2') else 1
 parts=[box([4,1,4],[12,3,12],'brass'),box([4,12,4],[12,14,12],'brass'),box([5,3,5],[11,12,11],'lens'),box([6,3,6],[10,12,10],'jade')]
 for x,z in [(4,4),(11,4),(4,11),(11,11)]:parts.append(box([x,3,z],[x+1,12,z+1],'brass'))
 for y in ([7] if tier==2 else [5,9] if tier==3 else []):parts.append(box([3.75,y,3.75],[12.25,y+1,12.25],'brass'))
 parts.append(box([6,14,6],[10,15,10],'jade' if tier==3 else 'brass'));model(name,parts)
for name in ['rune_chisel','resonance_fork']:
 path=A/f'models/item/{name}.json';value=json.loads(path.read_text());value['textures']['metal']='badspell:block/equipment_tool_steel'
 for element in value['elements']:
  element['from']=[max(0,min(16,n)) for n in element['from']];element['to']=[max(0,min(16,n)) for n in element['to']]
 write(path,value)
# Passive conductor: a jade body, brass contacts and an unmistakable south-pointing tip.
parts=[box([5,3,5],[11,12,11],'jade'),box([4,2,4],[12,3,12],'brass'),box([5,6,11],[11,9,13],'brass'),box([6,7,13],[10,8,15],'jade')]
write(A/'models/block/field_crystal.json',{'parent':'minecraft:block/block','textures':{'particle':tex['jade'],**tex},'elements':parts})
write(A/'blockstates/field_crystal.json',{'variants':{f'facing={side}':{'model':'badspell:block/field_crystal',**rotation} for side,rotation in {'south':{},'north':{'y':180},'west':{'y':90},'east':{'y':270},'up':{'x':270},'down':{'x':90}}.items()}})
write(A/'models/item/field_crystal.json',{'parent':'badspell:block/field_crystal','display':display})
write(A/'items/field_crystal.json',{'model':{'type':'minecraft:model','model':'badspell:item/field_crystal'}})
write(D/'loot_table/blocks/field_crystal.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'badspell:field_crystal'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
write(D/'recipe/field_crystal.json',{'type':'badspell:processing','station':'arcane_workbench','inputs':[{'ingredient':'badspell:field_crystal_shard','count':4},{'ingredient':'badspell:conductive_paste','count':1},{'ingredient':'minecraft:copper_ingot','count':1}],'outputs':[{'id':'badspell:field_crystal','count':1}],'ticks':100,'energy':20})
for name in ['arcane_workbench','crystal_grinder','alchemy_table','arcane_smelter','rune_press','engraving_station','rune_holder']:
 write(A/f'blockstates/{name}.json',{'variants':{f'facing={side}':{'model':f'badspell:block/{name}','y':y} for side,y in [('south',0),('west',90),('north',180),('east',270)]}})
for code in ['de_de','en_us']:
 lang=json.loads((A/f'lang/{code}.json').read_text());de=code=='de_de'
 lang.update({'tooltip.badspell.cell':'Feldladung: %s / %s FL' if de else 'Field charge: %s / %s FL','block.badspell.field_crystal':'Feldkristall' if de else 'Field crystal','rune.badspell.origin':'Ursprung' if de else 'Origin','help.badspell.origin':'Position des Spielers oder der freie Ausgang über dem Runenhalter. Geräte sind keine Wesen; verwende Ursprung statt Selbst für Gerätefelder.' if de else 'Player position or the clear output above the rune holder. Devices are not entities; use Origin instead of Self for device fields.','ui.badspell.target_self':'Ziel: Selbst' if de else 'Target: Self','ui.badspell.target_position':'Zielpunkt: %s %s %s' if de else 'Target point: %s %s %s','ui.badspell.run':'Start','ui.badspell.stop':'Stop','machine.badspell.status_6':'Zeitauftrag aktiv' if de else 'Scheduled spell active','machine.badspell.status_7':'Zeitauftrag wartet auf Energie/Ziel' if de else 'Scheduled spell waiting for power/target','message.badspell.crystal_direction':'Leitrichtung: %s' if de else 'Conduction direction: %s'})
 for direction,dname in [('down','Unten'),('up','Oben'),('north','Norden'),('south','Süden'),('west','Westen'),('east','Osten')]:lang['direction.badspell.'+direction]=dname if de else direction.title()
 for key in ['codex.badspell.stations.body','codex.badspell.targets.body','station.badspell.rune_holder.help']:
  lang[key]=lang[key].replace('Der Halter unterstützt derzeit nur Punktlicht!','Der Halter führt alle registrierten Wirkungen aus. Er blickt zur Vorderseite; Ursprung liefert den freien Ausgang über dem Gerät. Start/Stop steuern den Auftrag. Geräte bezahlen FL, keinen FK.').replace('The holder currently supports point light only!','The holder executes all registered effects. It aims forward; Origin returns its clear output above the device. Start/Stop control the job. Devices pay FL, not FK.').replace('Derzeit nur Punktlicht.','Alle registrierten Wirkungen; Ursprung statt Selbst für Gerätefelder.').replace('Currently point light only.','All registered effects; use Origin instead of Self for device fields.')
 lang['codex.badspell.stations.body']=lang['codex.badspell.stations.body'].split('\n\nFeldkristall: Pfeilrichtung')[0].split('\n\nField crystal: point its tip')[0]
 lang['codex.badspell.stations.body']+='\n\n'+('Feldkristall: Pfeilrichtung zum Verbraucher. Mit leerer Hand rechtsklicken, um die Richtung zu ändern. Zwischen eigenen Stationen werden bei Bedarf bis zu 80 FL/s über höchstens 16 Kristalle geleitet. Energie bleibt in den Zellen; an beiden Stationen sind Zelladapter erforderlich. Mondsalz benötigt zusätzlich Quarz als Kristallisationskeim.' if de else 'Field crystal: point its tip towards the consumer. Right-click with an empty hand to rotate. On demand, up to 80 FL/s travels through at most 16 crystals between your stations. Only cells store energy; both endpoints require adapters. Moon salt also needs quartz as a crystallization seed.')
 write(A/f'lang/{code}.json',lang)
# Refresh the visual guide from all real recipes, including the disambiguated salt route.
recipes=[json.loads(p.read_text()) for p in sorted((D/'recipe').glob('*.json'))];recipes=[r for r in recipes if r.get('type')=='badspell:processing'];write(A/'guides/recipes.json',recipes);write(ROOT/'docs/PRODUCTION_RECIPES.json',{p.stem:json.loads(p.read_text()) for p in sorted((D/'recipe').glob('*.json')) if json.loads(p.read_text()).get('type')=='badspell:processing'})
print('Generated coherent equipment models, directed crystal and complete device guide;',len(recipes),'processing recipes.')

# Cache rotated collision/selection shapes from the same visible cuboids.
entries=[]
for name in ['arcane_workbench','crystal_grinder','alchemy_table','arcane_smelter','rune_press','engraving_station','rune_holder']:
 elements=json.loads((A/f'models/block/{name}.json').read_text())['elements'];parts=[]
 for element in elements:parts.append('Shapes.box('+','.join(str(round(v/16,6)) for v in element['from']+element['to'])+')')
 entries.append('java.util.Map.entry("'+name+'",Shapes.or(Shapes.empty(),'+','.join(parts)+'))')
source='package com.badspell.production;\nimport net.minecraft.core.Direction;\nimport net.minecraft.world.phys.shapes.*;\npublic final class StationShapes {\nprivate StationShapes(){}\nprivate static final java.util.Map<String,VoxelShape> BASE=java.util.Map.ofEntries('+','.join(entries)+');\nprivate static final java.util.Map<String,VoxelShape> ROTATED=new java.util.concurrent.ConcurrentHashMap<>();\npublic static VoxelShape shape(String name){return BASE.getOrDefault(name,Shapes.block());}\npublic static VoxelShape shape(String name,Direction facing){return ROTATED.computeIfAbsent(name+facing.getName(),key->{var result=new VoxelShape[]{Shapes.empty()};shape(name).forAllBoxes((x,y,z,X,Y,Z)->{var box=switch(facing){case WEST->Shapes.box(1-Z,y,x,1-z,Y,X);case NORTH->Shapes.box(1-X,y,1-Z,1-x,Y,1-z);case EAST->Shapes.box(z,y,1-X,Z,Y,1-x);default->Shapes.box(x,y,z,X,Y,Z);};result[0]=Shapes.or(result[0],box);});return result[0];});}\n}\n'
(ROOT/'src/main/java/com/badspell/production/StationShapes.java').write_text(source)
