package cc.hicore.MiraiHCP.LoginManager;

import android.annotation.SuppressLint;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.events.BotLeaveEvent;
import net.mamoe.mirai.event.events.BotMuteEvent;
import net.mamoe.mirai.event.events.BotUnmuteEvent;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.GroupMuteAllEvent;
import net.mamoe.mirai.event.events.GroupTempMessageEvent;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MemberLeaveEvent;
import net.mamoe.mirai.event.events.MemberMuteEvent;
import net.mamoe.mirai.event.events.MemberUnmuteEvent;
import net.mamoe.mirai.event.events.MessageRecallEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.AtAll;
import net.mamoe.mirai.message.data.Face;
import net.mamoe.mirai.message.data.FileMessage;
import net.mamoe.mirai.message.data.FlashImage;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.LightApp;
import net.mamoe.mirai.message.data.MarketFace;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.OnlineAudio;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.PokeMessage;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.message.data.SimpleServiceMessage;
import net.mamoe.mirai.message.data.SingleMessage;

import java.util.ArrayList;

import cc.hicore.HCPBridge.data.BaseSession;
import cc.hicore.HCPBridge.data.event.EventForExit;
import cc.hicore.HCPBridge.data.event.EventForJoin;
import cc.hicore.HCPBridge.data.event.EventForMute;
import cc.hicore.HCPBridge.data.event.EventForRecall;
import cc.hicore.HCPBridge.data.msg.BaseChatMsg;
import cc.hicore.HCPBridge.data.msg.MsgForAt;
import cc.hicore.HCPBridge.data.msg.MsgForCard;
import cc.hicore.HCPBridge.data.msg.MsgForFile;
import cc.hicore.HCPBridge.data.msg.MsgForMix;
import cc.hicore.HCPBridge.data.msg.MsgForPic;
import cc.hicore.HCPBridge.data.msg.MsgForQQEmo;
import cc.hicore.HCPBridge.data.msg.MsgForReply;
import cc.hicore.HCPBridge.data.msg.MsgForText;
import cc.hicore.HCPBridge.data.msg.MsgForVoice;
import cc.hicore.MiraiHCP.PluginManager.PluginManager;
import cc.hicore.Utils.DataUtils;

