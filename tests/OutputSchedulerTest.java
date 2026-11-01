import fr.ilamartin.orchid.OutputScheduler;
import fr.ilamartin.orchid.OutputScheduler.Mode;
import java.util.*;
public class OutputSchedulerTest {
 static void ok(boolean b){if(!b)throw new AssertionError();}
 static class Task implements OutputScheduler.Task {int starts,cancels;Runnable done;public void start(Runnable r){starts++;done=r;}public void cancel(){cancels++;}void finish(){if(done!=null)done.run();}}
 public static void main(String[] args){
  int[] idle={0};OutputScheduler q=new OutputScheduler(()->idle[0]++);Task a=new Task(),b=new Task(),c=new Task();
  q.submit(a,Mode.SERIAL);q.submit(b,Mode.SERIAL);ok(a.starts==1&&b.starts==0);a.finish();ok(b.starts==1&&idle[0]==0);a.finish();ok(idle[0]==0);b.finish();ok(!q.busy()&&idle[0]==1);
  a=new Task();b=new Task();c=new Task();q.submit(a,Mode.OVERLAP);q.submit(b,Mode.OVERLAP);ok(a.starts==1&&b.starts==1);q.submit(c,Mode.SERIAL);a.finish();ok(c.starts==0);b.finish();ok(c.starts==1);c.finish();
  a=new Task();b=new Task();c=new Task();q.submit(a,Mode.OVERLAP);q.submit(b,Mode.SERIAL);q.submit(c,Mode.REPLACE);ok(a.cancels==1&&b.cancels==1&&b.starts==0&&c.starts==1);a.finish();ok(q.busy());c.finish();ok(!q.busy());
  a=new Task();b=new Task();q.submit(a,Mode.OVERLAP);ok(!q.submit(b,Mode.IGNORE));ok(b.starts==0&&b.cancels==1&&a.cancels==0);a.finish();c=new Task();ok(q.submit(c,Mode.IGNORE)&&c.starts==1);c.finish();
  a=new Task();b=new Task();q.submit(a,Mode.OVERLAP);q.submit(b,Mode.OVERLAP);q.cancelAll();ok(a.cancels==1&&b.cancels==1&&!q.busy());a.finish();b.finish();ok(!q.busy());q.close();c=new Task();ok(!q.submit(c,Mode.OVERLAP)&&c.cancels==1);
  ok(Mode.from(null)==Mode.OVERLAP&&Mode.from("bad")==Mode.OVERLAP&&Mode.from("serial")==Mode.SERIAL&&Mode.from("replace")==Mode.REPLACE&&Mode.from("ignore")==Mode.IGNORE);
  OutputScheduler sync=new OutputScheduler(()->{});sync.submit(new OutputScheduler.Task(){public void start(Runnable done){done.run();done.run();}public void cancel(){}},Mode.SERIAL);ok(!sync.busy());
  OutputScheduler reentrant=new OutputScheduler(()->{});Task[] holder={null};holder[0]=new Task(){public void cancel(){super.cancel();finish();}};reentrant.submit(holder[0],Mode.OVERLAP);Task queued=new Task();reentrant.submit(queued,Mode.SERIAL);Task next=new Task();reentrant.submit(next,Mode.REPLACE);ok(queued.starts==0&&next.starts==1);
  System.out.println("OK: complete-response FIFO, immediate overlap, replace cancellation incl. queued jobs, ignore while busy, stale/double completion, stop/close, default overlap and reentrant cancellation.");
 }
}
