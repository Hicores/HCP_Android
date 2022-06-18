package cc.hicore.MiraiHCP.KeepAliveHelper;

import android.content.Intent;

import cc.hicore.MiraiHCP.ApplicationImpl;
import cc.hicore.MiraiHCP.GlobalEnv;

public class KeepAliveHelper {
    public static void init(){
        Intent intent = new Intent(ApplicationImpl.app,MainServiceAlive.class);
        GlobalEnv.appContext.startService(intent);
    }
}
