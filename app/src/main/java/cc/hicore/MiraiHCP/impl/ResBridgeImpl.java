package cc.hicore.MiraiHCP.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import cc.hicore.HCPBridge.api.HCPResUtils;
import cc.hicore.MiraiHCP.GlobalEnv;
import cc.hicore.MiraiHCP.data.HCPPlugin;
import cc.hicore.Utils.DataUtils;

public class ResBridgeImpl implements HCPResUtils {
    private HCPPlugin plugin;
    public ResBridgeImpl(HCPPlugin plugin){
        this.plugin = plugin;
    }
    @Override
    public InputStream openRes(String s) {
        String IDMD5 = DataUtils.getStrMD5(plugin.id + "_ID");
        String dest = GlobalEnv.FilePath + "/PluginBin/" + IDMD5 + "/res/" + s;
        FileInputStream ins;
        try {
            ins = new FileInputStream(dest);
            return ins;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
