package com.my.canbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Presentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.my.GlobalDef;
import com.my.cartype.CarUtil;
import com.my.manager.McuManager;
import com.my.manager.OSProManager;
import com.my.out.R;
import com.common.util.AppConfig;
import com.common.util.BroadcastUtil;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;

public class SosManager {

	private static WindowManager mWindowManager;
	private static WindowManager.LayoutParams mLayoutParams;
	private static View mView;

	private static Context mContext;

	private static ImageView mSOS;
	private static void init(Context context) {
		
		if (mView == null) {
			mContext = context;
			mView = ((LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.sos, null);

			mSOS = (ImageView)mView.findViewById(R.id.sos);
			mLayoutParams = new WindowManager.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0,
					LayoutParams.TYPE_SYSTEM_ERROR,
					LayoutParams.FLAG_LAYOUT_NO_LIMITS
							| LayoutParams.FLAG_LAYOUT_IN_SCREEN,
					PixelFormat.RGBA_8888);

			mWindowManager = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);

		}

	}

	public static int mPreCmd = 0;
	public static void start(Context context, int cmd) {
		init(context);
		if (mView.getParent() == null) {
			mWindowManager.addView(mView, mLayoutParams);
			McuManager mMcuManager = McuManager.getInstanse(null);
			if (mMcuManager != null) {
				mMcuManager.setVoulume(0);
			}

			
			
		}
		if(mPreCmd != cmd){
			mPreCmd = cmd;
			String s = null;
			switch (cmd) {
			case 2:
				s = "sos_test.png";
				break;
			case 1:
				Locale locale = Locale.getDefault();
				Log.d("dd", "" + locale.getLanguage());

				s = "sos.png";
				if (locale.getLanguage().equals("ru")) {
					s = "sos_ru.png";
				}
				break;
			}

			s = MachineConfig.getParamterPath() + s;
			
			Drawable d = Drawable.createFromPath(s);
			if (d!=null){
				mSOS.setImageDrawable(d);
			}
		}
	}

	public static void stop() {
		if (mView.getParent() != null) {
			mWindowManager.removeView(mView);
		}
	}
}
