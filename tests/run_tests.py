#!/usr/bin/env python3
"""Run JVM regression tests from the source ZIP; no Android device required."""
from pathlib import Path
import argparse,os,subprocess,tempfile
p=argparse.ArgumentParser();p.add_argument('--json-jar',required=True,help='org.json json-20240303.jar');p.add_argument('--java-home',default=os.environ.get('JAVA_HOME',''));a=p.parse_args()
base=Path(__file__).resolve().parents[1];src=base/'orchid/app/src/main/java/fr/ilamartin/orchid';jar=Path(a.json_jar).resolve()
if not jar.is_file():p.error('The JSON test dependency is missing')
def binary(name):return str(Path(a.java_home)/'bin'/name) if a.java_home else name
def run(args):subprocess.run(list(map(str,args)),check=True)
with tempfile.TemporaryDirectory(prefix='orchid-tests-') as temp:
 out=Path(temp)/'rules';out.mkdir();audio=Path(temp)/'audio';audio.mkdir();voice=Path(temp)/'voice';voice.mkdir()
 classes=['KeywordRules','KeywordGrammar','DetectionGate','ConfidenceRules','TxtWords','PcmWav','Models','ModelDownload','OutputScheduler','BipChoice','Entry']
 files=[src/(name+'.java') for name in classes]+list((base/'tests').glob('*.java'))+list((base/'tests/support').rglob('*.java'))
 run([binary('javac'),'-encoding','UTF-8','-cp',jar,'-d',out]+files)
 for name in ['KeywordGrammarTest','ZeroIntervalTest','ConfidenceRulesTest','ContrastGrammarTest','TxtWordsTest','fr.ilamartin.orchid.ModelZipImportTest','PcmWavTest','OutputSchedulerTest','BipChoiceTest','EntryBipTest']:
  args=[binary('java'),'-cp',str(out)+os.pathsep+str(jar),name]
  if name=='PcmWavTest':args.append(base/'orchid/app/src/main/res/raw')
  run(args)
 run([binary('javac'),'-encoding','UTF-8','-d',audio]+[src/(name+'.java') for name in ['Bips','BipChoice','PcmWav']]+list((base/'tests/audio').rglob('*.java')))
 run([binary('java'),'-cp',audio,'fr.ilamartin.orchid.BipPlaybackTest',base/'orchid/app/src/main/res/raw'])

 # Real OfflineVoice + SoundOutputs exercised against an in-memory TTS/MediaPlayer Android double.
 run([binary('javac'),'-encoding','UTF-8','-d',voice]+[src/(name+'.java') for name in ['OfflineVoice','SoundOutputs','OutputScheduler']]+list((base/'tests/voice').rglob('*.java')))
 for suite in ['OfflineVoiceTest','SoundOutputsTest']:
  run([binary('java'),'-cp',voice,'fr.ilamartin.orchid.'+suite])
