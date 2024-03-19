package com.my.view;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.filterfw.geometry.Point;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
//import android.graphics.GraphicBuffer;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.common.util.AppConfig;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.ResourceUtil;
import com.common.util.Util;
import com.common.util.UtilSystem;
import com.common.view.KeyButtonRipple;
import com.my.canbox.Canbox;
import com.my.canbox.ReverseManager;
import com.my.cartype.CarUtil;
import com.my.cartype.raise.NissanRaise;
import com.my.cartype.simple.Nissan2013Simple;
import com.my.out.R;

//import android.app.ActivityTaskManager;
public class ParentView extends LinearLayout {
	public ParentView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ParentView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);

	}

	private View.OnKeyListener mOnKeyListener;

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {

		if (mOnKeyListener != null) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
					|| event.getKeyCode() == KeyEvent.KEYCODE_HOME) {
				mOnKeyListener.onKey(this, event.getKeyCode(), event);
				return true;
			}
		}

		return super.dispatchKeyEvent(event);
	}

	@Override
	public void setOnKeyListener(OnKeyListener l) {
		super.setOnKeyListener(l);
		mOnKeyListener = l;
	}

}
