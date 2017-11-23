package com.nee.game.data.poke;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wzh
 * 倍率配置
 */
public class RatioConfig {
	// 倍率
	public static Map<Integer, Integer> ratio = new HashMap<Integer, Integer>();
	static {
		ratio.put(-1, 1);
		ratio.put(1, 1);
		ratio.put(2, 2);
		ratio.put(3, 3);
		ratio.put(4, 4);
		ratio.put(5, 5);
		ratio.put(6, 6);
		ratio.put(7, 7);
		ratio.put(8, 8);
		ratio.put(9, 9);
		ratio.put(10, 10);
		ratio.put(11, 11);
		ratio.put(15, 15);
		ratio.put(16, 16);
		ratio.put(17, 17);
		ratio.put(18, 18);
		ratio.put(20, 20);
		ratio.put(25, 25);
		ratio.put(30, 30);
		ratio.put(40, 40);
	}
}
