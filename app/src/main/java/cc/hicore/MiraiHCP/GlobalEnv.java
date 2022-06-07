package cc.hicore.MiraiHCP;

import android.content.Context;

import java.util.concurrent.atomic.AtomicBoolean;

public class GlobalEnv {
    public static Context appContext;
    public static String FilePath;
    public static final AtomicBoolean IsInited = new AtomicBoolean();
}
