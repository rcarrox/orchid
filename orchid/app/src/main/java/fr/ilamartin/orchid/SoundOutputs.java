package fr.ilamartin.orchid;
import android.content.Context;import android.media.*;import android.os.*;import java.io.File;
/** Each job keeps its beep before its voice; separate jobs may overlap. Main thread only. */
final class SoundOutputs {
 private final Context context;private final Handler main=new Handler(Looper.getMainLooper());private final OfflineVoice voice;private final OutputScheduler scheduler;private boolean closed;
 SoundOutputs(Context c,Runnable idle){context=c;voice=new OfflineVoice(c,s->Store.status(c,s));scheduler=new OutputScheduler(idle);}
 void play(String bip,String text,String tag,String mode){main.post(()->{if(closed)return;OutputScheduler.Mode selected=OutputScheduler.Mode.from(mode);if(selected==OutputScheduler.Mode.REPLACE){scheduler.cancelAll();voice.cancelPending();}scheduler.submit(new Job(bip,text,tag),selected);});}
 void cancelAll(){main.post(()->{if(closed)return;scheduler.cancelAll();voice.cancelPending();});}
 void close(){closed=true;scheduler.close();voice.close();}
 private void report(String message){
  android.content.SharedPreferences prefs=Store.prefs(context);
  android.content.SharedPreferences.Editor change=prefs.edit().putString("audio_status",message);
  if(message.startsWith("Annonce absente")||message.startsWith("Lecture de l’annonce impossible")||message.startsWith("Lecture de l’annonce indisponible")||message.startsWith("Annonce impossible à démarrer")){
   change.putString("audio_error",message).putInt("audio_failure_count",prefs.getInt("audio_failure_count",0)+1);
  }
  change.apply();
 }
 private final class Job implements OutputScheduler.Task {
  final String bip,text,tag;boolean cancelled,finished,beepDone,voiceReady,playerStarted;Runnable done;Bips.Playback beep;MediaPlayer player;File clip;
  Job(String bip,String text,String tag){this.bip=bip;this.text=text;this.tag=tag;}
  public void start(Runnable complete){done=complete;voiceReady=text.trim().isEmpty();report(voiceReady?"Bip seul (annonce désactivée ou texte vide)":"Synthèse de l’annonce demandée");
   if(!voiceReady)voice.render(text,tag,()->!cancelled&&!finished&&!closed,result->{if(cancelled||finished){voice.release(result.file);return;}clip=result.file;voiceReady=true;if(clip==null)report("Annonce absente : "+result.error);advance();});
   beep=Bips.play(context,bip,()->main.post(()->{if(cancelled||finished)return;beepDone=true;advance();}));
  }
  private void advance(){if(cancelled||finished||!beepDone||!voiceReady||playerStarted)return;if(clip==null){finish();return;}playerStarted=true;
   try{player=new MediaPlayer();player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build());player.setDataSource(clip.getAbsolutePath());player.setOnPreparedListener(p->{if(!cancelled&&!finished)try{p.start();report("Annonce en lecture");}catch(RuntimeException ex){report("Annonce impossible à démarrer : "+ex.getMessage());finish();}});player.setOnCompletionListener(p->{if(!cancelled&&!finished){report("Annonce lue jusqu’à la fin");finish();}});player.setOnErrorListener((p,what,extra)->{if(!cancelled&&!finished){report("Lecture de l’annonce indisponible ("+what+" / "+extra+")");finish();}return true;});player.prepareAsync();}
   catch(Exception e){report("Lecture de l’annonce impossible : "+e.getMessage());finish();}
  }
  private void cleanup(){if(beep!=null)beep.cancel();if(player!=null){try{player.reset();player.release();}catch(RuntimeException ignored){}player=null;}voice.release(clip);clip=null;}
  private void finish(){if(finished||cancelled)return;finished=true;cleanup();done.run();}
  public void cancel(){if(cancelled||finished)return;cancelled=true;cleanup();}
 }
}
