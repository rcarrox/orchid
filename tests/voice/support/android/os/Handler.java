package android.os;
public class Handler {
 public Handler(Looper main){}
 public boolean post(Runnable job){job.run();return true;}
 public boolean postDelayed(Runnable job,long ms){return true;}
}
