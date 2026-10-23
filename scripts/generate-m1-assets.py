#!/usr/bin/env python3
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1];A=ROOT/'src/main/resources/assets/badspell';D=ROOT/'src/main/resources/data/badspell/recipe'
names={'rune_glove_q2':('Runenhandschuh Q2','Rune glove Q2'),'rune_glove_q3':('Runenhandschuh Q3','Rune glove Q3'),'field_cell_q2':('Feldzelle · 300 FL','Field cell · 300 FL'),'field_cell_q3':('Feldzelle · 600 FL','Field cell · 600 FL'),'rune_chisel':('Runenmeißel','Rune chisel'),'resonance_fork':('Resonanzgabel','Resonance fork'),'field_compass':('Feldkompass','Field compass'),'field_compass_q3':('Feldkompass Q3','Field compass Q3'),'arcane_monocle':('Arkanes Monokel','Arcane monocle'),'calibrated_coil':('Resonanzkalibrierte Spule Q3','Resonance-calibrated coil Q3'),'monocle_frame':('Monokelgestell','Monocle frame'),'raw_field_compass':('Feldkompassrohling','Unfinished field compass'),'raw_resonance_fork':('Resonanzgabelrohling','Unfinished resonance fork'),'blank_rune_plate_q2':('Leere Runenplatte Q2','Blank rune plate Q2'),'blank_rune_plate_q3':('Leere Runenplatte Q3','Blank rune plate Q3'),'inscribed_rune_plate_q2':('Gravierte Runenplatte Q2','Inscribed rune plate Q2'),'inscribed_rune_plate_q3':('Gravierte Runenplatte Q3','Inscribed rune plate Q3')}
def write(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
for name in names:
 write(A/f'items/{name}.json',{'model':{'type':'minecraft:model','model':'badspell:item/'+name}});write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'badspell:item/'+name}})
def process(name,station,inputs,output,count=1,ticks=100,energy=0):
 def item(i):return i if ':' in i else 'badspell:'+i
 write(D/f'{name}.json',{'type':'badspell:processing','station':station,'inputs':[{'ingredient':item(i),'count':n} for i,n in inputs],'outputs':[{'id':item(output),'count':count}],'ticks':ticks,'energy':energy})
