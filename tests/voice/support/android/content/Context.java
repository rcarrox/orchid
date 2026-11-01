package android.content;
import java.io.File;
public class Context {
 private final File cacheDir;
 public Context(File f){cacheDir=f;}
 public File getCacheDir(){return cacheDir;}
}
