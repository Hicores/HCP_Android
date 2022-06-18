package cc.hicore.MiraiHCP;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;

import cc.hicore.MiraiHCP.KeepAliveHelper.KeepAliveHelper;
import cc.hicore.MiraiHCP.LogHelper.LogUtils;
import cc.hicore.MiraiHCP.LoginManager.LoginManager;
import cc.hicore.MiraiHCP.PluginManager.PluginManager;

public class ApplicationImpl extends Application {
    public static Application app;
    @Override
    protected void attachBaseContext(Context base) {
        app = this;
        GlobalEnv.appContext = base;
        GlobalEnv.FilePath = base.getFilesDir().getAbsolutePath();
        new File(base.getExternalCacheDir()+"/log").mkdirs();
        super.attachBaseContext(base);
        String name=getCurProcessName(base);
        if (name!=null && !name.contains(":")){
            LogUtils.info("HCP_Android_Loader","Start load HCP....");
            PluginManager.PreLoadPluginToList();
            LogUtils.info("HCP_Android_Loader","Load all HCP success.");
            //自动登录账号
            LoginManager.loadAllAccount();
            LogUtils.info("HCP_Android_Loader","Check and login all autologin account in 3s.");
            new Handler(Looper.getMainLooper()).postDelayed(()->{
                LogUtils.info("HCP_Android_Loader","Start check and login autologin accounts.");
                LoginManager.loginAutoLogin();

            },3000);
            KeepAliveHelper.init();
        }


    }

    @Override
    public void onCreate() {
        super.onCreate();

        String name=getCurProcessName(GlobalEnv.appContext);
        if (name!=null && !name.contains(":")){
            Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
            try{
                System.loadLibrary("native_lib");
            }catch (Throwable th){
                th.printStackTrace();
            }
        }
    }
    private static class CrashHandler implements Thread.UncaughtExceptionHandler{
        @Override
        public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
            StringBuilder builder = new StringBuilder();
            builder.append("Thread:").append(thread.getName()).append("\n\n");

            builder.append(PluginManager.collectPluginInfo());

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
