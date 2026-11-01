package fr.ilamartin.orchid;
import android.content.Context;import java.util.*;
final class Store {
 static final Map<String,String> info=new HashMap<>();
 static final Map<String,Integer> integers=new HashMap<>();
 static android.content.SharedPreferences prefs(Context c){return new Preferences();}
 static void status(Context c,String s){info.put("status",s);}
 static class Preferences implements android.content.SharedPreferences {
  public int getInt(String k,int defaultValue){return integers.getOrDefault(k,defaultValue);}
  public Editor edit(){return new Editor();}
  static class Editor implements android.content.SharedPreferences.Editor {
   public Editor putString(String k,String v){info.put(k,v);return this;}
   public Editor putInt(String k,int v){integers.put(k,v);return this;}
   public void apply(){}
  }
 }
}
