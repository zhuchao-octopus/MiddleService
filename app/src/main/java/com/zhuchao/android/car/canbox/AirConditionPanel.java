package com.zhuchao.android.car.canbox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.utils.AppConfig;
import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.common.utils.UtilSystem;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.fbase.MMLog;

import java.util.Locale;
import java.util.Objects;


public class AirConditionPanel extends Handler {
    public static final int MESSAGE_AIR_CONDITION = 0x01;
    public static final int MESSAGE_AIR_TO_ACCONTROL_APK = 0x10;
    public static final int MESSAGE_AIRDATA_TO_ACCONTROL_APK = 0x11;
    /**
     * Called when the activity is first created.
     */
    private static final String TAG = "AirConditionPanel";
    //	public static final int MESSAGE_AIR_OUTDOOR_TEMP = 0x02;
    //	public static final int MESSAGE_AIR_HIDE = 0x03;
    private final byte[] mAirData = new byte[15];
    private final byte[] mAirDataBackup = new byte[15];
	/* 参考欣朴大众协议v2.61.002,考虑兼容性，不完全一致
	 data[0]
        Bit7: 空调开关指示
        Bit6: A/C指示
        Bit5: 内外循环指示  0:外循环
        Bit4 AUTO 大风灯指示
        Bit3:AUTO 小风灯指示
        Bit2: DUAL 指示
        Bit 1:  前窗除雾  MAX FRONT灯指示
        Bit 0: REAR灯指示 后窗 加热?

     data[1] 0:OFF 1:ON
        Bit7	向上送风指示
        Bit6	水平送风指示
        Bit5	向下送风指示
        Bit4	空调显示请求
        Bit3~Bit0
        风速	0x0~07	风速等级 指示 0-7级

     data[2] 左边设定温度
        0x00: LO
        0xff: HI
        0xfa: hide
        0xfb: no update
        0xf0~0xf9: show step
        0x01~0xef: 温度, 0.5 步进

     data[3] 右边设定温度
        0x00: LO
        0xff: HI
        0xfa: hide
        0x01~0xff: 温度

     data[4] 座椅加热
        Bit7 1==AQS内循环 0==非
        Bit5~4 左座椅 00:不显示 01~11:1~3级温度
        Bit3 rear lock 1==LOCK 0==非
        Bit2 1==AC MAX 0==非
        Bit1~0 右座椅 00:不显示 01~11:1~3级温度

     data[5] bit0: 0-> C 1-> F 温度单位
        b102,105,106,107,87,108,113,115,116,117,121,123",  "ids": [it1: 1: hide left temp
        bit2: 1: hide right temp
        bit3: 1: 分开左右吹风模式
        bit4: 前窗加热
        bit5: 空气质量

        bit7: 0：空调数据有变化就会弹出空调界面。
              1：强制参考空调开关位，如果此位设为1,空调开关(data[0]bit8)是关的话，不主动弹出空调界面。威驰协议一般都需要设置此位。



      data[6]
        同 data[1]，右边座椅吹风。  有些车分左右吹风。

      data[7]
        Bit 0: eco
        Bit 1~2:  0:off 1:soft 2:fast  3:Normal                       ( 0:soft 1:off 2:fast (bagoo GM) )
        Bit3 AUTO REAR SWITCH
        Bit4 AUTO 超大风灯指示
        Bit5 前窗除雾 （有些车有前窗除雾 MAX FRONT灯指示，又另外有一个前窗除雾）
        Bit6 0：手动空调, 1:自动空调
        Bit7 sync 指示

      data[8]
        Bit7 rest 指示
        Bit6 temp show level 指示
        Bit5~4 左座椅 00:不显示 01~11:1~3级冷风
        Bit3~2 右座椅 00:不显示 01~11:1~3级冷风
        Bit1 左座椅 >3级冷风高位
        Bit0 右座椅 >3级冷风高位

      data[9]
        Bit7 后座空调开关
        Bit6 左座椅 >3级加热高位
        Bit5 负离子 或者 森林
        Bit4 SWING吹风(出风口摆动) 或者  上出风口
        Bit3 花粉
        Bit2 右座椅 >3级加热高位

        Bit1 后座Ac Auto开头
        Bit0 Auto 吹风模式

      data[10]
        后座温度。 同前面左右温度8

      data[11] 0:OFF 1:ON 后座风速及模式
        Bit7	向上送风指示
        Bit6	水平送风指示
        Bit5	向下送风指示
        Bit4	后排AUTO状态 吹风模式
        Bit3~Bit0
        风速	0x0~07	风速等级 指示 0-7级

      data[12] 一些特殊车型的特殊信息
        Bit2~Bit0
        0~3 方向盘加热级别
        Bit4~Bit3
        风量等级 0:低 1：中 2：高
        Bit5 自动风量 Auto
        Bit6 前窗除冰

      data[13] 一些特殊车型的特殊信息
        Bit3~2 后区右座椅 00:不显示 01~11:1~3级温度
        Bit1~0 后区左座椅 00:不显示 01~11:1~3级温度

      data[14]
        后座右边温度。 同前面左右温度8
	 */
    //	private Toast mToast = null;
    private final Context mContext;
    WindowManager mWindowManager;
    WindowManager.LayoutParams mLayoutParams;
    View airConditionView = null;
    private final Handler mHandler = new Handler(Objects.requireNonNull(Looper.myLooper())) {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                if (airConditionView != null) {
                    if (airConditionView.getParent() != null) {
                        mWindowManager.removeView(airConditionView);
                    }
                }
            }
            super.handleMessage(msg);
        }
    };
    private int outDoorTemp = 0xff;
    private int mSeatHeat = 0;
    private int mSeatCold = 0;
    private boolean mSeatColdExit = false;

    @SuppressLint("InflateParams")
    public AirConditionPanel(Context context) {
        //    	Log.e(TAG,"AirConditionPanel");
        mContext = context;

        //		mToast = new Toast(context); //Toast.makeText(context,"", Toast.LENGTH_SHORT);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        airConditionView = inflater.inflate(R.layout.air, null);
        //		mToast.setGravity(Gravity.CENTER, 0, 0);
        //		mToast.setView(airConditionView);

        mLayoutParams = new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0, LayoutParams.TYPE_SYSTEM_ERROR, LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_LAYOUT_IN_SCREEN | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.RGBA_8888);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void postChanged(int type, Object obj) {
        ///		if (hasMessages(type))
        ///			return;
        obtainMessage(type, obj).sendToTarget();
    }

    public void postChanged(int type, int arg1, int arg2, Object obj) {
        //		if (hasMessages(type))
        //			return;
        obtainMessage(type, arg1, arg2, obj).sendToTarget();
    }

    public void postChanged(int type, int arg1, int arg2) {
        //		if (hasMessages(type))
        //			return;
        obtainMessage(type, arg1, arg2).sendToTarget();
    }

    private boolean isExtShow() {
        //		if (MachineConfig.VALUE_CANBOX_TOYOTA_BINARYTEK.equals(CarUtil.getCanboxType())) {
        //			return true;
        //		}
        return false;
    }

    private boolean isBufEqual(byte[] b1, byte[] b2, int len) {
        for (int i = 0; i < len; ++i) {
            if (b1[i] != b2[i]) {
                return false;
            }
        }
        return true;
    }

    public void handleMessage(Message msg) {
        ///MMLog.d(TAG, "AirConditionPanel.handleMessage " + msg.toString()+","+CarUtil.isShowAC());
        switch (msg.what) {
            case MESSAGE_AIR_CONDITION:
                byte[] airData = (byte[]) msg.obj;
                boolean off = false;

                ///boolean mAirDataBackupEmpty = Util.isZero(mAirDataBackup);
                boolean dataEqual = isBufEqual(mAirDataBackup, airData, airData.length);

                if (!dataEqual) {
                    Util.byteArrayCopy(mAirDataBackup, airData, 0, 0, airData.length);
                }

                if ((airData[0] & 0x80) == 0) {
                    //off = true;
                    if ((airData[5] & 0x80) != 0) {
                        off = true;
                    } else if ((mAirData[0] & 0x80) != 0) {
                        off = true;
                    }
                }

                ///Log.d("ffcc", ""+mAirDataBackupEmpty);
                ///if (mAirDataBackupEmpty) {
                ///off = true;
                ///}

                mAirData[0] = airData[0];
                mAirData[1] = airData[1];
                mAirData[2] = airData[2];
                mAirData[3] = airData[3];
                mAirData[4] = airData[4];
                mAirData[5] = airData[5];
                mAirData[6] = airData[6];
                mAirData[7] = airData[7];
                if (msg.arg1 == 0 && ((mAirData[5] & 0x8) == 0)) {
                    mAirData[6] = mAirData[1];
                }

                if (airData.length > 8) {
                    mAirData[8] = airData[8];
                }

                if (airData.length > 9) {
                    mAirData[9] = airData[9];
                }

                if (airData.length > 12) {
                    mAirData[12] = airData[12];
                }

                if (airData.length > 13) {
                    mAirData[13] = airData[13];
                }

                if (airData.length > 14) {
                    mAirData[14] = airData[14];
                }
                if (CarUtil.isChangeAirCondition()) {// change temp
                    byte temp = mAirData[2];
                    mAirData[2] = mAirData[3];
                    mAirData[3] = temp;
                    //return;
                } else if (CarUtil.getAirCondition() == 3) {// single temp
                    mAirData[3] = (byte) 0xfa;
                }

                if (GlobalDefinition.mRudder) {// change temp
                    byte temp = mAirData[2];
                    mAirData[2] = mAirData[3];
                    mAirData[3] = temp;
                    //return;
                }

                if (CarUtil.isShowAC()) {
                    if (!"com.canboxsetting/com.canboxsetting.CanAirControlActivity".equals(AppConfig.getTopActivity())) {
                        if (!off /*&& !dataEqual*/ && !CarUtil.isHideAirCondition()) {
                            UtilSystem.doRunActivity(mContext, "com.canboxsetting", "com.canboxsetting.CanAirControlActivity");

                            removeMessages(MESSAGE_AIR_TO_ACCONTROL_APK);
                            removeMessages(MESSAGE_AIR_TO_ACCONTROL_APK);
                            sendMessageDelayed(obtainMessage(MESSAGE_AIR_TO_ACCONTROL_APK, msg.obj), 600);

                            sendMessageDelayed(obtainMessage(MESSAGE_AIR_TO_ACCONTROL_APK, msg.obj), 1200);
                        }
                    } else {
                        removeMessages(MESSAGE_AIR_TO_ACCONTROL_APK);
                        sendMessage(obtainMessage(MESSAGE_AIR_TO_ACCONTROL_APK, msg.obj));
                        sendMessageDelayed(obtainMessage(MESSAGE_AIR_TO_ACCONTROL_APK, msg.obj), 500);
                    }
                    return;
                } else {
                    if ("com.canboxsetting/com.canboxsetting.CanAirControlActivity".equals(AppConfig.getTopActivity())) {
                        sendMessage(obtainMessage(MESSAGE_AIR_TO_ACCONTROL_APK, msg.obj));
                        return;
                    }
                }

                ///MMLog.d(TAG, "AirConditionPanel.handleMessage " + msg.toString()+",off="+off);
                if (off) {
                    if (airConditionView.getParent() != null) {
                        mWindowManager.removeView(airConditionView);
                    }
                    mAirData[0] &= (byte) ~0x80;
                    return;
                }

                if (CarUtil.isHideAirCondition() || msg.arg2 == 1) {
                    //MMLog.d(TAG, "AirConditionPanel.handleMessage "+CarUtil.isHideAirCondition()+","+msg.arg2);
                    if (airConditionView.getParent() != null) {
                        mWindowManager.removeView(airConditionView);
                    }
                    return;
                }

                if (!isExtShow()) {
                    //View airConditionView = mToast.getView();
                    setAirConditionTitle(airConditionView);
                    setAirConditionWind(airConditionView);
                    if (mAirData[2] != (byte) 0xfb) {
                        setAirConditionTemperature(airConditionView);
                    }

                    setAirConditionAction(airConditionView);
                    //mToast.setView(airConditionView);
                    //mToast.show();
                    ///if (!dataEqual)
                    {
                        mHandler.removeMessages(0);
                        mHandler.sendEmptyMessageDelayed(0, 3000);
                        if (airConditionView != null && airConditionView.getParent() == null) {
                            mWindowManager.addView(airConditionView, mLayoutParams);
                        }
                    }
                }
                ///else {
                ///AirManager.start(mContext, mAirData);
                ///}

                break;
            case MESSAGE_AIR_TO_ACCONTROL_APK:
                if (mContext != null) {
                    Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
                    i.putExtra("buf", (byte[]) msg.obj);
                    i.putExtra(MyCmd.EXTRA_COMMON_CMD, "ac");
                    mContext.sendBroadcast(i);
                }
                break;
            case MESSAGE_AIRDATA_TO_ACCONTROL_APK:
                if (mContext != null) {
                    Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
                    i.putExtra("buf", mAirData);
                    i.putExtra(MyCmd.EXTRA_COMMON_CMD, "ac");
                    mContext.sendBroadcast(i);
                }
                break;
            // case MESSAGE_AIR_OUTDOOR_TEMP:
            // outDoorTemp = msg.arg1;
            //  	break;
            // case MESSAGE_AIR_HIDE:
            //   	mToast.cancel();
            //   	break;
        }
    }

    void setAirConditionTitle(View view) {
        if ((mAirData[0] & 0x40) != 0) {//ac
            view.findViewById(R.id.ac).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.ac).setVisibility(View.GONE);
        }

        if ((mAirData[4] & 0x04) != 0) {//ac max
            view.findViewById(R.id.ac_max).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.ac_max).setVisibility(View.GONE);
        }

        if ((mAirData[0] & 0x04) != 0) {//dual
            view.findViewById(R.id.dual).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.dual).setVisibility(View.GONE);
        }

        if ((mAirData[0] & 0x02) != 0) {//max fornt
            view.findViewById(R.id.max).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.max).setVisibility(View.GONE);
        }

        if ((mAirData[0] & 0x01) != 0) {//rear
            view.findViewById(R.id.rear).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.rear).setVisibility(View.GONE);
        }

        if ((mAirData[4] & 0x08) != 0) {//rear lock
            view.findViewById(R.id.rear_lock).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.rear_lock).setVisibility(View.GONE);
        }

        if ((mAirData[4] & 0x80) != 0) {
            ((ImageView) view.findViewById(R.id.inner_loop)).getDrawable().setLevel(2);
        } else {
            if ((mAirData[0] & 0x20) != 0) {//inner loop,loop a
                ((ImageView) view.findViewById(R.id.inner_loop)).getDrawable().setLevel(1);
            } else {
                ((ImageView) view.findViewById(R.id.inner_loop)).getDrawable().setLevel(0);
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

        if ((mAirData[5] & 0x10) != 0) {
            view.findViewById(R.id.air_fwindow_heat).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.air_fwindow_heat).setVisibility(View.GONE);
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
            ((ImageView) view.findViewById(R.id.ac_auto)).getDrawable().setLevel(2);
        } else if ((mAirData[0] & 0x10) != 0) {// ac auto
            view.findViewById(R.id.ac_auto).setVisibility(View.VISIBLE);
            ((ImageView) view.findViewById(R.id.ac_auto)).getDrawable().setLevel(1);
        } else if ((mAirData[0] & 0x08) != 0) {
            view.findViewById(R.id.ac_auto).setVisibility(View.VISIBLE);
            ((ImageView) view.findViewById(R.id.ac_auto)).getDrawable().setLevel(0);
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

    void setAirConditionWind(View view) {
        if ((mAirData[1] & 0x80) != 0) {
            view.findViewById(R.id.wind_up1).setVisibility(View.VISIBLE);
            //    		view.findViewById(R.id.wind_up2).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.wind_up1).setVisibility(View.INVISIBLE);
            //    		view.findViewById(R.id.wind_up2).setVisibility(View.INVISIBLE);
        }

        if ((mAirData[1] & 0x40) != 0) {
            view.findViewById(R.id.wind_horizontal1).setVisibility(View.VISIBLE);
            //			view.findViewById(R.id.wind_horizontal2).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.wind_horizontal1).setVisibility(View.INVISIBLE);
            //			view.findViewById(R.id.wind_horizontal2).setVisibility(View.INVISIBLE);
        }

        if ((mAirData[1] & 0x20) != 0) {
            view.findViewById(R.id.wind_down1).setVisibility(View.VISIBLE);
            //			view.findViewById(R.id.wind_down2).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.wind_down1).setVisibility(View.INVISIBLE);
            //			view.findViewById(R.id.wind_down2).setVisibility(View.INVISIBLE);
        }

        if ((mAirData[6] & 0x80) != 0) {
            //    		view.findViewById(R.id.wind_up1).setVisibility(View.VISIBLE);
            view.findViewById(R.id.wind_up2).setVisibility(View.VISIBLE);
        } else {
            //    		view.findViewById(R.id.wind_up1).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.wind_up2).setVisibility(View.INVISIBLE);
        }

        if ((mAirData[6] & 0x40) != 0) {
            //			view.findViewById(R.id.wind_horizontal1).setVisibility(View.VISIBLE);
            view.findViewById(R.id.wind_horizontal2).setVisibility(View.VISIBLE);
        } else {
            //			view.findViewById(R.id.wind_horizontal1).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.wind_horizontal2).setVisibility(View.INVISIBLE);
        }

        if ((mAirData[6] & 0x20) != 0) {
            //			view.findViewById(R.id.wind_down1).setVisibility(View.VISIBLE);
            view.findViewById(R.id.wind_down2).setVisibility(View.VISIBLE);
        } else {
            //			view.findViewById(R.id.wind_down1).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.wind_down2).setVisibility(View.INVISIBLE);
        }

        if ((mAirData[9] & 0x01) != 0) {
            view.findViewById(R.id.wind_down2).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.wind_down1).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.wind_horizontal2).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.wind_horizontal1).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.wind_up2).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.wind_up1).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.wind_auto2).setVisibility(View.VISIBLE);
            view.findViewById(R.id.wind_auto1).setVisibility(View.VISIBLE);

        } else {
            view.findViewById(R.id.wind_auto2).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.wind_auto1).setVisibility(View.INVISIBLE);
        }

        if ((mAirData[12] & 0x0f) != 0) {
            view.findViewById(R.id.wheel).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.wheel).setVisibility(View.INVISIBLE);
        }

        for (int i = 0; i < 0xf; i++) {
            if (i < (mAirData[1] & 0x0F)) {
                ((ImageView) view.findViewById(R.id.wind_rate_1 + i)).getDrawable().setLevel(1);
                if ((mAirData[1] & 0x0F) >= 7 && (mAirData[1] & 0x0F) <= 0xf) {
                    view.findViewById(R.id.wind_rate_1 + i).setVisibility(View.VISIBLE);
                }
            } else {
                ((ImageView) view.findViewById(R.id.wind_rate_1 + i)).getDrawable().setLevel(0);
            }
        }
        //		if ((mAirData[1]&0x0F) >= 7 && (mAirData[1]&0x0F) <= 0xf) {
        //			((ImageView)view.findViewById(R.id.wind_rate_1 + (mAirData[1]&0x0F) -1)).setVisibility(View.VISIBLE);
        //		}
    }

    @SuppressLint("DefaultLocale")
    String getOurDoorTemperature(View view, double temperature) {
        String temp = null;

        boolean isCentigradeUnit = (mAirData[5] & 0x1) == 0;
        if (isCentigradeUnit) {
            temp = String.format("%.1f%s", temperature, view.getResources().getString(R.string.temp_unic_centigrade));
        } else {
            return String.format("%d%s", (int) temperature, view.getResources().getString(R.string.temp_unic_fahrenheit));

        }

        return temp;
    }

    String getAirTemperature(View view, float temperature) {
        if ((mAirData[7] & 0x40) != 0) {
            if (temperature != 0) {
                return String.valueOf((int) temperature);
            } else {
                return "";
            }
        }


        boolean isCentigradeUnit = (mAirData[5] & 0x1) == 0;

        if (CarUtil.mTempUnit == 1) {
            if (!isCentigradeUnit) {
                temperature = ((temperature) - 32) / 1.8f;
                isCentigradeUnit = !isCentigradeUnit;
            }
        } else if (CarUtil.mTempUnit == 2) {
            if (isCentigradeUnit) {
                temperature = ((temperature / 2) * 1.8f + 32);
                isCentigradeUnit = !isCentigradeUnit;
            }
        }


        if (isCentigradeUnit) {
            return String.format(Locale.ENGLISH, "%.1f%s", temperature / 2, view.getResources().getString(R.string.temp_unic_centigrade));
        } else {
            return String.format(Locale.ENGLISH, "%d%s", (int) temperature, view.getResources().getString(R.string.temp_unic_fahrenheit));
        }

    }

    @SuppressLint("SetTextI18n")
    void setAirConditionTemperature(View view) {
        int leftTemp = mAirData[2] & 0xff;
        int rightTemp = mAirData[3] & 0xff;
        MMLog.d(TAG, "leftTemp=" + leftTemp + ",rightTemp=" + rightTemp);
        if ((mAirData[5] & 0x2) == 0) {
            if ((CarUtil.getAirCondition() == 4) || CarUtil.getAirCondition() == 5) {
                switch (leftTemp) {
                    case 0x00:
                        ((TextView) view.findViewById(R.id.left_temp)).setText(R.string.LO);
                        break;
                    case 0x20:
                        ((TextView) view.findViewById(R.id.left_temp)).setText("14°C");
                        break;
                    case 0x22:
                        ((TextView) view.findViewById(R.id.left_temp)).setText("15°C");
                        break;
                    case 0x24:
                        ((TextView) view.findViewById(R.id.left_temp)).setText("16°C");
                        break;
                    case 0x26:
                        ((TextView) view.findViewById(R.id.left_temp)).setText("17°C");
                        break;
                    case 0x28:
                        ((TextView) view.findViewById(R.id.left_temp)).setText("18°C");
                        break;
                    case 0x29:
                        ((TextView) view.findViewById(R.id.left_temp)).setText("18.5°C");
                        break;
                    case 0x2B:
                        ((TextView) view.findViewById(R.id.left_temp)).setText("19.5°C");
                        break;
                    case 0x2D:
                        ((TextView) view.findViewById(R.id.left_temp)).setText("20.5°C");
                        break;
                    case 0x2F:
                        ((TextView) view.findViewById(R.id.left_temp)).setText("21.5°C");
                        break;
                    case 0x31:
                        ((TextView) view.findViewById(R.id.left_temp)).setText("22.5°C");
                        break;
                    case 0x33:
                        ((TextView) view.findViewById(R.id.left_temp)).setText("23.5°C");
                        break;
                    case 0x34:
                        ((TextView) view.findViewById(R.id.left_temp)).setText("24°C");
                        break;
                    case 0x36:
                        ((TextView) view.findViewById(R.id.left_temp)).setText("25°C");
                        break;
                    case 0x38:
                        ((TextView) view.findViewById(R.id.left_temp)).setText("26°C");
                        break;
                    case 0x3a:
                        ((TextView) view.findViewById(R.id.left_temp)).setText("27°C");
                        break;
                    case 0x3c:
                        ((TextView) view.findViewById(R.id.left_temp)).setText("28°C");
                        break;
                    case 0xFF:
                        ((TextView) view.findViewById(R.id.left_temp)).setText(R.string.HI);
                        break;
                }
            } else if (leftTemp == 0x00) {
                ((TextView) view.findViewById(R.id.left_temp)).setText(R.string.LO);
            } else if (leftTemp == 0xff) {
                ((TextView) view.findViewById(R.id.left_temp)).setText(R.string.HI);
            } else if (leftTemp == 0xfa) {
                ((TextView) view.findViewById(R.id.left_temp)).setText("");
            } else if (leftTemp >= 0xf0 && leftTemp <= 0xf8) {
                ((TextView) view.findViewById(R.id.left_temp)).setText(String.valueOf(leftTemp - 0xf0));
            } else {
                ((TextView) view.findViewById(R.id.left_temp)).setText(getAirTemperature(view, leftTemp));
            }
            view.findViewById(R.id.left_temp).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.left_temp).setVisibility(View.INVISIBLE);
        }
        if ((mAirData[5] & 0x4) == 0) {
            if ((CarUtil.getAirCondition() == 4) || CarUtil.getAirCondition() == 5) {
                switch (rightTemp) {
                    case 0x00:
                        ((TextView) view.findViewById(R.id.right_temp)).setText(R.string.LO);
                        break;
                    case 0x20:
                        ((TextView) view.findViewById(R.id.right_temp)).setText("14°C");
                        break;
                    case 0x22:
                        ((TextView) view.findViewById(R.id.right_temp)).setText("15°C");
                        break;
                    case 0x24:
                        ((TextView) view.findViewById(R.id.right_temp)).setText("16°C");
                        break;
                    case 0x26:
                        ((TextView) view.findViewById(R.id.right_temp)).setText("17°C");
                        break;
                    case 0x28:
                        ((TextView) view.findViewById(R.id.right_temp)).setText("18°C");
                        break;

                    case 0x29:
                        ((TextView) view.findViewById(R.id.right_temp)).setText("18.5°C");
                        break;
                    case 0x2B:
                        ((TextView) view.findViewById(R.id.right_temp)).setText("19.5°C");
                        break;
                    case 0x2D:
                        ((TextView) view.findViewById(R.id.right_temp)).setText("20.5°C");
                        break;
                    case 0x2F:
                        ((TextView) view.findViewById(R.id.right_temp)).setText("21.5°C");
                        break;
                    case 0x31:
                        ((TextView) view.findViewById(R.id.right_temp)).setText("22.5°C");
                        break;
                    case 0x33:
                        ((TextView) view.findViewById(R.id.right_temp)).setText("23.5°C");
                        break;

                    case 0x34:
                        ((TextView) view.findViewById(R.id.right_temp)).setText("24°C");
                        break;
                    case 0x36:
                        ((TextView) view.findViewById(R.id.right_temp)).setText("25°C");
                        break;
                    case 0x38:
                        ((TextView) view.findViewById(R.id.right_temp)).setText("26°C");
                        break;
                    case 0x3a:
                        ((TextView) view.findViewById(R.id.right_temp)).setText("27°C");
                        break;
                    case 0x3c:
                        ((TextView) view.findViewById(R.id.right_temp)).setText("28°C");
                        break;
                    case 0xFF:
                        ((TextView) view.findViewById(R.id.right_temp)).setText(R.string.HI);
                        break;
                }
            } else if (rightTemp == 0x00) {
                ((TextView) view.findViewById(R.id.right_temp)).setText(R.string.LO);
            } else if (rightTemp == 0xff) {
                ((TextView) view.findViewById(R.id.right_temp)).setText(R.string.HI);
            } else if (rightTemp == 0xfa) {
                ((TextView) view.findViewById(R.id.right_temp)).setText("");
            } else if (rightTemp >= 0xf0 && rightTemp <= 0xf8) {
                ((TextView) view.findViewById(R.id.right_temp)).setText(String.valueOf(rightTemp - 0xf0));
            } else {
                ((TextView) view.findViewById(R.id.right_temp)).setText(getAirTemperature(view, rightTemp));
            }
            view.findViewById(R.id.right_temp).setVisibility(View.VISIBLE);
        } else {

            view.findViewById(R.id.right_temp).setVisibility(View.INVISIBLE);
        }

        if ((mAirData[8] & 0x40) != 0) {
            ((TextView) view.findViewById(R.id.left_temp)).setText(String.valueOf(leftTemp));
            ((TextView) view.findViewById(R.id.right_temp)).setText(String.valueOf(rightTemp));
        }

        if (outDoorTemp != 0xff) {
            if ((outDoorTemp & 0x80) != 0) {
                outDoorTemp = -(((~outDoorTemp) & 0xff) + 1);
            }

            view.findViewById(R.id.outdoor_temp).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.outdoor_temp)).setText(view.getResources().getString(R.string.outdoor_temp) + getOurDoorTemperature(view, outDoorTemp));
        } else {
            view.findViewById(R.id.outdoor_temp).setVisibility(View.GONE);
        }

        int seatHeat = (mAirData[4] & 0x33);
        seatHeat |= (mAirData[9] & 0x44);
        int seatCold = (mAirData[8] & 0x3f);
        if ((seatHeat != mSeatHeat) || (seatCold != mSeatCold)) {
            mSeatHeat = (mAirData[4] & 0x33);
            view.findViewById(R.id.air_action_seat).setVisibility(View.VISIBLE);
            if ((seatHeat & 0x44) != 0) {
                view.findViewById(R.id.seat_heat_right_4).setVisibility(View.VISIBLE);
                view.findViewById(R.id.seat_heat_left_4).setVisibility(View.VISIBLE);
            }

            for (int i = 0; i < 4; i++) {
                if (i < ((seatHeat >> 4) & 0x07)) {
                    ((ImageView) view.findViewById(getLeftHeat(1 + i))).getDrawable().setLevel(1);
                } else {
                    ((ImageView) view.findViewById(getLeftHeat(1 + i))).getDrawable().setLevel(0);
                }

                if (i < (seatHeat & 0x07)) {
                    ((ImageView) view.findViewById(getRightHeat(1 + i))).getDrawable().setLevel(1);
                } else {
                    ((ImageView) view.findViewById(getRightHeat(1 + i))).getDrawable().setLevel(0);
                }
            }
            if ((mAirData[8] & 0x3c) != mSeatCold) {
                mSeatCold = (mAirData[8] & 0x3f);
                mSeatColdExit = true;

                int left = ((mAirData[8] & 0x30) >> 4) | ((mAirData[8] & 0x02) << 1);
                int right = ((mAirData[8] & 0xc) >> 2) | ((mAirData[8] & 0x01) << 2);

                if (left >= 4 || right >= 4) {
                    view.findViewById(R.id.seat_cold_right_4).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.seat_cold_left_4).setVisibility(View.VISIBLE);
                }

                view.findViewById(R.id.seat_cold_layout).setVisibility(View.VISIBLE);
                for (int i = 0; i < 4; i++) {
                    if (i < (left)) {
                        ((ImageView) view.findViewById(getLeftCold(1 + i))).getDrawable().setLevel(1);
                    } else {
                        ((ImageView) view.findViewById(getLeftCold(1 + i))).getDrawable().setLevel(0);
                    }

                    if (i < (right)) {
                        ((ImageView) view.findViewById(getRightCold(1 + i))).getDrawable().setLevel(1);
                    } else {
                        ((ImageView) view.findViewById(getRightCold(1 + i))).getDrawable().setLevel(0);
                    }
                }
            } else {
                if (!mSeatColdExit) {
                    view.findViewById(R.id.seat_cold_layout).setVisibility(View.GONE);
                }
            }
        } else {
            view.findViewById(R.id.air_action_seat).setVisibility(View.GONE);
        }
    }

    private int getLeftHeat(int i) {
        switch (i) {
            case 1:
                return R.id.seat_heat_left_1;
            case 2:
                return R.id.seat_heat_left_2;
            case 3:
                return R.id.seat_heat_left_3;
            case 4:
                return R.id.seat_heat_left_4;
        }
        return 0;
    }

    private int getRightHeat(int i) {
        switch (i) {
            case 1:
                return R.id.seat_heat_right_1;
            case 2:
                return R.id.seat_heat_right_2;
            case 3:
                return R.id.seat_heat_right_3;
            case 4:
                return R.id.seat_heat_right_4;
        }
        return 0;
    }

    private int getLeftCold(int i) {
        switch (i) {
            case 1:
                return R.id.seat_cold_left_1;
            case 2:
                return R.id.seat_cold_left_2;
            case 3:
                return R.id.seat_cold_left_3;
            case 4:
                return R.id.seat_cold_left_4;
        }
        return 0;
    }

    private int getRightCold(int i) {
        switch (i) {
            case 1:
                return R.id.seat_cold_right_1;
            case 2:
                return R.id.seat_cold_right_2;
            case 3:
                return R.id.seat_cold_right_3;
            case 4:
                return R.id.seat_cold_right_4;
        }
        return 0;
    }

    void setAirConditionAction(View view) {
        //    	((ImageView)view.findViewById(R.id.air_action_user)).setVisibility(View.VISIBLE);
        //		switch(mAction){
        //			case Canbox.ACTION_AC:
        //				view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
        //				view.findViewById(R.id.air_action).setVisibility(View.VISIBLE);
        //				if((mState1&MASK_AC) != 0){
        //					((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
        //				}else{
        //					((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_off);
        //				}
        //				((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_ac);
        //				break;
        //
        //			case Canbox.ACTION_AC_MAX:
        //				view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
        //				view.findViewById(R.id.air_action).setVisibility(View.VISIBLE);
        //				if((mState1&MASK_AC_MAX) != 0){
        //					((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
        //				}else{
        //					((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_off);
        //				}
        //				((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_ac_max);
        //				break;
        //
        //			case Canbox.ACTION_DUAL:
        //				view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
        //				view.findViewById(R.id.air_action).setVisibility(View.VISIBLE);
        //				if((mState1&MASK_DUAL) != 0){
        //					((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
        //				}else{
        //					((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_off);
        //				}
        //				((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_dual);
        //				break;
        //
        //			case Canbox.ACTION_MAX_FRONT:
        //				view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
        //				view.findViewById(R.id.air_action).setVisibility(View.VISIBLE);
        //				if((mState1&MASK_MAX) != 0){
        //					((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
        //				}else{
        //					((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_off);
        //				}
        //				((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_max);
        //				break;
        //
        //			case Canbox.ACTION_REAR:
        //				view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
        //				view.findViewById(R.id.air_action).setVisibility(View.VISIBLE);
        //				if((mState1&MASK_REAR) != 0){
        //					((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
        //				}else{
        //					((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_off);
        //				}
        //				((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_rear);
        //				break;
        //
        //			case Canbox.ACTION_REAR_LOCK:
        //				view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
        //				view.findViewById(R.id.air_action).setVisibility(View.VISIBLE);
        //				if((mState1&MASK_REAR_LOCK) != 0){
        //					((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
        //				}else{
        //					((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_off);
        //				}
        //				((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_rear_lock);
        //				break;
        //
        //			case Canbox.ACTION_LOOP:
        //				view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
        //				view.findViewById(R.id.air_action).setVisibility(View.VISIBLE);
        //				if((mState2&MASK_INNER_LOOP) == 0xc0){//inner loop,loop a
        //					((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_inner_loop_a);
        //					((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
        //				}else if((mState2&MASK_INNER_LOOP) == 0x40){
        //					((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_inner_loop);
        //					((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
        //				}else{
        //					((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_inner_loop_a);
        //					((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_off);
        //				}
        //				break;
        //
        //			case Canbox.ACTION_WIND_DIRECTION:
        //				view.findViewById(R.id.air_action).setVisibility(View.VISIBLE);
        //				view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
        //				if((mWindMode&MASK_AC_AUTO) == 0x10){//inner loop,loop a
        //					((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_auto_large);
        //					((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
        //				}else if((mWindMode&MASK_AC_AUTO) == 0x08){
        //					((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_auto_small);
        //					((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_on);
        //				}else{
        //					((ImageView)view.findViewById(R.id.air_action_user)).setImageResource(R.drawable.air_auto_large);
        //					((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_off);
        //				}
        //				break;
        //
        //			case Canbox.ACTION_LEFT_SEAT_HEAT:
        //			case Canbox.ACTION_RIGHT_SEAT_HEAT:{
        //					view.findViewById(R.id.air_action).setVisibility(View.INVISIBLE);
        //					view.findViewById(R.id.air_action_seat).setVisibility(View.VISIBLE);
        //					int seat = mCanBox.sendCommand(Canbox.CANBOX_READ_AIR_SEAT);
        //					for(int i = 0;i < 3;i++){
        //						if(i < ((seat>>4)&0x03)){
        //							((ImageView)view.findViewById(R.id.seat_heat_left_1 + i)).getDrawable().setLevel(1);
        //						}else{
        //							((ImageView)view.findViewById(R.id.seat_heat_left_1 + i)).getDrawable().setLevel(0);
        //						}
        //
        //						if(i < (seat&0x03)){
        //							((ImageView)view.findViewById(R.id.seat_heat_right_1 + i)).getDrawable().setLevel(1);
        //						}else{
        //							((ImageView)view.findViewById(R.id.seat_heat_right_1 + i)).getDrawable().setLevel(0);
        //						}
        //					}
        //				}
        //				break;
        //			case Canbox.ACTION_AC_OFF:
        //				view.findViewById(R.id.air_action).setVisibility(View.VISIBLE);
        //				view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
        //				((ImageView)view.findViewById(R.id.air_action_user)).setVisibility(View.INVISIBLE);
        //				((ImageView)view.findViewById(R.id.air_action_on)).setImageResource(R.drawable.air_off);
        //				break;
        //
        //			default:
        //				view.findViewById(R.id.air_action).setVisibility(View.INVISIBLE);
        //				view.findViewById(R.id.air_action_seat).setVisibility(View.INVISIBLE);
        //				break;
        //		}
    }
}