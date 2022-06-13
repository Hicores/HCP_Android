package cc.hicore.MiraiHCP;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;

import cc.hicore.MiraiHCP.PluginManager.PluginManager;

public class ApplicationImpl extends Application {
    static Application app;
    @Override
    protected void attachBaseContext(Context base) {
        GlobalEnv.appContext = base;
        GlobalEnv.FilePath = base.getFilesDir().getAbsolutePath();
        new File(base.getExternalCacheDir()+"/log").mkdirs();
        super.attachBaseContext(base);
        String name=getCurProcessName(base);
        if (name!=null && !name.contains(":")){
            PluginManager.PreLoadPluginToList();
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
    }
    private static class CrashHandler implements Thread.UncaughtExceptionHandler{
        @Override
        public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
            StringBuilder builder = new StringBuilder();
            builder.append("Thread:").append(thread.getName()).append("\n\n");
            builder.append(Log.getStackTraceString(throwable));
            Intent intent = new Intent(app,CrashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("dump",builder.toString());
            app.startActivity(intent);
            System.exit(0);
        }
    }

    public static String getCurProcessName(Context context) {
        // 获取此进程的标识符
        int pid = android.os.Process.myPid();
        // 获取活动管理器
        ActivityManager activityManager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        // 从应用程序进程列表找到当前进程，是：返回当前进程名
        for (ActivityManager.RunningAppProcessInfo appProcess :
                activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }
}
