package cc.hicore.MiraiHCP.KeepAliveHelper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import cc.hicore.MiraiHCP.R;

public class MainServiceAlive extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannel channel = new NotificationChannel("HCP_Tip",
                "fore_service", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
        Notification notification = new NotificationCompat
                .Builder(this, "HCP_Tip")
                .setContentTitle("HCP_Android")
                .setContentText("HCP_Android正在运行..")
                .setSmallIcon(R.drawable.global_icon)
                .setWhen(System.currentTimeMillis())
                .build();
        startForeground(1, notification);
    }
}
