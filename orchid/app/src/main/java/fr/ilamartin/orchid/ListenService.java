package fr.ilamartin.orchid;
import android.app.*;import android.content.*;import android.content.pm.ServiceInfo;import android.media.*;import android.os.*;import org.json.*;import org.vosk.*;import java.util.*;
public class ListenService extends Service {
 static final String START="fr.ilamartin.orchid.START",STOP="fr.ilamartin.orchid.STOP",TEST="fr.ilamartin.orchid.TEST";
 static volatile boolean alive=false;
 private volatile boolean active=false;private volatile AudioRecord recorder;private Thread worker;private SoundOutputs outputs;private PowerManager.WakeLock wake;
 @Override public void onCreate(){super.onCreate();alive=true;outputs=new SoundOutputs(this,()->{if(!active)stopSelf();});NotificationManager nm=getSystemService(NotificationManager.class);nm.createNotificationChannel(new NotificationChannel("orchid_listen","OrchiD · microphone",NotificationManager.IMPORTANCE_LOW));}
 @Override public int onStartCommand(Intent i,int flags,int startId){if(i==null){stopSelf();return START_NOT_STICKY;}if(STOP.equals(i.getAction())){Store.status(this,"Écoute arrêtée");stopListening();return START_NOT_STICKY;}if(TEST.equals(i.getAction())){try{Entry e=Entry.from(new JSONObject(i.getStringExtra("entry")));announce(e,true);}catch(Exception ignored){}return START_NOT_STICKY;}if(active)return START_NOT_STICKY;if(Models.importing||Models.downloading){Store.status(this,"Installation d’un modèle en cours. Attendez sa fin.");stopSelf();return START_NOT_STICKY;}
  Intent stop=new Intent(this,ListenService.class).setAction(STOP);PendingIntent pi=PendingIntent.getService(this,1,stop,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);Notification n=new Notification.Builder(this,"orchid_listen").setSmallIcon(R.drawable.ic_notification_orchid).setColor(0xffac239f).setContentTitle("Microphone actif").setOnlyAlertOnce(true).setContentIntent(PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT)).addAction(new Notification.Action.Builder(null,"Arrêter",pi).build()).setOngoing(true).build();
  try{if(Build.VERSION.SDK_INT>=29)startForeground(41,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);else startForeground(41,n);}catch(Exception e){Store.status(this,"Autorisez le microphone puis réessayez");stopSelf();return START_NOT_STICKY;}
  active=true;Store.prefs(this).edit().putBoolean("running",true).apply();worker=new Thread(this::listen,"orchid-local");worker.start();return START_NOT_STICKY;
 }
 private void listen(){Map<String,String> grammars=new HashMap<>();Map<String,Model> models=new LinkedHashMap<>();Map<String,Recognizer> engines=new LinkedHashMap<>();DetectionGate gate=new DetectionGate();String listeningLanguages="",lastPolicy="";ConfidenceRules.Stability stability=new ConfidenceRules.Stability();long audioBytes=0;
  try{List<Entry> entries=Store.entries(this);entries.removeIf(e->!e.enabled||e.keyword.trim().isEmpty());Set<String> langs=new LinkedHashSet<>();
   int min=AudioRecord.getMinBufferSize(16000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);recorder=new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,16000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,Math.max(min,8192));if(recorder.getState()!=AudioRecord.STATE_INITIALIZED)throw new IllegalStateException("Microphone indisponible");wake=((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"OrchiD:local");wake.acquire(12*60*60*1000L);recorder.startRecording();Store.status(this,"Écoute locale · "+"liste modifiable pendant l’écoute");byte[] buffer=new byte[2560];
   while(active){entries=Store.entries(this);entries.removeIf(e->!e.enabled||e.keyword.trim().isEmpty());langs.clear();for(Entry e:entries){if(Models.ready(this,e.listenLang))langs.add(e.listenLang);}
for(String lang:new ArrayList<>(engines.keySet()))if(!langs.contains(lang)){engines.remove(lang).close();models.remove(lang).close();grammars.remove(lang);gate.finish(lang);stability.reset();}
for(String lang:langs){
 List<String> keys=new ArrayList<>();for(Entry e:entries)if(e.listenLang.equals(lang))keys.add(e.keyword);
 String grammar=new JSONArray(KeywordGrammar.words(keys,Store.contrast(this,lang))).toString();
 if(!grammar.equals(grammars.get(lang))){
  if(!models.containsKey(lang)){Store.status(this,"Chargement du modèle "+lang+"…");models.put(lang,new Model(Models.directory(this,lang).getAbsolutePath()));}
  Recognizer replacement;
  try{replacement=new Recognizer(models.get(lang),16000f,grammar);replacement.setWords(true);replacement.setPartialWords(true);}catch(Exception ex){throw new IllegalStateException("Modèle "+lang+" incompatible avec l’écoute ciblée : utilisez un modèle Vosk small à vocabulaire modifiable",ex);}
  Recognizer old=engines.put(lang,replacement);if(old!=null)old.close();grammars.put(lang,grammar);stability.reset();

 }
}
String currentLanguages=languageLabels(langs);if(!currentLanguages.equals(listeningLanguages)){listeningLanguages=currentLanguages;if(!langs.isEmpty())Store.status(this,"Langue(s) d’écoute : "+currentLanguages);}
if(langs.isEmpty()){String idle=entries.isEmpty()?"Écoute en attente · ajoutez ou activez un mot-clé":"Écoute en attente · installez le modèle des mots-clés";if(!idle.equals(Store.prefs(this).getString("status","")))Store.status(this,idle);}
int n=recorder.read(buffer,0,buffer.length);if(n<=0){if(active)throw new IllegalStateException("Le microphone ne fournit plus de son");break;}
    audioBytes+=n;long audioMs=audioBytes/32;
    boolean fast="fast".equals(Store.prefs(this).getString("detection_mode","reliable"));
    int threshold=ConfidenceRules.clamp(Store.prefs(this).getInt("confidence_threshold",ConfidenceRules.DEFAULT_THRESHOLD));
    String policy=fast+":"+threshold;if(!policy.equals(lastPolicy)){stability.reset();lastPolicy=policy;}
    for(Map.Entry<String,Recognizer> item:engines.entrySet()){
     Recognizer engine=item.getValue();String lang=item.getKey();boolean full=engine.acceptWaveForm(buffer,n);
     JSONObject result=new JSONObject(full?engine.getResult():engine.getPartialResult());
     String text=result.optString(full?"text":"partial","");
     if(!text.isEmpty())Store.prefs(this).edit().putString("transcript",lang+" · "+(full?"confirmé":"provisoire")+" · "+text).apply();
     List<ConfidenceRules.Word> words=scoredWords(result.optJSONArray(full?"result":"partial_result"));
     for(Entry e:entries){
      if(!e.listenLang.equals(lang))continue;
      String candidateId=lang+"/"+e.id;ConfidenceRules.Match match=ConfidenceRules.match(words,e.keyword);
      boolean passes=ConfidenceRules.passes(match,threshold);
      boolean stable=stability.ready(candidateId,passes?match:null,audioMs);
      if(match!=null){String verdict=!passes?"rejeté · seuil insuffisant":full?"confirmé":fast?(stable?"stabilisé":"en stabilisation"):"en attente de confirmation";
       Store.prefs(this).edit().putString("candidate_score",match.matchedAlias+" · "+lang+" · score "+String.format(Locale.FRANCE,"%.2f",match.score)+" · "+verdict).apply();}
      if(passes&&(full||(fast&&stable))&&gate.accept(lang,e.id,SystemClock.elapsedRealtime(),Store.prefs(this).getInt("cooldown",0))){
       Store.prefs(this).edit().putString("last",match.matchedAlias+" → "+e.spoken).putString("accepted_score",match.matchedAlias+" · "+lang+" · score "+String.format(Locale.FRANCE,"%.2f",match.score)+" · "+(full?"confirmé":"provisoire stabilisé")).apply();announce(e);
      }
     }
     if(full){gate.finish(lang);for(Entry e:entries)if(e.listenLang.equals(lang))stability.clear(lang+"/"+e.id);}
    }
   }
  }catch(OutOfMemoryError e){Store.status(this,"Mémoire insuffisante : activez moins de langues");}catch(Exception e){if(active)Store.status(this,"Arrêt : "+e.getMessage());}
  finally{active=false;AudioRecord r=recorder;recorder=null;if(r!=null){try{r.stop();}catch(Exception ignored){}r.release();}for(Recognizer e:engines.values())try{e.close();}catch(Exception ignored){}for(Model m:models.values())try{m.close();}catch(Exception ignored){}if(wake!=null&&wake.isHeld())wake.release();Store.prefs(this).edit().putBoolean("running",false).apply();stopForeground(true);stopSelf();}
 }
 private static List<ConfidenceRules.Word> scoredWords(JSONArray array){
  List<ConfidenceRules.Word> words=new ArrayList<>();if(array==null)return words;
  for(int i=0;i<array.length();i++){JSONObject word=array.optJSONObject(i);if(word==null)continue;words.add(new ConfidenceRules.Word(word.optString("word",""),word.optDouble("conf",Double.NaN),word.optDouble("start",0),word.optDouble("end",0)));}return words;
 }
 private static String languageLabels(Set<String> langs){List<String> labels=new ArrayList<>();for(String lang:langs)labels.add(lang.substring(0,1).toUpperCase(Locale.ROOT)+lang.substring(1));return String.join(", ",labels);}
 private void announce(Entry e){announce(e,Store.prefs(this).getBoolean("announce",true));}
 private void announce(Entry e,boolean announce){outputs.play(BipChoice.resolve(e.bip,Store.prefs(this).getString("bip","arcade")),announce?e.spoken:"",e.voiceLang,Store.prefs(this).getString("output_mode","overlap"));}
 private void stopListening(){active=false;if(outputs!=null)outputs.cancelAll();AudioRecord r=recorder;if(r!=null)try{r.stop();}catch(Exception ignored){}if(worker==null){Store.prefs(this).edit().putBoolean("running",false).apply();stopSelf();}}
 @Override public void onDestroy(){alive=false;stopListening();outputs.close();super.onDestroy();}
 @Override public IBinder onBind(Intent intent){return null;}
}
