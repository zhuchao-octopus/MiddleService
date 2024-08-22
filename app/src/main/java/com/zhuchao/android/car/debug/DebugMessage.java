package com.zhuchao.android.car.debug;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.common.utils.MachineConfig;
import com.common.utils.Util;
import com.common.utils.UtilSystem;
import com.common.utils.UtilSystem.StorageInfo;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.cartype.CarUtil;

import java.util.Date;
import java.util.List;

public class DebugMessage {

    private static WindowManager mWindowManager;
    private static WindowManager.LayoutParams mLayoutParams;
    private static View mView;

    private static Context mContext;

    private static TextView mDebug;

    private static float mTouchX;
    private static float mTouchY;
    private static int mOldX;
    private static int mOldY;
    private static boolean mHaveMove;

    private static void init(Context context) {

        if (mView == null) {
            mContext = context;
            mView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.debug_msg, null);


            mLayoutParams = new WindowManager.LayoutParams(660, 400, 0, 0, LayoutParams.TYPE_SYSTEM_ERROR, LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.RGBA_8888);

            mLayoutParams.gravity = Gravity.TOP | Gravity.START;
            mLayoutParams.x = 100;
            mLayoutParams.x = 50;
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

            mView.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // TODO Auto-generated method stub
                    float x = event.getRawX();
                    float y = event.getRawY();

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mTouchX = x;
                            mTouchY = y;
                            mOldX = mLayoutParams.x;
                            mOldY = mLayoutParams.y;
                            mHaveMove = false;
                            v.setAlpha(0.5f);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            // Log.d("dd", ""+mHaveMove);
                            if (((x - mTouchX) != 0) || ((y - mTouchY) != 0)) {
                                mHaveMove = true;
                            }

                            if (!mHaveMove) {
                                break;
                            }

                            mLayoutParams.x = (int) (mOldX + x - mTouchX);
                            mLayoutParams.y = (int) (mOldY + y - mTouchY);
                            if (mLayoutParams.x < 0) {
                                mLayoutParams.x = 0;
                            }
                            if (mLayoutParams.y < 0) {
                                mLayoutParams.y = 0;
                            }

                            mWindowManager.updateViewLayout(mView, mLayoutParams);

                            break;
                        case MotionEvent.ACTION_UP:
                            v.setAlpha(1f);
                            if (mHaveMove) {
                                return true;
                            }
                            break;
                    }
                    return false;
                }
            });

            mScrollView = mView.findViewById(R.id.debug_scrollview);
            mView.findViewById(R.id.clear_debug).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub

                    if (mDebug != null) {
                        mDebug.setText("");
                    }

                    if (mAdapter != null) {
                        mAdapter.clear();
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });

            mView.findViewById(R.id.rx).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub

                    if ((mRXTX & DEUBG_RXONLY) != 0) {
                        mRXTX &= ~DEUBG_RXONLY;
                        ((TextView) arg0).setTextColor(0xffffffff);
                    } else {
                        mRXTX |= DEUBG_RXONLY;
                        ((TextView) arg0).setTextColor(0xffff0000);
                    }
                }
            });
            mView.findViewById(R.id.tx).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub

                    if ((mRXTX & DEUBG_TXONLY) != 0) {
                        mRXTX &= ~DEUBG_TXONLY;
                        ((TextView) arg0).setTextColor(0xffffffff);
                    } else {
                        mRXTX |= DEUBG_TXONLY;
                        ((TextView) arg0).setTextColor(0xffff0000);
                    }
                }
            });

            mView.findViewById(R.id.stop_debug).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub

                    if (mDebug != null) {
                        mDebug = null;
                        ((TextView) arg0).setTextColor(0xffff0000);
                    } else {
                        mDebug = mView.findViewById(R.id.msg_debug);
                        ((TextView) arg0).setTextColor(0xffffffff);
                    }
                }
            });

            mView.findViewById(R.id.export).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub

                    logcat();

                }
            });

            mView.findViewById(R.id.switch_debug).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub

                    mMsgType = (mMsgType + 1) % DEUBG_MAX;
                    // String s = "SWITCH";
                    String s = "";
                    switch (mMsgType) {
                        case 0:
                            s += "MCU";
                            break;
                        case 1:
                            s += "CAN";
                            break;
                    }

                    ((TextView) arg0).setText(s);
                }
            });

            mView.findViewById(R.id.quit_debug).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub

                    stop();
                }
            });
        }

        // set version
        String version = "";

        if (CarUtil.getCanboxVersion() != null) {
            version += "CAN:" + CarUtil.getCanboxVersion();
        }
        if (CarUtil.getProIndex() != -1) {
            if (version.length() > 1) {
                version += "     ";
            }
            version += "Pro:" + CarUtil.getProIndex();
        }

        String mCanboxType = MachineConfig.getPropertyOnce(MachineConfig.KEY_CAN_BOX);
        if (mCanboxType != null) {
            String[] ss = mCanboxType.split(",");
            version += " " + ss[0];
            for (int i = 1; i < ss.length; ++i) {
                if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_ID)) {
                    version += " " + (ss[i].substring(1));
                } else if (ss[i].startsWith(MachineConfig.KEY_SUB_CANBOX_CAR_CONFIG)) {
                    version += ":" + (ss[i].substring(1));
                }
            }
        }

        if (version.length() > 1) {
            version += " ";
        }
        version += "Sys:" + Util.getFileString("/system/ak47_update_hold.txt");

        String vehicle_version = Util.getFileString("/system/etc/vehicle_version");
        if (vehicle_version != null) {
            version += " " + vehicle_version;
        }
        ((TextView) mView.findViewById(R.id.version)).setText(version);
        mDebug = mView.findViewById(R.id.msg_debug);

        initListView();
    }

    private static ScrollView mScrollView;
    private final static int DEUBG_MAX = 2;
    private final static int DEUBG_CANBOX = 1;
    public static int mMsgType = DEUBG_CANBOX;

    private final static int DEUBG_RXONLY = 2;
    private final static int DEUBG_TXONLY = 1;
    public static int mRXTX = 3;

    public static String byteArrayToHex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
            if (n < b.length - 1) {
                hs = hs + " ";
            }
        }
        return hs;
    }

    public static String byteArrayToHex(byte[] b, int start, int end) {
        String hs = "";
        String stmp = "";
        for (int n = start; n < b.length && n < end; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
            if (n < b.length - 1) {
                hs = hs + " ";
            }
        }
        return hs;
    }

    @SuppressLint("DefaultLocale")
    public static boolean updateText(byte[] param, boolean recv) {
        if (mDebug != null) {
            String s = null;
            if (recv) {
                if ((mRXTX & DEUBG_RXONLY) == 0) {
                    return false;
                }
            } else {
                if ((mRXTX & DEUBG_TXONLY) == 0) {
                    return false;
                }
            }
            switch (mMsgType) {
                case DEUBG_CANBOX:
                    if (param[0] == 0x5 && (param[1] == 0x1 || param[1] == 0x3)) {
                        s = byteArrayToHex(param, 2, param.length);
                    }
                    break;
                default:
                    s = byteArrayToHex(param);
                    break;
            }

            if (s != null) {
                mTest++;
                //				if (mTest >= 500) {
                //					mTest = 1;
                //					mDebug.setText("");
                //				}
                String pre;
                if (recv) {
                    pre = "->";
                } else {
                    pre = "<-";
                }

                long time = System.currentTimeMillis();
                Date d1 = new Date(time);
                //				byte h = (byte) d1.getHours();
                //				byte m = (byte) d1.getMinutes();
                byte sec = (byte) d1.getSeconds();
                //				SimpleDateFormat format = new SimpleDateFormat("mm:ss");
                s = String.format("%03d-%02d:%03d", mTest % 1000, sec, time % 1000) + " " + pre + s;
                //mDebug.setText(mDebug.getText() + "\n" + s);
                //mScrollView.fullScroll(View.FOCUS_DOWN);
                mAdapter.addData(s);
                mAdapter.notifyDataSetChanged();
                mListView.setSelection(mAdapter.getCount() - 1);
                return true;
            }
        }
        return false;
    }

    private static int mTest = 0;

    public static void start(Context context) {
        init(context);
        if (mView.getParent() == null) {
            mWindowManager.addView(mView, mLayoutParams);
        }
    }

    public static void stop() {
        if (mView.getParent() != null) {
            mWindowManager.removeView(mView);
            mDebug = null;
            //			mContext = null;
        }
    }

    private static void logcat() {
        // Util.do_exec("logcat > /sdcard/logcat.txt");
        // Util.setProperty("ctl.start", "logcat_service");

        List<StorageInfo> list = UtilSystem.listAllStorage(mContext);
        for (int i = 0; i < list.size(); ++i) {

            StorageInfo si = list.get(i);
            if (si.mType == StorageInfo.TYPE_USB && (si.mPath != null && si.mPath.indexOf("cdrom") < 0)) {

                String path = si.mPath + "/canbox_msg.txt";
                Util.setFileValue(path, mDebug.getText().toString());
                Toast.makeText(mContext, "Write canbox_msg.txt to !" + path + " success!", Toast.LENGTH_LONG).show();
                return;
            }
        }

        Toast.makeText(mContext, "No USB Disk!", Toast.LENGTH_LONG).show();

    }

    //listview
    private static ListView mListView;
    private static MyListViewAdapter mAdapter;

    private static void initListView() {
        mListView = mView.findViewById(R.id.tv_sd_list);
        mAdapter = new MyListViewAdapter(mContext, R.layout.tl_list);
        mListView.setAdapter(mAdapter);
    }
}
