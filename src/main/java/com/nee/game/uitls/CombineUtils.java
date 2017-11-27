package com.nee.game.uitls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;


public class CombineUtils {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Byte[] tmp = {1, 2, 1, 3, 4};
//        ArrayList<Object[]> rs = randomC(tmp);
        ArrayList<Byte[]> rs = cmn(tmp, 5);
        for (int i = 0; i < rs.size(); i++) {
//            System.out.print(i+"=");
            for (int j = 0; j < rs.get(i).length; j++) {
                System.out.print(rs.get(i)[j] + ",");
            }
            System.out.println();

        }
    }


    // 求一个数组的任意组合
    public static ArrayList<Object[]> randomC(Object[] source) {
        ArrayList<Object[]> result = new ArrayList<Object[]>();
        if (source.length == 1) {
            result.add(source);
        } else {
            Object[] psource = new Object[source.length - 1];
            System.arraycopy(source, 0, psource, 0, psource.length);
            result = randomC(psource);
            int len = result.size();//fn组合的长度
            result.add((new Object[]{source[source.length - 1]}));
            for (int i = 0; i < len; i++) {
                Object[] tmp = new Object[result.get(i).length + 1];
                System.arraycopy(result.get(i), 0, tmp, 0, tmp.length - 1);
                tmp[tmp.length - 1] = source[source.length - 1];
                result.add(tmp);
            }

        }
        return result;
    }

    public static ArrayList<Byte[]> cmn(Byte[] source, int n) {
        ArrayList<Byte[]> result = new ArrayList<Byte[]>();
        if (n == 1) {
            for (Byte aSource : source) {
                result.add(new Byte[]{aSource});

            }
        } else if (source.length == n) {
            result.add(source);
        } else {
            Byte[] psource = new Byte[source.length - 1];
            System.arraycopy(source, 0, psource, 0, psource.length);
            result = cmn(psource, n);
            ArrayList<Byte[]> tmp = cmn(psource, n - 1);
            for (Byte[] aTmp : tmp) {
                Byte[] rs = new Byte[n];
                System.arraycopy(aTmp, 0, rs, 0, n - 1);
                rs[n - 1] = source[source.length - 1];
                result.add(rs);
            }
        }
        return result;
    }
}
