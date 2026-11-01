package fr.ilamartin.orchid;
import android.content.Context;import java.io.*;import java.util.*;import java.util.zip.*;
final class Models {
 static File directory(Context c,String lang){if(!lang.matches("[a-z]{2,3}"))throw new IllegalArgumentException("Code de langue invalide");return new File(c.getFilesDir(),"models/"+lang);}
 static boolean ready(Context c,String lang){return valid(directory(c,lang));}
 static boolean valid(File d){return new File(d,"am/final.mdl").isFile()&&new File(d,"conf/mfcc.conf").isFile()&&new File(d,"graph").isDirectory();}
 static volatile boolean downloading=false,importing=false;
 static boolean beginImport(){if(downloading||importing)return false;synchronized(Models.class){if(downloading||importing)return false;importing=true;return true;}}
 static synchronized void finishImport(){importing=false;}
 private static final String[][] DEFAULTS={
  {"fr","Français","https://alphacephei.com/vosk/models/vosk-model-small-fr-0.22.zip","cabf6180e177eb9b3a9a9d43a437bd5e549f3a7d09525e5d69a3fed787be12ad"},
  {"en","Anglais","https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip","30f26242c4eb449f948e42cb302dd7a686cb29a3423a8367f99ff41780942498"},
  {"zh","Chinois","https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip","3af8b0e7e0f835ae9d414ce5df580237a3cfb08d586c9fbbb0f7ff29ad5b14ba"}
 };
 static synchronized void downloadSelected(Context context,Set<String> selected){
  if(downloading||importing||selected.isEmpty())return;final Set<String> wanted=new HashSet<>(selected);final Context c=context.getApplicationContext();downloading=true;Store.status(c,"Préparation des langues choisies…");
  new Thread(()->{try{
   for(String[] spec:DEFAULTS){if(!wanted.contains(spec[0])||ready(c,spec[0]))continue;final String label=spec[1];File archive=new File(c.getFilesDir(),"downloads/model-"+spec[0]+".zip.part");Store.status(c,label+" : connexion…");final long[] last={0};
    ModelDownload.fetch(spec[2],spec[3],archive,(done,total)->{long now=System.currentTimeMillis();if(now-last[0]>700){last[0]=now;Store.status(c,label+" : "+(total>0?(done*100/total)+" %":(done/1048576)+" Mo"));}});
    Store.status(c,label+" : vérification et installation…");try(InputStream in=new FileInputStream(archive)){install(c,spec[0],in);}archive.delete();
   }Store.status(c,"Langues choisies prêtes · mode hors ligne");
  }catch(Exception e){Store.status(c,"Installation interrompue : "+e.getMessage()+" · choisissez à nouveau la langue pour reprendre");}finally{downloading=false;}},"orchid-model-download").start();
 }
 static synchronized void install(Context c,String lang,InputStream in)throws Exception{
  if(in==null)throw new IOException("Fichier ZIP inaccessible : choisissez à nouveau le fichier");
  File target=directory(c,lang),stage=new File(c.getFilesDir(),"model-import-"+lang);remove(stage);if(!stage.mkdirs())throw new IOException("Stockage inaccessible");long total=0;int count=0;
  try{try(ZipInputStream zip=new ZipInputStream(new BufferedInputStream(in))){ZipEntry e;byte[] buf=new byte[65536];while((e=zip.getNextEntry())!=null){if(++count>10000)throw new IOException("Archive trop volumineuse");String name=e.getName().replace('\\','/');if(name.startsWith("/")||name.contains("../"))throw new IOException("Chemin non autorisé");File f=new File(stage,name);if(!f.getCanonicalPath().startsWith(stage.getCanonicalPath()+File.separator))throw new IOException("Chemin non autorisé");if(e.isDirectory()){f.mkdirs();continue;}File parent=f.getParentFile();if(!parent.isDirectory()&&!parent.mkdirs())throw new IOException("Écriture impossible");try(OutputStream out=new FileOutputStream(f)){int n;while((n=zip.read(buf))!=-1){total+=n;if(total>700L*1024*1024)throw new IOException("Modèle trop volumineux (700 Mo maximum)");out.write(buf,0,n);}}}}
   File model=find(stage,0);if(model==null)throw new IOException("ZIP Vosk invalide : am, conf et graph attendus");target.getParentFile().mkdirs();File old=new File(target.getPath()+".old");remove(old);if(target.exists()&&!target.renameTo(old))throw new IOException("Ancien modèle occupé");if(!model.renameTo(target)){if(old.exists())old.renameTo(target);throw new IOException("Installation impossible");}remove(old);
  }finally{remove(stage);}
 }
 private static File find(File root,int depth){if(valid(root))return root;if(depth>2)return null;File[] children=root.listFiles();if(children!=null)for(File f:children)if(f.isDirectory()){File m=find(f,depth+1);if(m!=null)return m;}return null;}
 static List<String> installed(Context c){List<String>a=new ArrayList<>();File root=new File(c.getFilesDir(),"models");File[] files=root.listFiles();if(files!=null)for(File d:files)if(d.getName().matches("[a-z]{2,3}")&&valid(d))a.add(d.getName());Collections.sort(a);return a;}
 private static void remove(File f){if(f.isDirectory()){File[] a=f.listFiles();if(a!=null)for(File x:a)remove(x);}f.delete();}
}
