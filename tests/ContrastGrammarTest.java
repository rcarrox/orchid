import fr.ilamartin.orchid.*;
import java.util.*;
public class ContrastGrammarTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args){
  List<String> keys=Arrays.asList("mouly;Georges;Michel");
  List<String> grammar=KeywordGrammar.words(keys,KeywordGrammar.defaultContrast("fr"));
  ok(grammar.containsAll(Arrays.asList("oui","non","mouly","georges","michel","[unk]")));
  ok(!KeywordRules.matches("oui","mouly"));
  ok(KeywordRules.matches("oui","oui")); // A real active keyword still wins normally.
  ok(grammar.equals(KeywordGrammar.words(Arrays.asList("Michel;Georges;mouly"),"non;oui;oui")));
  ok(!KeywordGrammar.words(keys,"").contains("oui"));
  ok(!KeywordGrammar.words(keys,KeywordGrammar.defaultContrast("en")).contains("oui"));
  ok(!KeywordGrammar.words(keys,"oui;bonjour").equals(grammar));
  ok(KeywordGrammar.words(Collections.emptyList(),"oui").contains("[unk]"));
  System.out.println("OK: competing words, per-language defaults, stable vocabulary, live edits and explicit removal. Acoustic accuracy not tested.");
 }
}
