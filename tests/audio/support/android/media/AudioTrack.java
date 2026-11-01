package android.media;
import java.util.concurrent.*;import java.util.concurrent.atomic.*;
/** State-machine double for Android's documented static track lifecycle; no hardware. */
public class AudioTrack {
 public static final int STATE_UNINITIALIZED=0,STATE_INITIALIZED=1,STATE_NO_STATIC_DATA=2,MODE_STATIC=0;
 public static volatile boolean failCreate,shortWrite;public static final AtomicInteger plays=new AtomicInteger(),releases=new AtomicInteger(),pauses=new AtomicInteger(),writes=new AtomicInteger(),active=new AtomicInteger(),maxActive=new AtomicInteger();
 public static volatile CountDownLatch played=new CountDownLatch(1);private int state;private boolean released,playing;
 public AudioTrack(int stream,int rate,int channel,int format,int bytes,int mode){state=failCreate?STATE_UNINITIALIZED:STATE_NO_STATIC_DATA;}
 public int getState(){return state;}
 public int write(short[] pcm,int offset,int count){if(state==STATE_UNINITIALIZED)return -3;writes.incrementAndGet();state=STATE_INITIALIZED;return shortWrite?count-1:count;}
 public void play(){if(state!=STATE_INITIALIZED)throw new IllegalStateException("play before data");playing=true;plays.incrementAndGet();maxActive.accumulateAndGet(active.incrementAndGet(),Math::max);played.countDown();}
 public void pause(){pauses.incrementAndGet();}
 public void flush(){}
 public void release(){if(released)throw new AssertionError("Double release");released=true;releases.incrementAndGet();if(playing)active.decrementAndGet();}
 public static void reset(){failCreate=false;shortWrite=false;plays.set(0);releases.set(0);pauses.set(0);writes.set(0);active.set(0);maxActive.set(0);played=new CountDownLatch(1);}
}
