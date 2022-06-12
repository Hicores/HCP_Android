package cc.hicore.MiraiHCP.LoginManager;

import android.util.Log;

import net.mamoe.mirai.Bot;

import cc.hicore.MiraiHCP.LogHelper.LogUtils;
import cc.hicore.MiraiHCP.ReflectUtils.MClass;
import cc.hicore.MiraiHCP.ReflectUtils.MField;
import cc.hicore.MiraiHCP.ReflectUtils.MMethod;

public class CommonBridge {
    public static String getSkey(Bot bot){
        try{
            if (bot.getClass().getSimpleName().equals("QQAndroidBot")){
                Object client = MMethod.CallMethodNoParam(bot,"getClient", MClass.loadClass("net.mamoe.mirai.internal.network.QQAndroidClient"));
                Object wtLonginInfo = MMethod.CallMethodNoParam(client,"getWLoginSigInfo",MClass.loadClass("net.mamoe.mirai.internal.network.WLoginSigInfo"));
                byte[] mData = MField.GetField(MField.GetField(wtLonginInfo,"sKey"),"data");
                return new String(mData);
            }
        }catch (Exception e){
            LogUtils.error("KEYClient_getSkey", Log.getStackTraceString(e));
        }
        return "";
    }
    public static String getPSkey(Bot bot,String Domain){
        try{
            if (bot.getClass().getSimpleName().equals("QQAndroidBot")){
                Object client = MMethod.CallMethodNoParam(bot,"getClient", MClass.loadClass("net.mamoe.mirai.internal.network.QQAndroidClient"));
                Object wtLonginInfo = MMethod.CallMethodNoParam(client,"getWLoginSigInfo",MClass.loadClass("net.mamoe.mirai.internal.network.WLoginSigInfo"));
                String psKey = MMethod.CallMethodSingle(wtLonginInfo,"getPsKey",String.class,Domain);
                return psKey;
            }
        }catch (Exception e){
            LogUtils.error("KEYClient_getPsKey", Log.getStackTraceString(e));
        }
        return "";
    }
}
