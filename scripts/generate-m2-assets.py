#!/usr/bin/env python3
"""Original 16px research items and complete, derived crafting-path guide. No recipe changes."""
from pathlib import Path
from collections import Counter
import json
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]
assets=ROOT/'src/main/resources/assets/badspell'
def write(p,data):
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
for name,lesson in [('rune_fragment',False),('teaching_folio',True)]:
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
 if lesson:
  d.rectangle((3,2,12,13),fill='#d8c89d');d.rectangle((4,3,11,12),fill='#eee2bd');d.line((5,5,10,5),fill='#88734f');d.line((5,7,9,7),fill='#88734f');d.rectangle((9,10,11,12),fill='#35685a');d.point((10,11),fill='#9ac4a1');d.line((3,14,11,14),fill='#786343')
 else:
  d.polygon([(4,2),(11,2),(13,5),(12,11),(9,14),(3,12),(2,7)],fill='#716866');d.polygon([(4,3),(10,3),(12,6),(10,11),(8,13),(4,11),(3,7)],fill='#b0a58b');d.line([(5,10),(5,6),(8,4),(10,7),(7,9),(10,10)],fill='#386d63',width=1);d.point((7,5),fill='#9ac4ac')
 im.save(assets/'textures/item'/f'{name}.png')
 write(assets/'items'/f'{name}.json',{'model':{'type':'minecraft:model','model':f'badspell:item/{name}'}})
 write(assets/'models/item'/f'{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'badspell:item/{name}'}})
paths=[]
for f in sorted((ROOT/'src/main/resources/data/badspell/recipe').glob('*.json')):
 r=json.loads(f.read_text());kind=r['type'];inputs=[]
 def ingredient(value,count=1):
  if isinstance(value,str):return {'id':value,'count':count}
  if 'items' in value:return {'id':value['items'] if isinstance(value['items'],str) else value['items'][0],'count':count,'water':True}
  raise ValueError(value)
 if kind=='badspell:processing':
  inputs=[ingredient(i['ingredient'],i['count']) for i in r['inputs']];outputs=r['outputs'];station=r['station'];ticks=r['ticks'];energy=r['energy']
 else:
  if kind=='minecraft:crafting_shaped':
   for key,n in Counter(''.join(r['pattern']).replace(' ','')).items():inputs.append(ingredient(r['key'][key],n))
  elif kind=='minecraft:crafting_shapeless':
   for value in r['ingredients']:
    item=ingredient(value);old=next((i for i in inputs if i['id']==item['id']),None)
    if old:old['count']+=1
    else:inputs.append(item)
  elif kind=='minecraft:smelting':inputs=[ingredient(r['ingredient'])]
  else:raise ValueError(kind)
  outputs=[r['result']];station='furnace' if kind=='minecraft:smelting' else 'crafting_table';ticks=r.get('cookingtime',0);energy=0
 tier=2 if any('q2' in i['id'] or 'q3' in i['id'] for i in outputs) or station in ('engraving_station','rune_press') else 1 if station not in ('crafting_table','furnace') else 0
 paths.append({'id':f.stem,'station':station,'inputs':inputs,'outputs':outputs,'ticks':ticks,'energy':energy,'tier':tier})
write(assets/'guides/paths.json',paths)
print('Generated research assets and',len(paths),'crafting paths')
