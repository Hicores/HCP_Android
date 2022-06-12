package cc.hicore.MiraiHCP.impl;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.AtAll;
import net.mamoe.mirai.message.data.Audio;
import net.mamoe.mirai.message.data.Face;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.LightApp;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.MessageSourceBuilder;
import net.mamoe.mirai.message.data.MessageSourceKind;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.message.data.SimpleServiceMessage;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
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
import cc.hicore.HCPBridge.data.msg.MsgForAt;
import cc.hicore.HCPBridge.data.msg.MsgForCard;
import cc.hicore.HCPBridge.data.msg.MsgForMix;
import cc.hicore.HCPBridge.data.msg.MsgForPic;
import cc.hicore.HCPBridge.data.msg.MsgForQQEmo;
import cc.hicore.HCPBridge.data.msg.MsgForReply;
import cc.hicore.HCPBridge.data.msg.MsgForText;
import cc.hicore.HCPBridge.data.msg.MsgForVoice;
import cc.hicore.MiraiHCP.GlobalEnv;
import cc.hicore.MiraiHCP.LogHelper.LogUtils;
import cc.hicore.MiraiHCP.LogHelper.PluginLogUtils;
import cc.hicore.MiraiHCP.LoginManager.CommonBridge;
import cc.hicore.MiraiHCP.LoginManager.LoginManager;
import cc.hicore.MiraiHCP.data.HCPPlugin;
import cc.hicore.Utils.DataUtils;

@SuppressLint("UnsafeOptInUsageError")
public class HCPBridgeImpl implements HCPBridge {
    private HCPPlugin plugin;
    public HCPBridgeImpl(HCPPlugin plugin){
        this.plugin = plugin;
    }
    @Override
    public void sendMsg(BaseChatMsg baseChatMsg) {
        String botUin = baseChatMsg.SelfUin;
        Bot bot = LoginManager.getAvailBot(botUin);
        if (bot == null)return;
        Contact contact;
        if (baseChatMsg.type == 0){
            contact = bot.getGroup(Long.parseLong(baseChatMsg.GroupUin));
        }else if (baseChatMsg.type == 1){
            contact = bot.getFriend(Long.parseLong(baseChatMsg.UserUin));
        }else {
            contact = bot.getGroup(Long.parseLong(baseChatMsg.GroupUin)).getOrFail(Long.parseLong(baseChatMsg.UserUin));
        }

        if (baseChatMsg instanceof MsgForCard){
            MsgForCard card = (MsgForCard) baseChatMsg;
            if (card.cardType == 0){
                LightApp newApp = new LightApp(card.CardCode);
                MessageChainBuilder builder = new MessageChainBuilder();
                builder.add(newApp);
                MessageChain chain = builder.build();
                bot.getGroup(Long.parseLong(baseChatMsg.GroupUin)).sendMessage(chain);
            }else {
                SimpleServiceMessage serviceMsg = new SimpleServiceMessage(60,((MsgForCard) baseChatMsg).CardCode);
                MessageChainBuilder builder = new MessageChainBuilder();
                builder.add(serviceMsg);
                MessageChain chain = builder.build();
                bot.getGroup(Long.parseLong(baseChatMsg.GroupUin)).sendMessage(chain);
            }

        }
        else if (baseChatMsg instanceof MsgForMix){
            MsgForMix mix = (MsgForMix) baseChatMsg;
            MessageChainBuilder builder = new MessageChainBuilder();
            for (BaseChatMsg base : mix.msgList){
                if (base instanceof MsgForText){
                    builder.append(new PlainText(((MsgForText) base).text));
                }else if (base instanceof MsgForPic){
                    Image image = contact.uploadImage(ExternalResource.create(new File(((MsgForPic) base).LocalPath)));
                    builder.append(image);
                }else if (base instanceof MsgForAt){
                    MsgForAt at = (MsgForAt) base;
                    if (at.atType == 1){
                        builder.append(AtAll.INSTANCE);
                    }else if (at.atType == 0){
                        builder.append(new At(Long.parseLong(at.atTarget)));
                    }
                }else if (base instanceof MsgForQQEmo){
                    builder.append(new Face(((MsgForQQEmo) base).EmoID));
                }else if (base instanceof MsgForReply){
                    String[] id = base.messageID.split("->");
                    if (id.length > 1){
                        MessageSource source = new MessageSourceBuilder()
                                .sender(Long.parseLong(baseChatMsg.UserUin))
                                .target(Long.parseLong(baseChatMsg.GroupUin))
                                .id(StringToId(id[0]))
                                .internalId(StringToId(id[1]))
                                .messages(new PlainText(baseChatMsg.content))
                                .build(Long.parseLong(baseChatMsg.SelfUin), MessageSourceKind.GROUP);
                        builder.append(new QuoteReply(source));
                    }
                }
            }
            contact.sendMessage(builder.build());
        }
        else if (baseChatMsg instanceof MsgForPic){
            Image image = contact.uploadImage(ExternalResource.create(new File(((MsgForPic) baseChatMsg).LocalPath)));
            contact.sendMessage(image);
        }else if (baseChatMsg instanceof MsgForVoice){
            if (contact instanceof Group){
                Audio audio = ((Group) contact).uploadAudio(ExternalResource.create(new File(((MsgForVoice) baseChatMsg).localUrl)));
                contact.sendMessage(audio);
            }else if (contact instanceof Friend){
                Audio audio = ((Friend) contact).uploadAudio(ExternalResource.create(new File(((MsgForVoice) baseChatMsg).localUrl)));
                contact.sendMessage(audio);
            }

        }else if (baseChatMsg instanceof MsgForText){
            contact.sendMessage(new PlainText(((MsgForText) baseChatMsg).text));
        }
    }
    @Override
    public void kick(EventForExit eventForExit) {
        try{
            String botUin = eventForExit.SelfUin;
            Bot bot = LoginManager.getAvailBot(botUin);
            if (bot == null)return;
            if (eventForExit.type == 0){
                Group group = bot.getGroup(Long.parseLong(eventForExit.GroupUin));
                if (group != null){
                    NormalMember member = group.getOrFail(Long.parseLong(eventForExit.UserUin));
                    member.kick("",eventForExit.isBlack);
                }
            }
        }catch (Exception e){
            LogUtils.warn("HCPBridgeImpl_Kick", Log.getStackTraceString(e));
        }
    }

