package cc.hicore.MiraiHCP.LoginManager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.event.events.BotOfflineEvent;
import net.mamoe.mirai.event.events.BotOnlineEvent;
import net.mamoe.mirai.network.LoginFailedException;
import net.mamoe.mirai.network.UnsupportedSliderCaptchaException;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.LoginSolver;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import cc.hicore.MiraiHCP.GlobalEnv;
import cc.hicore.MiraiHCP.R;
import cc.hicore.MiraiHCP.config.GlobalConfig;
import cc.hicore.Utils.HttpUtils;
import kotlin.coroutines.Continuation;

public class LoginManager {
    public static class BotStatus{
        int LoginStatus;
        Bot botInstance;
        String AccountUin;

        private String Name;
        private String AvatarUrl;

        public String getName(){
            if (botInstance != null && botInstance.isOnline()){
                Name = botInstance.getNick();
                return Name;
            }
            if (Name != null)return Name;
            new Thread(this::initAvatarAndName).start();
            return "";
        }
        public Bot getBot(){
            return botInstance;
        }
        public String getStatus(){
            if (LoginStatus == 1)return "正在登录";
            if (LoginStatus == 2)return "离线";
            if (LoginStatus == 0)return "未登录";
            if (LoginStatus == 3)return "等待验证";
            if (LoginStatus == 4)return "登录失败";
            if (LoginStatus == 5)return "内部错误";
            if (LoginStatus == 6)return "已登录";
            return "未知状态";
        }
        public String getAvatarLink(){
            if (botInstance != null && botInstance.isOnline()){
                AvatarUrl = botInstance.getAvatarUrl();
                return AvatarUrl;
            }
            if (AvatarUrl != null)return AvatarUrl;
            new Thread(this::initAvatarAndName).start();
            return "";
        }
        private void initAvatarAndName(){
            if (TextUtils.isEmpty(AvatarUrl)){
                try{
                    String Content = HttpUtils.getContent("https://r.qzone.qq.com/fcg-bin/cgi_get_portrait.fcg?uins="+AccountUin);
                    JSONObject newJson = new JSONObject(Content.substring(Content.indexOf("(")+1,Content.lastIndexOf(")")));
                    JSONArray mArray = newJson.getJSONArray(AccountUin);
                    AvatarUrl = mArray.getString(0);
                    Name = mArray.getString(6);
                }catch (Exception e){}
            }
        }
    }
    public static HashMap<String, BotStatus> addBots = new HashMap<>();
    public static void addNewAccountDialog(Context context){
        LinearLayout mRoot = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.dialog_account_set,null);

        RadioButton btn_form_android = mRoot.findViewById(R.id.Login_Form_Android);
        RadioButton btn_form_ipad = mRoot.findViewById(R.id.Login_Form_ipad);
        RadioButton btn_form_watch = mRoot.findViewById(R.id.Login_Form_Watch);
        btn_form_android.setChecked(true);

        CheckBox btn_save_pass = mRoot.findViewById(R.id.Login_Save_Password);
        CheckBox btn_auto_login = mRoot.findViewById(R.id.Login_Auto_Login);

