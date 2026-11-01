package fr.ilamartin.orchid;
import java.util.*;
/** Stable persisted IDs shared by global settings, entries and playback. */
public final class BipChoice {
 public static final String[] IDS={"none","arcade","rising","double_signal","tetard","bip_x1","bip_x2","triple_x3","escalier","virgule","ding_dong_strident","triple_court","soft_steps","soft_halo","soft_crystal","soft_shore"};
 public static final String[] NAMES={"Aucun","Arcade","Montant franc","Signal net (double bip)","Tétard","Bip x1","Bip x2","Triple x3","Escalier","Virgule","Ding dong strident","Triple court","Petites marches","Halo","Cristal tendre","Rivage"};
 public static String normalize(String value){return Arrays.asList(IDS).contains(value)?value:"default";}
 public static String resolve(String entry,String global){String own=normalize(entry);return !"default".equals(own)?own:Arrays.asList(IDS).contains(global)?global:"arcade";}
 public static String[] entryIds(){return prepend("default",IDS);}
 public static String[] entryNames(){return prepend("Défaut",NAMES);}
 public static String label(String value){int i=Arrays.asList(IDS).indexOf(value);return i<0?"Défaut":NAMES[i];}
 private static String[] prepend(String first,String[] values){String[] result=new String[values.length+1];result[0]=first;System.arraycopy(values,0,result,1,values.length);return result;}
}
