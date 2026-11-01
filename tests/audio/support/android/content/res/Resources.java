package android.content.res;import java.io.*;
public class Resources {private final File root;public Resources(File root){this.root=root;}public InputStream openRawResource(int id){String[] files={"","soft_steps","soft_halo","soft_crystal","soft_shore"};try{return new FileInputStream(new File(root,files[id]+".wav"));}catch(IOException e){throw new RuntimeException(e);}}}