        EditText edit_accountUin = mRoot.findViewById(R.id.Login_Input_QQNumber);
        EditText edit_password = mRoot.findViewById(R.id.Login_Input_Password);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("添加账号")
                .setView(mRoot)
                .setNegativeButton("确定", (dialogInterface, i) -> {
                    String AccountUin = edit_accountUin.getText().toString();
                    String Password = edit_password.getText().toString();
                    if (AccountUin.length() < 5 || AccountUin.length() > 10){
                        Toast.makeText(context, "账号输入有误", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (Password.length() < 6){
                        Toast.makeText(context, "密码输入有误", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //检测是否勾选保存密码
                    if (btn_save_pass.isChecked()){
                        GlobalConfig.putString(AccountUin,"pass",Password);
                    }
                    //检测是否勾选自动登录
                    if (btn_auto_login.isChecked()){
                        GlobalConfig.putBoolean(AccountUin,"autoLogin",true);
                    }
                    //判断协议类型并保存
                    if (btn_form_android.isChecked()){
                        GlobalConfig.putInt(AccountUin,"Use_Form",1);
                    }else if (btn_form_ipad.isChecked()){
                        GlobalConfig.putInt(AccountUin,"Use_Form",2);
                    }else if (btn_form_watch.isChecked()){
                        GlobalConfig.putInt(AccountUin,"Use_Form",3);
                    }

                    BotStatus newStatus = new BotStatus();
                    newStatus.AccountUin = AccountUin;
                    addBots.put(AccountUin,newStatus);
                    addAccountInList(AccountUin);
                    //如果勾选了自动登录则进行自动登录
                    if (btn_auto_login.isChecked()){
                        newLoginAccount(context,AccountUin,Password);
                    }
                }).create();
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        dialog.show();
    }
    public static void removeAccountFromList(String Account){
        if (addBots.containsKey(Account)){
            BotStatus status = addBots.get(Account);
            if (status != null && status.botInstance != null){
                if (status.botInstance.isOnline()){
                    status.botInstance.close();
                }
            }
        }

        List<String> accounts = GlobalConfig.getList("global","accountList");
        accounts.remove(Account);
        GlobalConfig.putList("global","accountList",accounts);
    }
    private static void addAccountInList(String Account){
        List<String> accounts = GlobalConfig.getList("global","accountList");
        if (!accounts.contains(Account)){
            accounts.add(Account);
        }
        GlobalConfig.putList("global","accountList",accounts);
    }
    @SuppressLint("UnsafeOptInUsageError")
    public static void newLoginAccount(Context context, String AccountUin, String Password){
        LoginSolver solver = new LoginSolver() {
            @Nullable
            @Override
            public Object onSolvePicCaptcha(@NonNull Bot bot, @NonNull byte[] bytes, @NonNull Continuation<? super String> continuation) {
                AtomicReference<String> onSolveResult = new AtomicReference<>();
                AtomicInteger lockerForSolve = new AtomicInteger();
                AtomicBoolean unlock = new AtomicBoolean();
                LoginSolverDialog.onSolvePictureCaptcha(context, bytes, result -> {
                    onSolveResult.getAndSet(result);
                    unlock.getAndSet(true);
                });

                while (lockerForSolve.incrementAndGet() < 120){
                    if (unlock.get()){
                        if (onSolveResult.get() == null){
                            throw new UnsupportedSliderCaptchaException("取消验证");
                        }
                        return onSolveResult.get();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                throw new UnsupportedSliderCaptchaException("超时未进行验证");
            }

            @Nullable
            @Override
            public Object onSolveSliderCaptcha(@NonNull Bot bot, @NonNull String s, @NonNull Continuation<? super String> continuation) {
                AtomicReference<String> onSolveResult = new AtomicReference<>();
                AtomicInteger lockerForSolve = new AtomicInteger();
                AtomicBoolean unlock = new AtomicBoolean();
                LoginSolverDialog.onSolveSlideCaptcha(context, s, result -> {
                    onSolveResult.getAndSet(result);
                    unlock.getAndSet(true);
                });

                while (lockerForSolve.incrementAndGet() < 120){
                    if (unlock.get()){
                        if (onSolveResult.get() == null){
                            throw new UnsupportedSliderCaptchaException("取消验证");
                        }
                        return onSolveResult.get();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                throw new UnsupportedSliderCaptchaException("超时未进行验证");
            }

            @Nullable
            @Override
            public Object onSolveUnsafeDeviceLoginVerify(@NonNull Bot bot, @NonNull String s, @NonNull Continuation<? super String> continuation) {
                AtomicReference<String> onSolveResult = new AtomicReference<>();
                AtomicInteger lockerForSolve = new AtomicInteger();
                AtomicBoolean unlock = new AtomicBoolean();
                LoginSolverDialog.onDeviceProtectCaptcha(context, s, result -> {
                    onSolveResult.getAndSet(result);
                    unlock.getAndSet(true);
                });

                while (lockerForSolve.incrementAndGet() < 120){
                    if (unlock.get()){
                        if (onSolveResult.get() == null){
                            throw new UnsupportedSliderCaptchaException("取消验证");
                        }
                        return "6666";
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                throw new UnsupportedSliderCaptchaException("超时未进行验证");
            }

            @Override
            public boolean isSliderCaptchaSupported() {
                return true;
            }
        };
        Bot newBotInstance = BotFactory.INSTANCE.newBot(Long.parseLong(AccountUin),Password,new BotConfiguration(){{
            setLoginSolver(solver);
            setCacheDir(context.getFilesDir());
            fileBasedDeviceInfo(context.getFilesDir()+"/device.json");
            redirectBotLogToFile(new File(context.getExternalCacheDir()+"/log","mirai.log"));
            redirectNetworkLogToFile(new File(context.getExternalCacheDir()+"/log","mirai_net.log"));

            int form = GlobalConfig.getInt(AccountUin,"Use_Form",1);
            if (form == 1){
                setProtocol(MiraiProtocol.ANDROID_PHONE);
            }else if (form == 2){
                setProtocol(MiraiProtocol.IPAD);
            }else if (form == 3){
                setProtocol(MiraiProtocol.ANDROID_WATCH);
            }
        }});
        BotStatus status = addBots.get(AccountUin);
        if (status != null){
            status.botInstance = newBotInstance;
            new Thread(()->{
                status.LoginStatus = 1;
                try {
                    newBotInstance.login();
                    newBotInstance.getEventChannel().subscribeAlways(BotOfflineEvent.class,LoginManager::onOfflineEvent);
                    newBotInstance.getEventChannel().subscribeAlways(BotOnlineEvent.class,LoginManager::onlineEvent);
                    registerProcessorEvent(newBotInstance);
                    status.LoginStatus = 6;
                }catch (LoginFailedException e){
                    status.LoginStatus = 4;
                }catch (Throwable th){
                    status.LoginStatus = 5;
                    newBotInstance.close();
                }
            }).start();
        }
    }
    private static void onOfflineEvent(BotOfflineEvent event){
        String Account = String.valueOf(event.getBot().getId());
        BotStatus status = addBots.get(Account);
        if (status != null){
            status.LoginStatus = 2;
        }
    }
    private static void onlineEvent(BotOnlineEvent event){
        String Account = String.valueOf(event.getBot().getId());
        BotStatus status = addBots.get(Account);
        if (status != null){
            status.LoginStatus = 6;
        }
    }
    public static void loadAllAccount(){
        List<String> accounts = GlobalConfig.getList("global","accountList");
        for (String AccountUin : accounts){
            BotStatus status = new BotStatus();
            status.AccountUin = AccountUin;
            addBots.put(AccountUin,status);
        }
    }
    public static void loginAutoLogin(Context context){
        for (String AccountUin : addBots.keySet()){
            if (GlobalConfig.getBoolean(AccountUin,"autoLogin",false)){
                String Password = GlobalConfig.getString(AccountUin,"pass","");
                Bot bot = addBots.get(AccountUin).botInstance;
                if (bot == null){
                    newLoginAccount(context,AccountUin,Password);
                }
            }
        }
    }
    private static void registerProcessorEvent(Bot bot){

    }
}
