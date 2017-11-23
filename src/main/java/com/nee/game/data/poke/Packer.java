package com.nee.game.data.poke;

/**
 * 
 * @author wzh
 * @date 2016-12-31 下午2:44:34
 * @QQ 154710510
 * @describe
 */
public class Packer {
	
	private Num num;
	private Color color;
	
	public Packer(String num){
		this.num=Num.valueOf(num);
	}
	
	public Packer(Num num,Color color){
		this.num=num;
		this.color=color;
	}
	public Packer(){
	}
	public Num getNum() {
		return num;
	}
	public void setNum(Num num) {
		this.num = num;
	}
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}
	
	
	//牌比大小
	public int compare(Packer p){
		int compare=this.num.getNum()==p.num.getNum()?0:this.num.getNum()>p.num.getNum()?1:-1;
		if(compare!=0){
			return compare;
		}else{
			//牌点相同比花色
			compare=this.getColor().getColor()==p.getColor().getColor()?0:this.getColor().getColor()>p.getColor().getColor()?1:-1;
		}
		return compare;
	}
	
	//牌只比点数
	public int compareNum(Packer p){
		int t=this.getNum().getNum();
		int p1=p.getNum().getNum();
		
		if(t==p1)return 0;
		return t>p1?1:-1;
	}
	
	//排序
	public static Packer[] sort(Packer[] ps){
		for(int i=0;i<ps.length;i++){
			int k=i;
			for(int j=i-1;j>=0;j--){
				if(ps[k].compareNum(ps[j])<0){
					exchange(ps,k,j);
					k--;
				}else{
					break;
				}
			}
		}
		return ps;
	}
	
	private static void exchange(Packer[] ps ,int i,int j){
		Packer p=ps[i];
		ps[i]=ps[j];
		ps[j]=p;
	}
	
	/*public static void main(String[] args) {
		Packer[] ps=new Packer[5];
		for(int i=6;i>1;i--){
			ps[6-i]=new Packer("P_"+i);
		}
		Gson gson=new Gson();
		
		System.out.println(gson.toJson(ps));
		System.out.println(gson.toJson(Packer.sort(ps)));
	}*/
	
}
