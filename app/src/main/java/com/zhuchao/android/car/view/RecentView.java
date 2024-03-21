package com.zhuchao.android.car.view;

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
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Display;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.common.util.AppConfig;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.Util;
import com.common.util.UtilSystem;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.canbox.ReverseManager;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.cartype.raise.NissanRaise;
import com.zhuchao.android.car.cartype.simple.Nissan2013Simple;
import com.zhuchao.android.car.R;

//import android.app.ActivityTaskManager;
public class RecentView {
	private final WindowManager mWindowManager;
	private final WindowManager.LayoutParams mLayoutParamsMicButton;

	private final View mViewMicButton;
	private final boolean isShowSpeech = false;
	private boolean isstart = false;
	private Context mContext;
	LinearLayout mHorizontalScrollView;
	public static RecentView mThis;

	public static void start(Context context) {
		if (mThis == null) {
			mThis = new RecentView(context);
		}
		mThis.doStart();
	}
	
	public static void toggle(Context context) {
		if (mThis == null) {
			mThis = new RecentView(context);
		}
		
		if (!mThis.isstart){
			mThis.doStart();
		} else {
			mThis.hideMicButton();
		}
	}

	public RecentView(Context context) {
		mContext = context;
		mWindowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);

		mLayoutParamsMicButton = new WindowManager.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0,
				LayoutParams.TYPE_PHONE, 0, PixelFormat.RGBA_8888);
		mLayoutParamsMicButton.gravity = Gravity.CENTER;
		// mLayoutParamsMicButton.alpha = 0.80f;

		mViewMicButton = ((LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.recent_main, null);

		mViewMicButton.findViewById(R.id.clear_all).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						clearAll();
					}
				});

		mViewMicButton.setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						hideMicButton();
					}
				});
		
		mViewMicButton.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				// TODO Auto-generated method stub
				hideMicButton();
				return true;
			}
		});

		mTrList = mViewMicButton.findViewById(R.id.list_view);
		mTrList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
