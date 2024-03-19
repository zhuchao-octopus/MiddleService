package com.my.cartype.update;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.lang.reflect.Field;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.car.hardware.BackTrack;
import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.my.GlobalDef;
import com.common.util.Util;
import com.my.canbox.CanService;
import com.my.canbox.Canbox;
import com.my.canbox.RadarManager;
import com.my.cartype.CarUtil;
import com.my.manager.McuManager;
import com.my.out.R;

public class UpdateDialog extends Dialog {

	TextView mTitle;
	TextView mMessage;
	McuManager mcu;

	public UpdateDialog(Context c) {
		super(c);
		startKeepAcc();
		
	}

	public void setTitle(String s) {
		mTitle.setText(s);
	}

	public void setMsg(String s) {
		mMessage.setText(s);
	}

	private View.OnClickListener mOnClickDialogCancel = new View.OnClickListener() {
		public void onClick(View v) {
			stoptKeepAcc();
			dismiss();
		}
	};
	
	
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			startKeepAcc();
					
			super.handleMessage(msg);
		}
	};

	private void startKeepAcc() {
		mHandler.removeMessages(0);
		mHandler.sendEmptyMessageDelayed(0, 2000);
		if (mcu != null){
			mcu.setKeepAcc(1);
		}	
		
		
	}

	private void stoptKeepAcc() {
		mHandler.removeMessages(0);
		if (mcu != null) {
			mcu.setKeepAcc(0);
		}

		if (mCanbox != null) {
			CanService.mCanbox = mCanbox;
			mCanbox = null;
		}
	}


	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		CarUtil.mIsUpdating = true;
		startKeepAcc();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		CarUtil.mIsUpdating = false;
		stoptKeepAcc();
	}
	
	private Canbox mCanbox;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.canbox_update_msg);
		// ((TextView)
		// findViewById(R.id.alertTitle)).setText(R.string.update_canbox);
		mTitle = (TextView) findViewById(R.id.alertTitle);
		mMessage = (TextView) findViewById(R.id.message);
		findViewById(R.id.cancel).setOnClickListener(mOnClickDialogCancel);
		getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ERROR));
		mcu = McuManager.getInstanse();
		
		if (CanService.mCanbox != null){
			mCanbox = CanService.mCanbox;
			CanService.mCanbox = null;
		}
	}
}
