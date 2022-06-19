package cc.hicore.MiraiHCP.KeepAliveHelper;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import cc.hicore.MiraiHCP.ApplicationImpl;
import cc.hicore.MiraiHCP.BuildConfig;
import cc.hicore.MiraiHCP.GlobalEnv;
import cc.hicore.MiraiHCP.config.GlobalConfig;
import cc.hicore.Utils.ToastUtils;
import rikka.shizuku.Shizuku;

public class KeepAliveHelper {
    static Shizuku.OnRequestPermissionResultListener listener = (requestCode, grantResult) -> {
        if (requestCode == 77 && grantResult == PackageManager.PERMISSION_GRANTED){
            startShizukuService();
        }
    };
   static Shizuku.OnBinderReceivedListener BINDER_RECEIVED_LISTENER = () -> {
       if (requestShizuku()){
           startShizukuService();
       }
    };
    public static void init(){
        if (GlobalConfig.getBoolean("global","keepAlive",false)){
            Intent intent = new Intent(ApplicationImpl.app,MainServiceAlive.class);
            GlobalEnv.appContext.startService(intent);
            Shizuku.addBinderReceivedListener(BINDER_RECEIVED_LISTENER);
        }
    }
    private static boolean requestShizuku(){
        try{
            if (Shizuku.isPreV11())return false;
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED)return true;
            if (Shizuku.shouldShowRequestPermissionRationale()){
                return false;
            }
            Shizuku.addRequestPermissionResultListener(listener);
            Shizuku.requestPermission(77);
            return false;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    private static final ServiceConnection userServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private static final Shizuku.UserServiceArgs userServiceArgs =
            new Shizuku.UserServiceArgs(new ComponentName(BuildConfig.APPLICATION_ID, ShizukuService.class.getName()))
                    .daemon(true)
                    .processNameSuffix("daemon")
                    .version(BuildConfig.VERSION_CODE);
    private static void startShizukuService(){
        try{
            Shizuku.bindUserService(userServiceArgs, userServiceConnection);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public static void StopKeepAliveService(){
        GlobalConfig.putBoolean("global","keepAlive",false);
        try {
            Shizuku.unbindUserService(userServiceArgs,userServiceConnection,true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
