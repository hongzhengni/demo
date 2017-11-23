package com.nee.game.data.poke;
/**
 * 
 */
public enum Color {
	HEI_TAO(3),
	HON_GTAO(2),
	MEI_HAU(1),
	FANG_KUAI(0);
	
	
	private int color;

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}
	
	Color(int i){
		this.color=i;
	}

	public static Color getColorByValue(int value) {
		for(Color color : Color.values()){
			if(value == color.getColor()){
				return color;
			}
		}
		return null;
	}
}
