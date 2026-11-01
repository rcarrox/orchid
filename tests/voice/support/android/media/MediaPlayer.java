package android.media;
import java.util.*;
public class MediaPlayer {
 public interface OnPreparedListener {void onPrepared(MediaPlayer player);}
 public interface OnCompletionListener {void onCompletion(MediaPlayer player);}
 public interface OnErrorListener {boolean onError(MediaPlayer player,int what,int extra);}
 public static final List<MediaPlayer> players=new ArrayList<>();
 private OnPreparedListener prepared;private OnCompletionListener completed;private OnErrorListener error;
 public boolean started,finished;
 public MediaPlayer(){players.add(this);}
 public void setAudioAttributes(AudioAttributes attrs){}
 public void setDataSource(String file){}
 public void setOnPreparedListener(OnPreparedListener p){prepared=p;}
 public void setOnCompletionListener(OnCompletionListener c){completed=c;}
 public void setOnErrorListener(OnErrorListener e){error=e;}
 public void prepareAsync(){prepared.onPrepared(this);}
 public void start(){started=true;}
 public void reset(){}
 public void release(){finished=true;}
 public void complete(){completed.onCompletion(this);}
 public void fail(){error.onError(this,1,2);}
}