//				openActivity(position);
			//	hideMicButton();
				mHandler.removeMessages(0);
				mHandler.sendMessageDelayed(mHandler.obtainMessage(0, position, 0), 100);
			}
		});
	}
	
	private final Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			hideMicButton();
			openActivity(msg.arg1);
		}
	};

	private class MyListAdapter extends BaseAdapter {
		public MyListAdapter(Context context) {
			mContext = context;
		}

		@Override
		public int getCount() {
			// return recentTasks.size();
			return mListData.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v;
			if (convertView == null) {
				v = newView(parent);
			} else {
				v = convertView;
			}
			bindView(v, position, parent);
			return v;
		}

		private class ViewHolder {
			TextView name;
			View bg;
			ImageView bt;
		}

		private View newView(ViewGroup parent) {
			View v = LayoutInflater.from(mContext).inflate(
					R.layout.recent_view, parent, false);
			ViewHolder vh = new ViewHolder();
			vh.name = v.findViewById(R.id.text);
			vh.bg =  v.findViewById(R.id.img);
			vh.bt = v.findViewById(R.id.close);
			v.setTag(vh);
			return v;
		}

		private void bindView(View v, int position, ViewGroup parent) {

			if (position < mListData.size()) {
				ViewHolder vh = (ViewHolder) v.getTag();
				CData c = mListData.get(position);
				try {
					BitmapDrawable bd = new BitmapDrawable(c.bmp);
					vh.bg.setBackground(bd);
					vh.name.setText(c.name);
					vh.bt.setOnClickListener(mClickListenerClose);
					vh.bt.setTag(position);
				} catch (Exception e) {
					Log.d("fff", "bindView!!!"+e);
				}
			}
		}

	}

	OnClickListener mClickListenerClose = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			int index = (Integer) arg0.getTag();
			if (index < mListData.size()) {
				CData c = mListData.get(index);
				c.bmp.recycle();
				clearTask(c.id);
			}
			mListData.remove(index);
			mMyListAdapter.notifyDataSetChanged();
		}
	};

	private void clearAllBmp() {
//		for (int i = 0; i < mListData.size(); ++i) {
//			CData c = mListData.get(i);
//			c.bmp.recycle();
//		}
		mListData.clear();
	}

	private void clearAll() {

	//	Log.d("fff", "clearAll!!!");
		for (int i = 0; i < mListData.size(); ++i) {
			CData c = mListData.get(i);
			clearTask(c.id);
		}
		clearAllBmp();
		mMyListAdapter.notifyDataSetChanged();
	}

	private void clearTask(int id) {
		Log.d("fff", "clearTask:" + id);

		ActivityManager am = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		try {
		//ActivityTaskManager.getService().removeTask(id);
	//		am.removeTask(id);
		} catch (Exception e) {
			Log.d("fff", String.valueOf(e));
		}
	}

	MyListAdapter mMyListAdapter;

	private final ListView mTrList;

	private void initTask() {
		mMyListAdapter = new MyListAdapter(mContext);
		mTrList.setAdapter(mMyListAdapter);
		
		loadThumbnail();
		// test

//		Bitmap b = BitmapFactory.decodeFile("/mnt/paramter/icon/bt.png");
//
//		CData c = new CData("abc", 123, b);
//		mListData.add(c);
//		b = BitmapFactory.decodeFile("/mnt/paramter/icon/music.png");
//
//		c = new CData("abcdsfs", 123, b);
//		mListData.add(c);
//
//		b = BitmapFactory.decodeFile("/mnt/paramter/icon/video.png");
//
//		c = new CData("abcdsfs", 123, b);
//		mListData.add(c);
	}

	private void doStart() {
		if (!isstart) {
			isstart = true;
			mWindowManager.addView(mViewMicButton, mLayoutParamsMicButton);
		}
		// getRecentTasks(mContext, 20);
		// reloadButtons(mContext, 20);
		// loadThumbnail(0);
		initTask();
	}

	public void hideMicButton() {
		if (isstart) {
		//	clearAllBmp();
			try {
				mWindowManager.removeView(mViewMicButton);
			} catch (Exception e) {

			}
			isstart = false;
		}
	}

	public static void getRecentTasks(Context context, int appNumber) {

	}
	private void openActivity(int pos){
		
		try {
			CData c = mListData.get(pos);
			mContext.startActivity(c.intent);

		} catch (Exception e) {
			Log.d("fff", "openActivity:" + e);
		}
	}

	public class CData {
		public String name;
		public int id;
		public Bitmap bmp;
		Intent intent;

		CData(String s, int i, Bitmap b,Intent it) {
			bmp = b;
			id = i;
			name = s;
			intent = it;
		}
	}

	ArrayList<CData> mListData = new ArrayList<CData>();
	/**
	 * 调用逻辑
	 * 
	 * TaskThumbnails o = am.getTaskThumbnails(id) Bitmap b = o.mainThumbnail;
	 * 
	 * @param am
	 *            ActivityManager 类实例
	 * @param id
	 *            标识值
	 * @return
	 */

	public static Bitmap getTaskThumbnailsBitmap(ActivityManager am, int id) {
		Log.d("ffck1", "??getTaskThumbnailsxxxxxx:" + id);
		//synchronized (refLock) {
			    /*
			     ActivityManager.TaskSnapshot snapshot = null;
			        try {
			            snapshot = ActivityTaskManager.getService().getTaskSnapshot(id, false);
			           // Log.d("ffck1", "1111aaaaaaaaaaaaa:" + snapshot);
			            if (snapshot != null){
			            	
			            Bitmap thumbnail = null;
			            final GraphicBuffer buffer = snapshot.getSnapshot();
			          //  Log.d("ffck1", "1111aaaaaaaaaaaaa:" + buffer);
			            if (buffer == null ) {
			                // TODO(b/157562905): Workaround for a crash when we get a snapshot without this state
			              //  Log.e("ffck1", "Unexpected snapshot without USAGE_GPU_SAMPLED_IMAGE: "
			              //          + buffer);
			             //   Point taskSize = snapshot.getTaskSize();
			             //   thumbnail = Bitmap.createBitmap(taskSize.x, taskSize.y, Config.ARGB_8888);
			             //   thumbnail.eraseColor(Color.BLACK);
			            } else {
			                thumbnail = Bitmap.wrapHardwareBuffer(buffer, snapshot.getColorSpace());
			            }
			           return thumbnail;
			            }
			        } catch (RemoteException e) {
			            Log.w("ffck1", "Failed to retrieve task snapshot", e);
			        }
				*/
			//	TaskThumbnail tb = am.getTaskThumbnail(id);
				return null;
//				if (getTaskThumbnails == null) {
//					getTaskThumbnails = am.getClass().getDeclaredMethod(
//							"getTaskThumbnails", int.class);
//				}
//				Log.d("ffck", "getTaskThumbnails:" + getTaskThumbnails);
//				if (getTaskThumbnails != null) {
//					Object thumbnails = getTaskThumbnails.invoke(am,
//							Integer.valueOf(id));
//
//					Log.d("ffck", "thumbnails:" + thumbnails);
//					if (thumbnails != null) {
//						if (taskThumbnailsBitmap == null)
//							taskThumbnailsBitmap = thumbnails.getClass()
//									.getField("mainThumbnail");
//
//						Log.d("ffck", "thumbnails:" + taskThumbnailsBitmap);
//						if (taskThumbnailsBitmap != null) {
//							return (Bitmap) taskThumbnailsBitmap
//									.get(thumbnails);
//						}
//					}
//				}
			
	//	}
	}

	static final int MAX_RECENT_TASKS = 64;
	List<ActivityManager.RecentTaskInfo> recentTasks;

	void loadThumbnail() {
		int index;
		ActivityManager am = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		recentTasks = am.getRecentTasks(MAX_RECENT_TASKS,
				ActivityManager.RECENT_IGNORE_UNAVAILABLE);
		int numTasks = recentTasks.size();

		PackageManager pm;
		pm = mContext.getPackageManager();
		String label = "";
		for (index = 0; index < numTasks; ++index) {
			if (index < numTasks) {
				final ActivityManager.RecentTaskInfo info = recentTasks
						.get(index);
				if (info != null) {
					Bitmap b = getTaskThumbnailsBitmap(am, info.id);
					if (b != null) {
						
						ComponentName cn = info.baseIntent.getComponent();

						Log.d("ffck2", "1loadThumbnail:" + cn);
						try {
							ActivityInfo ainfo = pm.getActivityInfo(cn, 0);
							label = ainfo.loadLabel(pm).toString();
						} catch (Exception e) {
							Log.d("ffck2", "!!!!!!!!!!!!!!!!!" + e);
						}
						boolean exist = true;
						for (int i = 0; i < mListData.size(); ++i) {
							CData c = mListData.get(i);
							if (info.id == c.id){
								exist = false;
								Log.d("ffck2", "exist break!!!!!!!!!!!!!!!!!");
								break;
							}
						}
						
						if (exist) {
							CData c = new CData(label, info.id, b,
									info.baseIntent);
							mListData.add(c);
						}
					}
					Log.d("ffck1", info + "1loadThumbnail:" + b);
					Log.d("ffck2", info.id + "1loadThumbnail:" + b);

					// b = getTaskThumbnailsBitmap(am, info.affiliatedTaskId);
					// Log.d("ffck", index + "2loadThumbnail:" + b);
					//
					//
					// b = getTaskThumbnailsBitmap(am, info.stackId);
					// Log.d("ffck", info.lastActiveTime + "3loadThumbnail:" +
					// b);
					//
					//
					// b = getTaskThumbnailsBitmap(am, info.userId);
					// Log.d("ffck", info.baseIntent + "4loadThumbnail:" + b);
					//
					//
					// b = getTaskThumbnailsBitmap(am, info.describeContents());
					// Log.d("ffck", info.toString() + "5loadThumbnail:" + b);
				}
			}
		}
	}
	
}
