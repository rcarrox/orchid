package fr.ilamartin.orchid;
import org.json.*;
import java.util.*;
public final class Entry {
 public String id=UUID.randomUUID().toString(),keyword="",spoken="",listenLang="fr",voiceLang="fr-FR",bip="default";public boolean enabled=true;
 public JSONObject json(){JSONObject j=new JSONObject();try{j.put("id",id).put("keyword",keyword).put("spoken",spoken).put("listenLang",listenLang).put("voiceLang",voiceLang).put("enabled",enabled).put("bip",BipChoice.normalize(bip));}catch(JSONException ignored){}return j;}
 public static Entry from(JSONObject j){Entry e=new Entry();e.id=j.optString("id",e.id);e.keyword=j.optString("keyword");e.spoken=j.optString("spoken");e.listenLang=j.optString("listenLang","fr");e.voiceLang=j.optString("voiceLang","fr-FR");e.enabled=j.optBoolean("enabled",true);e.bip=BipChoice.normalize(j.optString("bip","default"));return e;}
}