    @Override
    public void mute(EventForMute eventForMute) {
        try{
            String botUin = eventForMute.SelfUin;
            Bot bot = LoginManager.getAvailBot(botUin);
            if (bot == null)return;
            if (eventForMute.type == 0){
                Group group = bot.getGroup(Long.parseLong(eventForMute.GroupUin));
                if (group != null){
                    if (TextUtils.isEmpty(eventForMute.UserUin)){
                        group.getSettings().setMuteAll(eventForMute.time != 0);
                    }else{
                        NormalMember member = group.getOrFail(Long.parseLong(eventForMute.UserUin));
                        if (eventForMute.time == 0){
                            member.unmute();
                        }else {
                            member.mute((int) eventForMute.time);
                        }
                    }

                }
            }
        }catch (Exception e){
            LogUtils.warn("HCPBridgeImpl_mute", Log.getStackTraceString(e));
        }
    }

    @Override
    public void exit(BaseSession baseSession) {
        try{
            String botUin = baseSession.SelfUin;
            Bot bot = LoginManager.getAvailBot(botUin);
            if (bot == null)return;
            if (baseSession.type == 0){
                Group group = bot.getGroup(Long.parseLong(baseSession.GroupUin));
                if (group != null){
                    group.quit();
                }
            }
        }catch (Exception e){
            LogUtils.warn("HCPBridgeImpl_exit", Log.getStackTraceString(e));
        }
    }

    @Override
    public void handlerJoin(EventForRequestJoin eventForRequestJoin, boolean b, String s) {

    }

