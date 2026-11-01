package android.speech.tts;
import java.util.*;
public class Voice {
 private final String name;private final Locale locale;private final boolean network;
 public Voice(String n,Locale l,boolean net){name=n;locale=l;network=net;}
 public String getName(){return name;}
 public Locale getLocale(){return locale;}
 public boolean isNetworkConnectionRequired(){return network;}
 public Set<String> getFeatures(){return Collections.emptySet();}
}
