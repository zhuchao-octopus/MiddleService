package com.zhuchao.android.car.manager.key;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.common.util.MyCmd;
import com.common.util.Util;
import com.common.util.decode.JavaDecode;
import com.common.util.log.JLog;
import com.zhuchao.android.car.manager.OSProManager;
import com.zhuchao.android.car.manager.key.TouchKeyProcessor.Keys;

/**
 * 处理触摸按键，形成各种事件
 * 
 * @author sky
 *
 */
public class JoyKey {

	private static final String TAG = "JoyKey";

	private final static String TOUCH_KEY_MAPPING_FILE = MyCmd.VENDOR_DIR
			+ ".joy_key_mapping";
	private static final int[] KEYS = new int[] { MyCmd.Keycode.KEY_JOY_UP,
			MyCmd.Keycode.KEY_JOY_DOWN, MyCmd.Keycode.KEY_JOY_LEFT,
			MyCmd.Keycode.KEY_JOY_RIGHT, MyCmd.Keycode.KEY_JOY_ENTER,
			MyCmd.Keycode.KEY_JOY_ROLL_LEFT, MyCmd.Keycode.KEY_JOY_ROLL_RIGHT,
			MyCmd.Keycode.KEY_JOY_HOME, MyCmd.Keycode.KEY_JOY_BACK };

	private int[] mMapKey;
	private int[] mMapKeyStudy;

	private final Context mContext;

	public JoyKey(Context c) {
		mContext = c;
		initMapKey();
		registerListener();
	}

