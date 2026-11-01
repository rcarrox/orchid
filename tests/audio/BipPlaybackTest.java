package fr.ilamartin.orchid;
import android.content.Context;import android.media.AudioTrack;import java.io.*;import java.util.concurrent.*;import java.util.concurrent.atomic.*;
public class BipPlaybackTest {
 static void ok(boolean b,String message){if(!b)throw new AssertionError(message+" / "+Store.error);}
 static void reset(){AudioTrack.reset();Store.error="";}
 static void waitFor(CountDownLatch done)throws Exception{ok(done.await(5,TimeUnit.SECONDS),"Playback completion timed out");}
 public static void main(String[] args)throws Exception {
  Context c=new Context(new File(args[0]));
  for(String id:new String[]{"bip_x1","soft_steps","soft_halo","soft_crystal","soft_shore"}){reset();CountDownLatch done=new CountDownLatch(1);Bips.play(c,id,done::countDown);waitFor(done);ok(AudioTrack.plays.get()==1&&AudioTrack.writes.get()==1&&AudioTrack.releases.get()==1&&Store.error.isEmpty(),"Static data must be written before initialized-state check: "+id);}
  reset();CountDownLatch none=new CountDownLatch(1);Bips.play(c,"none",none::countDown);waitFor(none);ok(AudioTrack.plays.get()==0&&Store.error.isEmpty(),"None must stay silent");
  reset();AudioTrack.failCreate=true;CountDownLatch bad=new CountDownLatch(1);Bips.play(c,"soft_halo",bad::countDown);waitFor(bad);ok(AudioTrack.plays.get()==0&&AudioTrack.writes.get()==0&&AudioTrack.releases.get()==1&&!Store.error.isEmpty(),"Creation failure must release and complete");
  reset();AudioTrack.shortWrite=true;CountDownLatch shortDone=new CountDownLatch(1);Bips.play(c,"soft_halo",shortDone::countDown);waitFor(shortDone);ok(AudioTrack.plays.get()==0&&AudioTrack.releases.get()==1&&!Store.error.isEmpty(),"Short write must not play");
  reset();AtomicInteger callbacks=new AtomicInteger();CountDownLatch cancelled=new CountDownLatch(1);Bips.Playback playback=Bips.play(c,"ding_dong_strident",()->{callbacks.incrementAndGet();cancelled.countDown();});ok(AudioTrack.played.await(2,TimeUnit.SECONDS),"Start signal");playback.cancel();waitFor(cancelled);ok(AudioTrack.pauses.get()==1&&AudioTrack.releases.get()==1&&callbacks.get()==1,"Cancel must stop and complete exactly once");
  reset();CountDownLatch mixed=new CountDownLatch(2);Bips.play(c,"soft_halo",mixed::countDown);Bips.play(c,"soft_shore",mixed::countDown);waitFor(mixed);ok(AudioTrack.plays.get()==2&&AudioTrack.maxActive.get()==2&&AudioTrack.releases.get()==2,"Independent overlapping tracks");
  System.out.println("OK: static lifecycle regression, legacy beep and four WAVs, none, creation/short-write errors, release, cancel callback and independent overlap. AudioTrack simulated; no physical audio test.");
 }
}
