package cc.hicore.MiraiHCP.PluginManager;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;


import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import cc.hicore.MiraiHCP.GlobalEnv;
import cc.hicore.MiraiHCP.R;
import cc.hicore.MiraiHCP.data.HCPPlugin;
import cc.hicore.Utils.DataUtils;
import cc.hicore.Utils.FileUtils;
import cc.hicore.Utils.ToastUtils;

public class PluginManager {
    public static final HashMap<String, HCPPlugin> pluginInfo = new HashMap<>();
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
                })
                .setOnDismissListener(dialog -> {
                    if (removeAuto.get()){
                        FileUtils.deleteFile(new File(cachePath));
                    }
                })
                .show();
    }
    private static String getContainPluginVersion(String PluginID){
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
        }catch (Exception e){ }

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
                        }
                    }catch (Exception e){

                    }
                }
            }
        }
    }
}
