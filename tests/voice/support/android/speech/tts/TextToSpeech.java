package android.speech.tts;
import android.content.Context;import android.os.Bundle;
import java.util.*;import java.io.*;
public class TextToSpeech {
 public interface OnInitListener {void onInit(int result);}
 public static final int SUCCESS=0,ERROR=-1;
 public static class Engine {public static final String KEY_FEATURE_NOT_INSTALLED="notInstalled",KEY_FEATURE_EMBEDDED_SYNTHESIS="embedded";}
 public static TextToSpeech instance;
 public int synthCount,stops,maxPending,active,failEnqueue;
 public String lastId;public File lastFile;public Voice currentVoice;
 private UtteranceProgressListener listener;
 public TextToSpeech(Context c,OnInitListener init){instance=this;init.onInit(SUCCESS);}
 public void setOnUtteranceProgressListener(UtteranceProgressListener l){listener=l;}
 public Set<Voice> getVoices(){return new LinkedHashSet<>(Arrays.asList(new Voice("french",Locale.FRANCE,false),new Voice("english",Locale.US,false),new Voice("network",Locale.FRANCE,true)));}
 public int setVoice(Voice voice){currentVoice=voice;return SUCCESS;}
 public int synthesizeToFile(String text,Bundle opts,File f,String id){
  synthCount++;active++;maxPending=Math.max(maxPending,active);
  if(failEnqueue-->0){active--;return ERROR;}
  lastId=id;lastFile=f;return SUCCESS;
 }
 public void success(){try(FileOutputStream s=new FileOutputStream(lastFile)){s.write(new byte[]{82,73,70,70,1});}catch(IOException e){throw new RuntimeException(e);}active--;listener.onDone(lastId);}
 public void error(){active--;listener.onError(lastId);}
 public int stop(){stops++;active=0;return SUCCESS;}
 public void shutdown(){}
}
