package fr.ilamartin.orchid;
import java.text.Normalizer;import java.util.Locale;
public final class KeywordRules {
 public static String normalize(String s){return Normalizer.normalize(s==null?"":s,Normalizer.Form.NFKC).toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+"," ").trim().replaceAll("\\s+"," ");}
 public static boolean matches(String phrase,String keyword){String p=normalize(phrase),k=normalize(keyword);if(k.isEmpty())return false;boolean cjk=k.codePoints().anyMatch(c->Character.UnicodeScript.of(c)==Character.UnicodeScript.HAN||Character.UnicodeScript.of(c)==Character.UnicodeScript.HIRAGANA||Character.UnicodeScript.of(c)==Character.UnicodeScript.KATAKANA);if(cjk)return p.replace(" ","").contains(k.replace(" ",""));return (" "+p+" ").contains(" "+k+" ");}
 public static int clampInterval(int seconds){return Math.max(0,Math.min(3600,seconds));}
 public static boolean canTrigger(long now,long previous,int seconds){return previous<=0||now-previous>=seconds*1000L;}
 private KeywordRules(){}
}
