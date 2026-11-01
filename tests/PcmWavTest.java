import fr.ilamartin.orchid.PcmWav;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PcmWavTest {
 static void ok(boolean value,String message){if(!value)throw new AssertionError(message);}
 static void rejected(byte[] bytes)throws Exception{try{PcmWav.decode(bytes);throw new AssertionError("Invalid WAV accepted");}catch(IOException expected){}}
 public static void main(String[] args)throws Exception {
  Path folder=Paths.get(args.length==0?"project/orchid/app/src/main/res/raw":args[0]);
  String[] names={"soft_steps","soft_halo","soft_crystal","soft_shore"};int[] frames={20286,16758,20066,26240};
  for(int i=0;i<names.length;i++){
   byte[] wav=Files.readAllBytes(folder.resolve(names[i]+".wav"));PcmWav.Audio sound=PcmWav.read(new ByteArrayInputStream(wav));
   ok(sound.sampleRate==22050,"Original sample rate");ok(sound.samples.length==frames[i],"Original duration");
   ok(sound.samples[0]==0&&sound.samples[sound.samples.length-1]==0,"Soft endpoints");
   for(int j=0;j<sound.samples.length;j++){
    int expected=(short)((wav[44+2*j]&255)|((wav[45+2*j]&255)<<8));ok(sound.samples[j]==expected,"Original signed PCM sample "+j);ok(Math.abs((int)sound.samples[j])<=13763,"No clipping");
   }
   rejected(Arrays.copyOf(wav,wav.length-1));byte[] stereo=wav.clone();stereo[22]=2;rejected(stereo);byte[] bad=wav.clone();bad[0]=0;rejected(bad);
  }
  rejected(new byte[0]);
  System.out.println("OK: four selected WAVs, exact PCM samples/durations, 22050 Hz, soft endpoints, peak bounds, truncated/invalid formats rejected.");
 }
}
