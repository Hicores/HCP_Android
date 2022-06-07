package cc.hicore.MiraiHCP;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import cc.hicore.MiraiHCP.LoginManager.LoginManager;
import cc.hicore.Utils.DataUtils;
import cc.hicore.Utils.FileUtils;

public class MainActivity extends AppCompatActivity {
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
    }

    private void add_new_account_click(){
        LoginManager.addNewAccountDialog(this);
    }
    private void add_new_plugin_click(){

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

        AtomicBoolean isNewAccount = new AtomicBoolean();

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
            Toast.makeText(this, "已清除日志", Toast.LENGTH_SHORT).show();
        });
        //后台设置按钮事件
        findViewById(R.id.Main_Set_Button_Battery_While).setOnClickListener(v->{
            if (!isIgnoringBatteryOptimizations()){
                requestWhileList();
            }
        });
        findViewById(R.id.Main_Set_Button_Keep_Alive).setOnClickListener(v->{
            Toast.makeText(this, "请自行设置", Toast.LENGTH_LONG).show();

            Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        });
    }
    ActivityResultLauncher<Intent> launcher_output_devInfo;
    ActivityResultLauncher<Intent> launcher_input_devInfo;
    ActivityResultLauncher<Intent> launcher_output_miraiLog;
    private void registerCallback(){
        launcher_output_devInfo = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK){
                try{
                    Uri u = result.getData().getData();
                    OutputStream out = getContentResolver().openOutputStream(u);
                    out.write(FileUtils.ReadFile(new File(getFilesDir() + "/device.json")));
                    out.close();
                    Toast.makeText(this, "导出成功", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(this, "导出失败", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(this, "不是有效的JSON文件", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FileUtils.WriteToFile(getFilesDir() + "/device.json",arr);
                    Toast.makeText(this, "导入成功", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(this, "导入失败", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "导出成功", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(this, "导出失败", Toast.LENGTH_SHORT).show();
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