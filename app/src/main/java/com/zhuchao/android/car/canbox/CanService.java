package com.zhuchao.android.car.canbox;

import java.util.Date;
import java.util.Locale;

import com.zhuchao.android.car.R;
import com.common.util.AppConfig;
import com.common.util.MyCmd;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.cartype.other.TestKLD;
import com.zhuchao.android.car.manager.AutoIlluminManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class CanService {
    private static final String TAG = "CanService";
    @SuppressLint("StaticFieldLeak")
    public static Canbox mCanbox;//for canbox mcu update set to null
    @SuppressLint("StaticFieldLeak")
    private static CanService mThis;

    private AirConditionPanel mAirConditionPanel;
    private DoorStatusPanel mDoorStatusPanel;

    public CarUtil mCarUtil;

    public static CanService getInstance(Context context) {
        if (mThis == null) {
            mThis = new CanService(context);

            mThis.onCreate();
        }
        return mThis;
    }

    Context mContext;

    public CanService(Context context) {

        mContext = context;

    }

    public void doCmd(int cmd, Intent intent) {
        byte[] buf = intent.getByteArrayExtra(MyCmd.EXTRA_COMMON_DATA);

        canboxDataParser(buf, buf.length);

    }

    private static final int PARSER_CANBOX_DATA = 0x01;
    private static final int CANBOX_UPDATE_TIME = 0x02;
    private final static int MSG_GPS_COMPASS = 0x03;

    private int mUpdateCaboxTime = 0;

    public void canboxDataParser(byte[] data, int len) {
        mHandler.sendMessage(mThis.mHandler.obtainMessage(PARSER_CANBOX_DATA, len, 0, data));

    }

    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Canbox.CANBOX_RETURN_AIR:
                    if (mAirConditionPanel != null) {
                        mAirConditionPanel.postChanged(AirConditionPanel.MESSAGE_AIR_CONDITION, msg.arg1, msg.arg2, msg.obj);
                    }
                    break;
                case Canbox.CANBOX_HIDE_AIR:
                    if (mAirConditionPanel != null) {
                        mAirConditionPanel.postChanged(AirConditionPanel.MESSAGE_AIR_CONDITION, msg.arg1, 1, msg.obj);
                    }
                    break;

                case Canbox.CMD_GROUP_AC:
                    if (mAirConditionPanel != null) {
                        mAirConditionPanel.postChanged(AirConditionPanel.MESSAGE_AIRDATA_TO_ACCONTROL_APK, 0, 0, null);
                    }
                    break;
                // break;
                // case Canbox.CANBOX_OUT_DOOR_TEMP:
                // if (mAirConditionPanel != null) {
                // mAirConditionPanel.postChanged(
                // AirConditionPanel.MESSAGE_AIR_OUTDOOR_TEMP,
                // msg.arg1, msg.arg2);
                // }
                // break;
                case Canbox.CANBOX_DOOR_STATUS:
                    if (mDoorStatusPanel != null) {
                        mDoorStatusPanel.postChanged(DoorStatusPanel.MESSAGE_DOOR_CONDITION, msg.arg1);
                    }
                    break;
                case PARSER_CANBOX_DATA: {
                    if (mCarUtil != null) {
                        mCarUtil.canboxParser((byte[]) msg.obj, msg.arg1);
                    }
                }
                break;
                case CANBOX_UPDATE_TIME:
                    updateCanboxTime();
                    break;
                case MSG_GPS_COMPASS:
                    initGpsCompass();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    // ReverseUI mReverseUI;
    //
    // void initReverseUI() {
    // mReverseUI = new ReverseUI();
    // mReverseUI.onCreate();
    // }

    public void onCreate() {

        mThis = this;

        if (mAirConditionPanel == null) {
            mAirConditionPanel = new AirConditionPanel(mContext);
        }
        if (mDoorStatusPanel == null) {
            mDoorStatusPanel = new DoorStatusPanel(mContext);
        }
        Canbox.addHandler("CanService", mHandler);

        updateCanbox();
        mHandlerMediaInfoToCanbox.sendEmptyMessageDelayed(0, 1000);
    }

    public static void updateCanboxEx() {
        if (mThis != null) {
            mThis.updateCanbox();
        }
    }

    public static void startConnectEx() {
        if (mThis != null) {
            mThis.startConnect();
        }
    }

    public static void updateCanboxSettings() {
        if (mThis != null) {
            mCanbox.updateCanboxSettings();
        }
    }

    public void updateCanbox() {

        mCarUtil = CarUtil.getCarUtilInstance();
        mCanbox = CarUtil.getCanboxInstance();

        unregisterListener();
        if (mCanbox != null) {
            mAppSource = -1;
            mCanbox.setContext(mContext);
            // mCanbox.startConnect();
            registerListener();
        } else {
            if (GlobalDefinition.mIs8600 || GlobalDefinition.mMediaInfoToastBackground != 0) {
                registerListener();
            } else {
                unregisterListener();
            }
        }

        initCanboxTime();
    }

    private void initCanboxTime() {
        mHandler.removeMessages(CANBOX_UPDATE_TIME);
        mUpdateCaboxTime = CarUtil.isNeedSendTime();
        if (mUpdateCaboxTime != 0) {

            updateCanboxTime();
            if (mUpdateCaboxTime == 60000) { //1 min
                Date curDate = new Date(System.currentTimeMillis());
                int second = (byte) curDate.getSeconds();
                second = ((60 - second) % 60);
                if (second == 0) {
                    second = 60;
                }
                mHandler.removeMessages(CANBOX_UPDATE_TIME);
                mHandler.sendEmptyMessageDelayed(CANBOX_UPDATE_TIME, second * 1000);
            }
            registerReceiver();
        } else {
            unregisterReceiver();
        }
    }

    private void startConnect() {
        if (mCanbox != null) {
            mCanbox.startConnect();
            initCanboxTime();
        }
    }

    private int mMediaPlayTime = 0;
    private final Handler mHandlerMediaInfoToCanbox = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //				if (msg.obj != null) {
                    mCanbox.setSongAritst((String) msg.obj);
                    //				}
                    break;
                case 2:
                    //				if (msg.obj != null) {
                    mCanbox.setSongAlbum((String) msg.obj);
                    //				}
                    break;
            }
        }
    };

    private BroadcastReceiver mReceiver;

    private void unregisterListener() {
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    private void registerListener() {
        if (mReceiver == null) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(MyCmd.BROADCAST_ACC_DELAY_POWER_OFF)) {
                        if (mCanbox != null) {
                            // mCanbox.poweroff();
                            mCanbox.stopConnect();
                        }
                        clearToastInfo();
                    }
                    // if (action.equals(MyCmd.BROADCAST_POWER_OFF)) {
                    // if (mCanbox != null) {
                    // mCanbox.poweroff();
                    // }
                    // }
                    else if (action.equals(RADIO_SOURCE_CHANGE)) {
                        if (GlobalDefinition.mIsTesting) return;

                        if (CarUtil.getCanboxType() != null) {
                            int value1 = intent.getIntExtra("value1", 0);// freq
                            int value2 = intent.getIntExtra("value2", 0);// baud
                            int value3 = intent.getIntExtra("value3", 0);// channel
                            byte[] b = new byte[6];

                            if (value2 >= 3) {
                                value2 = 0x10 + (value2 - 3);
                            } else {
                                // value1 = value1*10;
                            }
                            b[0] = (byte) value2;
                            b[1] = (byte) (value1 & 0xff);
                            b[2] = (byte) ((value1 >> 8) & 0xff);
                            b[3] = (byte) (value3 & 0xff);

                            CarUtil.getCanboxInstance().setMediaSrc(MyCmd.SOURCE_RADIO, (byte) 1, b);
                        }

                        if (GlobalDefinition.mIs8600) {
                            int freqency = intent.getIntExtra("value1", 0);// freq
                            int band = intent.getIntExtra("value2", 0);// baud

                            String s = null;
                            int fm = 0;
                            if (band < 3) { // fm
                                s = "FM ";
                                if ((freqency / 10000) <= 0) {
                                    s += " ";
                                }
                                String sf = String.valueOf(freqency / 100);
                                if ((freqency % 100) < 10) {
                                    sf += "0";
                                }
                                sf += (freqency % 100);
                                s += sf;

                                fm = 0x100;
                            } else {
                                s = "AM  ";
                                if ((freqency / 1000) <= 0) {
                                    s += " ";
                                    s += freqency;
                                } else {
                                    s += freqency;
                                }
                            }

                            if (s != null) {
                                GlobalDefinition.updateLcd(fm | MyCmd.SOURCE_RADIO, s);
                            }
                        }

                        if (GlobalDefinition.mMediaInfoToastBackground != 0) {
                            int freqency = intent.getIntExtra("value1", 0);// freq
                            int band = intent.getIntExtra("value2", 0);// baud
                            showToastMediaBackgroundRadio(freqency, band);
                        }

                    } else if (action.equals(AUDIO_SOURCE_CHANGE)) {
                        if (CarUtil.getCanboxType() != null) {
                            int value1 = intent.getIntExtra("value1", 0);
                            int value2 = intent.getIntExtra("value2", 0);
                            int value3 = intent.getIntExtra("value3", 0);
                            int value4 = intent.getIntExtra("value4", 0);
                            mMediaPlayTime = value2;
                            if (mAppSource == MyCmd.SOURCE_MUSIC) {
                                CarUtil.getCanboxInstance().setMediaMoreInfo(MyCmd.SOURCE_MUSIC, value1, value3, value2, value4);
                            }
                        }

                        if (GlobalDefinition.mIs8600) {

                            String s;
                            int time = intent.getIntExtra("value2", 0);
                            int value3 = intent.getIntExtra("value3", 0);
                            if (value3 > 0) {
                                int value1 = intent.getIntExtra("value1", 0);
                                ++value1;
                                s = String.format(Locale.ENGLISH, "%03d-%02d%02d", value1, time / 60, time % 60);
                            } else {
                                s = "MP3";
                            }

                            GlobalDefinition.updateLcd(MyCmd.SOURCE_MUSIC, s);

                        }
                    } else if (action.equals(DVD_SOURCE_CHANGE)) {
                        if (CarUtil.getCanboxType() != null) {
                            int value1 = intent.getIntExtra("value1", 0);
                            int value2 = intent.getIntExtra("value2", 0);
                            int value3 = intent.getIntExtra("value3", 0);
                            mMediaPlayTime = value2;
                            if (mAppSource == MyCmd.SOURCE_DVD) {
                                CarUtil.getCanboxInstance().setMediaMoreInfo(MyCmd.SOURCE_DVD, value1, value3, value2, 0);
                            }
                        }

                        if (GlobalDefinition.mIs8600) {
                            int mTrackCurrent = intent.getIntExtra("value1", 0);
                            int mCurTime = intent.getIntExtra("value2", 0);

                            String s = String.format(Locale.ENGLISH, "%03d-%02d%02d", mTrackCurrent, mCurTime / 60, mCurTime % 60);
                            GlobalDefinition.updateLcd(MyCmd.SOURCE_DVD, s);

                        }
                    } else if (action.equals(VIDEO_SOURCE_CHANGE)) {
                        if (CarUtil.getCanboxType() != null) {
                            int value1 = intent.getIntExtra("value1", 0);
                            int value2 = intent.getIntExtra("value2", 0);
                            int value3 = intent.getIntExtra("value3", 0);
                            int value4 = intent.getIntExtra("value4", 0);
                            mMediaPlayTime = value2;
                            if (mAppSource == MyCmd.SOURCE_VIDEO) {
                                CarUtil.getCanboxInstance().setMediaMoreInfo(MyCmd.SOURCE_VIDEO, value1, value3, value2, value4);
                            }
                        }

                        if (GlobalDefinition.mIs8600) {

                            String s;
                            int time = intent.getIntExtra("value2", 0);
                            int value3 = intent.getIntExtra("value3", 0);
                            if (value3 > 0) {
                                int value1 = intent.getIntExtra("value1", 0);
                                ++value1;
                                s = String.format(Locale.ENGLISH, "%03d-%02d%02d", value1, time / 60, time % 60);
                            } else {
                                s = "AV";
                            }

                            GlobalDefinition.updateLcd(MyCmd.SOURCE_VIDEO, s);

                        }

                    } else if (action.equals(MY_OUT_VOLUME_CHANGE)) {
                        if (CarUtil.getCanboxType() != null) {
                            int value1 = intent.getIntExtra("id", 0);
                            CarUtil.getCanboxInstance().setVolume(value1);

                        }
                    } else if (action.equals(BT_PHONE_BROADCAST)) {
                        if (CarUtil.getCanboxType() != null) {
                            int status = intent.getIntExtra("status", 0);
                            String num = intent.getStringExtra("num");
                            String name = intent.getStringExtra("name");
                            // Log.e("", status+":"+num);
                            if (num == null) {
                                num = "  ";
                            }
                            CarUtil.getCanboxInstance().setPhone(status, num);
                            CarUtil.getCanboxInstance().setPhoneEx(status, num, name);

                        }
                    } else if (action.equals(MyCmd.BROADCAST_SEND_TO_CAN) || action.equals(MyCmd.BROADCAST_SEND_TO_CAN_FROM_BT)) {
                        if (mCanbox != null) {

                            byte[] buf = intent.getByteArrayExtra("buf");

                            //							Log.d("can", buf.toString() + "");
                            if (buf != null) {
                                mCanbox.sendDataToCanbox(buf, buf.length);
                            } else {
                                int cmd = intent.getIntExtra(MyCmd.EXTRA_COMMON_CMD, 0);
                                if (cmd != 0) {
                                    int data = intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0);
                                    mCanbox.doCmd(cmd, data);
                                }
                            }

                        }
                    } else if (MyCmd.BROADCAST_CMD_FROM_MUSIC.equals(action)) {
                        if (mCanbox != null || (GlobalDefinition.mMediaInfoToastBackground != 0)) {
                            String name = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA);

                            if (mAppSource != MyCmd.SOURCE_MUSIC) {
                                return;
                            }

                            if (mCanbox != null) {
                                String artist = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA2);
                                String album = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA3);

                                mCanbox.setSongName(name);

                                mHandlerMediaInfoToCanbox.removeMessages(1);
                                mHandlerMediaInfoToCanbox.sendMessageDelayed(mHandlerMediaInfoToCanbox.obtainMessage(1, artist), 40);
                                mHandlerMediaInfoToCanbox.removeMessages(2);
                                mHandlerMediaInfoToCanbox.sendMessageDelayed(mHandlerMediaInfoToCanbox.obtainMessage(2, album), 120);
                            }
                            if (GlobalDefinition.mMediaInfoToastBackground != 0) {
                                showToastMediaBackgroundMusic(name);
                            }
                            // mHandler.postDelayed(new Runnable() {
                            // public void run() {
                            // mCanbox.setSongAritst(artist);
                            // };
                            // }, 50);
                            // mHandler.postDelayed(new Runnable() {
                            // public void run() {
                            // mCanbox.setSongAlbum(album);
                            // };
                            // }, 100);
                        }

                    } else if (action.equals(MyCmd.BROADCAST_CMD_FROM_BT)) {
                        doBTCmd(intent);
                    } else if (action.equals("AUTONAVI_STANDARD_BROADCAST_SEND")) {

                        if (mCanbox != null) {
                            int key = intent.getIntExtra("KEY_TYPE", 0);
                            if (key == 10001) {
                                //								int icon = intent.getIntExtra("ICON", 0);
                                int direction = intent.getIntExtra("CAR_DIRECTION", 0);
                                //								Log.d("ffck", icon + "::" + direction);
                                //								mCanbox.updateCompass(direction);
                            }
                        }
                    }
                }
            };
            IntentFilter iFilter = new IntentFilter();
            // iFilter.addAction(MyCmd.BROADCAST_POWER_OFF);
            iFilter.addAction(MyCmd.BROADCAST_ACC_DELAY_POWER_OFF);
            iFilter.addAction(RADIO_SOURCE_CHANGE);
            iFilter.addAction(DVD_SOURCE_CHANGE);
            iFilter.addAction(AUDIO_SOURCE_CHANGE);
            iFilter.addAction(VIDEO_SOURCE_CHANGE);
            iFilter.addAction(IPOD_SOURCE_CHANGE);
            iFilter.addAction(MY_OUT_VOLUME_CHANGE);
            iFilter.addAction(MyCmd.BROADCAST_SEND_TO_CAN);
            iFilter.addAction(MyCmd.BROADCAST_SEND_TO_CAN_FROM_BT);
            iFilter.addAction(MyCmd.BROADCAST_CMD_FROM_MUSIC);
            iFilter.addAction(MyCmd.BROADCAST_CMD_FROM_BT);

            iFilter.addAction(BT_PHONE_BROADCAST);

            mHandler.removeMessages(MSG_GPS_COMPASS);
            if (mCanbox != null && (mCanbox.isSupportCompass() || (CarUtil.getExternalRadarId() > 0))) {
                // iFilter.addAction("AUTONAVI_STANDARD_BROADCAST_SEND");
                mGpsInitTime = 5;
                mHandler.sendEmptyMessageDelayed(MSG_GPS_COMPASS, 2000);
            }
            mContext.registerReceiver(mReceiver, iFilter);
        }
    }

    private int mGpsInitTime = 5;

    private LocationManager mLocationManager = null;
    private MyLocationListener mLocationListener = null;

    private void initGpsCompass() {

        if (mLocationManager == null) {
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        Log.d(TAG, "initGpsCompass:" + mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));

        mHandler.removeMessages(MSG_GPS_COMPASS);
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (mLocationListener != null) {
                mLocationManager.removeUpdates(mLocationListener);
            }
            mLocationListener = new MyLocationListener();
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        } else {
            mGpsInitTime--;
            if (mGpsInitTime > 0) {
                mHandler.sendEmptyMessageDelayed(MSG_GPS_COMPASS, 2000);
            }
        }

        // mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SAVE_TIME, 1);
        // doUpdateGpsTime();
    }

    public class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            float bearing = location.getBearing();
            double altitude = location.getAltitude();

            //Log.d(TAG, "bearing:"+bearing);
            if (mCanbox != null) {
                mCanbox.updateCompass((int) bearing);
                mCanbox.updateCompass((int) bearing, altitude);
                mCanbox.updateExtRadar((int) location.getSpeed());
            }
        }

        public void onProviderDisabled(String provider) {

        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }


    private void doBTCmd(Intent intent) {
        //		if (GlobalDef.mMediaInfoToastBackground != 0) {
        int cmd = intent.getIntExtra(MyCmd.EXTRA_COMMON_CMD, 0);

        switch (cmd) {
            case MyCmd.Cmd.BT_SEND_A2DP_STATUS: {
                String name = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA2);
                String mac = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA4);
                if (GlobalDefinition.getTestingEx() && mCanbox != null) {
                    if (mCanbox instanceof TestKLD) {
                        TestKLD new_name = (TestKLD) mCanbox;
                        new_name.sendBTName(name, mac);
                    }
                }
            }
            break;
            case MyCmd.Cmd.BT_SEND_ID3_INFO: {
                if (mAppSource != MyCmd.SOURCE_BT_MUSIC) {
                    //					Log.d(TAG , "BT_SEND_ID3_INFO return!");
                    return;
                }
                String name = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA);

                if (mCanbox != null) {
                    String artist = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA2);
                    String album = intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA3);

                    mCanbox.setSongName(name);

                    mHandlerMediaInfoToCanbox.removeMessages(1);
                    mHandlerMediaInfoToCanbox.sendMessageDelayed(mHandlerMediaInfoToCanbox.obtainMessage(1, artist), 40);
                    mHandlerMediaInfoToCanbox.removeMessages(2);
                    mHandlerMediaInfoToCanbox.sendMessageDelayed(mHandlerMediaInfoToCanbox.obtainMessage(2, album), 120);
                }

                if (GlobalDefinition.mMediaInfoToastBackground != 0) {
                    showToastMediaBackgroundBTMusic(name);
                }
            }
            break;
            default:
        }
        //		}
    }

    private int mAppSource = MyCmd.SOURCE_NONE;
    private static final String RADIO_SOURCE_CHANGE = "com.zhuchao.android.car.radio.SOURCE_CHANGE";
    private static final String DVD_SOURCE_CHANGE = "com.zhuchao.android.car.dvd.SOURCE_CHANGE";
    private static final String AUDIO_SOURCE_CHANGE = "com.zhuchao.android.car.audio.SOURCE_CHANGE";
    private static final String VIDEO_SOURCE_CHANGE = "com.zhuchao.android.car.video.SOURCE_CHANGE";
    private static final String IPOD_SOURCE_CHANGE = "com.zhuchao.android.car.ipod.SOURCE_CHANGE";
    private static final String MY_OUT_VOLUME_CHANGE = "com.zhuchao.android.car.out.VOLUME_CHANGE";
    private static final String BT_PHONE_BROADCAST = "com.zhuchao.android.car.bt.BT_PHONE_BROADCAST";

    public void setSource(int id) {
        if (mAppSource != id) {
            if (id != -1) {
                if (mCanbox != null) {
                    mCanbox.setMediaSrc(id);
                }
            }
            mAppSource = id;
        }
    }

    private void updateCanboxTime() {

        mHandler.removeMessages(CANBOX_UPDATE_TIME);
        if (mCanbox != null) {
            if (!CarUtil.mIsUpdating) {
                mCanbox.updateTime();
            }
            if (mUpdateCaboxTime > 0) {
                mHandler.sendEmptyMessageDelayed(CANBOX_UPDATE_TIME, mUpdateCaboxTime);
            }
        }
    }

    private BroadcastReceiver mBroadcastReceiver;

    private void unregisterReceiver() {
        if (mBroadcastReceiver != null) {
            mContext.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    private void registerReceiver() {
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    if (intent.getAction().equals(Intent.ACTION_TIME_CHANGED)) {
                        initCanboxTime();
                        AutoIlluminManager.updateIlluminModeEx();
                    }
                }
            };
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);

        mContext.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private Toast mToastMedia;
    private String mMusicName = null;
    private String mBTMusicName = null;
    private String mRadioName = null;

    private void showToastMediaBackground(String s) {
        if (mContext != null) {
            try {
                if (mToastMedia != null) {
                    mToastMedia.cancel();
                    mToastMedia = null;
                }

                if (s != null && s.length() > 0) {

                    mToastMedia = new Toast(mContext);

                    LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View v = inflate.inflate(R.layout.source_key, null);
                    TextView tv = v.findViewById(R.id.mode_text);

                    mToastMedia.setView(v);
                    mToastMedia.setDuration(Toast.LENGTH_LONG);
                    mToastMedia.setGravity(Gravity.CENTER, 0, 0);
                    tv.setText(s);
                    tv.setTextSize(21);
                    // mToastMedia = Toast.makeText(mContext, s,
                    // Toast.LENGTH_LONG);
                    mToastMedia.show();
                }
            } catch (Exception e) {

            }
        }
    }

    private void showToastMediaBackgroundMusic(String s) {
        if (GlobalDefinition.mMediaInfoToastBackground != 0) {
            if (mAppSource != MyCmd.SOURCE_MUSIC) {
                return;
            }
            if (s != null && !s.equals(mMusicName)) {
                if (mMusicName == null) {
                    mMusicName = s;
                    return;
                }
                mMusicName = s;
                if (!AppConfig.CAR_UI_AUDIO.equals(AppConfig.getTopActivity())) {
                    showToastMediaBackground(s);
                }
            }
        }
    }

    private void showToastMediaBackgroundBTMusic(String s) {
        if (GlobalDefinition.mMediaInfoToastBackground != 0) {
            if (mAppSource != MyCmd.SOURCE_BT_MUSIC) {
                return;
            }
            if (s != null && !s.equals(mBTMusicName)) {
                if (mBTMusicName == null) {
                    mBTMusicName = s;
                    return;
                }

                mBTMusicName = s;
                if (!((AppConfig.CAR_UI_BT_MUSIC.equals(AppConfig.getTopActivity())) || (AppConfig.CAR_BT.equals(AppConfig.getTopActivity()))

                )) {
                    showToastMediaBackground(s);
                }
            }
        }
    }

    private int mFreqency = 0;

    private void showToastMediaBackgroundRadio(int freqency, int band) {

        if (mAppSource != MyCmd.SOURCE_RADIO) {
            return;
        }

        if (mFreqency != freqency) {
            String s = null;
            if (band < 3) { // fm
                s = "FM ";
                s += (freqency / 100) + ".";
                if ((freqency % 100) < 10) {
                    s += "0";
                }
                s += (freqency % 100);
                s += " MHz";
            } else {
                s = "AM  ";
                if ((freqency / 1000) <= 0) {
                    s += " ";
                    s += freqency;
                } else {
                    s += freqency;
                }
                s += " KHz";
            }

            if (!s.equals(mRadioName)) {
                if (mRadioName == null) {
                    mRadioName = s;
                    return;
                }
                mRadioName = s;
                if (!AppConfig.CAR_UI_RADIO.equals(AppConfig.getTopActivity())) {
                    showToastMediaBackground(s);
                }
            }
            mFreqency = freqency;
        }
    }

    private void clearToastInfo() {
        if (GlobalDefinition.mMediaInfoToastBackground != 0) {
            mRadioName = null;
            mMusicName = null;
            mBTMusicName = null;
        }
    }


}
