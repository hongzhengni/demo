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
        /** 抢庄*/
        int HOG = 3;
        /** 加注*/
        int ADD_MONEY = 4;
        /** 发一张牌*/
        int DEAL_1 = 5;
        /** 比牌开始*/
        int COMPARE_START = 6;
        /** 比牌结束*/
        int COMPARE_END = 7;
        /** 结算*/
        int SETTLE = 8;

    }
}
