package com.my.cartype.hiworld;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.canbox.RadarManager;
import com.my.cartype.CarUtil;
import com.my.out.R;
import com.my.view.LedView;

public class Odyssey04Hiworld extends Canbox {

	public Odyssey04Hiworld() {
		buildCmdVersion((byte) 0xf0, (byte) 0x0);
	}

	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mLayoutParams;
	private View mMainView;
	private boolean mIsShow;

	private void initView() {
		mWindowManager = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		mLayoutParams = new WindowManager.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,
				0, 0, LayoutParams.TYPE_PHONE,
				LayoutParams.FLAG_LAYOUT_NO_LIMITS 
				| LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.RGBA_8888);

		mLayoutParams.gravity = Gravity.BOTTOM;
		mLayoutParams.alpha = 0.92f;
		mMainView = ((LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.odysseus_old_layout, null);
	}
	public void startConnect() {

	}


	private void show() {
		if (!mIsShow) {
			mIsShow = true;
			mWindowManager.addView(mMainView, mLayoutParams);			
		}
		mHandler.removeMessages(0);
		mHandler.sendEmptyMessageDelayed(0, 5000);
	}

	private void hide() {
		if (mIsShow) {
			try {
				mWindowManager.removeView(mMainView);
			} catch (Exception e) {

			}
			mIsShow = false;
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				hide();
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void stopConnect() {
		hide();
	}

	@Override
	public void setContext(Context c) {
		// TODO Auto-generated method stub
		super.setContext(c);
		initView();
	}
	
	public static class Node {
		public int mShow; 
		public int mId;

		public Node(int show, int id) {
			mShow = show;
			mId = id;
		}
	}

	private static final Node[] NODES = {
		new Node(0x0008, R.id.number1),
		new Node(0x0108, R.id.number2),
		new Node(0x0508, R.id.number3),
		new Node(0x0504, R.id.number4),
		new Node(0x0610, R.id.number5),
		new Node(0x0640, R.id.number6),
		new Node(0x0680, R.id.number7),
		new Node(0x0780, R.id.number8),
		new Node(0x0908, R.id.number9),
		new Node(0x1408, R.id.number10),
		new Node(0x0180, R.id.number11),
		new Node(0x0280, R.id.number12),
		new Node(0x0308, R.id.number13),
		new Node(0x0580, R.id.number14),
		new Node(0x0502, R.id.number15),
		new Node(0x0620, R.id.number16),
		new Node(0x0c08, R.id.number17_1),
		new Node(0x0c08, R.id.number17_3),
		new Node(0x0f80, R.id.number17_2),
		new Node(0x1108, R.id.number18),

		new Node(0x0708, R.id.number24_st),
		new Node(0x0704, R.id.number24_zhong1),
		new Node(0x0702, R.id.number24_zhong2),
		new Node(0x0701, R.id.number24_ch),
		
		

	};
	
	private int buildNumber(int cmd, int mask, char c) {
		if ((cmd & (0x1 << mask)) != 0) {
			int index = c - 'a';
			return (0x1 << index);
		}
		return 0;
	}
	
	private byte[] mData0x80 = new byte[0x19];

	private void do0x80(byte[] data) {
		if (!Util.isBufEquals(mData0x80, data)) {
			show();
			Util.byteArrayCopy(mData0x80, data, 0, 0, mData0x80.length);

			int index;
			int mask;
			for (int i = 0; i < NODES.length; ++i) {
				index = (NODES[i].mShow & 0xff00) >> 8;
				mask = (NODES[i].mShow & 0xff);

				setViewVisible(NODES[i].mId, mData0x80[2+index] & mask);
			}
			
			int num;
			//19
			num = 0;
			num |= buildNumber(mData0x80[3], 5, 'a');
			num |= buildNumber(mData0x80[3], 5, 'b');
			num |= buildNumber(mData0x80[3], 5, 'c');
			num |= buildNumber(mData0x80[3], 5, 'd');
			num |= buildNumber(mData0x80[3], 6, 'm');
			num |= buildNumber(mData0x80[3], 4, 'p');
			num |= buildNumber(mData0x80[3], 0, 'r');
			num |= buildNumber(mData0x80[3], 0, 's');
			num |= buildNumber(mData0x80[3], 1, 'o');
			num |= buildNumber(mData0x80[3], 1, 'q');
			num |= buildNumber(mData0x80[3], 2, 'i');
			num |= buildNumber(mData0x80[3], 2, 'j');
			num |= buildNumber(mData0x80[3], 2, 'k');
			num |= buildNumber(mData0x80[3], 2, 'l');
			//20

			setViewNum(R.id.odyssey19, num);
			num = 0;
			num |= buildNumber(mData0x80[4], 6, 'a');
			num |= buildNumber(mData0x80[4], 6, 'b');
			num |= buildNumber(mData0x80[4], 6, 'c');
			num |= buildNumber(mData0x80[4], 6, 'd');
			num |= buildNumber(mData0x80[4], 5, 'p');
			num |= buildNumber(mData0x80[4], 4, 'f');
			num |= buildNumber(mData0x80[4], 3, 'm');
			num |= buildNumber(mData0x80[4], 2, 'e');
			num |= buildNumber(mData0x80[4], 1, 'o');
			num |= buildNumber(mData0x80[4], 1, 'q');
			num |= buildNumber(mData0x80[4], 0, 'n');
			num |= buildNumber(mData0x80[5], 7, 'i');
			num |= buildNumber(mData0x80[5], 6, 'h');
			num |= buildNumber(mData0x80[5], 5, 'j');
			num |= buildNumber(mData0x80[5], 5, 'k');
			num |= buildNumber(mData0x80[5], 5, 'l');
			num |= buildNumber(mData0x80[5], 4, 'g');

			
			setViewNum(R.id.odyssey20, num);
			

			num = 0;
			num |= buildNumber(mData0x80[5], 2, 'a');
			num |= buildNumber(mData0x80[5], 2, 'b');
			num |= buildNumber(mData0x80[5], 1, 'c');
			num |= buildNumber(mData0x80[5], 1, 'd');
			num |= buildNumber(mData0x80[5], 0, 'n');
			num |= buildNumber(mData0x80[6], 3, 'm');
			num |= buildNumber(mData0x80[6], 2, 'r');
			num |= buildNumber(mData0x80[6], 2, 's');
			num |= buildNumber(mData0x80[6], 1, 'o');
			num |= buildNumber(mData0x80[6], 1, 'q');
			num |= buildNumber(mData0x80[6], 0, 'p');
			num |= buildNumber(mData0x80[7], 6, 'i');
			num |= buildNumber(mData0x80[7], 6, 'j');
			num |= buildNumber(mData0x80[7], 5, 'k');
			num |= buildNumber(mData0x80[7], 5, 'l');			
			

			setViewNum(R.id.odyssey21, num);
			
			num = 0;
			num |= buildNumber(mData0x80[7], 4, 'a');	
			num |= buildNumber(mData0x80[7], 4, 'b');	
			num |= buildNumber(mData0x80[7], 4, 'c');	
			num |= buildNumber(mData0x80[7], 4, 'd');	
			num |= buildNumber(mData0x80[7], 4, 'm');	
			num |= buildNumber(mData0x80[7], 4, 'n');	
			num |= buildNumber(mData0x80[7], 0, 'o');	
			num |= buildNumber(mData0x80[7], 0, 'p');	
			num |= buildNumber(mData0x80[7], 0, 'q');			
			

			setViewNum(R.id.odyssey22, num);
			
			num = 0;
			num |= buildNumber(mData0x80[8], 3, 'm');
			num |= buildNumber(mData0x80[8], 2, 'a');
			num |= buildNumber(mData0x80[8], 2, 'b');	
			num |= buildNumber(mData0x80[8], 1, 'c');
			num |= buildNumber(mData0x80[8], 1, 'd');
			num |= buildNumber(mData0x80[8], 0, 'n');
			num |= buildNumber(mData0x80[9], 6, 'i');	
			num |= buildNumber(mData0x80[9], 6, 'j');
			num |= buildNumber(mData0x80[9], 5, 'o');	
			num |= buildNumber(mData0x80[9], 5, 'p');	
			num |= buildNumber(mData0x80[9], 5, 'q');	
			num |= buildNumber(mData0x80[9], 4, 'k');	
			num |= buildNumber(mData0x80[9], 4, 'l');			
			

			setViewNum(R.id.odyssey23, num);
			

			num = 0;
			num |= buildNumber(mData0x80[10], 7, 'a');	
			num |= buildNumber(mData0x80[10], 6, 'b');	
			num |= buildNumber(mData0x80[10], 5, 'c');	
			num |= buildNumber(mData0x80[10], 4, 'd');	
			num |= buildNumber(mData0x80[10], 3, 'm');	
			num |= buildNumber(mData0x80[10], 2, 'e');	
			num |= buildNumber(mData0x80[10], 1, 'o');	
			num |= buildNumber(mData0x80[10], 0, 'f');	
			num |= buildNumber(mData0x80[11], 7, 'r');	
			num |= buildNumber(mData0x80[11], 6, 'p');	
			num |= buildNumber(mData0x80[11], 5, 's');	
			num |= buildNumber(mData0x80[11], 4, 'n');	
			num |= buildNumber(mData0x80[11], 2, 'h');	
			num |= buildNumber(mData0x80[11], 1, 'q');	
			num |= buildNumber(mData0x80[11], 0, 'g');		
			num |= buildNumber(mData0x80[12], 7, 'i');		
			num |= buildNumber(mData0x80[12], 6, 'j');		
			num |= buildNumber(mData0x80[12], 5, 'k');		
			num |= buildNumber(mData0x80[12], 4, 'l');	
			

			setViewNum(R.id.odyssey25, num);
			

			num = 0;
			num |= buildNumber(mData0x80[12], 3, 'a');
			num |= buildNumber(mData0x80[12], 2, 'b');
			num |= buildNumber(mData0x80[12], 1, 'c');	
			num |= buildNumber(mData0x80[12], 0, 'd');

			num |= buildNumber(mData0x80[13], 3, 'm');
			num |= buildNumber(mData0x80[13], 2, 'e');
			num |= buildNumber(mData0x80[13], 1, 'o');	
			num |= buildNumber(mData0x80[13], 0, 'f');

			num |= buildNumber(mData0x80[14], 7, 'r');
			num |= buildNumber(mData0x80[14], 6, 'p');
			num |= buildNumber(mData0x80[14], 5, 's');	
			num |= buildNumber(mData0x80[14], 4, 'n');
			num |= buildNumber(mData0x80[14], 2, 'h');
			num |= buildNumber(mData0x80[14], 1, 'q');
			num |= buildNumber(mData0x80[14], 0, 'g');

			num |= buildNumber(mData0x80[15], 7, 'i');
			num |= buildNumber(mData0x80[15], 6, 'j');
			num |= buildNumber(mData0x80[15], 5, 'k');	
			num |= buildNumber(mData0x80[15], 4, 'l');
			
			setViewNum(R.id.odyssey26, num);
			

			num = 0;

			num |= buildNumber(mData0x80[15], 3, 'a');
			num |= buildNumber(mData0x80[15], 2, 'b');
			num |= buildNumber(mData0x80[15], 1, 'c');	
			num |= buildNumber(mData0x80[15], 0, 'd');
			num |= buildNumber(mData0x80[16], 7, 'm');
			num |= buildNumber(mData0x80[16], 6, 'e');
			num |= buildNumber(mData0x80[16], 5, 'o');
			num |= buildNumber(mData0x80[16], 4, 'f');
			num |= buildNumber(mData0x80[16], 3, 'r');
			num |= buildNumber(mData0x80[16], 2, 'p');
			num |= buildNumber(mData0x80[16], 1, 's');
			num |= buildNumber(mData0x80[16], 0, 'n');

			num |= buildNumber(mData0x80[17], 6, 'h');
			num |= buildNumber(mData0x80[17], 5, 'q');
			num |= buildNumber(mData0x80[17], 4, 'g');
			num |= buildNumber(mData0x80[17], 3, 'i');
			num |= buildNumber(mData0x80[17], 2, 'j');
			num |= buildNumber(mData0x80[17], 1, 'k');
			num |= buildNumber(mData0x80[17], 0, 'l');
			
			
			setViewNum(R.id.odyssey27, num);
			

			num = 0;

			num |= buildNumber(mData0x80[18], 7, 'a');
			num |= buildNumber(mData0x80[18], 6, 'b');
			num |= buildNumber(mData0x80[18], 5, 'c');
			num |= buildNumber(mData0x80[18], 4, 'd');
			num |= buildNumber(mData0x80[18], 3, 'm');
			num |= buildNumber(mData0x80[18], 2, 'e');
			num |= buildNumber(mData0x80[18], 1, 'o');
			num |= buildNumber(mData0x80[18], 0, 'f');

			num |= buildNumber(mData0x80[19], 7, 'r');
			num |= buildNumber(mData0x80[19], 6, 'p');
			num |= buildNumber(mData0x80[19], 5, 's');
			num |= buildNumber(mData0x80[19], 4, 'n');
			num |= buildNumber(mData0x80[19], 2, 'h');
			num |= buildNumber(mData0x80[19], 1, 'q');
			num |= buildNumber(mData0x80[19], 0, 'g');

			num |= buildNumber(mData0x80[20], 3, 'i');
			num |= buildNumber(mData0x80[20], 2, 'j');
			num |= buildNumber(mData0x80[20], 1, 'k');
			num |= buildNumber(mData0x80[20], 0, 'l');
			
			setViewNum(R.id.odyssey28, num);
			

			num = 0;

			num |= buildNumber(mData0x80[21], 7, 'a');
			num |= buildNumber(mData0x80[21], 6, 'b');
			num |= buildNumber(mData0x80[21], 5, 'c');
			num |= buildNumber(mData0x80[21], 4, 'd');
			num |= buildNumber(mData0x80[21], 3, 'm');
			num |= buildNumber(mData0x80[21], 2, 'e');
			num |= buildNumber(mData0x80[21], 1, 'o');
			num |= buildNumber(mData0x80[21], 0, 'f');

			num |= buildNumber(mData0x80[22], 7, 'r');
			num |= buildNumber(mData0x80[22], 6, 'p');
			num |= buildNumber(mData0x80[22], 5, 's');
			num |= buildNumber(mData0x80[22], 4, 'n');
			num |= buildNumber(mData0x80[22], 2, 'h');
			num |= buildNumber(mData0x80[22], 1, 'q');
			num |= buildNumber(mData0x80[22], 0, 'g');

			num |= buildNumber(mData0x80[23], 7, 'i');
			num |= buildNumber(mData0x80[23], 6, 'j');
			num |= buildNumber(mData0x80[23], 5, 'k');
			num |= buildNumber(mData0x80[23], 4, 'l');
			
			setViewNum(R.id.odyssey29, num);
		}
	}

	private void setViewNum(int id, int num) {
		LedView v = (LedView)mMainView.findViewById(id);
		if (v != null) {
			v.updateView(num);
		}
	}
	
	private void setViewVisible(int id, int visible) {
		View v = mMainView.findViewById(id);
		if (v != null) {
			v.setVisibility(visible == 0 ? View.INVISIBLE : View.VISIBLE);
		}
	}
	
	@Override
	public void parseCanboxData(byte[] data, int len) {
		switch (data[0]) {
		case (byte) 0x80:
			do0x80(data);
			break;
		default:
			super.parseCanboxData(data, len);
		}
	}

	public void setMediaMoreInfo(int source, int play, int total, int time,
			int total_time) {

	}

	public void setMediaSrc(int source, byte type, byte[] b) {

	}

	public void setMediaSrc(int source) {

	}

}
