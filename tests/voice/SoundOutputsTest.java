package fr.ilamartin.orchid;
import android.content.Context;import android.media.MediaPlayer;import android.speech.tts.TextToSpeech;
import java.nio.file.*;
public class SoundOutputsTest {
 static void ok(boolean b,String msg){if(!b)throw new AssertionError(msg);}
 public static void main(String[] args)throws Exception {
  int[] idle={0};SoundOutputs s=new SoundOutputs(new Context(Files.createTempDirectory("orchid-playback-").toFile()),()->idle[0]++);
  s.play("arcade","bonjour","fr-FR","overlap");
  s.play("arcade","goodbye","en-US","overlap");
  ok(Bips.callbacks.size()==2,"Both beeps start immediately in overlap mode");
  Bips.finishOne(0);Bips.finishOne(1);
  ok(MediaPlayer.players.size()==0,"A beep finishing must not falsely complete an unfinished voice");
  TextToSpeech tts=TextToSpeech.instance;
  ok(tts.synthCount==1,"Only one voice synthesis active in overlap mode");
  tts.success();ok(tts.synthCount==2&&MediaPlayer.players.size()==1,"First voice begins playing; second starts synthesizing");
  tts.success();ok(MediaPlayer.players.size()==2,"Both independently prepared announcements can play concurrently");
  ok(MediaPlayer.players.get(0).started&&MediaPlayer.players.get(1).started,"Both audio players active");
  MediaPlayer.players.get(0).complete();ok(idle[0]==0,"Second announcement must continue");
  MediaPlayer.players.get(1).complete();ok(idle[0]==1,"Callback after final spoken announcement");
  s.play("arcade","broken","fr-FR","overlap");Bips.finishOne(2);
  tts.error();tts.error();
  ok(Store.info.get("audio_status").contains("Annonce absente"),"Persisted diagnostic on absent voice");
  ok(Store.info.get("audio_error").contains("Annonce absente")&&Store.integers.get("audio_failure_count")==1,"Failure retained independently of success");
  ok(idle[0]==2,"Failed voice must not leave scheduler stuck");
  s.play("none","","fr-FR","overlap");Bips.finishOne(3);
  ok(idle[0]==3,"Bip-only response remains functional");ok(Store.integers.get("audio_failure_count")==1,"Successful bip-only does not clear error history");s.close();
  System.out.println("OK: beep before voice, TTS queue during playback overlap, simultaneous players, failure diagnostic, idle and bip-only.");
 }
}
