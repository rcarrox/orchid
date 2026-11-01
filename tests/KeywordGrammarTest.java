import fr.ilamartin.orchid.*;
import java.util.*;
public class KeywordGrammarTest {
 public static void main(String[] args){
  List<String> a=KeywordGrammar.words(Arrays.asList("Ray ; Alex","  BONJOUR   monsieur  ; ray", "été", ""));
  if(!a.equals(Arrays.asList("alex","bonjour monsieur","ray","été","[unk]")))throw new AssertionError(a);
  if(!a.equals(KeywordGrammar.words(Arrays.asList("été","ray;bonjour monsieur","alex;Ray"))))throw new AssertionError("Unnecessary rebuild on reorder");
  if(a.equals(KeywordGrammar.words(Arrays.asList("ray;alex"))))throw new AssertionError("Edit must update vocabulary");
  if(!KeywordGrammar.words(Collections.emptyList()).equals(Arrays.asList("[unk]")))throw new AssertionError("Empty list");
  if(!KeywordRules.matches("[unk] ray [unk]","ray")||KeywordRules.matches("[unk]","ray"))throw new AssertionError("Unknown speech matching");
  System.out.println("OK: aliases, phrases, accents, stable vocabulary, live update detection, unknown-speech handling");
 }
}
