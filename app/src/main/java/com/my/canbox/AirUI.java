package com.my.canbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.my.cartype.CarUtil;
import com.my.manager.McuManager;
import com.my.manager.OSProManager;
import com.my.out.R;
import com.car.view.BackTrackView;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;

public class AirUI extends UIBase implements View.OnClickListener {
	private Canbox mCanBox;

	private static AirUI[] mUI = new AirUI[MAX_DISPLAY];

	/** Called when the activity is first created. */
	public static AirUI getInstanse(Context context, View view, int index) {
		if (index >= MAX_DISPLAY) {
			return null;
		}

		mUI[index] = new AirUI(context, view, index);

		return mUI[index];
	}

	public AirUI(Context context, View view, int index) {
		super(context, view, index);
	}

	private static final int[] BUTTON_ON_CLICK = new int[] { R.id.wind_add,
			R.id.wind_add2, R.id.wind_minus, R.id.wind_minus2,
			R.id.left_temp_add, R.id.left_temp_minus, R.id.right_temp_add,
			R.id.right_temp_minus, R.id.left_temp_add2, R.id.left_temp_minus2,
			R.id.right_temp_add2, R.id.right_temp_minus2, R.id.wind_mode_add,
			R.id.wind_mode_minus, R.id.air_control_ac, R.id.air_control_auto,
			R.id.air_control_dual, R.id.air_control_rear, R.id.air_control_max,
			R.id.air_title_ce_inner_loop, R.id.air_control_ce_rear,
			R.id.air_control_power, };

