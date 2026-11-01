#!/usr/bin/env python3
"""Audio + scheduler regression tests, no Android SDK and no extra JAR required."""
from pathlib import Path
import subprocess,tempfile
base=Path(__file__).resolve().parents[1]
src=base/'orchid/app/src/main/java/fr/ilamartin/orchid'
def run(cmd):subprocess.run([str(x) for x in cmd],check=True)
with tempfile.TemporaryDirectory(prefix='orchid-audio-') as tmp:
 root=Path(tmp)
 scheduler=root/'scheduler';scheduler.mkdir()
 run(['javac','-encoding','UTF-8','-d',scheduler,src/'OutputScheduler.java',base/'tests/OutputSchedulerTest.java'])
 run(['java','-cp',scheduler,'OutputSchedulerTest'])
 audio=root/'audio';audio.mkdir()
 run(['javac','-encoding','UTF-8','-d',audio]+[src/(n+'.java') for n in ['Bips','BipChoice','PcmWav']]+list((base/'tests/audio').rglob('*.java')))
 run(['java','-cp',audio,'fr.ilamartin.orchid.BipPlaybackTest',base/'orchid/app/src/main/res/raw'])
 voice=root/'voice';voice.mkdir()
 run(['javac','-encoding','UTF-8','-d',voice]+[src/(n+'.java') for n in ['OfflineVoice','SoundOutputs','OutputScheduler']]+list((base/'tests/voice').rglob('*.java')))
 for suite in ['OfflineVoiceTest','SoundOutputsTest']:
  run(['java','-cp',voice,'fr.ilamartin.orchid.'+suite])
