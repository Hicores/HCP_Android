package cc.hicore.MiraiHCP.KeepAliveHelper;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.Socket;

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
        new Thread(this::newMonitor).start();
    }
    private void newMonitor(){
        while (true){
            try {
                Socket socket = new Socket("127.0.0.1",33661);
                byte[] b = new byte[1024];
                socket.getInputStream().read(b);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(this,MainServiceAlive.class);
            startService(intent);
        }
    }
}
