package com.nee.game.common;

/**
 * @Author: heikki.
 * @Description:
 * @DATE: 下午10:48 17/10/30.
 */
public class PokeData {

    public final static Byte GameLogic[] = {
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D,    //方块 A - K
            0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D,    //梅花 A - K
            0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D,    //红桃 A - K
            0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D,    //黑桃 A - K
            //0x4E, 0x4F
    };

    public final static Byte[] _Mahjong =  { 0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,     //1万到9万
            0x11,0x12,0x13,0x14,0x15,0x16,0x17,0x18,0x19,     //1索到9索
            0x21,0x22,0x23,0x24,0x25,0x26,0x27,0x28,0x29,     //1筒到9筒
            0x31,0x32,0x33,0x34,0x35,0x36,0x37                //依次是   东南西北中发白
    };

    public static final Byte BLANK = 0x37;

    public static final Byte EAST = 0x31;
    public static final Byte SOUTH = 0x32;
    public static final Byte NORTH = 0x33;
    public static final Byte WEST = 0x34;
    public static final Byte CENTER = 0x35;
}
