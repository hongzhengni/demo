package com.nee.game.data.poke;

import java.util.Arrays;

public class UserPokes {

    private Byte[] ps = new Byte[14];//手里的13张牌


    public UserPokes(Byte[] ps) {
        this(ps, null);
    }

    public UserPokes(Byte[] pokes, Byte poke) {
        for (int i = 0; i < pokes.length; i++) {
            ps[i] = pokes[i];
        }


        if (poke != null) {
            ps[13] = poke;
        }
        if (ps.length != 14) {
            return;
        }

        Arrays.sort(ps);

    }



    public static void main(String[] args) {

    }
}
