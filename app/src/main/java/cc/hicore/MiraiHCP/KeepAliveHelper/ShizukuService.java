package cc.hicore.MiraiHCP.KeepAliveHelper;

import android.content.Intent;
import android.os.RemoteException;
import android.system.Os;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import cc.hicore.MiraiHCP.IUserService;

public class ShizukuService extends IUserService.Stub{
    /**
     * Constructor is required.
     */
    public ShizukuService() {
        new Thread(this::newMonitor,"Daemon_Monitor").start();
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
                        exit();
                        return;
                    }
                    Log.d("UserService",""+i);
                    if (i == -1){
                        throw new IOException("Socket broken.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                count++;
            }
            if (count > 5)System.exit(0);
            restartService();
        }
    }
    private void restartService(){
        try {
            Log.d("UserService","HCP_Android dead ,restarting...");
            Process process = Runtime.getRuntime().exec("am startservice cc.hicore.MiraiHCP/cc.hicore.MiraiHCP.KeepAliveHelper.ServiceMonitor");
            process.waitFor();
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reserved destroy method
     */
    @Override
    public void destroy() {
        System.exit(0);
    }

    @Override
    public void exit() {
        destroy();
    }

    @Override
    public String doSomething() throws RemoteException {

        return "Normal";
    }
}
