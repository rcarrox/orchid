package fr.ilamartin.orchid;
import android.content.*;import org.json.*;import java.util.*;
final class Store {
 static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences("orchid_local",0);}
 static synchronized List<Entry> entries(Context c){List<Entry> a=new ArrayList<>();try{JSONArray j=new JSONArray(prefs(c).getString("entries","[]"));for(int i=0;i<j.length();i++)a.add(Entry.from(j.getJSONObject(i)));}catch(Exception ignored){}return a;}
 static synchronized void save(Context c,List<Entry> entries){JSONArray a=new JSONArray();for(Entry e:entries)a.put(e.json());prefs(c).edit().putString("entries",a.toString()).apply();}
 static String contrast(Context c,String lang){return prefs(c).getString("contrast_"+lang,KeywordGrammar.defaultContrast(lang));}
 static void status(Context c,String s){prefs(c).edit().putString("status",s).apply();}
}
