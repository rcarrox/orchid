import fr.ilamartin.orchid.*;
import java.util.*;

public class ConfidenceRulesTest {
 static ConfidenceRules.Word word(String text,double confidence,double start){return new ConfidenceRules.Word(text,confidence,start,start+.2);}
 static void ok(boolean value,String message){if(!value)throw new AssertionError(message);}
 static ConfidenceRules.Match match(String aliases,ConfidenceRules.Word... words){return ConfidenceRules.match(Arrays.asList(words),aliases);}
 public static void main(String[] args){
  ConfidenceRules.Match m=match("bonjour Orchid",word("bonjour",.97,0),word("orchid",.50,.3));
  ok(!ConfidenceRules.passes(m,85),"Weak keyword word must reject the whole keyword");
  m=match("Ray",word("parole",.12,0),word("ray",.91,.3),word("ensuite",.1,.6));
  ok(ConfidenceRules.passes(m,85)&&!ConfidenceRules.passes(m,95),"Only keyword confidence counts; stricter threshold rejects");
  ok(!ConfidenceRules.passes(match("ray",word("ray",Double.NaN,0)),0),"Missing confidence never bypasses the filter, even at zero");
  ok(match("ray",word("ray",1.01,0))==null,"Invalid confidence");
  ok(match("ray",word("rayon",.99,0))==null,"Latin word boundaries");
  ok(match("bonjour orchid",word("bonjour",.99,0),word("[unk]",1,.3),word("orchid",.99,.6))==null,"Unknown speech cannot bridge a phrase");
  ok(ConfidenceRules.passes(match("été ; salut",word("salut",.9,0)),85),"Aliases");
  ok(ConfidenceRules.passes(match("été",word("ÉTÉ",.9,0)),85),"Accents and normalization");
  ok(ConfidenceRules.passes(match("你好",word("你",.95,0),word("好",.9,.3)),85),"CJK split into several words");
  ok(!ConfidenceRules.passes(match("你好",word("你",.95,0),word("好",.4,.3)),85),"CJK weak component");
  ok(match("你好",word("你",.95,0),word("[unk]",1,.3),word("好",.9,.6))==null,"CJK unknown barrier");
  ok(ConfidenceRules.match(Collections.emptyList(),"ray")==null,"No scored words");
  ConfidenceRules.Stability stable=new ConfidenceRules.Stability();m=match("ray",word("ray",.91,0));
  ok(!stable.ready("fr/ray",m,1000),"First partial must not fire");
  ok(!stable.ready("fr/ray",m,1349),"Not stable long enough");
  ok(stable.ready("fr/ray",m,1350),"350 ms of processed audio");
  ok(!stable.ready("en/ray",m,1350),"Languages remain independent");
  stable.ready("fr/ray",null,1400);ok(!stable.ready("fr/ray",m,1500),"Disappearing or weak partial resets stability");
  ok(!stable.ready("fr/ray",match("ray",word("ray",.92,5)),1900),"Changed word timestamps reset stability");
  stable.reset();ok(!stable.ready("fr/ray",m,2500),"Settings or grammar changes reset stability");
  DetectionGate gate=new DetectionGate();ok(gate.accept("fr","ray",10000,0),"Accept valid keyword");
  ok(!gate.accept("fr","ray",10001,0),"Partial and final cannot both trigger");gate.finish("fr");
  ok(gate.accept("fr","ray",10002,0),"Next utterance works at interval zero");
  System.out.println("OK: keyword-only scores, thresholds, missing scores, aliases, CJK, unknown barriers, partial stability and deduplication.");
 }
}
