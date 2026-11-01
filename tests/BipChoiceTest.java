import fr.ilamartin.orchid.BipChoice;
import java.util.*;
public class BipChoiceTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args){
  ok(BipChoice.resolve(null,"soft_steps").equals("soft_steps"));ok(BipChoice.resolve("default","soft_halo").equals("soft_halo"));
  ok(BipChoice.resolve("soft_shore","soft_halo").equals("soft_shore"));ok(BipChoice.resolve("none","soft_halo").equals("none"));ok(BipChoice.resolve("default","none").equals("none"));
  ok(BipChoice.resolve("unknown","soft_crystal").equals("soft_crystal"));ok(BipChoice.resolve("default","unknown").equals("arcade"));ok(BipChoice.normalize("").equals("default"));
  String[] ids=BipChoice.entryIds(),names=BipChoice.entryNames();ok(ids.length==17&&ids.length==names.length);ok(ids[0].equals("default")&&names[0].equals("Défaut"));ok(new HashSet<>(Arrays.asList(ids)).size()==ids.length);
  for(int i=0;i<BipChoice.IDS.length;i++){ok(ids[i+1].equals(BipChoice.IDS[i]));ok(names[i+1].equals(BipChoice.NAMES[i]));ok(BipChoice.resolve(ids[i+1],"arcade").equals(ids[i+1]));ok(BipChoice.label(ids[i+1]).equals(names[i+1]));}
  ok(BipChoice.resolve("default","arcade").equals("arcade"));ok(BipChoice.resolve("default","soft_shore").equals("soft_shore"));
  System.out.println("OK: inherited/default beep, per-entry override, none, changing global default, invalid IDs, all 16 choices and labels.");
 }
}
