package cc.hicore.MiraiHCP;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;
import java.util.HashSet;

import cc.hicore.Utils.DataUtils;
import cc.hicore.Utils.FileUtils;
import cc.hicore.Utils.Utils;

public class ManagerSpaceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_space);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
    }
    private void onFlushData(){

        TextView avatarView = findViewById(R.id.Manager_Space_Avatar);
        TextView logView = findViewById(R.id.Manager_Space_Log);
        TextView pluginView = findViewById(R.id.Manager_Space_Plugin_Main);
        TextView pluginCacheView = findViewById(R.id.Manager_Space_Plugin_Cache);
        TextView pluginConfigView = findViewById(R.id.Manager_Space_Plugin_Config);
        TextView shareConfigView = findViewById(R.id.Manager_Space_Plugin_Config_Share);

        new Thread(()->{
            long size = FileUtils.getDirSize(new File(getCacheDir(),"avatar"));
            Utils.PostToMain(()->avatarView.setText("头像缓存:" + Utils.bytes2kb(size)));
            long logSize = FileUtils.getDirSize(new File(getCacheDir(),"log"));
            Utils.PostToMain(()->logView.setText("日志缓存:" + Utils.bytes2kb(logSize)));
            long pluginSize = FileUtils.getDirSize(new File(getFilesDir(),"PluginBin"));
            Utils.PostToMain(()->pluginView.setText("插件本体占用："+Utils.bytes2kb(pluginSize)));
            long pluginCacheSize = FileUtils.getDirSize(new File(getCacheDir(),"PluginCache"));
            Utils.PostToMain(()->pluginCacheView.setText("插件缓存占用:"+Utils.bytes2kb(pluginCacheSize)));
            long pluginConfigSize = FileUtils.getDirSize(new File(getFilesDir(),"PluginData"));
            Utils.PostToMain(()->pluginConfigView.setText("插件配置占用:"+Utils.bytes2kb(pluginConfigSize)));
            long pluginShareConfigSize = FileUtils.getDirSize(new File(getFilesDir(),"globalConfig"));
            Utils.PostToMain(()->shareConfigView.setText("插件共享配置占用:"+Utils.bytes2kb(pluginShareConfigSize)));

        },"Space_Calc_Thread").start();

        LinearLayout mPluginList = findViewById(R.id.Manager_Space_Plugin_Item_List);
        mPluginList.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        new Thread(()->{
            HashSet<String> PluginHash = new HashSet<>();
            File[] fs = new File(getFilesDir(),"PluginBin").listFiles();
            if (fs != null){
                for (File f : fs){
                    String name = f.getName();
                    if (f.isDirectory() && name.length() > 20){
                        PluginHash.add(name);
                    }
                }
            }

            fs = new File(getFilesDir(),"PluginData").listFiles();
            if (fs != null){
                for (File f : fs){
                    String name = f.getName();
                    if (f.isDirectory() && name.length() > 20){
                        PluginHash.add(name);
                    }
                }
            }

            for (String hash : PluginHash){
                try {
                    if (new File(getFilesDir()+"/PluginBin/"+hash+"/info.bin").exists() ||
                            new File(getFilesDir()+"/PluginData/"+hash+"/info.bin").exists()){
                        long BinSize = FileUtils.getDirSize(new File(getFilesDir()+"/PluginBin/"+hash));
                        long ConfigSize = FileUtils.getDirSize(new File(getFilesDir()+"/PluginData/"+hash));
                        String readInfo = FileUtils.ReadFileString(new File(getFilesDir()+"/PluginBin/"+hash+"/info.bin"));
                        if (TextUtils.isEmpty(readInfo))readInfo = FileUtils.ReadFileString(new File(getFilesDir()+"/PluginData/"+hash+"/info.bin"));
                        JSONObject newJson = new JSONObject(readInfo);
                        Utils.PostToMain(()->{
                            ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.manager_plugin_item,null);
                            TextView PluginName = vg.findViewById(R.id.Space_Manager_Plugin_Main_Name);
                            PluginName.setText("插件名字:"+newJson.optString("name"));

                            ImageView icon = vg.findViewById(R.id.Space_Manager_Plugin_Icon);
                            icon.setImageDrawable(Drawable.createFromPath(getFilesDir()+"/PluginData/"+hash+"/icon.png"));

                            TextView pluginMainSize = vg.findViewById(R.id.Space_Manager_Plugin_Main_Size);
                            pluginMainSize.setText("插件本体大小:"+Utils.bytes2kb(BinSize));

                            TextView pluginConfigSize = vg.findViewById(R.id.Space_Manager_Plugin_ConfigSize);
                            pluginConfigSize.setText("插件配置大小:"+Utils.bytes2kb(ConfigSize));

                            Button cleanSize = vg.findViewById(R.id.Space_Manager_Plugin_Clean_Instance);
                            cleanSize.setOnClickListener(v->{
                                new AlertDialog.Builder(ManagerSpaceActivity.this)
                                        .setTitle("是否清理插件配置数据")
                                        .setMessage("清理后该插件处共享配置外的其他配置都会丢失,是否继续?\n(如果需要清理本体请在主界面清理)")
                                        .setNeutralButton("不清理", (dialog, which) -> {

                                        }).setPositiveButton("确定清理", (dialog, which) -> {
                                            FileUtils.deleteFile(new File(getFilesDir()+"/PluginData/"+hash));
                                            if (new File(getFilesDir()+"/PluginBin/"+hash+"/info.bin").exists()){
                                                FileUtils.copy(getFilesDir()+"/PluginBin/"+hash+"/res/icon.png",getFilesDir()+"/PluginData/"+hash+"/icon.png");
                                                FileUtils.copy(getFilesDir()+"/PluginBin/"+hash+"/info.bin",getFilesDir()+"/PluginData/"+hash+"/info.bin");
                                            }
                                            onFlushData();
                                        }).show();
                            });
                            mPluginList.addView(vg);
                        });
                    }
                }catch (Exception e){

                }
            }
        },"Space_Calc_Thread").start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onFlushData();
    }
}