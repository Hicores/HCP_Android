package cc.hicore.MiraiHCP;

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
        PluginManager.PreLoadPluginToList();
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
}