    @Override
    public List<GroupInfo> getGroupList(BaseSession baseSession) {
        List<GroupInfo> newList = new ArrayList<>();
        try{
            String botUin = baseSession.SelfUin;
            Bot bot = LoginManager.getAvailBot(botUin);
            if (bot == null)return newList;
            ContactList<Group> groups = bot.getGroups();
            for (Group g : groups){
                GroupInfo newInfo = new GroupInfo();
                newInfo.GroupUin = String.valueOf(g.getId());
                newInfo.Creator = String.valueOf(g.getOwner().getId());
                newInfo.GroupName = g.getName();
                newList.add(newInfo);
            }
        }catch (Exception e){
            LogUtils.warn("HCPBridgeImpl_getGroupList", Log.getStackTraceString(e));
        }
        return newList;
    }

    @Override
    public List<GroupMemberInfo> getMemberList(BaseSession baseSession) {
        List<GroupMemberInfo> newList = new ArrayList<>();
        try{
            String botUin = baseSession.SelfUin;
            Bot bot = LoginManager.getAvailBot(botUin);
            if (bot == null)return newList;
            ContactList<NormalMember> members = bot.getGroup(Long.parseLong(baseSession.GroupUin)).getMembers();
            for (NormalMember m : members){
                GroupMemberInfo newInfo = new GroupMemberInfo();
                newInfo.GroupUin = baseSession.GroupUin;
                newInfo.UserUin = String.valueOf(m.getId());
                newInfo.MemberCard = m.getNameCard();
                if (TextUtils.isEmpty(newInfo.MemberCard)){
                    newInfo.MemberCard = m.getNick();
                }
                newInfo.isAdmin = m.getPermission().getLevel()== 1;
                newInfo.isCreator = m.getPermission().getLevel() == 2;
                newInfo.title = m.getSpecialTitle();
                newList.add(newInfo);
            }
        }catch (Exception e){
            LogUtils.warn("HCPBridgeImpl_getMemberList", Log.getStackTraceString(e));
        }
        return newList;
    }

    @Override
    public List<GroupMemberInfo> getNewMemberList(BaseSession baseSession) {
        return getMemberList(baseSession);
    }

    @Override
    public List<FriendInfo> getFriendInfos(BaseSession baseSession) {
        List<FriendInfo> newList = new ArrayList<>();
        try{
            String botUin = baseSession.SelfUin;
            Bot bot = LoginManager.getAvailBot(botUin);
            if (bot == null)return newList;
            ContactList<Friend> friends = bot.getFriends();
            for (Friend f : friends){
                FriendInfo newInfo = new FriendInfo();
                newInfo.FriendUin = String.valueOf(f.getId());
                newInfo.Name = f.getNick();
                newList.add(newInfo);
            }
        }catch (Exception e){
            LogUtils.warn("HCPBridgeImpl_getFriendInfos", Log.getStackTraceString(e));
        }
        return newList;
    }

    @Override
    public GroupInfo getGroupInfo(BaseSession baseSession) {
        String botUin = baseSession.SelfUin;
        Bot bot = LoginManager.getAvailBot(botUin);
        if (bot == null)return null;
        Group g = bot.getGroup(Long.parseLong(baseSession.GroupUin));
        if (g != null){
            GroupInfo newInfo = new GroupInfo();
            newInfo.GroupUin = String.valueOf(g.getId());
            newInfo.Creator = String.valueOf(g.getOwner().getId());
            newInfo.GroupName = g.getName();
            return newInfo;
        }
        return null;
    }

