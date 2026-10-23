#!/usr/bin/env python3
"""Original 16x16 Bad Spell pixel art; standard library only, no source assets."""
from pathlib import Path
import struct,zlib,json
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/badspell'
def png(name,pixels):
 def chunk(t,data):return struct.pack('>I',len(data))+t+data+struct.pack('>I',zlib.crc32(t+data)&0xffffffff)
 data=b''.join(b'\0'+bytes(sum(row,())) for row in pixels)
 path=ROOT/'textures/item'/f'{name}.png';path.parent.mkdir(parents=True,exist_ok=True)
 path.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',16,16,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(data))+chunk(b'IEND',b''))
for name in ['formula_book','rune_glove','field_cell','rune_ink']:
 pixels=[[(0,0,0,0) for x in range(16)] for y in range(16)]
 def rect(x0,y0,x1,y1,color):
  for y in range(y0,y1):
   for x in range(x0,x1):pixels[y][x]=color
 dark=(22,32,48,255);blue=(36,93,111,255);gold=(227,179,87,255);aqua=(97,224,209,255);pale=(220,223,189,255)
 if name=='formula_book':
  rect(2,2,14,15,dark);rect(3,2,13,13,blue);rect(3,13,13,14,pale);rect(3,2,5,13,gold);rect(8,4,9,11,aqua);rect(6,6,11,7,aqua);rect(10,7,11,10,aqua)
 elif name=='rune_glove':
  rect(4,7,12,14,dark);rect(3,6,12,11,blue);rect(4,2,6,9,blue);rect(7,1,9,9,blue);rect(10,3,12,9,blue);rect(1,8,4,12,blue);rect(5,12,11,14,gold);rect(7,7,10,10,aqua)
 elif name=='field_cell':
  rect(5,1,11,3,gold);rect(4,3,12,14,dark);rect(5,4,11,13,blue);rect(6,5,10,12,aqua);rect(7,6,8,10,pale);rect(5,14,11,15,gold)
 else:
  rect(6,1,10,3,gold);rect(5,3,11,5,pale);rect(3,5,13,14,dark);rect(4,6,12,13,blue);rect(5,8,11,12,aqua);rect(5,6,6,8,pale)
 png(name,pixels)
 for folder,data in [('items',{'model':{'type':'minecraft:model','model':f'badspell:item/{name}'}}),('models/item',{'parent':'minecraft:item/generated','textures':{'layer0':f'badspell:item/{name}'}})]:
  path=ROOT/folder/f'{name}.json';path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,indent=2)+'\n')
