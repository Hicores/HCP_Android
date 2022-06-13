package cc.hicore.MiraiHCP;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import cc.hicore.MiraiHCP.LoginManager.LoginManager;
import cc.hicore.MiraiHCP.PluginManager.PluginManager;
import cc.hicore.MiraiHCP.data.HCPPlugin;
import cc.hicore.Utils.DataUtils;
import cc.hicore.Utils.FileUtils;
import cc.hicore.Utils.HttpUtils;
import cc.hicore.Utils.ToastUtils;

public class MainActivity extends AppCompatActivity {
    private static final HandlerThread worker = new HandlerThread("HCP_Android_Worker");
    private static final Handler handler;
    static {
        worker.start();
        handler = new Handler(worker.getLooper());
    }
    private LinearLayout AccountList;
    private LinearLayout PluginList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //创建日志文件夹
        new File(getCacheDir() + "/log").mkdirs();
        //设置窗口风格为全屏风格
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        registerTabEvent();
        registerSetButtonEvent();
        registerCallback();


        //这里判断是否已经登录,后续可能加上一些使用服务来进行保活的操作,有可能在Activity没创建之前就进行了登录,所以就需要判断一下防止重复加载登录
        if (!GlobalEnv.IsInited.getAndSet(true)){
            LoginManager.loadAllAccount();
            LoginManager.loginAutoLogin(this);
        }
        AccountList = findViewById(R.id.Main_Account_List);
        PluginList = findViewById(R.id.Main_Plugin_List);
        handler.postDelayed(this::onFlushList,1000);
        handler.postDelayed(this::onFlushPluginList,1000);
    }
    AtomicBoolean isShow = new AtomicBoolean();
    @SuppressLint("SetTextI18n")
    private void onFlushList(){
        if (isShow.get()){
            new Handler(Looper.getMainLooper()).post(()->{
                if (AccountList.getChildCount() != LoginManager.addBots.size()){
                    AccountList.removeAllViews();
                    for (String AccountUin : LoginManager.addBots.keySet()){
                        LoginManager.BotStatus status = LoginManager.addBots.get(AccountUin);
                        if (status != null){
                            RelativeLayout newItem = (RelativeLayout) getLayoutInflater().inflate(R.layout.account_item,null);
                            newItem.setTag(status);
                            newItem.setOnClickListener(v->LoginManager.onAccountItemClick(this,status));
                            AccountList.addView(newItem);
                        }
                    }
                }
                for (int i=0;i < AccountList.getChildCount();i++){
                    View v = AccountList.getChildAt(i);
                    if (v instanceof RelativeLayout){
                        RelativeLayout mItemLayout = (RelativeLayout) v;
                        LoginManager.BotStatus status = (LoginManager.BotStatus) mItemLayout.getTag();
                        if (status != null) {
                            ImageView avatarImage = mItemLayout.findViewById(R.id.Account_Item_Header);
                            String avaPath = updateAvatarCache(status.getAvatarLink());
                            if (avaPath != null && !avaPath.equals(avatarImage.getTag())){
                                avatarImage.setBackground(Drawable.createFromPath(avaPath));
                                avatarImage.setTag(avaPath);
                            }

                            TextView nameView = mItemLayout.findViewById(R.id.Account_Item_Uin);
                            nameView.setText(status.getName() + "(" + status.AccountUin + ")");

                            TextView statusView = mItemLayout.findViewById(R.id.Account_Item_Status);
                            statusView.setText("状态:" + status.getStatus());
                        }
                    }
                }
            });
        }
        if (!isDestroyed()){
            handler.postDelayed(this::onFlushList,1000);
        }
    }
    @SuppressLint("SetTextI18n")
    private void onFlushPluginList(){
        if (isShow.get()){
            new Handler(Looper.getMainLooper()).post(()->{
                if (PluginList.getChildCount() != PluginManager.pluginInfo.size()){
                    PluginList.removeAllViews();
                    for (String id : PluginManager.pluginInfo.keySet()){
                        HCPPlugin plugin = PluginManager.pluginInfo.get(id);
                        if (plugin != null){
                            ViewGroup plugin_item = (ViewGroup) getLayoutInflater().inflate(R.layout.plugin_item,null);
                            plugin_item.setTag(plugin);
                            PluginList.addView(plugin_item);
                            plugin_item.setOnClickListener(v->{
                                PluginManager.showPluginControlDialog(this,id);
                            });

                            ImageView plugin_icon = plugin_item.findViewById(R.id.Plugin_Item_Icon);
                            plugin_icon.setBackground(Drawable.createFromPath(PluginManager.getPluginIconPath(plugin.id)));
                        }
                    }
                }

                for (int i=0;i<PluginList.getChildCount();i++){
                    ViewGroup plugin_item = (ViewGroup) PluginList.getChildAt(i);
                    TextView plugin_name = plugin_item.findViewById(R.id.Plugin_Item_Name);

                    TextView plugin_port = plugin_item.findViewById(R.id.Plugin_Item_Port);
                    HCPPlugin plugin = (HCPPlugin) plugin_item.getTag();

                    plugin_name.setText(plugin.pluginName+"("+plugin.version+")");
                    if (plugin.isRunning && plugin.eventReceiver != null){
                        plugin_port.setText("管理端口:"+plugin.eventReceiver.getPort());
                    }else {
                        plugin_port.setText("管理端口:不可用");
                    }
                }
            });
        }
        if (!isDestroyed()){
            handler.postDelayed(this::onFlushPluginList,1000);
        }
    }
    private String updateAvatarCache(String Link){
        if (TextUtils.isEmpty(Link))return null;
        String MD5 = DataUtils.getStrMD5(Link);
        File cachePath = new File(getCacheDir()+"/avatar",MD5);
        if (cachePath.exists())return cachePath.getAbsolutePath();
        new Thread(()-> HttpUtils.DownloadToFile(Link,cachePath.getAbsolutePath())).start();
        return null;
    }
    @Override
    protected void onResume() {
        super.onResume();
        isShow.getAndSet(true);
    }
    @Override
    protected void onPause() {
        super.onPause();
        isShow.getAndSet(false);
    }
    private void add_new_account_click(){
        LoginManager.addNewAccountDialog(this);
    }
    private void add_new_plugin_click(){
        new AlertDialog.Builder(this)
                .setTitle("选择添加方式")
                .setItems(new String[]{"在线下载", "本地添加"}, (dialog, which) -> {
                    if (which == 0){
                        ToastUtils.ShowToast(this,"未实装");

                    }else if (which == 1){
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*/*");
                        launcher_input_hcp.launch(intent);
                    }
                }).show();

    }
    //注册Tab栏切换事件
    private void registerTabEvent(){
        TextView account_tab = findViewById(R.id.Tab_Account_List);
        TextView plugin_tab = findViewById(R.id.Tab_Plugin);
        TextView set_tab = findViewById(R.id.Tab_Set);

        View account_view = findViewById(R.id.Main_Account_View);
        View plugin_view = findViewById(R.id.Main_Plugin_View);
        View set_view = findViewById(R.id.Main_Set_View);

        View add_new_button = findViewById(R.id.Main_Add_New_Button);

        AtomicBoolean isNewAccount = new AtomicBoolean(true);

        account_tab.setOnClickListener(v->{
            account_view.setVisibility(View.VISIBLE);
            plugin_view.setVisibility(View.GONE);
            set_view.setVisibility(View.GONE);

            add_new_button.setVisibility(View.VISIBLE);
            isNewAccount.getAndSet(true);

            account_tab.setBackgroundColor(Color.parseColor("#aa9999"));
            plugin_tab.setBackgroundColor(getResources().getColor(R.color.back_main_tab,null));
            set_tab.setBackgroundColor(getResources().getColor(R.color.back_main_tab,null));

        });

        plugin_tab.setOnClickListener(v->{
            account_view.setVisibility(View.GONE);
            plugin_view.setVisibility(View.VISIBLE);
            set_view.setVisibility(View.GONE);

            add_new_button.setVisibility(View.VISIBLE);
            isNewAccount.getAndSet(false);

            account_tab.setBackgroundColor(getResources().getColor(R.color.back_main_tab,null));
            plugin_tab.setBackgroundColor(Color.parseColor("#aa9999"));
            set_tab.setBackgroundColor(getResources().getColor(R.color.back_main_tab,null));

        });

        set_tab.setOnClickListener(v->{
            account_view.setVisibility(View.GONE);
            plugin_view.setVisibility(View.GONE);
            set_view.setVisibility(View.VISIBLE);

            add_new_button.setVisibility(View.GONE);

            account_tab.setBackgroundColor(getResources().getColor(R.color.back_main_tab,null));
            plugin_tab.setBackgroundColor(getResources().getColor(R.color.back_main_tab,null));
            set_tab.setBackgroundColor(Color.parseColor("#aa9999"));

        });

        add_new_button.setOnClickListener(v->{
            if (isNewAccount.get()){
                add_new_account_click();
            }else {
                add_new_plugin_click();
            }
        });
    }
    private void registerSetButtonEvent(){
        //设备信息设置
        findViewById(R.id.Main_Set_Button_Edit_Devinfo).setOnClickListener(v->{
            Intent intent = new Intent(this,EditTextFileActivity.class);
            intent.putExtra("type",1);
            intent.putExtra("path",getFilesDir()+"/device.json");
            startActivity(intent);
        });
        findViewById(R.id.Main_Set_Button_Output_Devinfo).setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_TITLE,"device.json");
            launcher_output_devInfo.launch(intent);
        });
        findViewById(R.id.Main_Set_Button_Input_Devinfo).setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            launcher_input_devInfo.launch(intent);
        });
        //日志按钮事件
        findViewById(R.id.Main_Set_Button_View_Bridge_Log).setOnClickListener(v->{
            Intent intent = new Intent(this,EditTextFileActivity.class);
            intent.putExtra("type",2);
            intent.putExtra("path",getCacheDir()+"/log/bridge.log");
            startActivity(intent);
        });

        findViewById(R.id.Main_Set_Button_View_PluginLog).setOnClickListener(v->{
            Intent intent = new Intent(this,EditTextFileActivity.class);
            intent.putExtra("type",2);
            intent.putExtra("path",getCacheDir()+"/log/plugin.log");
            startActivity(intent);
        });
        findViewById(R.id.Main_Set_Button_Output_MiraiLog).setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_TITLE,"mirai.zip");
            launcher_output_miraiLog.launch(intent);
        });
        findViewById(R.id.Main_Set_Button_Clean_All_Log).setOnClickListener(v->{
            FileUtils.deleteFile(new File(getCacheDir() + "/log"));
            new File(getCacheDir() + "/log").mkdirs();
            ToastUtils.ShowToast(this,"已清除日志");
        });
        //后台设置按钮事件
        findViewById(R.id.Main_Set_Button_Battery_While).setOnClickListener(v->{
            if (!isIgnoringBatteryOptimizations()){
                requestWhileList();
            }
        });
        findViewById(R.id.Main_Set_Button_Keep_Alive).setOnClickListener(v->{
            ToastUtils.ShowToast(this,"请自行设置");

            Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        });
    }
    ActivityResultLauncher<Intent> launcher_output_devInfo;
    ActivityResultLauncher<Intent> launcher_input_devInfo;
    ActivityResultLauncher<Intent> launcher_output_miraiLog;
    ActivityResultLauncher<Intent> launcher_input_hcp;
    private void registerCallback(){
        launcher_output_devInfo = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK){
                try{
                    Uri u = result.getData().getData();
                    OutputStream out = getContentResolver().openOutputStream(u);
                    out.write(FileUtils.ReadFile(new File(getFilesDir() + "/device.json")));
                    out.close();
                    ToastUtils.ShowToast(this,"导出成功");
                }catch (Exception e){
                    ToastUtils.ShowToast(this,"导出失败");
                }
            }
        });
        launcher_input_devInfo = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK){
                try{
                    Uri uri = result.getData().getData();
                    InputStream ins = getContentResolver().openInputStream(uri);
                    byte[] arr = DataUtils.readAllBytes(ins);
                    ins.close();
                    try{
                        JSONObject json = new JSONObject(new String(arr));
                    }catch (Exception e){
                        ToastUtils.ShowToast(this,"不是有效的JSON文件");
                        return;
                    }
                    FileUtils.WriteToFile(getFilesDir() + "/device.json",arr);
                    ToastUtils.ShowToast(this,"导入成功");
                }catch (Exception e){
                    ToastUtils.ShowToast(this,"导入失败");
                }
            }
        });
        launcher_output_miraiLog = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK){
                try{
                    Uri u = result.getData().getData();
                    OutputStream out = getContentResolver().openOutputStream(u);
                    ZipOutputStream zipOut = new ZipOutputStream(out);
                    if (new File(getCacheDir()+"/log/mirai.log").exists()){
                        ZipEntry zipEntry = new ZipEntry("mirai.log");
                        zipOut.putNextEntry(zipEntry);
                        zipOut.write(FileUtils.ReadFile(new File(getCacheDir()+"/log/mirai.log")));
                    }
                    if (new File(getCacheDir()+"/log/mirai_net.log").exists()){
                        ZipEntry zipEntry = new ZipEntry("mirai_net.log");
                        zipOut.putNextEntry(zipEntry);
                        zipOut.write(FileUtils.ReadFile(new File(getCacheDir()+"/log/mirai_net.log")));
                    }
                    zipOut.close();
                    ToastUtils.ShowToast(this,"导出成功");
                }catch (Exception e){
                    ToastUtils.ShowToast(this,"导出失败");
                }
            }
        });
        launcher_input_hcp = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK){
                try{
                    Uri uri = result.getData().getData();
                    InputStream ins = getContentResolver().openInputStream(uri);
                    String cacheHCP = getCacheDir()+"/" + Math.random();
                    FileOutputStream out = new FileOutputStream(cacheHCP);
                    DataUtils.copyIns(ins,out);
                    out.close();
                    PluginManager.PreloadHCPDialog(this,cacheHCP);
                }catch (Exception e){
                    ToastUtils.ShowToast(this,"导入失败:\n"+e);
                }
            }
        });
    }
    private boolean isIgnoringBatteryOptimizations() {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        return isIgnoring;
    }
    private void requestWhileList(){
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:"+getPackageName()));
            startActivity(intent);
        }catch (Exception e){

        }
    }
}