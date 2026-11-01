package fr.ilamartin.orchid;
import java.util.*;
/** A partial result and its final result belong to the same utterance. */
public final class DetectionGate {
 private final Map<String,Set<String>> seen=new HashMap<>();private final Map<String,Long> recent=new HashMap<>();
 public boolean accept(String language,String id,long now,int seconds){Set<String> current=seen.computeIfAbsent(language,k->new HashSet<>());if(current.contains(id)||!KeywordRules.canTrigger(now,recent.getOrDefault(id,0L),KeywordRules.clampInterval(seconds)))return false;current.add(id);recent.put(id,now);return true;}
 public void finish(String language){seen.remove(language);}
 public void resetUtterances(){seen.clear();}
}
