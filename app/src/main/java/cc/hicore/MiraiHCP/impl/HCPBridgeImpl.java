package cc.hicore.MiraiHCP.impl;

import java.util.List;

import cc.hicore.HCPBridge.api.HCPBridge;
import cc.hicore.HCPBridge.api.HCPResUtils;
import cc.hicore.HCPBridge.data.BaseSession;
import cc.hicore.HCPBridge.data.event.EventForExit;
import cc.hicore.HCPBridge.data.event.EventForMute;
import cc.hicore.HCPBridge.data.event.EventForRequestJoin;
import cc.hicore.HCPBridge.data.info.FriendInfo;
import cc.hicore.HCPBridge.data.info.GroupInfo;
import cc.hicore.HCPBridge.data.info.GroupMemberInfo;
import cc.hicore.HCPBridge.data.msg.BaseChatMsg;

public class HCPBridgeImpl implements HCPBridge {
    @Override
    public void sendMsg(BaseChatMsg baseChatMsg) {

    }

    @Override
    public void kick(EventForExit eventForExit) {

    }

    @Override
    public void mute(EventForMute eventForMute) {

    }

    @Override
    public void exit(BaseSession baseSession) {

    }

    @Override
    public void handlerJoin(EventForRequestJoin eventForRequestJoin, boolean b, String s) {

    }

    @Override
    public List<GroupInfo> getGroupList(BaseSession baseSession) {
        return null;
    }

    @Override
    public List<GroupMemberInfo> getMemberList(BaseSession baseSession) {
        return null;
    }

    @Override
    public List<GroupMemberInfo> getNewMemberList(BaseSession baseSession) {
        return null;
    }

    @Override
    public List<FriendInfo> getFriendInfos(BaseSession baseSession) {
        return null;
    }

    @Override
    public GroupInfo getGroupInfo(BaseSession baseSession) {
        return null;
    }

    @Override
    public GroupMemberInfo getGroupMemberInfo(BaseSession baseSession) {
        return null;
    }

    @Override
    public FriendInfo getFriendInfo(BaseSession baseSession) {
        return null;
    }

    @Override
    public void Revoke(BaseChatMsg baseChatMsg) {

    }

    @Override
    public List<String> getLoginUins() {
        return null;
    }

    @Override
    public HCPResUtils getResHelper() {
        return null;
    }

    @Override
    public String getSkey(String s) {
        return null;
    }

    @Override
    public String getPsKey(String s, String s1) {
        return null;
    }

    @Override
    public String getGlobalConfigPath(String s) {
        return null;
    }

    @Override
    public void log(int i, String s) {

    }
}
