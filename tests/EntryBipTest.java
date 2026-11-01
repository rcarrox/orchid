import fr.ilamartin.orchid.*;import org.json.*;
public class EntryBipTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[] args)throws Exception {
  JSONObject old=new JSONObject("{\"id\":\"old-entry\",\"keyword\":\"michel;lapin;poulet\",\"spoken\":\"bonjour\",\"listenLang\":\"fr\",\"voiceLang\":\"en-US\",\"enabled\":false}");
  Entry legacy=Entry.from(old);ok(legacy.bip.equals("default")&&legacy.id.equals("old-entry")&&!legacy.enabled);ok(BipChoice.resolve(legacy.bip,"soft_halo").equals("soft_halo"));
  for(String id:BipChoice.entryIds()){Entry e=Entry.from(old);e.bip=id;Entry restored=Entry.from(new JSONObject(e.json().toString()));ok(restored.bip.equals(id));ok(restored.keyword.equals(e.keyword)&&restored.spoken.equals(e.spoken)&&restored.listenLang.equals("fr")&&restored.voiceLang.equals("en-US")&&!restored.enabled&&restored.id.equals("old-entry"));}
  old.put("bip","invalid");ok(Entry.from(old).bip.equals("default"));old.put("bip",JSONObject.NULL);ok(Entry.from(old).bip.equals("default"));ok(new Entry().bip.equals("default"));
  System.out.println("OK: legacy JSON migration without data loss, all beep choices JSON round-trip, new/TXT entries inherit default and invalid/null values safely inherit.");
 }
}
