package com.zhuchao.android.car.ui;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.common.utils.SettingProperties;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.UIBase;

public class BacklightUI extends UIBase {

    public final static int MSG_SHOW = 0;
    public final static int MSG_HIDE = 1;
    public final static int MSG_SAVE_DATA = 10;
    private static final String TAG = "BacklightUI";
    private final static long DELAY_HIDE_TIME = 3000;
    private static Handler mHandler;
    // private final Toast mToast;
    private final SeekBar mLevel;
    private final TextView mLevelBacklight;
    private SeekBar mLevelContrast;
    private TextView mLevelConrast;
    private int mMinimumBacklight;
    private int mMaximumBacklight;
    private int mType;

    public BacklightUI(Context context, View view, int index) {
        super(context, view, index);
        mContext = context;
        init();
        mLevel = mMainView.findViewById(R.id.level);
        mLevelBacklight = mMainView.findViewById(R.id.backlight_text);
        ImageView mLargeStreamIcon = mMainView.findViewById(R.id.ringer_stream_icon);
        mLargeStreamIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doHide();
            }
        });

        mLevel.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                // prepareHide();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                prepareHide();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {

                    // 做过滤操作，不要调节过于灵敏，因为与mcu通信时差会导致快速拖动时，进度跳动

                    prepareHide();

                    int val = progress + mMinimumBacklight;
                    Log.d(TAG, "onProgressChanged:" + val);
                    setBrightness(val);
                    if (mType == -3 || mType == -4) {
                        if (val <= 1) {
                            val = 1;
                        }
                        mLevelBacklight.setText(String.valueOf(val));
                    }
                }
            }
        });
    }

    /**
     * Called when the activity is first created.
     */
    public static BacklightUI getInstanse(Context context, View view, int index) {
        if (index >= MAX_DISPLAY) {
            return null;
        }

        return new BacklightUI(context, view, index);
    }

    public static void setHandler(Handler h) {
        mHandler = h;
    }

    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        doShow();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private void init() {
        if (mType == -3 || mType == -4) {
            mMinimumBacklight = 0;
            mMaximumBacklight = 255;
        } else {
            try {
                PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
                ///mMinimumBacklight = pm.getMinimumScreenBrightnessSetting();
                ///mMaximumBacklight = pm.getMaximumScreenBrightnessSetting();
                /// mPower = IPowerManager.Stub.asInterface(ServiceManager
                /// .getService("power"));
            } catch (Exception e) {
                mMinimumBacklight = 50;
                mMaximumBacklight = 255;
            }
        }

    }

    private void setBrightness(int brightness) {
        BacklightPanel.setBrightness(brightness, mType);
        // if (mType == 0) {
        // try {
        // mPower.setTemporaryScreenBrightnessSettingOverride(brightness);
        // } catch (Exception ex) {
        // }
        //
        // final int val = brightness;
        // AsyncTask.execute(new Runnable() {
        // public void run() {
        // try {
        // Settings.System.putIntForUser(
        // mContext.getContentResolver(),
        // Settings.System.SCREEN_BRIGHTNESS, val,
        // UserHandle.USER_CURRENT);
        //
        // } catch (Exception e) {
        //
        // }
        // Log.d(TAG, "onProgressChanged22:" + val);
        // }
        // });
        // } else {
        //
        // brightness = (((brightness * 20 * 100) / 255) / 100);
        // Util.setFileValue("/sys/class/backlight/ak-backlight/aux_bkl_lvl",
        // brightness);
        // SettingProperties.setIntProperty(mContext,
        // SettingProperties.KEY_SCREEN1_BACKLIGHT, brightness);
        // }
    }

    public void setType(int t) {
        mType = t;
        init();
        if (t == -4) {

            mLevelConrast = mMainView.findViewById(R.id.contrast_text);

            mMainView.findViewById(R.id.contrast_layout).setVisibility(View.VISIBLE);
            mLevelContrast = mMainView.findViewById(R.id.contrast_level);
            ImageView contrastIcon = mMainView.findViewById(R.id.contrast_icon);
            contrastIcon.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    doHide();
                }
            });

            mLevelContrast.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    prepareHide();
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        prepareHide();
                        int val = progress;
                        Log.d(TAG, "setContrast:" + val);
                        if (val <= 1) {
                            val = 1;
                        }
                        BacklightPanel.setContrast(val, mType);
                        mLevelConrast.setText(String.valueOf(val));
                    }
                }
            });
        } else {
            mMainView.findViewById(R.id.contrast_layout).setVisibility(View.GONE);
        }
    }

    private void prepareHide() {
        mHandler.removeMessages(MSG_HIDE);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_HIDE, mDisplayIndex, 0), DELAY_HIDE_TIME);
    }

    private void doShow() {
        // if (mFirstShown) {
        // mFirstShown = false;
        // return;
        // }
        prepareHide();

        mLevel.setMax(mMaximumBacklight - mMinimumBacklight);

        int value = mLevel.getMax();
        try {

            value = Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, mMaximumBacklight);
        } catch (Exception ignored) {
        }

        if (mType == 0) {
            mLevel.setProgress(value - mMinimumBacklight);
            mLevelBacklight.setVisibility(View.GONE);
        } else if (mType == -1) {
            int s = SettingProperties.getIntProperty(mContext, SettingProperties.KEY_SCREEN1_BACKLIGHT);
            if (s != 0) {
                value = s;
                value = value * 255 / 20;
                if (value < mMinimumBacklight) {
                    value = mMinimumBacklight;
                }
            }
            // Log.d("allen", value + ":value");
            mLevel.setProgress(value - mMinimumBacklight);
            mLevelBacklight.setVisibility(View.GONE);
        } else if (mType == -3 || mType == -4) {
            int s = SettingProperties.getIntProperty(mContext, SettingProperties.KEY_REVERSE_BACKLIGHT);
            if (s == 0) {
                s = GlobalDefinition.CVBS_DEFALUT_BRIGHTNESS;
            }
            mLevel.setProgress(s);
            mLevel.setMax(255);
            mLevelBacklight.setText(String.valueOf(s));
            mLevelBacklight.setVisibility(View.VISIBLE);
            if (mType == -4) {
                s = SettingProperties.getIntProperty(mContext, SettingProperties.KEY_REVERSE_CONTRAST);
                if (s == 0) {
                    s = GlobalDefinition.CVBS_DEFALUT_BRIGHTNESS;
                }
                mLevelConrast.setText(String.valueOf(s));
                mLevelContrast.setProgress(s);
                mLevel.setMax(255);
            }
        }
    }

    private void doHide() {
        mHandler.removeMessages(MSG_HIDE);
        mHandler.sendMessage(mHandler.obtainMessage(MSG_HIDE, mDisplayIndex, 0));
    }

}
