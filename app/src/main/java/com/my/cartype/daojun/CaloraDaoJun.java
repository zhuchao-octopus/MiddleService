package com.my.cartype.daojun;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class CaloraDaoJun extends Canbox{

	public CaloraDaoJun(){
		buildCmdDoor((byte) 0x24, (byte) 0x1, (byte) 0xfc, (byte) 0x02);
		buildCmdVersion((byte) 0x30, (byte) 0x0);

		buildCmdAngle((byte) 0x26, (byte) 0x0, 6099);
	}
	
	

	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}
	public void setMediaSrc(int source, byte type, byte []b){
	}

	public void setMediaSrc(int source) {
	}	

	

}
