package fr.ilamartin.orchid;

import android.content.Context;
import android.os.*;
import android.speech.tts.*;
import java.io.*;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * One synthesis at a time: Android TTS is a shared, stateful engine (setVoice),
 * even though the completed clips can later play simultaneously.
 * All state transitions happen on main to prevent callback/cancellation races.
 */
final class OfflineVoice {
 static final class Result {
  final File file;final String error;
  private Result(File f,String e){file=f;error=e;}
  static Result ok(File f){return new Result(f,null);}
  static Result fail(String reason){return new Result(null,reason);}
 }
 private static final int MIN_TIMEOUT_MS=15000;
 private static final int MAX_ATTEMPTS=2;
 private final Handler main=new Handler(Looper.getMainLooper());
 private final Consumer<String> status;
 private final File folder;
 private final Queue<Request> waiting=new ArrayDeque<>();
 private final Map<String,File> cache=new HashMap<>();
 private TextToSpeech tts;
 private boolean ready,failed,closed;
 private Request current;
 private String currentId;
 private static final class Request {
  final String text,tag;
  final BooleanSupplier needed;
  final Consumer<Result> done;
  String key;
  Voice voice;
  File file;
  int attempts;
  Request(String t,String l,BooleanSupplier n,Consumer<Result> callback){text=t;tag=l;needed=n;done=callback;}
 }
 OfflineVoice(Context c,Consumer<String> report){
  status=report;folder=new File(c.getCacheDir(),"voice-"+UUID.randomUUID());folder.mkdirs();
  tts=new TextToSpeech(c,result->main.post(()->{
   if(closed)return;
   ready=result==TextToSpeech.SUCCESS;
   failed=!ready;
   if(failed)status.accept("Moteur vocal Android indisponible");
   pump();
  }));
  tts.setOnUtteranceProgressListener(new UtteranceProgressListener(){
   @Override public void onStart(String id){}
   @Override public void onDone(String id){complete(id,true);}
   @Override public void onError(String id){complete(id,false);}
   @Override public void onError(String id,int errorCode){complete(id,false);}
   @Override public void onStop(String id,boolean interrupted){complete(id,false);}
  });
  main.postDelayed(()->{
   if(closed||ready||failed)return;
   failed=true;status.accept("Initialisation vocale trop longue. Relancez l’écoute.");pump();
  },15000);
 }
 void render(String text,String tag,BooleanSupplier needed,Consumer<Result> done){
  main.post(()->{
   if(closed||!needed.getAsBoolean()){done.accept(Result.fail("réponse annulée"));return;}
   waiting.add(new Request(text,tag,needed,done));pump();
  });
 }
 private void pump(){
  if(closed||current!=null)return;
  if(!ready&&!failed)return;
  while(!waiting.isEmpty()){
   Request r=waiting.remove();
   if(!r.needed.getAsBoolean()){r.done.accept(Result.fail("réponse annulée"));continue;}
   if(!ready){r.done.accept(Result.fail("moteur vocal Android indisponible"));continue;}
   try{
    Locale wanted=Locale.forLanguageTag(r.tag);
    Voice found=null;
    Set<Voice> voices=tts.getVoices();
    if(voices!=null)for(Voice v:voices){
     if(v.isNetworkConnectionRequired()||!v.getLocale().getLanguage().equals(wanted.getLanguage())||
       (v.getFeatures()!=null&&v.getFeatures().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)))continue;
     if(found==null||v.getLocale().toLanguageTag().equalsIgnoreCase(r.tag))found=v;
     if(v.getLocale().toLanguageTag().equalsIgnoreCase(r.tag))break;
    }
    if(found==null){
     String error="voix hors ligne manquante pour "+r.tag;
     status.accept("Installez une voix locale "+r.tag+" dans les réglages vocaux Android");
     r.done.accept(Result.fail(error));continue;
    }
    r.voice=found;
    r.key=found.getName()+"\n"+r.tag+"\n"+r.text;
    File cached=cache.get(r.key);
    if(cached!=null&&cached.isFile()&&cached.length()>0){r.done.accept(Result.ok(cached));continue;}
    cache.remove(r.key);
    current=r;
    synthesizeCurrent();
    return;
   }catch(RuntimeException ex){
    String reason="préparation vocale : "+ex.getClass().getSimpleName();
    status.accept("Synthèse vocale impossible : "+reason);
    r.done.accept(Result.fail(reason));
   }
  }
 }
 private void synthesizeCurrent(){
  Request r=current;
  if(r==null||closed)return;
  if(!r.needed.getAsBoolean()){resolveCurrent(null,"réponse annulée");return;}
  r.attempts++;
  try{
   if(r.voice==null||tts.setVoice(r.voice)==TextToSpeech.ERROR){retryOrFail("impossible d’activer la voix locale "+r.tag);return;}
   r.file=File.createTempFile("announcement-",".wav",folder);
   String id=UUID.randomUUID().toString();currentId=id;
   Bundle params=new Bundle();params.putString(TextToSpeech.Engine.KEY_FEATURE_EMBEDDED_SYNTHESIS,"true");
   int timeout=Math.min(35000,Math.max(MIN_TIMEOUT_MS,5000+r.text.length()*125));
   main.postDelayed(()->{
    if(current==r&&id.equals(currentId))retryOrFail("délai de synthèse vocale dépassé");
   },timeout);
   if(tts.synthesizeToFile(r.text,params,r.file,id)==TextToSpeech.ERROR)retryOrFail("moteur vocal : synthèse refusée");
  }catch(Exception ex){retryOrFail("synthèse vocale : "+ex.getClass().getSimpleName());}
 }
 private void complete(String id,boolean success){main.post(()->{
  if(closed||current==null||!id.equals(currentId))return;
  if(!success||current.file==null||!current.file.isFile()||current.file.length()==0)
   retryOrFail("fichier vocal absent ou synthèse interrompue");
  else resolveCurrent(current.file,null);
 });}
 private void retryOrFail(String reason){
  Request r=current;if(r==null)return;
  currentId=null; // Ignore late callbacks from a failed attempt.
  try{tts.stop();}catch(RuntimeException ignored){}
  if(r.file!=null){r.file.delete();r.file=null;}
  if(r.attempts<MAX_ATTEMPTS&&r.needed.getAsBoolean()){
   status.accept("Nouvelle tentative de synthèse vocale");
   synthesizeCurrent();
  }else{
   status.accept("Annonce non produite : "+reason);
   resolveCurrent(null,reason);
  }
 }
 private void resolveCurrent(File file,String error){
  Request r=current;
  if(r==null)return;
  current=null;currentId=null;
  if(file!=null&&cache.size()<64)cache.putIfAbsent(r.key,file);
  r.done.accept(file==null?Result.fail(error):Result.ok(file));
  pump();
 }
 void cancelPending(){
  // Called from the service main thread. Cancel synthesis, not existing playback.
  waiting.clear();currentId=null;
  Request r=current;current=null;
  try{if(tts!=null)tts.stop();}catch(RuntimeException ignored){}
  if(r!=null&&r.file!=null)r.file.delete();
 }
 void release(File file){if(file!=null&&!cache.containsValue(file))file.delete();}
 void close(){
  if(closed)return;closed=true;cancelPending();
  if(tts!=null)tts.shutdown();
  cache.clear();File[] files=folder.listFiles();if(files!=null)for(File f:files)f.delete();folder.delete();
 }
}