	public void onCreate() {

		super.onCreate();

		initPresentationUI();

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public void onDestroy() {
		super.onDestroy();
	}

	// public update
	private void initPresentationUI() {
		for (int i : BUTTON_ON_CLICK) {
			View v = mMainView.findViewById(i);
			if (v != null) {
				v.setOnClickListener(this);
			}
		}

		Canbox mCanbox = CarUtil.getCanboxInstance();
		if (mCanbox != null) {
			if (mCanbox.mWindMaxStep != 0 && mCanbox.mWindMaxStep <= 0xc) {
				for (int i = 0; i < 0xc; i++) {
					if (i < mCanbox.mWindMaxStep) {
						mMainView.findViewById(R.id.wind_rate_1 + i)
								.setVisibility(View.VISIBLE);
					} else {
						mMainView.findViewById(R.id.wind_rate_1 + i)
								.setVisibility(View.GONE);
					}
				}
			}
		}
	}

	public void onClick(View view) {
		onClick(view.getId());
	}

	public void onClick(int id) {
		AirManager.prepareHide();
		setStyle(STYLE_ALL);
        if (id == R.id.wind_add || id == R.id.wind_add2) {
            sendAirFunchtion(0xa);
        } else if (id == R.id.wind_minus || id == R.id.wind_minus2) {
            sendAirFunchtion(0x9);
        } else if (id == R.id.left_temp_add || id == R.id.left_temp_add2) {
            sendAirFunchtion(0x3);
        } else if (id == R.id.left_temp_minus || id == R.id.left_temp_minus2) {
            sendAirFunchtion(0x2);
        } else if (id == R.id.right_temp_add || id == R.id.right_temp_add2) {
            sendAirFunchtion(0x5);
        } else if (id == R.id.right_temp_minus || id == R.id.right_temp_minus2) {
            sendAirFunchtion(0x4);
        } else if (id == R.id.wind_mode_add) {
            sendAirFunchtion(0x8);
        } else if (id == R.id.wind_mode_minus) {
            sendAirFunchtion(0x7);
        } else if (id == R.id.air_control_ac) {
            sendAirFunchtion(0x17);
        } else if (id == R.id.air_control_auto) {
            sendAirFunchtion(0x15);
        } else if (id == R.id.air_control_dual) {
            sendAirFunchtion(0x10);
        } else if (id == R.id.air_title_ce_inner_loop) {
            sendAirFunchtion(0x19);
        } else if (id == R.id.air_control_max) {
            sendAirFunchtion(0x13);
        } else if (id == R.id.air_control_rear) {
            sendAirFunchtion(0x2a);
        } else if (id == R.id.air_control_ce_rear) {
            sendAirFunchtion(0x14);
        } else if (id == R.id.air_control_power) {
            sendAirFunchtion(0x1);
        }
	}

	public void sendAirFunchtion(int i) {
		byte[] buf = new byte[] { (byte) 0xe0, 0x02, (byte) i, 0x1 };
		sendCanboxData(buf);
		Util.doSleep(20);
		buf[3] = 0x0;
		sendCanboxData(buf);
	}

	private void sendCanboxData(byte[] buf) {
		Canbox mCanbox = CarUtil.getCanboxInstance();
		if (mCanbox != null) {
			mCanbox.sendDataToCanbox(buf, buf.length);
		}
	}

	public void showStyle() {

	}

	// from air
	public static final int STYLE_ALL = 1;
	public static final int STYLE_INFO_ONLY = 2;
	private byte[] mAirData = new byte[9];
	public static int mStyle = 0;

	private void setStyle(int style) {
		mStyle = style;
		if (style == STYLE_ALL) {
			mMainView.findViewById(R.id.control_pannel).setVisibility(
					View.VISIBLE);
			// setViewVisible(mMainView, R.id.control_pannel, 1);
		} else {
			mMainView.findViewById(R.id.control_pannel).setVisibility(
					View.INVISIBLE);
			// setViewVisible(mMainView, R.id.control_pannel, 0);
		}
	}

	public void setAirData(byte[] airData) {
		setStyle(mStyle);
		if (airData != null) {
			Util.zeroBuf(mAirData);
			for (int i = 0; i < airData.length && i < mAirData.length; ++i) {
				mAirData[i] = airData[i];
			}
		}

		// test
		setAirCondtionTitle(mMainView);
		setAirCondtionWind(mMainView);
		if (mAirData[2] != (byte) 0xfb) {
			setAirCondtionTemperature(mMainView);
		}
	}

	private void setTextViewText(View view, int id, String s) {
		if (view != null && view.findViewById(id) != null) {
			((TextView) view.findViewById(id)).setText(s);
		}
	}

	private void setViewVisible(View view, int id, int show) {
		if (view != null && view.findViewById(id) != null) {
			if (show != 0) {// ac
				view.findViewById(id).setVisibility(View.VISIBLE);
			} else {
				view.findViewById(id).setVisibility(View.GONE);
			}
		}
	}

	void setAirCondtionTitle(View view) {
		setViewVisible(view, R.id.ac, (mAirData[0] & 0x40));
		setViewVisible(view, R.id.ac_max, (mAirData[4] & 0x04));
		setViewVisible(view, R.id.dual, (mAirData[0] & 0x04));
		setViewVisible(view, R.id.max, (mAirData[0] & 0x02));
		setViewVisible(view, R.id.rear, (mAirData[0] & 0x01));
		setViewVisible(view, R.id.rear_lock, (mAirData[4] & 0x08));
		// setViewVisible(view, R.id.ac, (mAirData[0]&0x40));
		// setViewVisible(view, R.id.ac, (mAirData[0]&0x40));
		// setViewVisible(view, R.id.ac, (mAirData[0]&0x40));

		if ((mAirData[4] & 0x80) != 0) {
			((ImageView) view.findViewById(R.id.inner_loop)).getDrawable()
					.setLevel(2);
			((ImageView) view.findViewById(R.id.air_title_ce_inner_loop))
					.getDrawable().setLevel(2);
		} else {
			if ((mAirData[0] & 0x20) != 0) {// inner loop,loop a
				((ImageView) view.findViewById(R.id.inner_loop)).getDrawable()
						.setLevel(1);
				((ImageView) view.findViewById(R.id.air_title_ce_inner_loop))
						.getDrawable().setLevel(1);

			} else {
				((ImageView) view.findViewById(R.id.inner_loop)).getDrawable()
						.setLevel(0);
				((ImageView) view.findViewById(R.id.air_title_ce_inner_loop))
						.getDrawable().setLevel(0);

			}
		}

		if ((mAirData[7] & 0x01) != 0) {
			view.findViewById(R.id.eco).setVisibility(View.VISIBLE);
		} else {
			view.findViewById(R.id.eco).setVisibility(View.GONE);
		}

		if ((mAirData[7] & 0x08) != 0) {
			view.findViewById(R.id.rear_switch).setVisibility(View.VISIBLE);
		} else {
			view.findViewById(R.id.rear_switch).setVisibility(View.GONE);
		}

		if ((mAirData[7] & 0x20) != 0) {
			view.findViewById(R.id.front).setVisibility(View.VISIBLE);
		} else {
			view.findViewById(R.id.front).setVisibility(View.GONE);
		}

		if ((mAirData[7] & 0x06) == 0) {
			view.findViewById(R.id.fast).setVisibility(View.GONE);
			view.findViewById(R.id.soft).setVisibility(View.GONE);
		} else {
			if ((mAirData[7] & 0x06) == 0x4) {
				view.findViewById(R.id.fast).setVisibility(View.VISIBLE);
				view.findViewById(R.id.soft).setVisibility(View.GONE);
			} else {
				view.findViewById(R.id.fast).setVisibility(View.GONE);
				view.findViewById(R.id.soft).setVisibility(View.VISIBLE);
			}
		}

		if ((mAirData[7] & 0x10) != 0) {
			view.findViewById(R.id.ac_auto).setVisibility(View.VISIBLE);
			((ImageView) view.findViewById(R.id.ac_auto)).getDrawable()
					.setLevel(2);
		} else if ((mAirData[0] & 0x10) != 0) {// ac auto
			view.findViewById(R.id.ac_auto).setVisibility(View.VISIBLE);
			((ImageView) view.findViewById(R.id.ac_auto)).getDrawable()
					.setLevel(1);
		} else if ((mAirData[0] & 0x08) != 0) {
			view.findViewById(R.id.ac_auto).setVisibility(View.VISIBLE);
			((ImageView) view.findViewById(R.id.ac_auto)).getDrawable()
					.setLevel(0);
		} else {
			view.findViewById(R.id.ac_auto).setVisibility(View.GONE);

		}

		if ((mAirData[7] & 0x80) != 0) {
			view.findViewById(R.id.sync).setVisibility(View.VISIBLE);
		} else {
			view.findViewById(R.id.sync).setVisibility(View.GONE);
		}

		if ((mAirData[8] & 0x80) != 0) {
			view.findViewById(R.id.rest).setVisibility(View.VISIBLE);
		} else {
			view.findViewById(R.id.rest).setVisibility(View.GONE);
		}
	}

	void setAirCondtionWind(View view) {
		if ((mAirData[1] & 0x80) != 0) {
			view.findViewById(R.id.wind_up1).setVisibility(View.VISIBLE);
			view.findViewById(R.id.wind_up12).setVisibility(View.VISIBLE);
			// view.findViewById(R.id.wind_up2).setVisibility(View.VISIBLE);
		} else {
			view.findViewById(R.id.wind_up1).setVisibility(View.INVISIBLE);
			view.findViewById(R.id.wind_up12).setVisibility(View.INVISIBLE);
			// view.findViewById(R.id.wind_up2).setVisibility(View.INVISIBLE);
		}

		if ((mAirData[1] & 0x40) != 0) {
			view.findViewById(R.id.wind_horizontal1)
					.setVisibility(View.VISIBLE);
			view.findViewById(R.id.wind_horizontal12).setVisibility(
					View.VISIBLE);
			// view.findViewById(R.id.wind_horizontal2).setVisibility(View.VISIBLE);
		} else {
			view.findViewById(R.id.wind_horizontal1).setVisibility(
					View.INVISIBLE);
			view.findViewById(R.id.wind_horizontal12).setVisibility(
					View.INVISIBLE);
			// view.findViewById(R.id.wind_horizontal2).setVisibility(View.INVISIBLE);
		}

		if ((mAirData[1] & 0x20) != 0) {
			view.findViewById(R.id.wind_down1).setVisibility(View.VISIBLE);
			view.findViewById(R.id.wind_down12).setVisibility(View.VISIBLE);
			// view.findViewById(R.id.wind_down2).setVisibility(View.VISIBLE);
		} else {
			view.findViewById(R.id.wind_down1).setVisibility(View.INVISIBLE);
			view.findViewById(R.id.wind_down12).setVisibility(View.INVISIBLE);
			// view.findViewById(R.id.wind_down2).setVisibility(View.INVISIBLE);
		}

		if ((mAirData[6] & 0x80) != 0) {
			// view.findViewById(R.id.wind_up1).setVisibility(View.VISIBLE);
			view.findViewById(R.id.wind_up2).setVisibility(View.VISIBLE);
		} else {
			// view.findViewById(R.id.wind_up1).setVisibility(View.INVISIBLE);
			view.findViewById(R.id.wind_up2).setVisibility(View.INVISIBLE);
		}

		if ((mAirData[6] & 0x40) != 0) {
			// view.findViewById(R.id.wind_horizontal1).setVisibility(View.VISIBLE);
			view.findViewById(R.id.wind_horizontal2)
					.setVisibility(View.VISIBLE);
		} else {
			// view.findViewById(R.id.wind_horizontal1).setVisibility(View.INVISIBLE);
			view.findViewById(R.id.wind_horizontal2).setVisibility(
					View.INVISIBLE);
		}

		if ((mAirData[6] & 0x20) != 0) {
			// view.findViewById(R.id.wind_down1).setVisibility(View.VISIBLE);
			view.findViewById(R.id.wind_down2).setVisibility(View.VISIBLE);
		} else {
			// view.findViewById(R.id.wind_down1).setVisibility(View.INVISIBLE);
			view.findViewById(R.id.wind_down2).setVisibility(View.INVISIBLE);
		}

		for (int i = 0; i < 0xc; i++) {
			if (i < (mAirData[1] & 0x0F)) {
				((ImageView) view.findViewById(R.id.wind_rate_1 + i))
						.getDrawable().setLevel(1);
			} else {
				((ImageView) view.findViewById(R.id.wind_rate_1 + i))
						.getDrawable().setLevel(0);
			}
		}
		if ((mAirData[1] & 0x0F) >= 7 && (mAirData[1] & 0x0F) <= 0xc) {
			((ImageView) view.findViewById(R.id.wind_rate_1
					+ (mAirData[1] & 0x0F) - 1)).setVisibility(View.VISIBLE);
		}

		setTextViewText(view, R.id.wind_status, (mAirData[1] & 0x0F) + "");
	}

	String getOurDoorTemperature(View view, double temperature) {
		String temp = null;

		boolean isCentigradeUnit = ((mAirData[5] & 0x1) == 0) ? true : false;
		if (isCentigradeUnit == true) {
			temp = String.format("%.1f%s", temperature, view.getResources()
					.getString(R.string.temp_unic_centigrade));
		} else {
			return String.format("%d%s", (int) temperature, view.getResources()
					.getString(R.string.temp_unic_fahrenheit));

		}

		return temp;
	}

	String getAirTemperature(View view, float temperature) {
		if ((mAirData[7] & 0x40) != 0) {
			return "";
		}
		boolean isCentigradeUnit = ((mAirData[5] & 0x1) == 0) ? true : false;
		if (isCentigradeUnit == true) {
			return String.format("%.1f%s", temperature / 2, view.getResources()
					.getString(R.string.temp_unic_centigrade));
		} else {
			return String.format("%d%s", (int) temperature, view.getResources()
					.getString(R.string.temp_unic_fahrenheit));
		}

	}

	private int outDoorTemp = 0xff;

	void setAirCondtionTemperature(View view) {
		int leftTemp = (int) (mAirData[2] & 0xff);
		int rightTemp = (int) (mAirData[3] & 0xff);

		String temp = "";
		if ((mAirData[5] & 0x2) == 0) {
			if (leftTemp == 0x00) {
				// ((TextView) view.findViewById(R.id.left_temp))
				// .setText(R.string.LO);
				temp = mContext.getString(R.string.LO);
			} else if (leftTemp == 0xff) {
				// ((TextView) view.findViewById(R.id.left_temp))
				// .setText(R.string.HI);
				temp = mContext.getString(R.string.HI);
			} else if (leftTemp == 0xfa) {
				// ((TextView) view.findViewById(R.id.left_temp)).setText("");
			} else {
				// ((TextView) view.findViewById(R.id.left_temp))
				// .setText(getAirTemperature(view, leftTemp));
				temp = getAirTemperature(view, leftTemp);
			}
			view.findViewById(R.id.left_temp).setVisibility(View.VISIBLE);

			setTextViewText(view, R.id.left_temp, temp);
			setTextViewText(view, R.id.left_temp2, temp);

		} else {
			view.findViewById(R.id.left_temp).setVisibility(View.INVISIBLE);
		}
		if ((mAirData[5] & 0x4) == 0) {
			if (rightTemp == 0x00) {
				// ((TextView) view.findViewById(R.id.right_temp))
				// .setText(R.string.LO);
				temp = mContext.getString(R.string.LO);
			} else if (rightTemp == 0xff) {
				// ((TextView) view.findViewById(R.id.right_temp))
				// .setText(R.string.HI);
				temp = mContext.getString(R.string.HI);
			} else if (rightTemp == 0xfa) {
				// ((TextView) view.findViewById(R.id.right_temp)).setText("");
			} else {
				// ((TextView) view.findViewById(R.id.right_temp))
				// .setText(getAirTemperature(view, rightTemp));
				temp = getAirTemperature(view, rightTemp);
			}
			view.findViewById(R.id.right_temp).setVisibility(View.VISIBLE);
			setTextViewText(view, R.id.right_temp, temp);
			setTextViewText(view, R.id.right_temp2, temp);
		} else {

			view.findViewById(R.id.right_temp).setVisibility(View.INVISIBLE);
		}

		// if (outDoorTemp != 0xff) {
		// if ((outDoorTemp & 0x80) != 0) {
		// outDoorTemp = -(((~outDoorTemp) & 0xff) + 1);
		// }
		//
		// view.findViewById(R.id.outdoor_temp).setVisibility(View.VISIBLE);
		// ((TextView) view.findViewById(R.id.outdoor_temp)).setText(view
		// .getResources().getString(R.string.outdoor_temp)
		// + getOurDoorTemperature(view, outDoorTemp));
		// } else {
		// view.findViewById(R.id.outdoor_temp).setVisibility(View.GONE);
		// }

		if ((mAirData[4] & 0x33) != mSeatHeat) {
			mSeatHeat = (mAirData[4] & 0x33);

			// view.findViewById(R.id.air_action_seat).setVisibility(View.VISIBLE);
			setViewVisible(view, R.id.air_action_seat, 1);
			setViewVisible(view, R.id.air_action_seat_left, 1);
			setViewVisible(view, R.id.air_action_seat_right, 1);
			for (int i = 0; i < 3; i++) {
				if (i < ((mAirData[4] >> 4) & 0x03)) {
					((ImageView) view.findViewById(R.id.seat_heat_left_1 + i))
							.getDrawable().setLevel(1);
				} else {
					((ImageView) view.findViewById(R.id.seat_heat_left_1 + i))
							.getDrawable().setLevel(0);
				}

				if (i < (mAirData[4] & 0x03)) {
					((ImageView) view.findViewById(R.id.seat_heat_right_1 + i))
							.getDrawable().setLevel(1);
				} else {
					((ImageView) view.findViewById(R.id.seat_heat_right_1 + i))
							.getDrawable().setLevel(0);
				}
			}
		} else {

			setViewVisible(view, R.id.air_action_seat, 0);
			setViewVisible(view, R.id.air_action_seat_left, 0);
			setViewVisible(view, R.id.air_action_seat_right, 0);
		}
	}

	private int mSeatHeat = 0;

	void setAirCondtionAction(View view) {
		// ((ImageView)view.findViewById(R.id.air_action_user)).setVisibility(View.VISIBLE);
		// switch(mAction){
		// case Canbox.ACTION_AC:
		// view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
		// view.findViewById(R.id.air_action).setVisibility(View.VISIBLE);
		// if((mState1&MASK_AC) != 0){
		// ((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
		// }else{
		// ((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_off);
		// }
		// ((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_ac);
		// break;
		//
		// case Canbox.ACTION_AC_MAX:
		// view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
		// view.findViewById(R.id.air_action).setVisibility(View.VISIBLE);
		// if((mState1&MASK_AC_MAX) != 0){
		// ((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
		// }else{
		// ((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_off);
		// }
		// ((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_ac_max);
		// break;
		//
		// case Canbox.ACTION_DUAL:
		// view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
		// view.findViewById(R.id.air_action).setVisibility(View.VISIBLE);
		// if((mState1&MASK_DUAL) != 0){
		// ((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
		// }else{
		// ((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_off);
		// }
		// ((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_dual);
		// break;
		//
		// case Canbox.ACTION_MAX_FRONT:
		// view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
		// view.findViewById(R.id.air_action).setVisibility(View.VISIBLE);
		// if((mState1&MASK_MAX) != 0){
		// ((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
		// }else{
		// ((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_off);
		// }
		// ((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_max);
		// break;
		//
		// case Canbox.ACTION_REAR:
		// view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
		// view.findViewById(R.id.air_action).setVisibility(View.VISIBLE);
		// if((mState1&MASK_REAR) != 0){
		// ((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
		// }else{
		// ((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_off);
		// }
		// ((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_rear);
		// break;
		//
		// case Canbox.ACTION_REAR_LOCK:
		// view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
		// view.findViewById(R.id.air_action).setVisibility(View.VISIBLE);
		// if((mState1&MASK_REAR_LOCK) != 0){
		// ((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
		// }else{
		// ((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_off);
		// }
		// ((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_rear_lock);
		// break;
		//
		// case Canbox.ACTION_LOOP:
		// view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
		// view.findViewById(R.id.air_action).setVisibility(View.VISIBLE);
		// if((mState2&MASK_INNER_LOOP) == 0xc0){//inner loop,loop a
		// ((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_inner_loop_a);
		// ((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
		// }else if((mState2&MASK_INNER_LOOP) == 0x40){
		// ((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_inner_loop);
		// ((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
		// }else{
		// ((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_inner_loop_a);
		// ((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_off);
		// }
		// break;
		//
		// case Canbox.ACTION_WIND_DIRECTION:
		// view.findViewById(R.id.air_action).setVisibility(View.VISIBLE);
		// view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
		// if((mWindMode&MASK_AC_AUTO) == 0x10){//inner loop,loop a
		// ((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_auto_large);
		// ((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
		// }else if((mWindMode&MASK_AC_AUTO) == 0x08){
		// ((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_auto_small);
		// ((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
		// }else{
		// ((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_auto_large);
		// ((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_off);
		// }
		// break;
		//
		// case Canbox.ACTION_LEFT_SEAT_HEAT:
		// case Canbox.ACTION_RIGHT_SEAT_HEAT:{
		// view.findViewById(R.id.air_action).setVisibility(View.INVISIBLE);
		// view.findViewById(R.id.air_action_seat).setVisibility(View.VISIBLE);
		// int seat = mCanBox.sendCommand(Canbox.CANBOX_READ_AIR_SEAT);
		// for(int i = 0;i < 3;i++){
		// if(i < ((seat>>4)&0x03)){
		// ((ImageView)view.findViewById(R.id.seat_heat_left_1 +
		// i)).getDrawable().setLevel(1);
		// }else{
		// ((ImageView)view.findViewById(R.id.seat_heat_left_1 +
		// i)).getDrawable().setLevel(0);
		// }
		//
		// if(i < (seat&0x03)){
		// ((ImageView)view.findViewById(R.id.seat_heat_right_1 +
		// i)).getDrawable().setLevel(1);
		// }else{
		// ((ImageView)view.findViewById(R.id.seat_heat_right_1 +
		// i)).getDrawable().setLevel(0);
		// }
		// }
		// }
		// break;
		// case Canbox.ACTION_AC_OFF:
		// view.findViewById(R.id.air_action).setVisibility(View.VISIBLE);
		// view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
		// ((ImageView)view.findViewById(R.id.air_action_user)).setVisibility(View.INVISIBLE);
		// ((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_off);
		// break;
		//
		// default:
		// view.findViewById(R.id.air_action).setVisibility(View.INVISIBLE);
		// view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
		// break;
		// }
	}

}
