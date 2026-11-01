package android.content;
public interface SharedPreferences {
 int getInt(String key,int fallback);
 Editor edit();
 interface Editor {
  Editor putString(String key,String value);
  Editor putInt(String key,int value);
  void apply();
 }
}
