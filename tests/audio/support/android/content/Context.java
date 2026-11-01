package android.content;import java.io.File;import android.content.res.Resources;
public class Context {private final Resources resources;public Context(File raw){resources=new Resources(raw);}public Resources getResources(){return resources;}}
