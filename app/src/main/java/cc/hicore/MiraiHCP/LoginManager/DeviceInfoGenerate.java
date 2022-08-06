package cc.hicore.MiraiHCP.LoginManager;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.util.Locale;
import java.util.UUID;

import cc.hicore.MiraiHCP.LogHelper.LogUtils;
import cc.hicore.Utils.FileUtils;
import cc.hicore.Utils.NameUtils;

public class DeviceInfoGenerate {
    public static void initDeviceInfo(Context context){
        try{
            if (!new File(context.getFilesDir()+"/device.json").exists()){
                String newDeviceInfo = collectDeviceInfo();
                FileUtils.WriteToFile(context.getFilesDir()+"/device.json",newDeviceInfo);
            }
        }catch (Exception e){
            LogUtils.error("DeviceIniter", Log.getStackTraceString(e));
        }

    }
    private static String collectDeviceInfo(){
        JSONObject obj = new JSONObject();
        try{
            obj.put("deviceInfoVersion",2);
            JSONObject data = new JSONObject();
            data.put("display", Build.DISPLAY);
            data.put("product",Build.PRODUCT);
            data.put("device",Build.DEVICE);
            data.put("board",Build.BOARD);
            data.put("brand",Build.BRAND);
            data.put("model",Build.MODEL);
            data.put("bootloader",Build.BOOTLOADER);
            data.put("bootId", UUID.randomUUID().toString());
            data.put("fingerprint",Build.FINGERPRINT);
            data.put("procVersion",getKernelVersion());
            data.put("baseBand","");
            JSONObject version = new JSONObject();
            version.put("incremental",Build.VERSION.INCREMENTAL);
            version.put("release",Build.VERSION.RELEASE);
            version.put("codename",Build.VERSION.CODENAME);
            data.put("version",version);
            data.put("simInfo","T-Mobile");
            data.put("osType","android");
            data.put("macAddress","02:00:00:00:00:00");
            data.put("wifiBSSID","02:00:00:00:00:00");
            data.put("wifiSSID","<unknown ssid>");
            data.put("apn","wifi");
            data.put("imei", NameUtils.getRandomNumber(15));
            data.put("imsiMd5",NameUtils.getRamdomMD5(32).toLowerCase(Locale.ROOT));
            obj.put("data",data);
            return obj.toString(4);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    private static String getKernelVersion() {
        try{
            String readResult = FileUtils.ReadFileString("/proc/version");
            int index = readResult.indexOf(")");
            return readResult.substring(0,index+1);
        }catch (Exception e){
            return "";
        }

    }
}
