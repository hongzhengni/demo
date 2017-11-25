package com.nee.game.data.poke;
/**
 * 
 * @author wzh
 * @date 2016-12-31 下午2:45:03
 * @QQ 154710510
 * @describe
 */
public class PackerCompare {
	
	//得到赢得人
	public static UserPokes geWin(UserPokes up1, UserPokes up2){
		if(!up1.isBanker()^up2.isBanker()){
			try {
				throw new Exception("两个用户,必须一个是庄家、一个是用户");
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return CompareType(up1,up2);
	}
	
	
	//比较两个人的牌类型的大小，并计算输赢
	private static UserPokes CompareType(UserPokes up1, UserPokes up2){
		if(up1.type==up2.type){//当手牌类型相同时
			//如果类型是牛1或者牛2那么庄家赢
			if(up1.type==1||up1.type==2){
				if(up1.isBanker()){
					up1.setWin(true);
				}else{
					up2.setWin(true);
				}
			}
			//其他类型的话就开始比较牌点
			compareNum(up1,up2);
			return up1.isWin()?up1:up2;
		}
		if(up1.type>up2.type){
			up1.setWin(true);
		}else{
			up2.setWin(true);
		}
		return up1.isWin()?up1:up2;
	}
	
	//比较牌点
	private static void compareNum(UserPokes up1, UserPokes up2){
		Packer[] newP1=Packer.sort(up1.getPs());
		Packer[] newP2=Packer.sort(up1.getPs());
		//比较最大牌的大小
		if(newP1[4].compare(newP2[4])==0){
			try {
				throw new Exception("服务器异常，一副牌中出现大小花色完全一样的牌");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		if(newP1[4].compare(newP2[4])>0){
			up1.setWin(true);
		}else{
			up2.setWin(true);
		}
	}
}
