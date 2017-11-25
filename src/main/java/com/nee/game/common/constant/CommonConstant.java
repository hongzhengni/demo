package com.nee.game.common.constant;

public interface CommonConstant {

    interface USER_STATUS {
        int INIT = 0;
        int READY = 1;
        int PLAYING = 2;
    }


    /**
     * 是否默认：0-否，1-是
     */
    interface TABLE_TACHE {
        /** 初始化*/
        int NO_READY = 0;
        /** 准备*/
        int READY = 1;
        /** 发四张牌*/
        int PLAYING = 2;
        /** 发一张牌*/
        int DEAL_1 = 5;
        /** 比牌开始*/
        int COMPARE_START = 6;
        /** 比牌结束*/
        int COMPARE_END = 7;
        /** 结算*/
        int SETTLE = 8;
    }

    interface ACTION_TYPE {
        int CHI = 1;
        int PEN = 2;
        int GANG = 3;
        int HU = 4;
    }
}
