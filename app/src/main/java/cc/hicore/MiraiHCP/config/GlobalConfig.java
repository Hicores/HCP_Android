package cc.hicore.MiraiHCP.config;

import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import cc.hicore.MiraiHCP.GlobalEnv;

public class GlobalConfig {
    public static String getString(String col, String key,String def){
        SharedPreferences share =  GlobalEnv.appContext.getSharedPreferences(col,0);
        return share.getString(key, def);
    }
    public static void putString(String col, String key,String value){
        SharedPreferences share =  GlobalEnv.appContext.getSharedPreferences(col,0);
        SharedPreferences.Editor editor = share.edit();
        editor.putString(key,value);
        editor.apply();
    }
    public static int getInt(String col, String key,int def){
        SharedPreferences share =  GlobalEnv.appContext.getSharedPreferences(col,0);
        return share.getInt(key, def);
    }
    public static void putInt(String col, String key,int value){
        SharedPreferences share =  GlobalEnv.appContext.getSharedPreferences(col,0);
        SharedPreferences.Editor editor = share.edit();
        editor.putInt(key,value);
        editor.apply();
    }
    public static boolean getBoolean(String col, String key,boolean def){
        SharedPreferences share =  GlobalEnv.appContext.getSharedPreferences(col,0);
        return share.getBoolean(key, def);
    }
    public static void putBoolean(String col, String key,boolean value){
        SharedPreferences share = GlobalEnv.appContext.getSharedPreferences(col,0);
        SharedPreferences.Editor editor = share.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }

    public static List<String> getList(String col, String key){
        SharedPreferences share =  GlobalEnv.appContext.getSharedPreferences(col,0);
        String jsonListCol = share.getString(key,"[]");
        try{
            JSONArray mJson = new JSONArray(jsonListCol);
            ArrayList<String> mArr = new ArrayList<>();
            for (int i=0;i<mJson.length();i++){
                mArr.add(mJson.optString(i));
            }
            return mArr;
        }catch (Exception e){
            return new ArrayList<>();
        }
    }
    public static void putList(String col,String key,List<String> value){
        SharedPreferences share = GlobalEnv.appContext.getSharedPreferences(col,0);
        JSONArray newArr = new JSONArray(value);
        SharedPreferences.Editor editor = share.edit();
        editor.putString(key,newArr.toString());
        editor.apply();
    }
}
