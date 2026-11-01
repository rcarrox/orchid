package fr.ilamartin.orchid;

import java.util.*;

/** Scores only the recognized words covering a keyword, never the whole phrase. */
public final class ConfidenceRules {
 public static final int DEFAULT_THRESHOLD=85;
 public static final long STABILITY_MS=350;
 public static final class Word {
  public final String text; public final double confidence,start,end;
  public Word(String text,double confidence,double start,double end){this.text=text;this.confidence=confidence;this.start=start;this.end=end;}
 }
 public static final class Match {
  public final double score; public final String identity,matchedAlias;
  Match(double score,String identity,String matchedAlias){this.score=score;this.identity=identity;this.matchedAlias=matchedAlias;}
 }
 public static int clamp(int value){return Math.max(0,Math.min(100,value));}
 public static boolean passes(Match match,int threshold){return match!=null&&match.score>=clamp(threshold)/100.0;}
 public static Match match(List<Word> words,String aliases){
  Match best=null;
  for(String alias:aliases.split(";")){
   String key=KeywordRules.normalize(alias);if(key.isEmpty())continue;
   boolean cjk=key.codePoints().anyMatch(c->Character.UnicodeScript.of(c)==Character.UnicodeScript.HAN||Character.UnicodeScript.of(c)==Character.UnicodeScript.HIRAGANA||Character.UnicodeScript.of(c)==Character.UnicodeScript.KATAKANA);
   StringBuilder text=new StringBuilder();List<Integer> owners=new ArrayList<>();
   if(!cjk){text.append(' ');owners.add(-1);}
   for(int i=0;i<words.size();i++){
    Word word=words.get(i);String normalized="[unk]".equals(word.text)?"\u0000":KeywordRules.normalize(word.text);
    if(cjk)normalized=normalized.replace(" ","");
    for(int j=0;j<normalized.length();j++){text.append(normalized.charAt(j));owners.add(i);}
    if(!cjk){text.append(' ');owners.add(-1);}
   }
   String needle=cjk?key.replace(" ",""):" "+key+" ";String haystack=text.toString();
   for(int at=haystack.indexOf(needle);at>=0;at=haystack.indexOf(needle,at+1)){
    double score=1;int first=-1,last=-1;boolean valid=true;
    for(int j=at;j<at+needle.length();j++){
     int index=owners.get(j);if(index<0)continue;
     Word word=words.get(index);double confidence=word.confidence;
     if(!Double.isFinite(confidence)||confidence<0||confidence>1){valid=false;break;}
     score=Math.min(score,confidence);if(first<0)first=index;last=index;
    }
    if(valid&&first>=0&&(best==null||score>best.score))best=new Match(score,key+":"+words.get(first).start+":"+words.get(last).end,key);
   }
  }
  return best;
 }
 /** Uses processed audio time so a stalled microphone cannot validate a partial. */
 public static final class Stability {
  private final Map<String,String> identities=new HashMap<>();private final Map<String,Long> since=new HashMap<>();
  public boolean ready(String id,Match match,long audioMs){
   if(match==null){clear(id);return false;}
   if(!match.identity.equals(identities.get(id))){identities.put(id,match.identity);since.put(id,audioMs);return false;}
   return audioMs-since.get(id)>=STABILITY_MS;
  }
  public void clear(String id){identities.remove(id);since.remove(id);}
  public void reset(){identities.clear();since.clear();}
 }
 private ConfidenceRules(){}
}
