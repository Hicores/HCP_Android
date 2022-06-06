package cc.hicore.MiraiHCP;

import android.app.Application;
import android.content.Context;

public class ApplicationImpl extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        GlobalEnv.appContext = base;
        GlobalEnv.FilePath = base.getFilesDir().getAbsolutePath();
        super.attachBaseContext(base);
    }
}