AW='arcane_workbench';GS='engraving_station';RP='rune_press';AS='arcane_smelter'
process('rune_chisel',AW,[('minecraft:iron_ingot',2),('arcane_ingot',1),('conductive_paste',1),('field_crystal_shard',1)],'rune_chisel')
process('monocle_frame',AW,[('minecraft:gold_ingot',1),('precision_lens',1),('resonance_glass',1),('rune_resin',1)],'monocle_frame')
process('arcane_monocle',GS,[('monocle_frame',1),('stabilizer_seal',1)],'arcane_monocle',energy=60)
process('raw_field_compass',AW,[('minecraft:compass',1),('resonance_core',1),('conductive_paste',2)],'raw_field_compass')
process('field_compass',GS,[('raw_field_compass',1),('inscribed_rune_plate',1)],'field_compass',energy=80)
process('raw_resonance_fork',AW,[('arcane_ingot',2),('field_crystal_shard',2),('precision_coil',1)],'raw_resonance_fork')
process('resonance_fork',GS,[('raw_resonance_fork',1),('inscribed_rune_plate',1)],'resonance_fork',energy=45)
process('field_cell_q2',AW,[('empty_field_cell',1),('crystal_core',1),('conductive_paste',2),('minecraft:redstone',4),('minecraft:coal',3)],'field_cell_q2')
process('field_cell_q3',AW,[('empty_field_cell',1),('resonance_core',1),('arcane_alloy',2),('minecraft:redstone',8),('minecraft:coal',6)],'field_cell_q3')
process('calibrated_coil',AS,[('precision_coil',1),('resonance_core',1),('arcane_alloy',1)],'calibrated_coil',energy=180,ticks=240)
process('rune_glove_q2',GS,[('rune_glove',1),('precision_coil',2),('arcane_alloy',2),('stabilizer_seal',1)],'rune_glove_q2',energy=100)
process('rune_glove_q3',GS,[('rune_glove_q2',1),('calibrated_coil',2),('resonance_core',1),('stabilizer_seal',1)],'rune_glove_q3',energy=300)
process('field_compass_q3',AW,[('field_compass',1),('calibrated_coil',1),('resonance_core',1)],'field_compass_q3',energy=100)
process('blank_rune_plate_q2',RP,[('arcane_alloy',2),('conductive_paste',2),('resonance_glass',1)],'blank_rune_plate_q2',2,energy=80)
process('blank_rune_plate_q3',RP,[('blank_rune_plate_q2',2),('calibrated_coil',1),('resonance_core',1)],'blank_rune_plate_q3',2,energy=200)
ui={'library':('Bibliothek','Library'),'library_query':('Suchen oder neuer Titel …','Search or new title …'),'new_formula':('Neu','New'),'load':('Laden','Load'),'module':('Als Modul','As module'),'tags':('Tags','Tags'),'module_recursive':('Eine Formel kann sich nicht selbst aufrufen.','A formula cannot call itself.'),'module_invalid':('Modul ungültig oder Knotenlimit erreicht.','Invalid module or node limit reached.'),'durability':('Werkzeug: %s','Tool: %s'),'runes_short':('Runen','runes')}
messages={'cancelled':('Zeitpläne abgebrochen. Bereits ausgeführte Wirkungen laufen regulär aus.','Schedules cancelled. Committed effects expire normally.'),'schedule_limit':('Maximal vier Zeitpläne gleichzeitig.','At most four concurrent schedules.'),'entity_scan':('%s · %s/%s LP','%s · %s/%s HP'),'fork_diagnose':('Resonanzgabel: Diagnose','Resonance fork: diagnose'),'fork_activate':('Resonanzgabel: Aktivieren','Resonance fork: activate'),'monocle_intensity':('Monokelintensität: %s · 0 aus, 1 dezent, 2 deutlich','Monocle intensity: %s · 0 off, 1 subtle, 2 strong'),'surface':('Diese Fläche trägt keine Gravur.','This surface cannot hold engravings.'),'private':('Privates Feld · keine Formeldetails','Private field · formula details hidden'),'no_board':('Keine Gravur auf dieser Blockseite.','No engraving on this block face.'),'machine_scan':('%s · %s FL · %s','%s · %s FL · %s'),'board_scan':('%s Runen · %s Fehler','%s runes · %s errors'),'activation_denied':('Aktivierung abgewiesen; Ziel, Energie oder Formel prüfen.','Activation denied; check target, energy and formula.'),'signature_bound':('Neue, ortsfeste Signatur gebunden.','New fixed-location signature bound.'),'signature_missing':('Umschalt-Rechtsklick auf einen Block bindet einen Zielpunkt.','Shift-right-click a block to bind a destination.'),'signature_far':('Kein Signal in dieser Dimension oder Reichweite.','No signal in this dimension or range.'),'compass':('Richtung: %s · Signal %s/5','Direction: %s · signal %s/5')}
runes={'vector':('Vektor','Vector'),'direction':('Blickrichtung','Look direction'),'target_entity':('Zielentität','Target entity'),'self':('Selbst','Self'),'sphere':('Kugel','Sphere'),'ray':('Strahl','Ray'),'path':('Pfad','Path'),'delay':('Verzögere','Delay'),'interval':('Intervall','Interval'),'limit':('Begrenze','Limit'),'abort':('Abbruch','Abort'),'check':('Prüfe','Check'),'return':('Rückgabe','Return'),'call':('Aufruf','Call'),'heat':('Wärme','Heat'),'cold':('Kälte','Cold'),'shield':('Schild','Shield'),'heal':('Heil','Heal'),'push':('Schub','Push'),'see':('Sehen','See')}
for kind in ['number','boolean','position','vector']:
 runes['input_'+kind]=('Moduleingang: '+kind,'Module input: '+kind)
for op in ['equal','if']:
 for kind in ['number','boolean','position','vector','entity','text']:runes[op+'_'+kind]=(('Gleich' if op=='equal' else 'Falls')+': '+kind,('Equal' if op=='equal' else 'If')+': '+kind)
for sample,de in [('light','Helligkeit'),('rain','Regen'),('sky','Freier Himmel'),('health','Lebenspunkte'),('focus','Fokus'),('field','Feldladung'),('stability','Stabilität'),('biome','Biomkennung')]:runes['environment_'+sample]=('Umgebung: '+de,'Environment: '+sample)
for locale,index in [('de_de',0),('en_us',1)]:
 p=A/f'lang/{locale}.json';lang=json.loads(p.read_text())
 for name,values in names.items():lang['item.badspell.'+name]=values[index]
 for name,values in ui.items():lang['gui.badspell.'+name]=values[index]
 for name,values in messages.items():lang['message.badspell.'+name]=values[index]
 for name,values in runes.items():lang['rune.badspell.'+name]=values[index]
 for name,values in {'vector':('Vektor','Vector'),'entity':('Entität','Entity'),'text':('Text','Text')}.items():lang['type.badspell.'+name]=values[index]
 for name,values in {'x':('X · Milliblöcke','X · milliblocks'),'y':('Y · Milliblöcke','Y · milliblocks'),'z':('Z · Milliblöcke','Z · milliblocks'),'points':('Punkte','Points'),'ticks':('Ticks','Ticks'),'count':('Aufrufe','Calls'),'distance':('Distanz','Distance'),'name':('Eingangsname','Input name'),'snapshot':('Unveränderliches Modul','Immutable module')}.items():lang['parameter.badspell.'+name]=values[index]
 for name,values in {'forward':('vorne','ahead'),'back':('hinten','behind'),'left':('links','left'),'right':('rechts','right')}.items():lang['direction.badspell.'+name]=values[index]
 lang['result.badspell.waiting']=['vorgemerkt','scheduled'][index];lang['result.badspell.cancelled']=['abgebrochen','cancelled'][index]
 write(p,lang)
