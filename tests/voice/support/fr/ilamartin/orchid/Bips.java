package fr.ilamartin.orchid;
import android.content.Context;import java.util.*;
final class Bips {
 static final List<Runnable> callbacks=new ArrayList<>();
 static final class Playback {boolean cancelled;void cancel(){cancelled=true;}}
 static Playback play(Context c,String bip,Runnable done){callbacks.add(done);return new Playback();}
 static void finishOne(int index){callbacks.get(index).run();}
}
