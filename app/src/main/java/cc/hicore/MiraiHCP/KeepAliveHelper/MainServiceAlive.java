package cc.hicore.MiraiHCP.KeepAliveHelper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import cc.hicore.MiraiHCP.R;
import cc.hicore.MiraiHCP.config.GlobalConfig;

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
        new Thread(this::SocketMonitor).start();
        new Thread(this::killServiceMonitor).start();

        Intent intent = new Intent(this,ServiceMonitor.class);
        startService(intent);

    }
    private ArrayList<Socket> cacheSocket = new ArrayList<>();
    ServerSocket server;
    private void SocketMonitor(){
        try {
            server = new ServerSocket(33661);
            while (true){
                Socket socket = server.accept();
                cacheSocket.add(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void killServiceMonitor(){
        while (true){
            if (!GlobalConfig.getBoolean("global","keepAlive",false)){
                for (Socket socket :cacheSocket){
                    try{
                        OutputStream out = socket.getOutputStream();
                        out.write(88);
                        out.flush();
                        socket.close();
                    }catch (Exception e){

                    }
                }
                try {
                    server.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                stopSelf();
                return;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
