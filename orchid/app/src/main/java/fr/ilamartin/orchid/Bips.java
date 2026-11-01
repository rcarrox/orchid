package fr.ilamartin.orchid;
import android.media.*;import android.content.Context;import java.io.InputStream;
final class Bips {
static final String[] IDS=BipChoice.IDS;
static final String[] NAMES=BipChoice.NAMES;
static final class Playback {
 private boolean cancelled;private AudioTrack track;
 synchronized boolean attach(AudioTrack value){if(cancelled)return false;track=value;return true;}
 synchronized boolean cancelled(){return cancelled;}
 synchronized void start(){if(!cancelled&&track!=null)track.play();}
 synchronized void cancel(){cancelled=true;if(track!=null)try{track.pause();track.flush();}catch(RuntimeException ignored){}}
 synchronized void release(){if(track!=null){try{track.release();}catch(RuntimeException ignored){}track=null;}}
}
static Playback play(Context context,String id,Runnable done){Playback playback=new Playback();new Thread(()->{
 AudioTrack track=null;
 try{
  if(!"none".equals(id)){
   int rate=44100;short[] pcm;int resource=softResource(id);
   if(resource!=0){try(InputStream input=context.getResources().openRawResource(resource)){PcmWav.Audio sound=PcmWav.read(input);rate=sound.sampleRate;pcm=sound.samples;}}
   else pcm=buildSquareSequence(soundSpec(id),rate,.8);
   track=new AudioTrack(AudioManager.STREAM_MUSIC,rate,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT,pcm.length*2,AudioTrack.MODE_STATIC);
   if(!playback.attach(track)){track.release();track=null;return;}
   // A static track normally reports STATE_NO_STATIC_DATA until its first write.
   if(track.getState()==AudioTrack.STATE_UNINITIALIZED)throw new IllegalStateException("Échec de création de la sortie sonore");
   if(track.write(pcm,0,pcm.length)!=pcm.length)throw new IllegalStateException("Chargement du bip incomplet");
   if(track.getState()!=AudioTrack.STATE_INITIALIZED)throw new IllegalStateException("Sortie sonore non prête après chargement du bip");
   playback.start();long until=android.os.SystemClock.elapsedRealtime()+pcm.length*1000L/rate+70;
   while(!playback.cancelled()&&android.os.SystemClock.elapsedRealtime()<until)Thread.sleep(15);
  }
 }catch(Exception ex){Store.status(context,"Bip indisponible : "+ex.getMessage());}
 finally{try{playback.release();}finally{done.run();}}
 },"orchid-bip").start();return playback;}
 static int softResource(String id){switch(id){case "soft_steps":return R.raw.soft_steps;case "soft_halo":return R.raw.soft_halo;case "soft_crystal":return R.raw.soft_crystal;case "soft_shore":return R.raw.soft_shore;default:return 0;}}
    private static double[][] soundSpec(String sound){
        // f Hz, durée totale ms, gain par note, release final ms.
        if("ding_dong_strident".equals(sound))return new double[][]{{880,850,1.5,400},{698,2700,1,1300}};
        if("triple_court".equals(sound))return new double[][]{{850,60},{0,30},{850,60},{0,100},{850,140}};
        if("rising".equals(sound)) return new double[][]{{650,60},{0,20},{1150,90}};
        if("double_signal".equals(sound)) return new double[][]{{1000,70},{0,55},{1000,70}};
        if("tetard".equals(sound)) return new double[][]{{880,50},{0,50},{880,50},{0,50},{1180,65}};
        if("bip_x1".equals(sound)) return new double[][]{{980,160}};
        if("bip_x2".equals(sound)) return new double[][]{{980,160},{0,80},{980,160}};
        if("triple_x3".equals(sound)) return new double[][]{{1300,90},{0,60},{1300,90},{0,60},{1300,90}};
        if("escalier".equals(sound)) return new double[][]{{800,100},{0,50},{1100,100},{0,50},{1500,160}};
        if("virgule".equals(sound)) return new double[][]{{1500,100},{0,40},{900,180}};
        return new double[][]{{720,50},{0,15},{980,50},{0,15},{1300,70}};
    }

    private static short[] buildSquareSequence(double[][] spec,int sr,double volume){
        int total=0;for(double[] part:spec)total+=(int)Math.round(sr*part[1]/1000.0);
        short[] out=new short[Math.max(1,total)];int pos=0;double amp=0.36*Math.max(0.0,Math.min(1.0,volume));
        for(double[] part:spec){
            double f=part[0];int n=(int)Math.round(sr*part[1]/1000.0);
            if(f<=0){pos+=n;continue;}
            int edge=Math.max(1,Math.min((int)(sr*.004),n/3));
            double partGain=part.length>=3?part[2]:1.0;
            int release=part.length>=4?(int)(sr*part[3]/1000.0):edge;
            for(int i=0;i<n&&pos<out.length;i++,pos++){
                double env=1.0;if(i<edge)env=i/(double)edge;
                if(i>=n-release)env=Math.min(env,Math.max(0.0,(n-i-1)/(double)Math.max(1,release)));
                double phase=(i*f/sr)%1.0;double wave=phase<.5?1.0:-1.0;
                out[pos]=(short)Math.round(32767.0*Math.min(.98,amp*Math.max(0.0,env)*partGain)*wave);
            }
        }
        return out;
    }
}
