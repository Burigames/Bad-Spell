#!/usr/bin/env python3
"""Finite material/energy-aware acceptance itinerary; no intermediate item is a seed resource."""
from pathlib import Path
from collections import Counter
import json
ROOT=Path(__file__).resolve().parents[1];recipes={p.stem:json.loads(p.read_text()) for p in (ROOT/'src/main/resources/data/badspell/recipe').glob('*.json')}
stock=Counter();base=Counter();steps=[];placed=set();active=set();charge=Counter()
raw={'raw_field_crystal','raw_arcane_ore','runewood_log','moon_blossom','resonance_shard','glow_moss'}
preferred={'resonance_fork':'resonance_fork','field_cell':'starter_cell','empty_field_cell':'cell_casing','field_cell_q2':'field_cell_q2','field_cell_q3':'field_cell_q3'}
def ingredient(x):return x if isinstance(x,str) else x.get('items',x.get('item'))
def inputs(r):
 if 'inputs' in r:return Counter({ingredient(e['ingredient']):e['count'] for e in r['inputs']})
 if 'ingredients' in r:return Counter(map(ingredient,r['ingredients']))
 if 'ingredient' in r:return Counter({ingredient(r['ingredient']):1})
 return Counter(ingredient(r['key'][char]) for row in r['pattern'] for char in row if char!=' ')
def outputs(r):return r.get('outputs',[r['result']] if 'result' in r else [])
def ensure(item,count):
 if stock[item]>=count:return
 if item.startswith('minecraft:') or item.removeprefix('badspell:') in raw:
  need=count-stock[item];base[item]+=need;stock[item]+=need;return
 if item in active:raise RuntimeError('Production cycle: '+item)
 active.add(item)
 try:
  if item=='badspell:inscribed_rune_plate':
   while stock[item]<count:
    for need in ['engraving_station','formula_book','blank_rune_plate','field_cell']:ensure('badspell:'+need,1)
    place('engraving_station');ensure('badspell:cell_adapter',1)
    stock['badspell:blank_rune_plate']-=1;fuel,new=fuel_for(100)
    steps.append({'id':'snapshot_engraving','station':'engraving_station','fuel':fuel,'fuel_new':new,'inputs':{'badspell:formula_book':1,'badspell:blank_rune_plate':1},'outputs':[{'id':item,'count':1}],'preserve_book':True});stock[item]+=1
   return
  candidates=[(key,r) for key,r in recipes.items() if any(o['id']==item for o in outputs(r))]
  name=preferred.get(item.removeprefix('badspell:'));key,r=next((x for x in candidates if x[0]==name),sorted(candidates,key=lambda x:x[0])[0] if candidates else (None,None))
  if r is None:raise RuntimeError('No reachable source: '+item)
  while stock[item]<count:
   station=r.get('station');fuel=None;new=False
   if station:place(station)
   requirements=inputs(r)
   for resource,amount in requirements.items():ensure(resource,amount);stock[resource]-=amount
   if r.get('energy',0)>0:fuel,new=fuel_for(r['energy'])
   for out in outputs(r):stock[out['id']]+=out['count']
   steps.append({'id':key,'station':station,'fuel':fuel,'fuel_new':new,'inputs':dict(requirements),'outputs':outputs(r)})
 finally:active.remove(item)
def fuel_for(energy):
 fuel='badspell:field_cell_q2' if energy>100 else 'badspell:field_cell'
 new=charge[fuel]<energy
 if new:ensure(fuel,1);stock[fuel]-=1;charge[fuel]=300 if energy>100 else 100
 charge[fuel]-=energy
 return fuel,new
def place(station):
 if station in placed:return
 ensure('badspell:'+station,1);placed.add(station);steps.append({'place':station});ensure('badspell:cell_adapter',1);stock['badspell:cell_adapter']-=1;steps.append({'adapter':station})
goals=['rune_glove_q3','rune_glove_q2','rune_glove','arcane_monocle','field_compass_q3','field_compass','resonance_fork','rune_chisel','blank_rune_plate_q3','blank_rune_plate_q2','blank_rune_plate','field_crystal','arcane_codex','rune_ink','formula_book','field_cell_q3','field_cell_q2','field_cell']
for name in goals:ensure('badspell:'+name,1)
for name in ['rune_press','rune_holder']:place(name)
# Some upgraded goals consume lower tiers. Retain one of every promised final item.
for name in goals:ensure('badspell:'+name,1)
# Replay resource use independently. Nested recipes must not consume a parent's reserved inputs.
actual=Counter(base)
for index,step in enumerate(steps):
 consumed=Counter(step.get('inputs',{}))
 if 'place' in step:consumed['badspell:'+step['place']]+=1
 if 'adapter' in step:consumed['badspell:cell_adapter']+=1
 if step.get('fuel_new'):consumed[step['fuel']]+=1
 for item,n in consumed.items():
  actual[item]-=n
  if actual[item]<0:raise RuntimeError(f'Itinerary underflow at {index}: {item}')
 for output in step.get('outputs',[]):actual[output['id']]+=output['count']
 if step.get('preserve_book'):actual['badspell:formula_book']+=1
for name in goals:assert actual['badspell:'+name]>=1,name
result={'seed_resources':dict(base),'goals':goals,'steps':steps,'note':'Resource fixtures and vanilla components are supplied; every Bad Spell intermediate is crafted. Real station ticks and cell debits are required.'}
out=ROOT/'src/acceptance/resources/m1-journey.json';out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(result,indent=2)+'\n');print(len(steps),'actual crafting/processing steps;',sum(base.values()),'seed units;',len(base),'raw/vanilla resource types')
