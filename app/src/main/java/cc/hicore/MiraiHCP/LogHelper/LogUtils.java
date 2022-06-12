package cc.hicore.MiraiHCP.LogHelper;

import android.util.Log;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import cc.hicore.MiraiHCP.GlobalEnv;

public class LogUtils {
    public static final int LEVEL_ERROR = 1;
    public static final int LEVEL_WARNING = 2;
    public static final int LEVEL_INFO = 3;
    public static final int LEVEL_DEBUG = 4;


    public static void error(String tag,String text){
        writeLog(LEVEL_ERROR,tag+"->"+text);
    }
    public static void warn(String tag,String text){
        writeLog(LEVEL_WARNING,tag+"->"+text);
    }
    public static void info(String tag,String text){
        writeLog(LEVEL_INFO,tag+"->"+text);
    }
    public static void debug(String tag,String text){
        writeLog(LEVEL_DEBUG,tag+"->"+text);
    }
    private synchronized static void writeLog(int level,String text){
        try{
            String tag = level == LEVEL_DEBUG ? "[DEBUG]" : (level == LEVEL_INFO ? "[INFO]" : (level == LEVEL_WARNING ? "[WARNING]" : (LEVEL_ERROR == level) ? "[ERROR]" : ("[LOG_"+level+"]")));
            String path = GlobalEnv.appContext.getCacheDir()+"/log/bridge.log";
            FileOutputStream out = new FileOutputStream(path,true);
            String summary = getTime()+"  "+tag+":"+text+"\n";
            out.write(summary.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
        }catch (Exception e){
            Log.e("LogWriter",Log.getStackTraceString(e));
        }

    }
    private static String getTime(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return format.format(new Date());
    }
}

