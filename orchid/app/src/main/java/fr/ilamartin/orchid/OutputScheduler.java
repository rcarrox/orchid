package fr.ilamartin.orchid;
import java.util.*;
/** Runs on the main thread. One task is one complete beep + optional announcement. */
public final class OutputScheduler {
 public interface Task {void start(Runnable complete);void cancel();}
 public enum Mode {SERIAL,REPLACE,OVERLAP,IGNORE;
  public static Mode from(String value){if("serial".equals(value))return SERIAL;if("replace".equals(value))return REPLACE;if("ignore".equals(value))return IGNORE;return OVERLAP;}
 }
 private final Set<Task> active=new LinkedHashSet<>();private final Deque<Task> queue=new ArrayDeque<>();private final Runnable idle;private boolean closed;
 public OutputScheduler(Runnable idle){this.idle=idle;}
 public boolean busy(){return !active.isEmpty()||!queue.isEmpty();}
 public boolean submit(Task task,Mode mode){
  if(closed){task.cancel();return false;}
  if(mode==Mode.IGNORE&&busy()){task.cancel();return false;}
  if(mode==Mode.REPLACE)clear();
  if(mode==Mode.SERIAL&&busy())queue.addLast(task);else start(task);
  return true;
 }
 private void start(Task task){active.add(task);try{task.start(()->finish(task));}catch(RuntimeException e){task.cancel();finish(task);}}
 private void finish(Task task){if(!active.remove(task))return;if(active.isEmpty()){if(!queue.isEmpty())start(queue.removeFirst());else if(!closed)idle.run();}}
 private void clear(){List<Task> removed=new ArrayList<>(active);removed.addAll(queue);active.clear();queue.clear();for(Task task:removed)task.cancel();}
 public void cancelAll(){clear();}
 public void close(){closed=true;clear();}
}
