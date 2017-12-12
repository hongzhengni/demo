package com.nee.game.common.constant;

public class CmdConstant {

    public final static int LOGIN_HALL = 0x1001;//登陆大厅
    public final static int CREATE_ROOM = 0x1002; //
    public final static int SIT_DOWN = 0x1003; //坐下
    public final static int READY = 0x1005; //准备
    public final static int STAND_UP = 0x1007; //起立
    public final static int PLAY_CARD = 0x1009; //出牌
    public final static int PEN_CARD = 0x100B; //碰
    public final static int GANG_CARD = 0x100D; //杠
    public final static int CHI_CARD = 0x100F; //吃
    public final static int HU_CARD = 0x10021; //胡
    public final static int GIVE_UP_CARD = 0x10023; //弃
    public final static int DISMISS = 0x10025; //解散
    public final static int APPLY_SETTLE = 0x10027; //申请结算
    public final static int CHAT = 0x10029; //聊天

    public final static int REV_HALL_INFO = 0x2000; //大厅信息
    public final static int REV_ROOM_INFO = 0x2001; //房间信息
    public final static int BROADCAST_CREATE_ROOM = 0x2002; //
    public final static int BROADCAST_SIT_DOWN = 0x2003; //坐下广播消息
    public final static int BROADCAST_USER_READY = 0x2005; //准备广播消息
    public final static int BROADCAST_USER_DICE = 0x2006; //
    public final static int BROADCAST_STAND_UP = 0x2007; //起立广播消息
    public final static int BROADCAST_START_GAME = 0x2009; //开局广播消息
    public final static int REV_START_GAME = 0x200A; //开局DEAL消息
    public final static int BROADCAST_PLAY_CARD = 0x200B; //出牌广播消息
    public final static int REV_ACTION_CARD = 0x200D; //动作牌掩码消息
    public final static int BROADCAST_ACTION_CARD = 0x200F; //动作牌广播消息
    public final static int BROADCAST_CATCH_CARD = 0x2021; //摸牌广播消息
    public final static int BROADCAST_HU_CARD = 0x2023; //胡牌广播消息
    public final static int BROADCAST_DISMISS = 0x20025; //解散广播
    public final static int REV_APPLY_SETTLE = 0x20027; //申请结算广播
    public final static int BROADCAST_CHAT = 0x20029; //聊天广播

    public final static byte WIK_LEFT = 0x01;
    public final static byte WIK_CENTER = 0x02;
    public final static byte WIK_RIGHT = 0x04;
    public final static byte WIK_PENG = 0x08;
    public final static byte WIK_GANG = 0x10;
    public final static byte WIK_LISTEN = 0x20;
    public final static byte WIK_CHI_HU = 0x40;

}