	private void initMapKey() {
		File f = new File(TOUCH_KEY_MAPPING_FILE);
		if (f.exists()) {

			FileReader fr = null;
			BufferedReader br = null;
			try {
				fr = new FileReader(f);
				br = new BufferedReader(fr);
				String str = br.readLine();
				if (str != null) {
					String[] ss = str.split(",");
					if (ss != null && ss.length == KEYS.length) { // correct
																	// data
						mMapKey = new int[KEYS.length];

						for (int i = 0; i < KEYS.length; ++i) {
							Log.d("ee", "ff:" + mMapKey[i]);

							mMapKey[i] = Integer.valueOf(ss[i]);

							Log.d("ee", "22ff:" + mMapKey[i]);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (br != null) {
						br.close();
					}
					if (fr != null) {
						fr.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	private void saveMapping() {

		File f = new File(TOUCH_KEY_MAPPING_FILE);
		if (f.exists()) {
			f.delete();
		}
		if (mMapKeyStudy != null) {
			if (mMapKey == null) {
				mMapKey = new int[KEYS.length];
			}

			String s = "";
			for (int i = 0; i < mMapKeyStudy.length; ++i) {
				if (i != 0) {
					s += ",";
				}
				s += mMapKeyStudy[i];
				mMapKey[i] = mMapKeyStudy[i];
			}

			FileWriter fw = null;
			BufferedWriter bw = null;
			try {
				f.createNewFile();

				//FileUtils.setPermissions(TOUCH_KEY_MAPPING_FILE,FileUtils.S_IRWXU | FileUtils.S_IRWXG | FileUtils.S_IRWXO, -1, -1);

				fw = new FileWriter(f);
				bw = new BufferedWriter(fw);
				bw.write(s);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (bw != null) {
						bw.close();
					}
					if (fw != null) {
						fw.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			mMapKey = mMapKeyStudy;
		}
	}

	public static boolean isJoyKey(int key) {
		for (int i = 0; i < KEYS.length; ++i) {
			if (key == KEYS[i]) {
				return true;
			}
		}
		return false;
	}

	private int mJoyKeyType = 0;

	private int toMapShortKey(int key) {
		if (mMapKey != null) {
			for (int i = 0; i < mMapKey.length; ++i) {
				if (KEYS[i] == key) {
					if (mMapKey[i] != 0) {
						key = (mMapKey[i] & 0xff);
						return key;
					}
				}
			}
		}
		return 0;
	}

	private int toMapLongKey(int key) {
		if (mMapKey != null) {
			for (int i = 0; i < mMapKey.length; ++i) {
				if (KEYS[i] == key) {
					if (mMapKey[i] != 0) {
						key = (mMapKey[i] & 0xff00) >> 8;
						return key;
					}
				}
			}
		}
		return 0;
	}

	private boolean isContinueNoLongKey(int key) { // only volume now
        return key == MyCmd.Keycode.VOLUME_DOWN || key == MyCmd.Keycode.VOLUME_UP || key == MyCmd.Keycode.KEY_JOY_LEFT || key == MyCmd.Keycode.KEY_JOY_RIGHT;
    }

	private int mDownKey;
	private long mDownTime;
	private final static int LONG_CLICK_TIME = 1500;

	public int doKey(int key, boolean down) {
		if (mJoyKeyType == 0) {
			int toKey;

			if (down) {
				if (mDownKey == 0) {
					mDownTime = System.currentTimeMillis();
					mDownKey = key;
				}

				if (mDownKey > 0) {
					boolean longClick = (System.currentTimeMillis() - mDownTime) > LONG_CLICK_TIME;
                    Log.d("dd", "islong"+longClick);
					if (!longClick) {
						toKey = toMapShortKey(key);
						if (toKey != 0) {
							if (isContinueNoLongKey(toKey)) {
								return toKey;
							}
						} else {
							if (isContinueNoLongKey(key)) {
								return key;
							}
						}
					} else {
						toKey = toMapLongKey(key);
						if (toKey != 0) {
							mDownKey = -1;
							return toKey;
						} else {

						}
					}
				}
			} else {
				if (mDownKey == key) {
					toKey = toMapShortKey(key);
					if (toKey != 0) {
						if (!isContinueNoLongKey(toKey)) {
							mDownKey = 0;
							return toKey;
						}
					} else {
						if (!isContinueNoLongKey(key)) {
							mDownKey = 0;
							return key;
						}
					}
				}
				mDownKey = 0;
			}
		} else {
			// sendStudyMsg(key);
			if (!down) {
				Intent it = new Intent(MyCmd.BROADCAST_RETURN_JOY_STUDY);
				it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.TOUCH_STUDY_KEY);
				it.putExtra(MyCmd.EXTRA_COMMON_DATA, key);
				mContext.sendBroadcast(it);
			}
		}
		return 0;

	}

	private void doTouchStudy(int cmd, int key, int keyStudy) {
		switch (cmd) {
		case MyCmd.Cmd.TOUCH_STUDY_START:
			mJoyKeyType = 1;

			// if (mMapKeyStudy == null) {
			mMapKeyStudy = new int[KEYS.length];

			if (mMapKey != null) {
                System.arraycopy(mMapKey, 0, mMapKeyStudy, 0, KEYS.length);
			}
			// }

			Intent it = new Intent(MyCmd.BROADCAST_RETURN_JOY_STUDY);
			it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
			it.putExtra(MyCmd.EXTRA_COMMON_DATA, mMapKeyStudy);
			mContext.sendBroadcast(it);

			break;
		case MyCmd.Cmd.TOUCH_STUDY_KEY: {

			if (mMapKeyStudy == null) {
				mMapKeyStudy = new int[KEYS.length];
			}

			for (int i = 0; i < KEYS.length; ++i) {
				if (KEYS[i] == key) {
					mMapKeyStudy[i] = keyStudy;
				}
			}

			it = new Intent(MyCmd.BROADCAST_RETURN_JOY_STUDY);
			it.putExtra(MyCmd.EXTRA_COMMON_CMD, MyCmd.Cmd.TOUCH_STUDY_START);
			it.putExtra(MyCmd.EXTRA_COMMON_DATA, mMapKeyStudy);
			mContext.sendBroadcast(it);
		}
			break;
		case MyCmd.Cmd.TOUCH_STUDY_CLEAR:
			mMapKeyStudy = null;
			break;
		case MyCmd.Cmd.TOUCH_STUDY_END:
			saveMapping();
			mJoyKeyType = 0;
			break;
		case MyCmd.Cmd.STUDY_QUIT_WITHOUT_SAVE:
			mMapKeyStudy = null;
			mJoyKeyType = 0;
			break;
		default:
			mJoyKeyType = 0;
        }
	}

	// private void sendStudyMsg(int key) {
	// Intent it = new Intent(MyCmd.BROADCAST_RETURN_JOY_STUDY);
	// it.putExtra(MyCmd.EXTRA_COMMON_CMD, key);
	// it.putExtra(MyCmd.EXTRA_COMMON_DATA, key);
	// mContext.sendBroadcast(it);
	// }

	private BroadcastReceiver mReceiver = null;

	private void registerListener() {
		if (mReceiver == null) {
			mReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					Log.d(TAG, "registerListener" + action);
					if (action.equals(MyCmd.BROADCAST_SET_JOY_STUDY)) {
						int cmd = intent.getIntExtra(MyCmd.EXTRA_COMMON_CMD, 0);
						int data = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA,
								0);
						int data2 = intent.getIntExtra(
								MyCmd.EXTRA_COMMON_DATA2, 0);
						doTouchStudy(cmd, data, data2);
					}

				}
			};
			IntentFilter iFilter = new IntentFilter();
			iFilter.addAction(MyCmd.BROADCAST_SET_JOY_STUDY);

			mContext.registerReceiver(mReceiver, iFilter);
		}
	}

}
