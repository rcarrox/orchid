package fr.ilamartin.orchid;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;

/** Plain text only: no list entry can execute code or alter other settings. */
public final class TxtWords {
 public static final int MAX_BYTES=1024*1024,MAX_CONTRAST_CHARS=20000,MAX_CONTRAST_WORDS=2000,MAX_ENTRIES=1000;
 public static String read(InputStream input)throws IOException {
  if(input==null)throw new IOException("Fichier inaccessible");
  ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;
  while((n=input.read(buffer))!=-1){if(out.size()+n>MAX_BYTES)throw new IOException("Fichier trop volumineux : 1 Mo maximum");out.write(buffer,0,n);}
  return decode(out.toByteArray());
 }
 public static String decode(byte[] bytes)throws IOException {
  if(bytes.length>MAX_BYTES)throw new IOException("Fichier trop volumineux : 1 Mo maximum");
  boolean utf16=bytes.length>=2&&((bytes[0]==(byte)0xff&&bytes[1]==(byte)0xfe)||(bytes[0]==(byte)0xfe&&bytes[1]==(byte)0xff));String text;
  try{text=decode(bytes,utf16?"UTF-16":"UTF-8");}
  catch(CharacterCodingException ex){if(utf16)throw new IOException("Texte UTF-16 invalide");try{text=decode(bytes,"windows-1252");}catch(CharacterCodingException second){throw new IOException("Encodage invalide : utilisez UTF-8 ou UTF-16 avec BOM");}}
  if(text.startsWith("\ufeff"))text=text.substring(1);
  for(int i=0;i<text.length();i++){char c=text.charAt(i);if(Character.isISOControl(c)&&c!='\n'&&c!='\r'&&c!='\t')throw new IOException("Le fichier ne semble pas être un texte TXT");}
  return text;
 }
 private static String decode(byte[] bytes,String charset)throws CharacterCodingException{return Charset.forName(charset).newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();}
 public static List<String> lines(String text){List<String> result=new ArrayList<>();for(String line:text.replace("\r\n","\n").replace('\r','\n').split("\n")){line=line.trim();if(!line.isEmpty()&&!line.startsWith("#"))result.add(line);}return result;}
 public static List<String> contrast(String text){
  Map<String,String> unique=new LinkedHashMap<>();for(String line:lines(text))for(String word:line.split(";")){word=word.trim().replaceAll("\\s+"," ");String key=KeywordRules.normalize(word);if(!key.isEmpty()&&!"[unk]".equalsIgnoreCase(word))unique.putIfAbsent(key,word);}
  if(unique.size()>MAX_CONTRAST_WORDS)throw new IllegalArgumentException("Maximum 2 000 mots de contraste par langue");return new ArrayList<>(unique.values());
 }
 public static String mergeContrast(String existing,String imported){String merged=String.join(" ; ",contrast(existing+"\n"+imported));if(merged.length()>MAX_CONTRAST_CHARS)throw new IllegalArgumentException("Maximum 20 000 caractères de contraste par langue");return merged;}
 /** Each line is an entry; semicolons remain aliases within that entry. */
 public static List<String> newKeywords(Collection<String> existing,String imported){
  Set<String> seen=new HashSet<>();for(String entry:existing)for(String alias:entry.split(";"))seen.add(KeywordRules.normalize(alias));
  List<String> result=new ArrayList<>();for(String line:lines(imported)){
   List<String> fresh=new ArrayList<>();for(String alias:line.split(";")){alias=alias.trim().replaceAll("\\s+"," ");String key=KeywordRules.normalize(alias);if(!key.isEmpty()&&!"[unk]".equalsIgnoreCase(alias)&&seen.add(key))fresh.add(alias);}
   if(!fresh.isEmpty()){String entry=String.join(" ; ",fresh);if(entry.length()>300)throw new IllegalArgumentException("Une ligne dépasse 300 caractères");result.add(entry);if(result.size()>MAX_ENTRIES)throw new IllegalArgumentException("Maximum 1 000 nouvelles entrées par import");}
  }return result;
 }
 private TxtWords(){}
}
