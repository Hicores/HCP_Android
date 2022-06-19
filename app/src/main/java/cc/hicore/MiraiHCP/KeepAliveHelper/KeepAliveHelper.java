package cc.hicore.MiraiHCP.KeepAliveHelper;

import android.content.Intent;
import android.content.pm.PackageManager;

import cc.hicore.MiraiHCP.ApplicationImpl;
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
        Intent intent = new Intent(ApplicationImpl.app,MainServiceAlive.class);
        GlobalEnv.appContext.startService(intent);
        Shizuku.addBinderReceivedListener(BINDER_RECEIVED_LISTENER);
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
    private static void startShizukuService(){
        ToastUtils.ShowToast(GlobalEnv.appContext,"授权成功 ");
    }
    public static void StopKeepAliveService(){
        GlobalConfig.putBoolean("global","keepAlive",false);
    }
}
