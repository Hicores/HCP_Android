package cc.hicore.MiraiHCP.data;

import org.json.JSONObject;

import cc.hicore.HCPBridge.api.HCPBridge;
import cc.hicore.HCPBridge.api.HCPResUtils;
import cc.hicore.HCPBridge.data.IHCPEvent;

public class HCPPlugin {
    //基础信息
    public String authorName;
    public String pluginName;
    public String version;
    public int reqPermission;
    public String desc;
    //运行信息
    public boolean isRunning;
    public boolean isLoaded;
    public boolean isRemoved;
    public HCPBridge bridge;
    public HCPResUtils resUtils;
    public IHCPEvent eventReceiver;
}