    @Override
    public GroupMemberInfo getGroupMemberInfo(BaseSession baseSession) {
        try{
            String botUin = baseSession.SelfUin;
            Bot bot = LoginManager.getAvailBot(botUin);
            if (bot == null)return null;
            Group g = bot.getGroup(Long.parseLong(baseSession.GroupUin));
            if (g != null){
                NormalMember m = g.getOrFail(Long.parseLong(baseSession.UserUin));
                GroupMemberInfo newInfo = new GroupMemberInfo();
                newInfo.GroupUin = baseSession.GroupUin;
                newInfo.UserUin = String.valueOf(m.getId());
                newInfo.MemberCard = m.getNameCard();
                if (TextUtils.isEmpty(newInfo.MemberCard)){
                    newInfo.MemberCard = m.getNick();
                }
                newInfo.isAdmin = m.getPermission().getLevel()== 1;
                newInfo.isCreator = m.getPermission().getLevel() == 2;
                newInfo.title = m.getSpecialTitle();
                return newInfo;
            }
        }catch (Exception e){
            LogUtils.warn("HCPBridgeImpl_getGroupMemberInfo", Log.getStackTraceString(e));
        }

        return null;
    }

    @Override
    public FriendInfo getFriendInfo(BaseSession baseSession) {
        try{
            String botUin = baseSession.SelfUin;
            Bot bot = LoginManager.getAvailBot(botUin);
            if (bot == null)return null;
            Friend f = bot.getFriend(Long.parseLong(baseSession.UserUin));
            FriendInfo newInfo = new FriendInfo();
            newInfo.FriendUin = String.valueOf(f.getId());
            newInfo.Name = f.getNick();
            return newInfo;

        }catch (Exception e){
            LogUtils.warn("HCPBridgeImpl_getFriendInfo", Log.getStackTraceString(e));
        }
        return null;
    }

    @Override
    public void Revoke(BaseChatMsg baseChatMsg) {
        try{
            String botUin = baseChatMsg.SelfUin;
            Bot bot = LoginManager.getAvailBot(botUin);
            if (bot == null)return;
            if (baseChatMsg.type == 0){
                String[] id = baseChatMsg.messageID.split("->");
                if (id.length > 1){
                    MessageSource source = new MessageSourceBuilder()
                            .sender(Long.parseLong(baseChatMsg.UserUin))
                            .target(Long.parseLong(baseChatMsg.GroupUin))
                            .id(StringToId(id[0]))
                            .internalId(StringToId(id[1]))
                            .messages(new PlainText(baseChatMsg.content))
                            .build(Long.parseLong(baseChatMsg.SelfUin), MessageSourceKind.GROUP);
                    Mirai.getInstance().recallMessage(bot,source);
                }

            }
        }catch (Exception e){
            LogUtils.warn("HCPBridgeImpl_Revoke", Log.getStackTraceString(e));
        }
    }

    @Override
    public List<String> getLoginUins() {
        return LoginManager.getAllUin();
    }
    private ResBridgeImpl impl;
    @Override
    public HCPResUtils getResHelper() {
        if (impl == null){
            impl = new ResBridgeImpl(plugin);
        }
        return impl;
    }

    @Override
    public String getSkey(String s) {
        Bot bot = LoginManager.getAvailBot(s);
        if (bot == null)return "";
        return CommonBridge.getSkey(bot);
    }

    @Override
    public String getPsKey(String s, String s1) {
        Bot bot = LoginManager.getAvailBot(s);
        if (bot == null)return "";
        return CommonBridge.getPSkey(bot,s1);
    }

    @Override
    public String getGlobalConfigPath(String s) {
        String globalToken = DataUtils.getStrMD5(s).substring(0,10);
        String cachePath = GlobalEnv.FilePath + File.separator + "globalConfig" + File.separator + globalToken;
        if (!new File(cachePath).exists())new File(cachePath).mkdirs();
        return cachePath;
    }

    @Override
    public void log(int i, String s) {
        PluginLogUtils.writeLog(i,"["+plugin.pluginName+"]",s);
    }
    private static String IdTOString(int[] id){
        StringBuilder builder = new StringBuilder();
        for (int i :id){
            builder.append(i).append(":");
        }
        return builder.toString().substring(0,builder.length()-1);
    }
    private static int[] StringToId(String s){
        try{
            String[] cut = s.split(":");
            int[] r = new int[cut.length];
            for (int i=0;i<cut.length;i++){
                r[i] = Integer.parseInt(cut[i]);
            }
            return r;
        }catch (Exception e){
            return new int[0];
        }
    }
}
