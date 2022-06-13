package cc.hicore.MiraiHCP;

import android.content.Context;

import java.util.concurrent.atomic.AtomicBoolean;

import cc.hicore.HCPBridge.api.PERMISSION;

public class GlobalEnv {
    public static Context appContext;
    public static String FilePath;
    public static final AtomicBoolean IsInited = new AtomicBoolean();
    public static final int AVAIL_PERMISSION = PERMISSION.FRIEND_MSG | PERMISSION.GROUP_MANAGER | PERMISSION.GROUP_EXIT | PERMISSION.GROUP_MSG | PERMISSION.GET_COOKIE;
}
