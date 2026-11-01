import fr.ilamartin.orchid.*;
public class ZeroIntervalTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args){ok(KeywordRules.clampInterval(0)==0);ok(KeywordRules.clampInterval(-1)==0);ok(KeywordRules.clampInterval(5000)==3600);DetectionGate gate=new DetectionGate();ok(gate.accept("fr","alice",10000,0));ok(!gate.accept("fr","alice",10001,0));ok(gate.accept("fr","bob",10001,0));gate.finish("fr");ok(gate.accept("fr","alice",10001,0));gate.finish("fr");ok(!gate.accept("fr","alice",10002,8));ok(gate.accept("en","charlie",10003,0));gate.resetUtterances();ok(!gate.accept("fr","alice",10004,8));ok(gate.accept("fr","alice",10004,0));System.out.println("OK : intervalle zéro, détection suivante sans délai, résultat partiel unique, plusieurs mots-clés et intervalle positif conservé.");}
}
