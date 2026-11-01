package fr.ilamartin.orchid;
import java.util.*;
/** Stable per-language vocabulary, including an unknown-speech escape. */
public final class KeywordGrammar {
 public static List<String> words(Collection<String> keywords){
  Set<String> unique=new TreeSet<>();
  for(String entry:keywords)for(String alias:entry.split(";")){
   String word=alias.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+"," ");
   if(!word.isEmpty()&&!word.equals("[unk]"))unique.add(word);
  }
  List<String> result=new ArrayList<>(unique);result.add("[unk]");return result;
 }
 public static List<String> words(Collection<String> keywords,String contrast){List<String> all=new ArrayList<>(keywords);all.add(contrast==null?"":contrast);return words(all);}
 public static String defaultContrast(String language){switch(language){case "fr":return "oui ; non";case "en":return "yes ; no";case "zh":return "是 ; 不是";default:return "";}}
 private KeywordGrammar(){}
}
