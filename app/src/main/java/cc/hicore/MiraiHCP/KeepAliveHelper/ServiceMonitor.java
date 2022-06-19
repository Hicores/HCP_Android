package cc.hicore.MiraiHCP.KeepAliveHelper;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import cc.hicore.MiraiHCP.config.GlobalConfig;

public class ServiceMonitor extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("HCP_Android","Background_Service_Start");
        if (!GlobalConfig.getBoolean("global","keepAlive",false)){
            stopSelf();
            return;
        }
        new Thread(this::newMonitor,"HCP_Monitor").start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }

    int count = 0;
    private void newMonitor(){
        while (true){
            try {
                Socket socket = new Socket("127.0.0.1",33661);
                InputStream ins = socket.getInputStream();
                count = 0;
                while (true){
                    int i = ins.read();
                    if (i == 88){
                        stopSelf();
                        return;
                    }
                    if (i == -1){
                        throw new IOException("Socket broken.");
                    }
                }
            } catch (IOException e) {
                count++;
                e.printStackTrace();
            }
            if (count >5){
                stopSelf();
                return;
            }
            Intent intent = new Intent(this,MainServiceAlive.class);
            startService(intent);
        }
    }
}
