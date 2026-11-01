import fr.ilamartin.orchid.*;
import java.util.*;
import java.io.*;
import java.nio.charset.*;
public class TxtWordsTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 static void fails(Runnable r){try{r.run();throw new AssertionError("Expected refusal");}catch(IllegalArgumentException expected){}}
 public static void main(String[] args)throws Exception {
  String raw="\ufeff# liste\r\noui\r\nnon ; OUI\rbonjour monsieur\n\n你好";
  String decoded=TxtWords.read(new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)));
  ok(TxtWords.contrast(decoded).equals(Arrays.asList("oui","non","bonjour monsieur","你好")));
  ok(TxtWords.decode("Été\n你好".getBytes(StandardCharsets.UTF_16)).equals("Été\n你好"));
  byte[] little="Été\n你好".getBytes(StandardCharsets.UTF_16LE);byte[] bom=new byte[little.length+2];bom[0]=(byte)255;bom[1]=(byte)254;System.arraycopy(little,0,bom,2,little.length);
  ok(TxtWords.decode(bom).equals("Été\n你好"));
  ok(TxtWords.decode("été".getBytes(Charset.forName("windows-1252"))).equals("été"));
  ok(TxtWords.mergeContrast("oui ; non","NON\nmerci").equals("oui ; non ; merci"));
  List<String> imported=TxtWords.newKeywords(Arrays.asList("Michel ; lapin"),"michel ; poulet\npoulet\nbonjour monsieur\n你好 ; 你好\n# ignoré");
  ok(imported.equals(Arrays.asList("poulet","bonjour monsieur","你好")));
  ok(TxtWords.newKeywords(Collections.emptyList(),"michel;lapin;poulet").equals(Arrays.asList("michel ; lapin ; poulet")));
  ok(TxtWords.newKeywords(Arrays.asList("oui"),"oui").isEmpty());
  ok(TxtWords.contrast("[unk]; ; ").isEmpty());
  fails(()->TxtWords.newKeywords(Collections.emptyList(),String.join("",Collections.nCopies(301,"a"))));
  StringBuilder many=new StringBuilder();for(int i=0;i<2001;i++)many.append("mot").append(i).append(';');fails(()->TxtWords.contrast(many.toString()));
  try{TxtWords.decode(new byte[]{0,1,2});throw new AssertionError("Binary file accepted");}catch(IOException expected){}
  try{TxtWords.read(new ByteArrayInputStream(new byte[TxtWords.MAX_BYTES+1]));throw new AssertionError("Oversized file accepted");}catch(IOException expected){}
  ConfidenceRules.Match match=ConfidenceRules.match(Arrays.asList(new ConfidenceRules.Word("poulet",.96,0,.4)),"michel;lapin; poulet");
  ok(match!=null&&match.matchedAlias.equals("poulet"));
  match=ConfidenceRules.match(Arrays.asList(new ConfidenceRules.Word("bonjour",.96,0,.3),new ConfidenceRules.Word("monsieur",.94,.3,.6)),"salut;bonjour monsieur");
  ok(match.matchedAlias.equals("bonjour monsieur"));
  System.out.println("OK: TXT encodings, BOM, line endings, phrases, alias groups, duplicates, append, limits, binary rejection and detected-alias display.");
 }
}
