package android.content;
import java.io.File;
/** JVM filesystem test double, not part of the Android app. */
public abstract class Context {
 public abstract File getFilesDir();
 public Context getApplicationContext(){return this;}
}