@SuppressLint("UnsafeOptInUsageError")
public class MessageProcessor {
    public static void onGroupMsg(GroupMessageEvent msg){
        BaseSession session = new BaseSession();
        session.type = 0;
        session.GroupUin = String.valueOf(msg.getGroup().getId());
        session.SelfUin = String.valueOf(msg.getBot().getAsFriend().getId());
        session.UserUin = String.valueOf(msg.getSender().getId());
        onCommonMsg(msg.getMessage(),session,msg.getBot());
    }
    public static void onFriendMsg(FriendMessageEvent msg){
        BaseSession session = new BaseSession();
        session.type = 1;
        session.SelfUin = String.valueOf(msg.getBot().getAsFriend().getId());
        session.UserUin = String.valueOf(msg.getSender().getId());
        onCommonMsg(msg.getMessage(),session,msg.getBot());
    }
    public static void onTempMsg(GroupTempMessageEvent msg){
        BaseSession session = new BaseSession();
        session.type = 2;
        session.SelfUin = String.valueOf(msg.getBot().getAsFriend().getId());
        session.UserUin = String.valueOf(msg.getSender().getId());
        session.GroupUin = String.valueOf(msg.getGroup().getId());
        onCommonMsg(msg.getMessage(),session,msg.getBot());
    }
    public static void onCommonMsg(MessageChain msg, BaseSession session, Bot bot){
        MsgForMix mix = new MsgForMix();
        mix.msgList = new ArrayList<>();
        StringBuilder mixContent = new StringBuilder();
        MessageSource source = msg.get(MessageSource.Key);
        if (source == null)return;
        for (SingleMessage oneMessage : msg){
            if (oneMessage instanceof LightApp){
                LightApp mApp = (LightApp) oneMessage;
                MsgForCard card = new MsgForCard();
                card.cardType = 1;
                card.CardCode = mApp.getContent();
                card.msgType = BaseChatMsg.MSG_TYPE_CARD;
                card.messageID = IdTOString(source.getIds())+"->"+IdTOString(source.getInternalIds());
                card.content = "[卡片]"+card.CardCode;
                CopyBaseSession(session,card);
                PluginManager.PluginMessageEvent(card);
            }else if (oneMessage instanceof SimpleServiceMessage){
                SimpleServiceMessage mMessage = (SimpleServiceMessage) oneMessage;
                if (mMessage.getServiceId() == 60){
                    MsgForCard card = new MsgForCard();
                    card.cardType = 0;
                    card.CardCode = mMessage.getContent();
                    card.msgType = BaseChatMsg.MSG_TYPE_CARD;
                    card.messageID = IdTOString(source.getIds())+"->"+IdTOString(source.getInternalIds());
                    card.content = "[卡片]"+card.CardCode;
                    CopyBaseSession(session,card);
                    PluginManager.PluginMessageEvent(card);
                }
            }else if (oneMessage instanceof FileMessage){
                FileMessage message = (FileMessage) oneMessage;
                MsgForFile file = new MsgForFile();
                file.msgType = BaseChatMsg.MSG_TYPE_File;
                file.name = message.getName();
                file.size = message.getSize();
                file.id = message.getId()+"->"+message.getInternalId();
                file.messageID = IdTOString(source.getIds())+"->"+IdTOString(source.getInternalIds());
                file.content = "[文件]"+file.name+",size = "+file.size;
                CopyBaseSession(session,file);
                PluginManager.PluginMessageEvent(file);
            }else if (oneMessage instanceof OnlineAudio){
                OnlineAudio audio = (OnlineAudio) oneMessage;
                MsgForVoice voice = new MsgForVoice();
                voice.hash = DataUtils.ByteArrayToHex(audio.getFileMd5());
                voice.DownUrl = audio.getUrlForDownload();
                voice.messageID = IdTOString(source.getIds())+"->"+IdTOString(source.getInternalIds());
                voice.content = "[语音]" + voice.hash;
               CopyBaseSession(session,voice);
                PluginManager.PluginMessageEvent(voice);
            }else if (oneMessage instanceof PlainText){
                PlainText text = (PlainText) oneMessage;
                MsgForText oneText = new MsgForText();
                oneText.text = text.getContent();
                oneText.msgType = BaseChatMsg.MSG_TYPE_TEXT;
                oneText.messageID = IdTOString(source.getIds())+"->"+IdTOString(source.getInternalIds());
                mixContent.append(oneText.text);
                CopyBaseSession(session,oneText);
                mix.msgList.add(oneText);
            }else if (oneMessage instanceof Image){
                Image image = (Image) oneMessage;
                MsgForPic pic = new MsgForPic();
                pic.msgType = BaseChatMsg.MSG_TYPE_PIC;
                pic.MD5 = DataUtils.ByteArrayToHex(image.getMd5());
                pic.Url = Image.queryUrl(image);
                pic.messageID = IdTOString(source.getIds())+"->"+IdTOString(source.getInternalIds());
                mixContent.append("[图片=").append(pic.MD5).append("]");
                CopyBaseSession(session,pic);
                mix.msgList.add(pic);
            }else if (oneMessage instanceof At){
                At atOne = (At) oneMessage;
                MsgForAt msgAt = new MsgForAt();
                msgAt.msgType = BaseChatMsg.MSG_TYPE_AT;
                msgAt.atTarget = String.valueOf(atOne.getTarget());
                msgAt.atType = 0;
                msgAt.messageID = IdTOString(source.getIds())+"->"+IdTOString(source.getInternalIds());
                mixContent.append("@").append(atOne.getDisplay(bot.getGroup(Long.parseLong(session.GroupUin))));
                CopyBaseSession(session,msgAt);
                mix.msgList.add(msgAt);
            }else if (oneMessage instanceof AtAll){
                MsgForAt msgAt = new MsgForAt();
                msgAt.atTarget = "0";
                msgAt.atType = 1;
                msgAt.messageID = IdTOString(source.getIds())+"->"+IdTOString(source.getInternalIds());
                mixContent.append("@全体成员 ");
                CopyBaseSession(session,msgAt);
                mix.msgList.add(msgAt);
            }else if (oneMessage instanceof Face){
                Face face = (Face) oneMessage;
                MsgForQQEmo QQEmo = new MsgForQQEmo();
                QQEmo.EmoID = face.getId();
                QQEmo.EmoName = face.getName();
                QQEmo.messageID = IdTOString(source.getIds())+"->"+IdTOString(source.getInternalIds());
                mixContent.append("[").append(face.getName()).append("]");
                CopyBaseSession(session,QQEmo);
                mix.msgList.add(QQEmo);
            }else if (oneMessage instanceof FlashImage){
                FlashImage flash = (FlashImage) oneMessage;
                MsgForPic pic = new MsgForPic();
                Image image = flash.getImage();
                pic.msgType = BaseChatMsg.MSG_TYPE_PIC;
                pic.MD5 = DataUtils.ByteArrayToHex(image.getMd5());
                pic.Url = Image.queryUrl(image);
                pic.isFlashPic = true;
                pic.messageID = IdTOString(source.getIds())+"->"+IdTOString(source.getInternalIds());
                mixContent.append("[闪照]");
                CopyBaseSession(session,pic);
                mix.msgList.add(pic);
            }else if (oneMessage instanceof PokeMessage){
                PokeMessage poke = (PokeMessage) oneMessage;
                BaseChatMsg baseMsg = new BaseChatMsg();
                baseMsg.content = poke.getName();
                baseMsg.extraType = 8;
                baseMsg.messageID = IdTOString(source.getIds())+"->"+IdTOString(source.getInternalIds());
                mixContent.append(baseMsg.content);
                CopyBaseSession(session,baseMsg);
                PluginManager.PluginMessageEvent(baseMsg);
            }else if (oneMessage instanceof MarketFace){
                MarketFace face = (MarketFace) oneMessage;
                BaseChatMsg baseMsg = new BaseChatMsg();
                baseMsg.content = face.getName();
                baseMsg.extraType = 9;
                baseMsg.messageID = IdTOString(source.getIds())+"->"+IdTOString(source.getInternalIds());
                CopyBaseSession(session,baseMsg);
                PluginManager.PluginMessageEvent(baseMsg);
            }else if (oneMessage instanceof QuoteReply){
                QuoteReply reply = (QuoteReply) oneMessage;
                MsgForReply re = new MsgForReply();
                re.replyToID = IdTOString(reply.getSource().getIds())+"->"+IdTOString(reply.getSource().getInternalIds());
                re.messageID = IdTOString(source.getIds())+"->"+IdTOString(source.getInternalIds());
                CopyBaseSession(session,re);
                mix.msgList.add(re);
                mixContent.append("[回复").append(reply.getSource().getTargetId()).append("]");
            }
        }
        if (mix.msgList.size() > 0){
            mix.msgType = BaseChatMsg.MSG_TYPE_PLAIN;
            mix.messageID = IdTOString(source.getIds())+"->"+IdTOString(source.getInternalIds());
            mix.content = mixContent.toString();
            CopyBaseSession(session,mix);
            PluginManager.PluginMessageEvent(mix);
        }
    }
    public static void onGroupEvent(GroupEvent event){
        BaseSession session = new BaseSession();
        session.type = 0;
        session.GroupUin = String.valueOf(event.getGroup().getId());
        session.SelfUin = String.valueOf(event.getBot().getAsFriend().getId());

        if (event instanceof MemberJoinEvent.Invite){
            MemberJoinEvent.Invite joinEvent = (MemberJoinEvent.Invite) event;
            session.UserUin = String.valueOf(joinEvent.getMember().getId());
            EventForJoin join= new EventForJoin();
            join.adminUin = String.valueOf(joinEvent.getInvitor().getId());
            CopyBaseSession(session,join);
            PluginManager.PluginMessageEvent(join);
        }else if (event instanceof MemberJoinEvent.Active){
            MemberJoinEvent.Active joinEvent = (MemberJoinEvent.Active) event;
            session.UserUin = String.valueOf(joinEvent.getMember().getId());
            EventForJoin join= new EventForJoin();
            CopyBaseSession(session,join);
            PluginManager.PluginMessageEvent(join);
        }else if (event instanceof MemberLeaveEvent.Kick){
            MemberLeaveEvent.Kick kickEvent = (MemberLeaveEvent.Kick) event;
            EventForExit exit = new EventForExit();
            exit.isKick = true;
            if (kickEvent.getOperator() == null){
                exit.adminUin = String.valueOf(kickEvent.getBot().getId());
            }else {
                exit.adminUin = String.valueOf(kickEvent.getOperator().getId());
            }
            session.UserUin = String.valueOf(kickEvent.getMember().getId());
            CopyBaseSession(session,exit);
            PluginManager.PluginMessageEvent(exit);
        }else if (event instanceof MemberLeaveEvent.Quit){
            MemberLeaveEvent.Quit quitEvent = (MemberLeaveEvent.Quit) event;
            EventForExit exit = new EventForExit();
            session.UserUin = String.valueOf(quitEvent.getMember().getId());
            CopyBaseSession(session,exit);
            PluginManager.PluginMessageEvent(exit);
        }else if (event instanceof BotLeaveEvent.Kick){
            BotLeaveEvent.Kick botKicked = (BotLeaveEvent.Kick) event;
            EventForExit exit = new EventForExit();
            session.UserUin = String.valueOf(botKicked.getBot().getId());
            exit.isKick = true;
            exit.adminUin = String.valueOf(botKicked.getOperator().getId());
            CopyBaseSession(session,exit);
            PluginManager.PluginMessageEvent(exit);
        }else if (event instanceof MemberMuteEvent){
            MemberMuteEvent muteEvent = (MemberMuteEvent) event;
            EventForMute mute = new EventForMute();
            session.UserUin = String.valueOf(muteEvent.getMember().getId());
            mute.adminUin = String.valueOf(muteEvent.getOperator().getId());
            mute.time = muteEvent.getDurationSeconds();
           CopyBaseSession(session,mute);
            PluginManager.PluginMessageEvent(mute);
        }else if (event instanceof GroupMuteAllEvent){
            GroupMuteAllEvent muteAll = (GroupMuteAllEvent) event;
            EventForMute mute = new EventForMute();
            mute.isMuteAll = true;
            mute.time = muteAll.getNew() ? 1 : 0;
            mute.adminUin = String.valueOf(muteAll.getOperator().getId());
            CopyBaseSession(session,mute);
            PluginManager.PluginMessageEvent(mute);
        }else if (event instanceof MemberUnmuteEvent){
            MemberUnmuteEvent muteEvent = (MemberUnmuteEvent) event;
            EventForMute mute = new EventForMute();
            session.UserUin = String.valueOf(muteEvent.getMember().getId());
            mute.adminUin = String.valueOf(muteEvent.getOperator().getId());
            mute.time = 0;
            CopyBaseSession(session,mute);
            PluginManager.PluginMessageEvent(mute);
        }else if (event instanceof BotMuteEvent){
            BotMuteEvent muteEvent = (BotMuteEvent) event;
            EventForMute mute = new EventForMute();
            session.UserUin = String.valueOf(muteEvent.getBot().getId());
            mute.adminUin = String.valueOf(muteEvent.getOperator().getId());
            mute.time = muteEvent.getDurationSeconds();
            CopyBaseSession(session,mute);
            PluginManager.PluginMessageEvent(mute);
        }else if (event instanceof BotUnmuteEvent){
            BotUnmuteEvent muteEvent = (BotUnmuteEvent) event;
            EventForMute mute = new EventForMute();
            session.UserUin = String.valueOf(muteEvent.getBot().getId());
            mute.adminUin = String.valueOf(muteEvent.getOperator().getId());
            mute.time = 0;
            CopyBaseSession(session,mute);
            PluginManager.PluginMessageEvent(mute);
        }
    }
    public static void onRecallEvent(MessageRecallEvent recall){
        EventForRecall event = new EventForRecall();
        if (recall instanceof MessageRecallEvent.GroupRecall){
            event.type = 0;
            event.GroupUin = String.valueOf(((MessageRecallEvent.GroupRecall) recall).getGroup().getId());
            event.UserUin = String.valueOf(recall.getAuthorId());
            event.adminUin = String.valueOf(((MessageRecallEvent.GroupRecall) recall).getOperator().getId());
            event.messageID = IdTOString(recall.getMessageIds())+"->"+IdTOString(recall.getMessageInternalIds());
            event.SelfUin = String.valueOf(recall.getBot().getId());
            PluginManager.PluginMessageEvent(event);
        }else if (recall instanceof MessageRecallEvent.FriendRecall){
            event.type = 1;
            event.UserUin = String.valueOf(recall.getAuthorId());
            event.adminUin = String.valueOf(((MessageRecallEvent.FriendRecall) recall).getOperator().getId());
            event.messageID = IdTOString(recall.getMessageIds())+"->"+IdTOString(recall.getMessageInternalIds());
            event.SelfUin = String.valueOf(recall.getBot().getId());
            PluginManager.PluginMessageEvent(event);
        }
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
    private static void CopyBaseSession(BaseSession source,BaseSession target){
        target.ExtraCode = source.ExtraCode;
        target.SessionID = source.SessionID;
        target.GroupUin = source.GroupUin;
        target.UserUin = source.UserUin;
        target.SelfUin = source.SelfUin;
        target.type = source.type;
    }
}
