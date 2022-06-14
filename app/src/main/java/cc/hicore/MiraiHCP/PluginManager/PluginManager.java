package cc.hicore.MiraiHCP.PluginManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import cc.hicore.HCPBridge.api.PERMISSION;
import cc.hicore.HCPBridge.data.BaseSession;
import cc.hicore.HCPBridge.data.IHCPEvent;
import cc.hicore.HCPBridge.data.InitInfo;
import cc.hicore.MiraiHCP.GlobalEnv;
import cc.hicore.MiraiHCP.R;
import cc.hicore.MiraiHCP.config.GlobalConfig;
import cc.hicore.MiraiHCP.data.HCPPlugin;
import cc.hicore.MiraiHCP.impl.HCPBridgeImpl;
import cc.hicore.MiraiHCP.impl.ResBridgeImpl;
import cc.hicore.Utils.DataUtils;
import cc.hicore.Utils.FileUtils;
import cc.hicore.Utils.ToastUtils;
import dalvik.system.PathClassLoader;

public class PluginManager {
    public static final HashMap<String, HCPPlugin> pluginInfo = new HashMap<>();
    public static void showPluginControlDialog(Context context,String PluginID){
        HCPPlugin plugin = pluginInfo.get(PluginID);
        if (plugin != null){
            ViewGroup ControlData = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.plugin_item_control,null);
            TextView statusView = ControlData.findViewById(R.id.Plugin_Control_Status);
            TextView authorView = ControlData.findViewById(R.id.Plugin_Control_Author);
            TextView outputDescView = ControlData.findViewById(R.id.Plugin_Control_Output_Desc);
            TextView descView = ControlData.findViewById(R.id.Plugin_Control_Desc);
            statusView.setText("插件状态:" + (plugin.isRunning ? "已启用" : "已停用"));
            authorView.setText("插件作者:" + plugin.authorName);
            outputDescView.setText(plugin.isRunning ? ("插件输出:" + plugin.eventReceiver.onGetDesc()) : "插件输出:插件未启用时不可用");
            descView.setText("插件描述:"+plugin.desc);

            new AlertDialog.Builder(context)
                    .setTitle("插件信息")
                    .setView(ControlData)
                    .setPositiveButton(plugin.isRunning ? "停用插件" : "启用插件", (dialog, which) -> {
                        if (plugin.isRunning) disablePlugin(plugin);
                        else enablePlugin(plugin);
                    }).setNeutralButton("删除插件", (dialog, which) -> {
                        new AlertDialog.Builder(context)
                                .setTitle("确定删除插件")
                                .setMessage("你真的要删除插件 "+plugin.pluginName+" 吗?\n(仅删除本体,不删除配置数据)")
                                .setPositiveButton("确认删除", (dialog1, which1) -> {
                                    checkAndRemovePlugin(PluginID);
                                }).setNeutralButton("不删除", (dialog12, which12) -> {

                                }).show();
                    }).show();
        }
    }
    private static void enablePlugin(HCPPlugin plugin){
        if (!plugin.isLoaded){
            loadPluginInner(plugin);
        }
        if (plugin.eventReceiver != null){
            plugin.eventReceiver.onEnable();
            plugin.isRunning = true;
            GlobalConfig.putBoolean(plugin.id,"isEnable",true);
        }

    }
    private static void disablePlugin(HCPPlugin plugin){
        GlobalConfig.putBoolean(plugin.id,"isEnable",false);
        if (plugin.isLoaded){
            plugin.eventReceiver.onDisable();
        }
        plugin.isRunning = false;
    }
    private static void loadPluginInner(HCPPlugin plugin){
        try{
            String IDMD5 = DataUtils.getStrMD5(plugin.id+"_ID");

            String dexPath = GlobalEnv.FilePath + "/PluginBin/" + IDMD5+"/dex.bin";
            ClassLoader loader = new PathClassLoader(dexPath,PluginManager.class.getClassLoader());
            Class<?> clzEntry = loader.loadClass("cc.hicore.Entry");
            Method m = clzEntry.getMethod("LoadEntry", InitInfo.class);

            InitInfo initInfo = new InitInfo();
            initInfo.bridge = new HCPBridgeImpl(plugin);
            initInfo.root = GlobalEnv.FilePath +File.separator +"PluginData" +File.separator+ IDMD5 + File.separator;
            initInfo.resHelper = new ResBridgeImpl(plugin);
            initInfo.type = InitInfo.TYPE_MIRAIHCPBRIDGE_ANDROID;
            initInfo.AvailPermission = GlobalEnv.AVAIL_PERMISSION;
            initInfo.cacheRoot = GlobalEnv.appContext.getCacheDir() + "/PluginCache/" + IDMD5 + "/";

            IHCPEvent eventReceiver = (IHCPEvent) m.invoke(null,initInfo);
            if (eventReceiver != null){
                plugin.eventReceiver = eventReceiver;
                plugin.isLoaded  = true;
            }else {
                throw new RuntimeException("加载错误,插件未返回事件实现类");
            }

        }catch (Exception e){
            ToastUtils.ShowToast(GlobalEnv.appContext,"无法加载插件:\n"+e);
        }
    }
    public static void PreloadHCPDialog(Context context,String HCPPath) throws Exception {
        String cachePath = context.getCacheDir() + "/" + Math.random() + "/";
        new File(cachePath).mkdirs();
        PluginDecoder.unpackToDic(HCPPath,cachePath);
        JSONObject InfoJson = new JSONObject(FileUtils.ReadFileString(cachePath + "info.bin"));
        String Name = InfoJson.getString("name");
        String id = InfoJson.getString("id");
        String author = InfoJson.getString("author");
        String desc = InfoJson.getString("desc");
        String version = InfoJson.getString("version");

        boolean isReplace = getContainPluginVersion(id) != null;

        ViewGroup Plugin_Add_View = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.plugin_input_show_info,null);
        TextView show_name = Plugin_Add_View.findViewById(R.id.Plugin_Add_Name);
        TextView show_id = Plugin_Add_View.findViewById(R.id.Plugin_Add_ID);
        TextView show_author = Plugin_Add_View.findViewById(R.id.Plugin_Add_Author);
        TextView show_version = Plugin_Add_View.findViewById(R.id.Plugin_Add_Version);
        TextView show_desc = Plugin_Add_View.findViewById(R.id.Plugin_Add_Desc);

        show_name.setText("插件名字:"+Name);
        if (isReplace){
            show_version.setText("版本替换:"+getContainPluginVersion(id)+"->"+version);
        }else {
            show_version.setText("插件版本:"+version);
        }

        show_author.setText("插件作者:"+author);
        show_desc.setText(desc);
        show_id.setText("插件ID:"+id);

        AtomicBoolean removeAuto = new AtomicBoolean(true);
        new AlertDialog.Builder(context)
                .setTitle("添加插件")
                .setView(Plugin_Add_View)
                .setNegativeButton(isReplace ? "替换" : "添加", (dialog, which) -> {
                    removeAuto.getAndSet(false);
                    saveHCPDataToFile(context,cachePath);
                    FileUtils.deleteFile(new File(cachePath));
                })
                .setOnDismissListener(dialog -> {
                    if (removeAuto.get()){
                        FileUtils.deleteFile(new File(cachePath));
                    }
                }).show();
    }
    private static String getContainPluginVersion(String PluginID){
        if (pluginInfo.containsKey(PluginID)){
            return pluginInfo.get(PluginID).version;
        }
        return null;
    }
    public static void checkAndStopPlugin(String ID){
        HCPPlugin plugin = pluginInfo.get(ID);
        if (plugin != null && plugin.isLoaded){
            if (plugin.isRunning){
                plugin.eventReceiver.onDisable();
                plugin.isRunning = false;
            }
        }
    }
    public static void checkAndRemovePlugin(String ID){
        checkAndStopPlugin(ID);
        String IDMD5 = DataUtils.getStrMD5(ID+"_ID");
        FileUtils.deleteFile(new File(GlobalEnv.FilePath + "/PluginBin/" + IDMD5));

        HCPPlugin plugin = pluginInfo.get(ID);
        if (plugin != null){
            if (plugin.isRunning){
                plugin.isRemoved = true;
            }
            pluginInfo.remove(ID);
        }
    }
    public static void removePluginData(String ID){
        String IDMD5 = DataUtils.getStrMD5(ID+"_ID");
        FileUtils.deleteFile(new File(GlobalEnv.FilePath + "/PluginData/" + IDMD5));
    }
    private static void saveHCPDataToFile(Context context,String HCPTempPath) {
        try {
            JSONObject newJson = new JSONObject(FileUtils.ReadFileString(new File(HCPTempPath, "info.bin")));
            String ID = newJson.getString("id");
            String IDMD5 = DataUtils.getStrMD5(ID + "_ID");

            FileUtils.copy(HCPTempPath + "/info.bin", GlobalEnv.FilePath + "/PluginBin/" + IDMD5 + "/info.bin");
            FileUtils.copy(HCPTempPath + "/dex.bin", GlobalEnv.FilePath + "/PluginBin/" + IDMD5 + "/dex.bin");


            ZipInputStream zInp = new ZipInputStream(new FileInputStream(HCPTempPath + "/res.bin"));
            ZipEntry entry;
            while ((entry = zInp.getNextEntry()) != null) {
                String name = entry.getName();
                if (!name.endsWith("/")) {
                    String dest = GlobalEnv.FilePath + "/PluginBin/" + IDMD5 + "/res/" + name;
                    File f = new File(dest).getParentFile();
                    if (!f.exists()) f.mkdirs();
                    FileOutputStream out = new FileOutputStream(dest);
                    DataUtils.copyIns(zInp, out);
                    out.close();
                }
            }
            zInp.close();
            new File(GlobalEnv.FilePath + "/PluginData/" + IDMD5).mkdirs();
            FileUtils.copy(GlobalEnv.FilePath + "/PluginBin/" + IDMD5 + "/res/icon.png", GlobalEnv.FilePath + "/PluginData/" + IDMD5 + "/icon.png");
            FileUtils.copy(GlobalEnv.FilePath + "/PluginBin/" + IDMD5 + "/info.bin", GlobalEnv.FilePath + "/PluginData/" + IDMD5 + "/info.bin");

            addNewPluginToList(ID);
        } catch (Exception e) {
            ToastUtils.ShowToast(context, "无法导入插件:\n" + e);
        }
    }
    private static void addNewPluginToList(String ID){
        try{
            String IDMD5 = DataUtils.getStrMD5(ID+"_ID");
            if (new File(GlobalEnv.FilePath + "/PluginBin/" + IDMD5+"/info.bin").exists()){
                JSONObject newJson = new JSONObject(FileUtils.ReadFileString(GlobalEnv.FilePath + "/PluginBin/"+IDMD5+"/info.bin"));
                String id = newJson.getString("id");
                if (!pluginInfo.containsKey(id)){
                    String Name = newJson.getString("name");
                    String author = newJson.getString("author");
                    String desc = newJson.getString("desc");
                    String version = newJson.getString("version");

                    HCPPlugin newInfo = new HCPPlugin();
                    newInfo.pluginName = Name;
                    newInfo.desc = desc;
                    newInfo.version = version;
                    newInfo.authorName = author;
                    newInfo.id = id;
                    pluginInfo.put(id,newInfo);
                }
            }
        }catch (Exception ignored){ }

    }
    public static String  getPluginIconPath(String PluginID){
        String IDMD5 = DataUtils.getStrMD5(PluginID+"_ID");
        return GlobalEnv.FilePath + "/PluginBin/" + IDMD5+"/res/icon.png";
    }
    public static void PreLoadPluginToList(){
        File[] fs = new File( GlobalEnv.FilePath + "/PluginBin/").listFiles();
        if (fs != null){
            for (File f : fs){
                if (f.isDirectory()){
                    try{
                        JSONObject newJson = new JSONObject(FileUtils.ReadFileString(GlobalEnv.FilePath + "/PluginBin/"+f.getName()+"/info.bin"));
                        String id = newJson.getString("id");
                        if (!pluginInfo.containsKey(id)){
                            String Name = newJson.getString("name");
                            String author = newJson.getString("author");
                            String desc = newJson.getString("desc");
                            String version = newJson.getString("version");

                            HCPPlugin newInfo = new HCPPlugin();
                            newInfo.pluginName = Name;
                            newInfo.desc = desc;
                            newInfo.version = version;
                            newInfo.authorName = author;
                            newInfo.id = id;
                            pluginInfo.put(id,newInfo);

                            if (GlobalConfig.getBoolean(newInfo.id,"isEnable",false)){
                                enablePlugin(newInfo);
                            }
                        }
                    }catch (Exception e){

                    }
                }
            }
        }
    }
}
