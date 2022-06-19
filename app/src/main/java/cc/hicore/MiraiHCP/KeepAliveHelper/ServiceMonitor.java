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
            System.exit(0);
            return;
        }
        new Thread(this::newMonitor).start();

    }
    private void newMonitor(){
        while (true){
            try {
                Socket socket = new Socket("127.0.0.1",33661);
                InputStream ins = socket.getInputStream();
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
                e.printStackTrace();
            }
            Intent intent = new Intent(this,MainServiceAlive.class);
            startService(intent);
        }
    }
}
