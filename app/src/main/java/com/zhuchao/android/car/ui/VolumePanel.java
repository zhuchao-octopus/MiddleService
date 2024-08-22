package com.zhuchao.android.car.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.common.utils.MyCmd;
import com.common.utils.ProtocolAk47;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.manager.McuManager;

public class VolumePanel extends Handler {

    private static final String TAG = "VolumePanel";
    // private final Toast mToast;
    private final View mView;
    private final ImageView mLargeStreamIcon;
    private final SeekBar mLevel;
    private final Context mContext;
    private final TextView mMessage;

    private final McuManager mMcuManager;

    private final static int MSG_SHOW = 0;
    private final static int MSG_HIDE = 1;

    public static boolean mShown = false;
    /**
     * 开机第一次不要显示
     */
    private final boolean mFirstShown = true;

    private static VolumePanel mThis;

    private WindowManager mWindowManager = null;

    private final WindowManager.LayoutParams mVolumeLayoutParams;
    private final static long DELAY_HIDE_TIME = 3000;

    public static int mCurrentVolume = 10;

    public VolumePanel(Context context) {
        mContext = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mView = inflater.inflate(R.layout.volume_layout, null);

        View v = view.findViewById(R.id.volume_back);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                doHide();
            }
        });


        mLargeStreamIcon = view.findViewById(R.id.ringer_stream_icon);
        mLargeStreamIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMcuManager.doKey(MyCmd.Keycode.MUTE);
            }
        });
        mLevel = view.findViewById(R.id.level);

        mLevel.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                // TODO Auto-generated method stub
                if (arg2.getKeyCode() == KeyEvent.KEYCODE_BACK || arg2.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    doHide();
                    return true;
                }
                return false;
            }
        });

        mMessage = view.findViewById(R.id.vol_text);
        mLevel.setMax(30);

        mLevel.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                //				prepareHide();
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

                    mMcuManager.setVolume(progress);

                }
            }
        });
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mVolumeLayoutParams = new WindowManager.LayoutParams();
        mVolumeLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        mVolumeLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // mVolumeLayoutParams.flags |=
        // WindowManager.LayoutParams.FLAG_FULLSCREEN |
        // WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        mVolumeLayoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        mVolumeLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mVolumeLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        mVolumeLayoutParams.format = PixelFormat.RGBA_8888;
        mMcuManager = McuManager.getInstance(null);
        registerListener();
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SHOW:
                show();
                break;
            case MSG_HIDE:
                doHide();
                break;

            default:
                break;
        }
    }

    private void prepareHide() {
        removeMessages(MSG_HIDE);
        sendEmptyMessageDelayed(MSG_HIDE, DELAY_HIDE_TIME);
    }

    private void show() {
        doShow();
        prepareHide();
    }

    private void doShow() {
        //		if (mFirstShown) {
        //			mFirstShown = false;
        //			return;
        //		}

        if (mCurrentVolume == 0) { // mute
            mLargeStreamIcon.setImageResource(R.drawable.volume_off);

        } else {
            mLargeStreamIcon.setImageResource(R.drawable.volume_icon);

        }

        mLevel.setProgress(mCurrentVolume);
        mMessage.setText(String.valueOf(mCurrentVolume));

        if (!mShown) {
            mWindowManager.addView(mView, mVolumeLayoutParams);
            mShown = true;
        }

    }

    private void doHide() {
        if (mShown) {

            removeMessages(MSG_HIDE);
            mWindowManager.removeView(mView);
            mShown = false;
        }

    }

    private void doMcuData(byte[] buf) {
        int ret = 0;
        if (buf != null && buf.length > 2) {
            if (buf[1] == ProtocolAk47.RECEVE_AUDIO_VOLUME_INFO) {
                mCurrentVolume = buf[2];
                boolean show = true;
                if (buf.length > 4) {
                    if (buf[4] == 1) {
                        show = false;
                    }
                }
                if (show) {
                    show();
                }
            }
        }

    }

    private BroadcastReceiver mReceiver = null;

    public void registerListener() {
        if (mReceiver == null) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    //Log.d(TAG, action);
                    if (action.equals(MyCmd.BROADCAST_CAR_SERVICE_SEND)) {

                        int cmd = intent.getIntExtra(MyCmd.EXTRA_COMMON_CMD, 0);

                        if (cmd == MyCmd.Cmd.MCU_AUDIO_RECEIVE_DATA) {
                            byte[] buf = intent.getByteArrayExtra(MyCmd.EXTRA_COMMON_DATA);
                            doMcuData(buf);
                        }

                    } else if (action.equals(MyCmd.BROADCAST_START_VOLUMESETTINGS) || action.equals(MyCmd.BROADCAST_START_VOLUMESETTINGS_COMMON)) {
                        show();

                    }

                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(MyCmd.BROADCAST_CAR_SERVICE_SEND);

            iFilter.addAction(MyCmd.BROADCAST_START_VOLUMESETTINGS);
            iFilter.addAction(MyCmd.BROADCAST_START_VOLUMESETTINGS_COMMON);
            mContext.registerReceiver(mReceiver, iFilter);
        }
    }

    public void unregisterListener() {
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }

    }
}
