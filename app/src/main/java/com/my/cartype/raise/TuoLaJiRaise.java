package com.my.cartype.raise;

import android.content.Intent;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.cartype.CarUtil;


public class TuoLaJiRaise extends Canbox{

	public TuoLaJiRaise(){
			
	}
	
	@Override
	public void parseCanboxData(byte[] data, int len) {
		sendCanboxInfo("com.canboxsetting", data);
		
		byte []buf = new byte[len - 3];
		Util.byteArrayCopy(buf, data, 0, 2, buf.length);
		Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);		
		i.putExtra("buf", buf);		
		mContext.sendBroadcast(i);
	}
	
	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {
	}
	public void setMediaSrc(int source, byte type, byte []b){
	}

	public void setMediaSrc(int source) {
	}
	

}
