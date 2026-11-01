package fr.ilamartin.orchid;
import android.content.Context;import android.speech.tts.TextToSpeech;
import java.io.*;import java.nio.file.*;import java.util.*;
public class OfflineVoiceTest {
 static void ok(boolean expected,String msg){if(!expected)throw new AssertionError(msg);}
 static void yes(OfflineVoice.Result r,String msg){ok(r!=null&&r.file!=null&&r.error==null,msg);}
 static void no(OfflineVoice.Result r,String msg){ok(r!=null&&r.file==null&&r.error!=null,msg);}
 public static void main(String[] args)throws Exception {
  File tmp=Files.createTempDirectory("orchid-voice-test-").toFile();
  List<String> statuses=new ArrayList<>();OfflineVoice v=new OfflineVoice(new Context(tmp),statuses::add);
  List<OfflineVoice.Result> results=new ArrayList<>();
  v.render("un","fr-FR",()->true,results::add);
  v.render("two","en-US",()->true,results::add);
  TextToSpeech fake=TextToSpeech.instance;
  ok(fake.synthCount==1&&fake.maxPending==1,"Syntheses must serialize, but they can later play together");
  fake.success();ok(results.size()==1&&fake.synthCount==2,"Second job begins after first finishes");yes(results.get(0),"First WAV exists");fake.success();yes(results.get(1),"Second WAV exists");
  ok(fake.maxPending==1,"No overlapping TTS requests");
  int n=fake.synthCount;v.render("un","fr-FR",()->true,results::add);
  ok(fake.synthCount==n,"Cached utterance must not be resynthesized");yes(results.get(2),"Cached WAV must exist");
  v.render("retry","fr-FR",()->true,results::add);fake.error();
  ok(fake.synthCount==n+2,"Transient TTS failure must retry once");fake.success();yes(results.get(3),"Retry should recover spoken response");
  v.render("fail","fr-FR",()->true,results::add);fake.error();fake.error();
  no(results.get(4),"Persistent TTS failure must be reported, not silently swallowed");
  v.render("missing","ja-JP",()->true,results::add);no(results.get(5),"Missing offline voice is explained");
  v.render("cancel","fr-FR",()->true,results::add);
  String oldId=fake.lastId;v.cancelPending();fake.error();
  ok(results.size()==6,"Cancelled request must not emit a ghost completion");
  v.render("after cancel","fr-FR",()->true,results::add);fake.success();yes(results.get(6),"New job works after cancellation");
  ok(!oldId.equals(fake.lastId),"Cancelled utterance ID ignored");
  v.close();ok(!tmp.toPath().resolve("voice-").toFile().exists(),"close executed");
  System.out.println("OK: 1 TTS synthesis at a time, FIFO, file cache, transient retry, persistent failure diagnostic, offline voice missing, cancellation and restart.");
 }
}
