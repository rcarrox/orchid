package fr.ilamartin.orchid;import android.content.Context;final class Store {static volatile String error="";static void status(Context c,String message){error=message;}}
