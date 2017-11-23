package com.nee.game.data.poke;

import java.util.*;

/**
 * @author wzh
 * @date 2016-12-31 下午2:45:14
 * @QQ 154710510
 * @describe
 */
public class UserPacket {

    private Packer[] ps = new Packer[5];//手里的5张牌

    public int type;//牌的类型  -1:无牛，1~9:牛一~牛9，10:牛牛，99:五花牛，100:四炸；

    private boolean win = false;//是否赢了

    private boolean isBanker = false;//是否是庄家

    public int[] threePokes = new int[3];


    // 判断是否是四炸
    private boolean isSiZha() {
        Packer[] newPs = this.ps;
        //数组第二个值
        int max2 = newPs[1].getNum().getNum();
        //数组倒数第二个
        int min3 = newPs[3].getNum().getNum();
        //如果数组第二个值和数组最后一个值一样，或者数组倒数第二个值个第一个一样那么是4炸
        return max2 == newPs[4].getNum().getNum() || min3 == newPs[0].getNum().getNum();
    }

    //判断是否是五花牛
    private boolean isWuHuaNiu() {
        //如果数组最小值是大于10，那么就是五花
        return ps[0].getNum().getNum() >= 10;
    }

    //判断是否是金牛
    private boolean isJinNiu() {
        //如果数组最小值是大于10，那么就是五花
        return ps[0].getNum().getNum() >= 10;
    }

    //判断是否同花顺牛
    private boolean isTongHuaShunNiu() {
        return isTongHuaNiu() && isShunZiNiu();
    }

    //判断是否顺子牛
    private boolean isShunZiNiu() {
        Packer[] newPs = this.ps;
        for (int i = 0; i < newPs.length - 1; i++) {
            if ((newPs[i].getNum().getNum() + 1) != newPs[i + 1].getNum().getNum())
                return false;
        }
        return true;
    }

    //判断是否对子牛
    private boolean isDuiZiNiu() {
        Packer[] newPs = this.ps;

        if (this.type > 0) {
            int a[] = new int[2];
            int index = 0;
            for (Packer packer : this.ps) {
                int threePoke = 0;
                for (int threePoke1 : threePokes) {
                    if (packer.getNum().getNum() == threePoke1) {
                        threePoke = 0;
                        break;
                    }
                    threePoke = threePoke1;
                }
                if (threePoke == 0) {
                    continue;
                }
                if (index > 1) {
                    return false;
                }
                a[index] = threePoke;
                index++;
            }
            if (a[0] == a[1]) {
                return true;
            }
        }

        return false;
    }

    //判断是否五小牛牛
    private boolean isWuXiaoNiu() {
        Packer[] newPs = this.ps;
        //如果数组最小值是大于10，那么就是五花
        int num = 0;
        for (Packer newP : newPs) {
            num += newP.getNum().getNum();
        }
        return num <= 10;
    }



    //判断是否同花牛
    private boolean isTongHuaNiu() {
        Packer[] newPs = this.ps;
        //如果数组最小值是大于10，那么就是五花
        for (int i = 0; i < newPs.length - 1; i++) {
            if (newPs[i].getColor().getColor() != newPs[i + 1].getColor().getColor())
                return false;
        }
        return true;
    }

    //判断是否是葫芦牛
    private boolean isHuluNiu() {
        Packer[] newPs = this.ps;
        return newPs[0].getNum().getNum() == newPs[1].getNum().getNum()
                && newPs[1].getNum().getNum() == newPs[2].getNum().getNum()
                && newPs[3].getNum().getNum() == newPs[4].getNum().getNum()
                || newPs[0].getNum().getNum() == newPs[1].getNum().getNum()
                && newPs[2].getNum().getNum() == newPs[3].getNum().getNum()
                && newPs[3].getNum().getNum() == newPs[4].getNum().getNum();
    }

    //判断是牛几
    public int isNiuNum() {
        int[] n = new int[5];
        for (int i = 0; i < 5; i++) {
            if (ps[i].getNum().getNum() > 10) {
                n[i] = 10;
            } else {
                n[i] = ps[i].getNum().getNum();
            }
        }
        Map<String, Boolean> map = NiuNiu(n);
        if (map.get("isNiuNiu")) {
            return 10;
        }
        if (map.get("isNiuNum")) {
            int num = 0;
            for (int i : n) {
                num += i;
            }
            return num % 10;
        } else {
            return -1;
        }
    }

    private Map<String, Boolean> NiuNiu(int[] i) {

        boolean isNiuNum = false;
        boolean isNiuNiu = false;
        for (int m = 0; m <= 2; m++) {
            for (int n = m + 1; n <= 3; n++) {
                for (int z = n + 1; z <= 4; z++) {
                    if ((i[m] + i[n] + i[z]) % 10 == 0) {
                        threePokes[0] = ps[m].getNum().getNum();
                        threePokes[1] = ps[n].getNum().getNum();
                        threePokes[2] = ps[z].getNum().getNum();

                        isNiuNum = true;
                        int num = 0;
                        for (int x = 0; x <= 4; x++) {
                            if (x != m && x != n && x != z) {
                                num += i[x];
                            }
                        }
                        if (num % 10 == 0) {
                            isNiuNiu = true;
                        }
                    }
                }
            }
        }
        Map<String, Boolean> result = new HashMap<String, Boolean>();
        result.put("isNiuNum", isNiuNum);
        result.put("isNiuNiu", isNiuNiu);
        return result;
    }

    public UserPacket(Packer[] ps) {
        this(ps, false);
    }

    public UserPacket(Packer[] ps, boolean isBanker) {
        this.ps = Packer.sort(ps);
        this.isBanker = isBanker;

        this.type = isNiuNum();

        if (isDuiZiNiu()) {
            this.type = 11;
        }
        if (this.type >= 10 && isTongHuaShunNiu()) {
            this.type = 40;
            return;
        }
        if (isSiZha()) {
            this.type = 30;
            return;
        }
        if (this.type >= 10 && isHuluNiu()) {
            this.type = 25;
            return;
        }
        if (isWuHuaNiu()) {
            this.type = 20;
            return;
        }
        if (this.type >= 10 && isWuXiaoNiu()) {
            this.type = 18;
            return;
        }
        if (this.type >= 10 && isJinNiu()) {
            this.type = 17;
            return;
        }
        if (this.type >= 10 && isTongHuaNiu()) {
            this.type = 16;
            return;
        }
        if (this.type >= 10 && isShunZiNiu()) {
            this.type = 15;
        }

    }


    public boolean isWin() {
        return win;
    }

    public void setWin(boolean win) {
        this.win = win;
    }

    public boolean isBanker() {
        return isBanker;
    }

    public void setBanker(boolean isBanker) {
        this.isBanker = isBanker;
    }

    Packer[] getPs() {
        return ps;
    }

    public void setPs(Packer[] ps) {
        this.ps = ps;
    }

    //倍率计算
    public int getRatio() {
        return RatioConfig.ratio.get(this.type);
    }


    public static void main(String[] args) {
        Packer[] packers = new Packer[5];
        packers[0] = new Packer(Num.P_3, Color.HEI_TAO);
        packers[1] = new Packer(Num.P_7, Color.HEI_TAO);
        packers[2] = new Packer(Num.P_7, Color.HEI_TAO);
        packers[3] = new Packer(Num.P_J, Color.HEI_TAO);
        packers[4] = new Packer(Num.P_7, Color.HON_GTAO);


        UserPacket up = new UserPacket(packers);
        System.out.println(up.type);

    }
}
