package fr.ilamartin.orchid;
import java.io.*;import java.net.*;import java.security.MessageDigest;
/** GET only. No audio, keyword or identity is included in any request. */
final class ModelDownload {
 interface Progress {void update(long bytes,long total);}
 static void fetch(String address,String sha,File target,Progress progress)throws Exception{
  target.getParentFile().mkdirs();long offset=target.isFile()?target.length():0;if(offset>100L*1024*1024){target.delete();offset=0;}
  if(offset>0&&checksum(target).equals(sha))return;
  URL url=new URL(address);HttpURLConnection conn=null;
  try{
   for(int redirects=0;redirects<6;redirects++){
    if(!"https".equalsIgnoreCase(url.getProtocol()))throw new IOException("Connexion HTTPS requise");conn=(HttpURLConnection)url.openConnection();conn.setRequestMethod("GET");conn.setConnectTimeout(20000);conn.setReadTimeout(30000);conn.setInstanceFollowRedirects(false);conn.setRequestProperty("User-Agent","OrChiD-model-installer/1.0.1");conn.setRequestProperty("Accept-Encoding","identity");if(offset>0)conn.setRequestProperty("Range","bytes="+offset+"-");int code=conn.getResponseCode();
    if(code==301||code==302||code==303||code==307||code==308){String next=conn.getHeaderField("Location");conn.disconnect();if(next==null)throw new IOException("Redirection invalide");url=new URL(url,next);continue;}
    if(code==416){target.delete();throw new IOException("Téléchargement incomplet. Relancez l’installation.");}
    if(code!=200&&code!=206)throw new IOException("Téléchargement indisponible (HTTP "+code+")");
    boolean append=code==206&&offset>0;
    if(code==206){String range=conn.getHeaderField("Content-Range");if(range==null||!range.startsWith("bytes "+offset+"-"))throw new IOException("Reprise du téléchargement invalide");}
    if(!append)offset=0;long remaining=conn.getContentLengthLong(),total=remaining>0?offset+remaining:-1;if(total>100L*1024*1024)throw new IOException("Archive trop volumineuse");long received=offset;
    try(InputStream in=conn.getInputStream();OutputStream out=new BufferedOutputStream(new FileOutputStream(target,append))){byte[] buf=new byte[65536];int n;while((n=in.read(buf))!=-1){received+=n;if(received>100L*1024*1024)throw new IOException("Archive trop volumineuse");out.write(buf,0,n);progress.update(received,total);}}
    if(total>0&&received!=total)throw new IOException("Connexion interrompue. Relancez pour reprendre.");if(!checksum(target).equals(sha)){target.delete();throw new IOException("Archive incorrecte : vérification SHA-256 échouée");}return;
   }throw new IOException("Trop de redirections");
  }finally{if(conn!=null)conn.disconnect();}
 }
 static String checksum(File file)throws Exception{MessageDigest digest=MessageDigest.getInstance("SHA-256");try(InputStream in=new FileInputStream(file)){byte[] buf=new byte[65536];int n;while((n=in.read(buf))!=-1)digest.update(buf,0,n);}StringBuilder out=new StringBuilder();for(byte b:digest.digest())out.append(String.format(java.util.Locale.ROOT,"%02x",b&255));return out.toString();}
}