print('Generated M1 assets, translations and production recipes.')
for locale,index in [('de_de',0),('en_us',1)]:
 p=A/f'lang/{locale}.json';lang=json.loads(p.read_text())
 for key,de,en in [('sign','Signa','Signa'),('measure','Messen','Measure'),('bind','Binde','Bind'),('seal','Siegel','Seal'),('transfer','Übertrage Gegenstände','Transfer items'),('transfer_energy','Übertrage FL','Transfer FL'),('path_start','Pfadbeginn','Path start'),('path_append','Wegpunkt anfügen','Append waypoint'),('target_block','Zielblock','Target block')]:lang['rune.badspell.'+key]=[de,en][index]
 for kind in ['number','boolean','position','vector','entity','text','region','event','effect']:lang['rune.badspell.return_'+kind]=['Rückgabe: ','Return: '][index]+kind
 for op in ['if','equal']:
  for kind in ['region','event']:lang['rune.badspell.'+op+'_'+kind]=(['Falls' if op=='if' else 'Gleich','If' if op=='if' else 'Equal'][index])+': '+kind
 lang['parameter.badspell.locked']=['Verriegelt','Locked'][index];lang['gui.badspell.invalid_field']=['Bitte die rot markierten Eingaben korrigieren.','Correct the fields marked in red.'][index]
 write(p,lang)
for locale,index in [('de_de',0),('en_us',1)]:
 p=A/f'lang/{locale}.json';lang=json.loads(p.read_text());lang['tooltip.badspell.cast_cost']=['Vorschau: %s FL · %s FK','Preview: %s FL · %s FK'][index];lang['key.badspell.monocle']=['Monokelintensität wechseln','Cycle monocle intensity'][index];lang['key.category.badspell.controls']='Bad Spell';write(p,lang)
process('fork_repair',AW,[('resonance_fork',1),('rune_resin',1),('field_crystal_shard',1)],'resonance_fork',ticks=80)
process('recharge_cell_q2',AW,[('field_cell_q2',1),('minecraft:redstone',4),('minecraft:coal',3)],'field_cell_q2',ticks=80)
process('recharge_cell_q3',AW,[('field_cell_q3',1),('minecraft:redstone',8),('minecraft:coal',6)],'field_cell_q3',ticks=120)
for locale,index in [('de_de',0),('en_us',1)]:
 p=A/f'lang/{locale}.json';lang=json.loads(p.read_text());lang['rune.badspell.none']=['Keine Wirkung','No effect'][index];lang['result.badspell.skipped']=['Keine Wirkung · Sensorik aktiv','No effect · sensing active'][index]
 for name,de,en in [('light','Licht','Light'),('heal','Heilung','Heal'),('push','Schub','Push'),('heat','Wärme','Heat'),('cold','Kälte','Cold'),('shield','Schild','Shield'),('see','Sehen','See'),('night_lamp','Nachtlampe','Night lamp'),('signature','Signatur','Signature')]:lang['template.badspell.'+name]=[de,en][index]
 write(p,lang)
for locale,index in [('de_de',0),('en_us',1)]:
 p=A/f'lang/{locale}.json';lang=json.loads(p.read_text());lang['rune.badspell.target_entity_id']=['Zielentität per UUID','Entity by UUID'][index];lang['rune.badspell.sign_entity']=['Entitätssignatur','Entity signature'][index];lang['parameter.badspell.id']=['Entitäts-UUID','Entity UUID'][index];lang['parameter.badspell.frequency']=['Frequenz 1–4 · A–D','Frequency 1–4 · A–D'][index];write(p,lang)
for locale,index in [('de_de',0),('en_us',1)]:
 p=A/f'lang/{locale}.json';lang=json.loads(p.read_text());lang['gui.badspell.from_book']=['Aus Buch','From book'][index];lang['gui.badspell.no_import_book']=['Ein eigenes, gespeichertes Formelbuch mit höchstens 16 Knoten wird benötigt.','An owned, saved formula book with at most 16 nodes is required.'][index];lang['message.badspell.entity_scan']=['%s · %s/%s LP · Signatur %s','%s · %s/%s HP · signature %s'][index];write(p,lang)
