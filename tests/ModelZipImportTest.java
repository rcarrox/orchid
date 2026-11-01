package fr.ilamartin.orchid;

import android.content.Context;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public class ModelZipImportTest {
 static void ok(boolean b,String message){if(!b)throw new AssertionError(message);}
 static byte[] zip(String prefix,String marker)throws IOException {
  ByteArrayOutputStream out=new ByteArrayOutputStream();try(ZipOutputStream z=new ZipOutputStream(out)){
   for(String name:new String[]{"am/final.mdl","conf/mfcc.conf","graph/words.txt"}){z.putNextEntry(new ZipEntry(prefix+name));z.write(marker.getBytes("UTF-8"));z.closeEntry();}
  }return out.toByteArray();
 }
 static String marker(Context c,String lang)throws IOException{return new String(Files.readAllBytes(new File(Models.directory(c,lang),"am/final.mdl").toPath()),"UTF-8");}
 public static void main(String[] args)throws Exception {
  final Path temp=Files.createTempDirectory("orchid-model-test");Context c=new Context(){public File getFilesDir(){return temp.toFile();}};
  try{
   ok(Models.beginImport(),"Initial import lock");ok(!Models.beginImport(),"Parallel import must be refused");Models.finishImport();
   Models.downloading=true;ok(!Models.beginImport(),"Do not import during download");Models.downloading=false;
   Models.install(c,"fr",new ByteArrayInputStream(zip("vosk-fr/","ancien-fr")));
   Models.install(c,"en",new ByteArrayInputStream(zip("","anglais")));
   ok(Models.ready(c,"fr")&&Models.ready(c,"en"),"Wrapped and root ZIP layouts");
   Models.install(c,"fr",new ByteArrayInputStream(zip("vosk-fr/","nouveau-fr")));
   ok(marker(c,"fr").equals("nouveau-fr")&&marker(c,"en").equals("anglais"),"Only selected language replaced");
   try{Models.install(c,"fr",new ByteArrayInputStream("pas un zip".getBytes("UTF-8")));throw new AssertionError("Bad ZIP accepted");}catch(IOException expected){}
   ok(marker(c,"fr").equals("nouveau-fr"),"Bad ZIP must preserve existing model");
   try{Models.install(c,"fr",null);throw new AssertionError("Null stream accepted");}catch(IOException expected){}
   ok(marker(c,"fr").equals("nouveau-fr"),"Unreadable file must preserve existing model");
   try{Models.install(c,"fr",new ByteArrayInputStream(zip("../outside/","attack")));throw new AssertionError("Traversal accepted");}catch(IOException expected){}
   ok(marker(c,"fr").equals("nouveau-fr"),"Invalid path must preserve existing model");
   ok(!new File(c.getFilesDir(),"model-import-fr").exists(),"Temporary extraction removed after failure");
   ok(Models.beginImport(),"Retry after release");Models.finishImport();
   System.out.println("OK: import/download exclusion, root/wrapped ZIPs, language isolation, replacement, invalid/unreadable ZIP preservation, invalid paths and retry. No Android picker or Vosk acoustic validation.");
  }finally{Models.downloading=false;Models.finishImport();try(java.util.stream.Stream<Path> paths=Files.walk(temp)){paths.sorted(Comparator.reverseOrder()).forEach(p->{try{Files.deleteIfExists(p);}catch(IOException ex){throw new UncheckedIOException(ex);}});}}
 }
}
