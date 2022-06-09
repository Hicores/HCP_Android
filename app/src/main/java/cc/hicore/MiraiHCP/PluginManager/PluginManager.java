package cc.hicore.MiraiHCP.PluginManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;

import cc.hicore.MiraiHCP.R;
import cc.hicore.Utils.FileUtils;

public class PluginManager {
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

        new AlertDialog.Builder(context)
                .setTitle("添加插件")
                .setView(Plugin_Add_View)
                .setNegativeButton(isReplace ? "替换" : "添加", (dialog, which) -> {

                }).setOnDismissListener(dialog -> FileUtils.deleteFile(new File(cachePath)))
                .show();
    }

    private static String getContainPluginVersion(String PluginID){
        return null;
    }
}
