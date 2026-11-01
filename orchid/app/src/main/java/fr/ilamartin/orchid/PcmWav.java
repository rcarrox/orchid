package fr.ilamartin.orchid;

import java.io.*;
import java.nio.*;
import java.util.Arrays;

/** Reader for the bundled PCM16 mono WAVs, retaining the original samples. */
public final class PcmWav {
 public static final class Audio {
  public final int sampleRate;public final short[] samples;
  Audio(int sampleRate,short[] samples){this.sampleRate=sampleRate;this.samples=samples;}
 }
 public static Audio read(InputStream input)throws IOException {
  if(input==null)throw new IOException("Son indisponible");
  ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;
  while((n=input.read(buffer))!=-1){if(out.size()+n>1024*1024)throw new IOException("Son trop volumineux");out.write(buffer,0,n);}
  return decode(out.toByteArray());
 }
 public static Audio decode(byte[] bytes)throws IOException {
  if(bytes.length<44||!tag(bytes,0,"RIFF")||!tag(bytes,8,"WAVE"))throw new IOException("En-tête WAV invalide");
  ByteBuffer data=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
  long total=Integer.toUnsignedLong(data.getInt(4))+8;
  if(total>bytes.length||total<44)throw new IOException("Fichier WAV tronqué");
  int rate=0,channels=0,bits=0,format=0,alignment=0;byte[] pcm=null;
  for(int pos=12;pos+8<=total;){
   long size=Integer.toUnsignedLong(data.getInt(pos+4)),end=pos+8L+size;
   if(end>total)throw new IOException("Bloc WAV tronqué");
   if(tag(bytes,pos,"fmt ")){if(size<16)throw new IOException("Format WAV invalide");format=Short.toUnsignedInt(data.getShort(pos+8));channels=Short.toUnsignedInt(data.getShort(pos+10));rate=data.getInt(pos+12);alignment=Short.toUnsignedInt(data.getShort(pos+20));bits=Short.toUnsignedInt(data.getShort(pos+22));}
   if(tag(bytes,pos,"data")&&pcm==null)pcm=Arrays.copyOfRange(bytes,pos+8,(int)end);
   pos=(int)(end+(size&1));
  }
  if(format!=1||channels!=1||bits!=16||alignment!=2||rate<8000||rate>48000||pcm==null||pcm.length==0||(pcm.length&1)!=0)throw new IOException("WAV PCM16 mono attendu");
  short[] samples=new short[pcm.length/2];ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
  return new Audio(rate,samples);
 }
 private static boolean tag(byte[] bytes,int offset,String tag){if(offset<0||offset+4>bytes.length)return false;for(int i=0;i<4;i++)if(bytes[offset+i]!=(byte)tag.charAt(i))return false;return true;}
 private PcmWav(){}
}
