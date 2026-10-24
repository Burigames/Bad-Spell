#!/usr/bin/env python3
"""Launch two real packaged-JAR clients using the official ModDevGradle runtime classpath."""
import json,os,shutil,subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
# Produced by the official prepared client run; preserve its dependency/native classpath.
command_file=Path(os.environ.get('BAD_SPELL_CLIENT_COMMAND',str(ROOT/'build/acceptance-client-command.json')))
base=json.loads(command_file.read_text());jar=ROOT/'build/libs/badspell-0.1.0-alpha.jar'
processes=[]
for number in (1,2):
 run=ROOT/f'run/m1-client-{number}';(run/'mods').mkdir(parents=True,exist_ok=True);shutil.copyfile(jar,run/'mods'/jar.name)
 options=(ROOT/'run/options.txt').read_text() if (ROOT/'run/options.txt').exists() else ''
 updates={'renderDistance':'2','simulationDistance':'2','maxFps':'15','pauseOnLostFocus':'false','guiScale':'2','fullscreen':'false','skipMultiplayerWarning':'true'}
 lines=[line for line in options.splitlines() if line.split(':')[0] not in updates];lines += [f'{key}:{value}' for key,value in updates.items()];(run/'options.txt').write_text('\n'.join(lines)+'\n')
 program=run/'client-args.txt';program.write_text('net.neoforged.fml.startup.Client\n--version\n26.2.0.93\n--assetIndex\n32\n--assetsDir\n/workspace/.gradle/caches/neoformruntime/assets\n--username\nBadSpellQA'+str(number)+'\n--gameDir\n'+str(run)+'\n--quickPlayMultiplayer\n127.0.0.1:25576\n')
 args=[x for x in base if not x.startswith('-Dfml.modFolders=')];args[1:1]=['-Xmx1536m'];args[-1]='@'+str(program)
 env=os.environ.copy();env.pop('MOD_CLASSES',None);env.pop('FML_MOD_FOLDERS',None)
 log=open(run/'client.log','w');process=subprocess.Popen(args,cwd=run,env=env,stdout=log,stderr=subprocess.STDOUT);(run/'client.pid').write_text(str(process.pid));print(number,process.pid,run,flush=True);processes.append(process)

for process in processes:process.wait()
