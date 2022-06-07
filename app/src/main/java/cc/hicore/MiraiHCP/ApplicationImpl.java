package cc.hicore.MiraiHCP;

import android.app.Application;
import android.content.Context;

import java.io.File;

public class ApplicationImpl extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        GlobalEnv.appContext = base;
        GlobalEnv.FilePath = base.getFilesDir().getAbsolutePath();
        new File(base.getExternalCacheDir()+"/log").mkdirs();
        super.attachBaseContext(base);
    }
}
