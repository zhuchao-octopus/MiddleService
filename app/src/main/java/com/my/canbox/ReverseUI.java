package com.my.canbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.my.GlobalDef;
import com.my.cartype.CarUtil;

import com.my.out.R;
import com.car.view.BackStaticView;
import com.car.view.BackTrackView;
import com.car.view.TrackParamterDialog;
import com.common.util.MachineConfig;
import com.common.util.MyCmd;
import com.common.util.SystemConfig;
import com.common.util.Util;
import com.rockchip.gl.GLSurfaceView;
public class ReverseUI extends UIBase implements View.OnClickListener, SurfaceHolder.Callback {
    private Canbox mCanBox;

    public boolean mPreviewing;
    // private boolean mPause;
    private boolean mStartPreviewFail = false;
    private android.hardware.Camera mCameraDevice;
    private SurfaceHolder mSurfaceHolder = null;
    private SurfaceView mSurfaceView;
    // private static ReverseActivity mThis;
    private int mADRotation = 0;

    private final static String TAG = "ReverseUI";

    private RadarUI mRadarUI;

    private static ReverseUI[] mUI = new ReverseUI[MAX_DISPLAY];

    /**
     * Called when the activity is first created.
     */
    public static ReverseUI getInstance(Context context, View view, int index) {
        if (index >= MAX_DISPLAY) {
            return null;
        }
        mUI[index] = new ReverseUI(context, view, index);
        return mUI[index];
    }

    public ReverseUI(Context context, View view, int index) {
        super(context, view, index);
    }

    private static final int[] BUTTON_ON_CLICK = new int[]{R.id.cam1, R.id.cam2, R.id.cam3, R.id.cam4, R.id.switch_camera, R.id.switch_camera_mirror};

    public void onCreate() {
        // super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        super.onCreate();
        mCamerFailTime = 0;
        // setContentView(R.layout.back);
        // noSignalShowText(true);
        Canbox.addHandler("Reverse", mHandlerCanbox);

        mReverseUICanbox = new ReverseUICanbox();
        mReverseUICanbox.init(mMainView);
        // test

        // Intent it = new Intent(Intent.ACTION_RUN);
        // it.setClass(this, CanService.class);
        // this.startService(it);
        // CarUtil.setCanboxType(CarUtil.TYPE_TEANA2013);
        // finish();

        // return;
        mCanBox = CarUtil.getCanboxInstance();

        mSurfaceView = (SurfaceView) mMainView.findViewById(R.id.camera_surfaceview);
        if (!(com.common.util.Util.isAndroidP() || com.common.util.Util.isAndroidQ() || com.common.util.Util.isAndroidR())) {
            SurfaceHolder holder = mSurfaceView.getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        } else {
            mSurfaceView.setVisibility(View.GONE);
        }

        if (mCanBox != null) {
            mCanBox.requestInfo((byte) 0x25);//
        }
        mMainView.findViewById(R.id.backstatic_view).setVisibility(View.GONE);
        mMainView.findViewById(R.id.backtrack_view).setVisibility(View.GONE);
        mMainView.findViewById(R.id.only_black).setVisibility(View.GONE);

        Log.d(TAG, "layout id = " + mMainView.findViewById(R.id.screen1_main).getTag());
        // mMainView.findViewById(R.id.backstatic_view).setOnLongClickListener(new
        // OnLongClickListener() {
        //
        // @Override
        // public boolean onLongClick(View arg0) {
        // // TODO Auto-generated method stub
        //
        // Log.d("eecd", "!!!!!!!!!!");
        // return false;
        // }
        // });

        mEmptyView = mMainView.findViewById(R.id.empty);
        mSignalView = mMainView.findViewById(R.id.no_signal);
        if (GlobalDef.mSystemUI != null && GlobalDef.mSystemUI.equals(MachineConfig.VALUE_SYSTEM_UI_KLD7_1992)) {
            // Drawable d = mContext.getResources().getDrawable(
            // R.drawable.car_pickup);
            // if (d != null) {
            // ((ImageView) mSignalView).setImageDrawable(null);
            // }

            View v = mMainView.findViewById(R.id.no_signal_image);
            if (v instanceof ImageView) {
                ImageView new_name = (ImageView) v;
                Drawable d = Drawable.createFromPath("/mnt/paramter/auxin_nosignal.png");
                if (d != null) {
                    new_name.setImageDrawable(d);
                }
            }
        } else if (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDef.mSystemUI) || MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDef.mSystemUI) || MachineConfig.VALUE_SYSTEM_UI21_RM12.equals(GlobalDef.mSystemUI)) {

            View v = mMainView.findViewById(R.id.no_signal_text);
            if (v != null) {
                v.setVisibility(View.VISIBLE);
            }

            v = mMainView.findViewById(R.id.switch_camera_parent);
            if (v != null) {
                v.setVisibility(View.VISIBLE);
            }
            initPresentationUI();
        }

        initPresentationUI();

        // mRadarUI = RadarUI.getInstanse(mContext ,
        // mMainView.findViewById(R.id.screen1_radar), 0);
        //
        // mRadarUI.onCreate();

        mMainView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                // TODO Auto-generated method stub
                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d("eed", ":" + arg1.getX() + ":" + arg0.getWidth());
                    if (CarUtil.getCanboxInstance() != null) {
                        CarUtil.getCanboxInstance().touchInReverse((int) arg1.getX(), (int) arg1.getY(), arg0.getWidth(), arg0.getHeight());
                        CarUtil.getCanboxInstance().touchInReverseEx((int) arg1.getX(), (int) arg1.getY(), arg0.getWidth(), arg0.getHeight(), 1);
                    }
                } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    if (CarUtil.getCanboxInstance() != null) {
                        CarUtil.getCanboxInstance().touchInReverseEx((int) arg1.getX(), (int) arg1.getY(), arg0.getWidth(), arg0.getHeight(), 0);
                    }
                }
                return false;
            }
        });

        mMainView.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                // TODO Auto-generated method stub
                // if (!Util.isRKSystem()) {
                Intent it = new Intent(MyCmd.BROADCAST_START_BACKLIGHTSETTINGS);
                if (MachineConfig.VALUE_SYSTEM_UI20_RM10_1.equals(GlobalDef.mSystemUI) || MachineConfig.VALUE_SYSTEM_UI21_RM10_2.equals(GlobalDef.mSystemUI) || MachineConfig.VALUE_SYSTEM_UI21_RM12.equals(GlobalDef.mSystemUI)) {
                    File f = new File(GlobalDef.BRIGHTNESS_CONTRAST);
                    if (f.exists()) {
                        it.putExtra("cmd", -4);//
                    } else {
                        it.putExtra("cmd", -3);//
                    }
                } else {
                    it.putExtra("cmd", -3);//
                }

                mContext.sendBroadcast(it);
                // }

                View vs = mMainView.findViewById(R.id.backstatic_view);
                View vb = mMainView.findViewById(R.id.backtrack_view);
                if ((vs != null && vs.getVisibility() == View.VISIBLE) || (vb != null && vs.getVisibility() == View.VISIBLE)) {
                    if (mTrackParamterDialog == null) {
                        mTrackParamterDialog = new TrackParamterDialog(mContext);
                        mTrackParamterDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ERROR));
                    }
                    if (vs != null && vs.getVisibility() == View.VISIBLE) {
                        mTrackParamterDialog.setBackStaticView((BackStaticView) vs);
                    }
                    if (vb != null && vs.getVisibility() == View.VISIBLE) {
                        mTrackParamterDialog.setBackTrackView((BackTrackView) vb);
                    }
                    mTrackParamterDialog.show();

                }
                return false;
            }
        });
        // Log.d(TAG, ">>onCreate");

        updateCameraType();

        if (Util.isGLCamera() && mGLSurfaceView == null) {
            FrameLayout v = (FrameLayout) mMainView.findViewById(R.id.glsuface_main);
            if (com.common.util.Util.isAndroidP() || com.common.util.Util.isAndroidQ() || com.common.util.Util.isAndroidR()) {
                v.setVisibility(View.GONE);
                mGLSurfaceView = new GLSurfaceView(mContext, (LinearLayout) mMainView.findViewById(R.id.screen1_main));
				/*if (Util.isPX6()) {
					((LinearLayout)mMainView.findViewById(R.id.screen1_main)).setBackgroundColor(Color.TRANSPARENT);
				}*/
            } else {
                mGLSurfaceView = new GLSurfaceView(mContext, v);
            }
            mSurfaceView.setVisibility(View.GONE);
            mFirstCheckSignalFast = 15;
        }

        initMirrorPreview(mContext);
        initNissianUI();
        initHYUI();
        initHondaUI();
        initDaciaUI();
        initMazdaRaiseUI();

    }

    private ReverseUICanbox mReverseUICanbox;

    private TrackParamterDialog mTrackParamterDialog;

    private int mFirstCheckSignalFast = 0;
    private int mMirrorPreview = 0;

    private void initMirrorPreview(Context c) {
        try {
            mMirrorPreview = Settings.Global.getInt(c.getContentResolver(), SystemConfig.MIRROR_PREVIEW);
        } catch (Exception ignored) {
        }
    }

    private GLSurfaceView mGLSurfaceView;

    private void init4Camera() {
        initPresentationUI();
        registerListener();

        Intent it = new Intent(MyCmd.BROADCAST_CMD_TO_CARUI_CAMERA);
        mContext.sendBroadcast(it);
    }

    private int mCameraType = MachineConfig.VAULE_CAMERA_FRONT;

    private void updateCameraType() {
        String s = MachineConfig.getPropertyOnce(MachineConfig.KEY_CAMERA_TYPE);
        if (s != null) {
            mCameraType = Integer.parseInt(s);
        }

        Log.d(TAG, "mCameraType=" + mCameraType);

        if (MachineConfig.VALUE_CANBOX_ZHONGXING_OD.equals(CarUtil.getCanboxType())) mCameraType = MachineConfig.VAULE_CAMERA4;

        if (mCameraType == MachineConfig.VAULE_CAMERA_FRONT) {

            mMainView.findViewById(R.id.camera4).setVisibility(View.GONE);
        } else {

            mMainView.findViewById(R.id.camera4).setVisibility(View.VISIBLE);
            mCameraIndex = 1;
            updateCamera4Icon(mCameraIndex);
            init4Camera();

            // s = getData(""+R.id.cam1);
            // if(s!=null){
            // ((EditText)mMainView.findViewById(R.id.cam1)).setText(s);
            // }
            // s = getData(""+R.id.cam2);
            // if(s!=null){
            // ((EditText)mMainView.findViewById(R.id.cam2)).setText(s);
            // }
            // s = getData(""+R.id.cam3);
            // if(s!=null){
            // ((EditText)mMainView.findViewById(R.id.cam3)).setText(s);
            // }
            // s = getData(""+R.id.cam4);
            // if(s!=null){
            // ((EditText)mMainView.findViewById(R.id.cam4)).setText(s);
            // }
        }
    }

    private void initPresentationUI() {
        for (int i : BUTTON_ON_CLICK) {
            View v = mMainView.findViewById(i);
            if (v != null) {
                v.setOnClickListener(this);
            }
        }
    }

    public void onClick(View v) {

        Log.d(TAG, "onClick=" + v.toString());
        byte[] data = new byte[]{(byte) 0xa7, 0x1, 0x01};
        int id = v.getId();
        if (id == R.id.cam1) {
            setCamera(1);
            if (MachineConfig.VALUE_CANBOX_ZHONGXING_OD.equals(CarUtil.getCanboxType())) {
                data[2] = 0x01;
                CarUtil.sendDataToCanbox(data);
            }
        } else if (id == R.id.cam2) {
            setCamera(4);
            if (MachineConfig.VALUE_CANBOX_ZHONGXING_OD.equals(CarUtil.getCanboxType())) {
                data[2] = 0x02;
                CarUtil.sendDataToCanbox(data);
            }
        } else if (id == R.id.cam3) {
            setCamera(5);
            if (MachineConfig.VALUE_CANBOX_ZHONGXING_OD.equals(CarUtil.getCanboxType())) {
                data[2] = 0x03;
                CarUtil.sendDataToCanbox(data);
            }
        } else if (id == R.id.cam4) {
            setCamera(6);
            if (MachineConfig.VALUE_CANBOX_ZHONGXING_OD.equals(CarUtil.getCanboxType())) {
                data[2] = 0x04;
                CarUtil.sendDataToCanbox(data);
            }
        } else if (id == R.id.switch_camera) {
            if ((System.currentTimeMillis() - mLockClickSwitch) > 1000) {
                mLockClickSwitch = System.currentTimeMillis();
                toggleCamera();
            }
        } else if (id == R.id.switch_camera_mirror) {
            if ((System.currentTimeMillis() - mLockClickSwitch) > 1000) {
                mLockClickSwitch = System.currentTimeMillis();
                toggleMirror();
            }
        }
    }

    private void toggleMirror() {
        if (Util.isRKSystem()) {
            if (isSignal() != 1) {
                return;
            }
            if (mMirrorPreview == 0) {
                mMirrorPreview = 1;
            } else {
                mMirrorPreview = 0;
            }

            try {
                Settings.Global.putInt(mContext.getContentResolver(), SystemConfig.MIRROR_PREVIEW, mMirrorPreview);
            } catch (Exception e) {

            }
            mADRotation = mGLSurfaceView.getADRotation(mContext);
            mGLSurfaceView.setMirror(mMirrorPreview | mADRotation << 8);

            mPreSignal = mSignal = -1;
            mSignalView.setVisibility(View.GONE);
            startCheckSignal(false);
            if (Util.isGLCamera()) {
                showBlackEx(true, 1400);
                closeCamera();
                mHandler.removeMessages(MSG_RESTART_CAMERA_FAIL);
                mHandler.sendEmptyMessageDelayed(MSG_RESTART_CAMERA_FAIL, 200);
                //	restartPreview();
            } else {
                showBlackEx(true, 800);
            }
        }
    }

    private void showCameraSwitchIcon() {
        if (mCameraIndex == 1) {
            ((ImageView) mMainView.findViewById(R.id.switch_camera)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.f_camera_b));
        } else {
            ((ImageView) mMainView.findViewById(R.id.switch_camera)).setImageDrawable(mContext.getResources().getDrawable(R.drawable.f_camera_f));
        }
    }

    private void toggleCamera() {

        if ((System.currentTimeMillis() - mUpdateCameraTime) < 1200) {
            return;
        }

        if (mCameraIndex == 4) {
            mCameraIndex = 1;
            // ((ImageView) mMainView.findViewById(R.id.switch_camera))
            // .setImageDrawable(mContext.getResources().getDrawable(
            // R.drawable.f_camera_b));
        } else {
            mCameraIndex = 4;
            // ((ImageView) mMainView.findViewById(R.id.switch_camera))
            // .setImageDrawable(mContext.getResources().getDrawable(
            // R.drawable.f_camera_f));
        }

        switchToFrontCamera(mCameraIndex);

//		mUpdateCameraTime = System.currentTimeMillis();
//		mPreSignal = mSignal = -1;
//		mSignalView.setVisibility(View.GONE);
//		startCheckSignal(false);
//		showBlackEx(true, 800);
//		setCameraSource(mCameraIndex);


    }

    private long mLockClickSwitch = 0;
    private final static String CAMERA_INDEX = "/sys/class/misc/mst701/device/source2";


    private static int mToSetCameraSource;

    public static void setCameraSource(int source) {
        mToSetCameraSource = source;
        new Thread() {
            public void run() {
                Util.setFileValue(CAMERA_INDEX, mToSetCameraSource);
            }
        }.start();
        // Log.d("acccd", source+":"+getCameraSource());
    }

    private long mUpdateCameraTime = 0;
    private int mCameraIndex = 1;

    private void setCamera(int index) {
        if (mCameraIndex != index) {
            if ((System.currentTimeMillis() - mUpdateCameraTime) < 1200) {
                return;
            }
            mUpdateCameraTime = System.currentTimeMillis();
            mCameraIndex = index;
            mPreSignal = mSignal = -1;
            mSignalView.setVisibility(View.GONE);
            startCheckSignal(false);
            showBlackEx(true, 800);
            setCameraSource(mCameraIndex);
            updateCamera4Icon(index);
        }
    }

    public void showBlackEx(boolean on, int time) {
        if (on) {
            mMainView.findViewById(R.id.only_black).setVisibility(View.VISIBLE);
            mHandler.removeMessages(MSG_REMOVE_BLACK);
            mHandler.sendEmptyMessageDelayed(MSG_REMOVE_BLACK, time);
        } else {
            mMainView.findViewById(R.id.only_black).setVisibility(View.GONE);
        }
    }

    private void showBlack(boolean on) {
        showBlackEx(on, 0);
    }

    private void updateCamera4Icon(int index) {

        int id = 0;
        switch (index) {
            case 1:
                id = R.id.cam1;
                break;
            case 4:
                id = R.id.cam2;
                break;
            case 5:
                id = R.id.cam3;
                break;
            case 6:
                id = R.id.cam4;
                break;
        }

        if (id != 0) {
            mMainView.findViewById(R.id.cam1).setBackground(mContext.getDrawable(R.drawable.button_click_back_camera));
            mMainView.findViewById(R.id.cam2).setBackground(mContext.getDrawable(R.drawable.button_click_back_camera));
            mMainView.findViewById(R.id.cam3).setBackground(mContext.getDrawable(R.drawable.button_click_back_camera));
            mMainView.findViewById(R.id.cam4).setBackground(mContext.getDrawable(R.drawable.button_click_back_camera));

            mMainView.findViewById(id).setBackground(mContext.getDrawable(R.drawable.com_button14));
        }
    }

    private void hideEmpty() {

        Log.d("camera", "HIDE_EMPTY");
        mEmptyView.setVisibility(View.GONE);
        initBackTrack();
    }

    private void setValue(String path, int value) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            StringBuffer buf = new StringBuffer();

            buf.append(value);

            String writeString = buf.toString();
            FileOutputStream fos = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(fos);
            pw.write(writeString);
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface() == null) {
            return;
        }
        mSurfaceHolder = holder;
        if (mCameraDevice == null) return;
        if (mPause) return;
        if (mPreviewing && holder.isCreating()) {
            setPreviewDisplay(holder);
            Camera.Parameters parameters = mCameraDevice.getParameters();
            List<Size> sizes = parameters.getSupportedPreviewSizes();
            Size optimalSize = getOptimalPreviewSize(sizes, width, height);
            parameters.setPreviewSize(optimalSize.width, optimalSize.height);
            mCameraDevice.setParameters(parameters);
        } else {
            restartPreview();
        }
    }

    private void restartPreview() {
        try {
            startPreview();
        } catch (Exception e) {
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPreview();
        mSurfaceHolder = null;
    }

    // private final static String CAMERA_INDEX =
    // "/sys/class/i2c-dev/i2c-1/device/1-0044/channel";

    // private boolean mSetSource = true;

    public void setSource() {
        // if (mSetSource) {
        // mSetSource = true;
        // setValue(CAMERA_INDEX, 2);
        // }
    }

    private void startPreview() throws Exception {
        if (Util.isGLCamera()) {
            mADRotation = mGLSurfaceView.getADRotation(mContext);
            mGLSurfaceView.setMirror(mMirrorPreview | mADRotation << 8);
            if (mADRotation == 1 || mADRotation == 4) { //90,270
                DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
//				Log.d(TAG, "startPreview dm:" + dm);
                int x = 0, y = 0, w = dm.widthPixels, h = dm.heightPixels;
                x = Math.abs(dm.widthPixels - dm.heightPixels) / 2;
                w = h = dm.widthPixels < dm.heightPixels ? dm.widthPixels : dm.heightPixels;
                mPreviewing = mGLSurfaceView.startPreview(x, y, w, h);
            } else {
                mPreviewing = mGLSurfaceView.startPreview();
            }
            if (!mPreviewing) {
                ++mCamerFailTime;
                if (mCamerFailTime < 20) {
                    Log.d(TAG, "reverse startPreview fial:" + mCamerFailTime);
                    if (!mPause) {
                        mHandler.sendEmptyMessageDelayed(MSG_RESTART_CAMERA_FAIL, 200);
                    }
                }
                return;
            }
            if (Util.isRK356X() || Util.isPX6() || Util.isPX5() || Util.isPX30()) {
                lastHaveSignalTime = SystemClock.uptimeMillis();
            }
        } else {
            if (mPause) return;
            // mAkKeypad.sendMSCCommand(AkKeypad.MSC_REVERSE_LOCK);
            // mAkKeypad.sendMSCCommand(AkKeypad.MSC_CAMERA);
//		if (mSetSource) {
//			setValue(SOURCE_INDEX, MyCmd.SOURCE_REVERSE);
//			setValue(CAMERA_INDEX, 2);
//		}
//		doSleep(50);
//		mSetSource = true;
//		Log.d(TAG, "Reverse startPreview");
            try {
                ensureCameraDevice();
                if (mPreviewing) stopPreview();
                setPreviewDisplay(mSurfaceHolder);
                Camera.Parameters parameters = mCameraDevice.getParameters();
                List<Size> sizes = parameters.getSupportedPreviewSizes();
                Size s = sizes.get(0);
                parameters.setPreviewSize(s.width, s.height);
//			parameters.setPictureSize(s.width, s.height);
                if (Util.isRKSystem()) {
                    parameters.set("soc_camera_channel", MyCmd.CAMERA_SOURCE_REVERSE);
                }
                if (mMirrorPreview != 0) {
                    parameters.set("mirror-preview", "true");
                }
//			else {
//				parameters.set("mirror-preview", "false");
//			}
                mCameraDevice.setParameters(parameters);
                mCameraDevice.startPreview();
            } catch (Throwable ex) {
                closeCamera();
                ++mCamerFailTime;
                if (mCamerFailTime < 20) {
                    Log.d(TAG, "reverse startPreview fial:" + mCamerFailTime);
                    if (!mPause) {
                        mHandler.sendEmptyMessageDelayed(MSG_RESTART_CAMERA_FAIL, 200);
                    }
                }
                throw new RuntimeException("startPreview failed", ex);
            }
            mPreviewing = true;
        }
        // if (mReverseLight > 0 && mReverseLight < 255) {
        // setValue(SCREEN_COLOR_BRIGHT, mReverseLight);
        // }
        mHandler.removeMessages(HIDE_EMPTY);
        mHandler.sendEmptyMessageDelayed(HIDE_EMPTY, 100);

        Log.d(TAG, ">>>HIDE_EMPTY startPreview");
    }

    private static String SCREEN_COLOR_BRIGHT = "/sys/class/ak/camera/bright";
    public static int mReverseLight = -1;

    private void ensureCameraDevice() throws Exception {
        if (mCameraDevice == null) {
            mCameraDevice = CameraHolder.instance().open();
        }
    }

    private void stopPreview() {
        try {
            if (mCameraDevice != null && mPreviewing) {
                Log.d(TAG, "1stopPreview");
                mCameraDevice.stopPreview();
                Log.d(TAG, "2stopPreview");
            }

            mPreviewing = false;
        } catch (Exception e) {
            Log.d(TAG, "stopPreview fial:" + e);
        }
    }

    private void closeCamera() {
        if (Util.isGLCamera()) {
            mGLSurfaceView.stoptPreview();
        }
        if (mCameraDevice != null) {
            Log.d(TAG, "1release");
            CameraHolder.instance().release();

            Log.d(TAG, "2release");
            mCameraDevice = null;
            mPreviewing = false;
        }
    }

    private void setPreviewDisplay(SurfaceHolder holder) {
        try {
            mCameraDevice.setPreviewDisplay(holder);
        } catch (Throwable ex) {
            closeCamera();
            throw new RuntimeException("setPreviewDisplay failed", ex);
        }
    }

    private int mSignal = -1;
    private int mPreSignal = -1;

    private View mSignalView;

    private View mEmptyView;

    private static final int MSG_CHECK_SIGNAL = 14;

    private static final int TIME_CHECK_SIGNAL = 1000;

    private void noSignalShowText(int s) {
        Log.d(TAG, "no signal ShowText:" + s);
        if (mSignalView != null) {

            View v = null;
            if (mStaticTrackExist != 0) {
                v = mMainView.findViewById(R.id.backstatic_view);
            }

            // Log.d(TAG, "noSignalShowText:"+s);

            if (s == 1) {
                // restartPreview();
                if (!mPreviewing && !mStartPreviewFail) {
                    try {
                        startPreview();
                    } catch (Exception e) {
                        return;
                    }
                }
                mSignalView.setVisibility(View.GONE);
                if (v != null) {
                    v.setVisibility(View.VISIBLE);
                }
                // showBlack(false);
            } else {
                // releaseCamera();
                mSignalView.setVisibility(View.VISIBLE);
                if (v != null) {
                    v.setVisibility(View.GONE);
                }
            }
        }
    }

    private void stopCheckSignal() {
        mHandler.removeMessages(MSG_CHECK_SIGNAL);
    }


    private long lastHaveSignalTime = SystemClock.uptimeMillis();

    private void startCheckSignal(boolean check) {
        int time = TIME_CHECK_SIGNAL;

        if (check) {
            int s = isSignal();
            if (Util.isRK356X() || Util.isPX6() || Util.isPX5() || Util.isPX30()) {
                if (s == 1) lastHaveSignalTime = SystemClock.uptimeMillis();
            }
            if (s == mPreSignal) {
                if (s != mSignal) {
                    if (Util.isRK356X() || Util.isPX6() || Util.isPX5() || Util.isPX30()) {
                        if (s != 1) {
                            if (SystemClock.uptimeMillis() - lastHaveSignalTime > 2000) {
                                noSignalShowText(s);
                                mSignal = s;
                            }
                        } else {
                            noSignalShowText(s);
                            mSignal = s;
                        }
                    } else {
                        noSignalShowText(s);
                        mSignal = s;
                    }
                }
            } else {
                mPreSignal = s;
                if (s == 1) {
                    time = 200;
                } else {
                    time = 700;
                }
            }

        } else {

            time = 700;
        }

        if (mFirstCheckSignalFast >= 0) {
            --mFirstCheckSignalFast;
            time = 100;
        }

        stopCheckSignal();

        // Log.d("allen", "startCheckSignal:"+time);
        mHandler.sendEmptyMessageDelayed(MSG_CHECK_SIGNAL, time);

//		Log.d(TAG, "screen1_main: " + ((View)mMainView.findViewById(R.id.screen1_main)).getVisibility());
//		Log.d(TAG, "camera_surfaceview: " + ((View)mMainView.findViewById(R.id.camera_surfaceview)).getVisibility());
//		Log.d(TAG, "glsuface_main: " + ((View)mMainView.findViewById(R.id.glsuface_main)).getVisibility());
//		Log.d(TAG, "no_signal: " + ((View)mMainView.findViewById(R.id.no_signal)).getVisibility());
//		Log.d(TAG, "backstatic_view: " + ((View)mMainView.findViewById(R.id.backstatic_view)).getVisibility());
//		Log.d(TAG, "backtrack_view: " + ((View)mMainView.findViewById(R.id.backtrack_view)).getVisibility());
//		Log.d(TAG, "empty: " + ((View)mMainView.findViewById(R.id.empty)).getVisibility());
//		Log.d(TAG, "only_black: " + ((View)mMainView.findViewById(R.id.only_black)).getVisibility());
//		Log.d(TAG, "camera4" + ((View)mMainView.findViewById(R.id.camera4)).getVisibility());
    }

    public static boolean checkCamera0IfFacing = false;

    private boolean isCamera0Facing0() {
        boolean ret = false;
        if (Util.isPX5()) {
            try {
                android.hardware.Camera.CameraInfo mCameraInfo = new android.hardware.Camera.CameraInfo();
                android.hardware.Camera.getCameraInfo(0, mCameraInfo);
                Log.w("ParkBrake", "isCamera0Facing0: " + mCameraInfo.facing);
                if (mCameraInfo.facing == 0) {
                    ret = true;
                    checkCamera0IfFacing = true;
                }
            } catch (RuntimeException e) {
                Log.w("ParkBrake", "camera_surfaceview 0" + " maybe doesn't exist");
            }
        } else {
            ret = true;
            checkCamera0IfFacing = true;
        }
        return ret;
    }

    public int isSignal() {
        String source;

        // if(Util.isPX5()){
        if (Util.isRKSystem()) {
            source = readLine("/sys/class/ak/source/cvbs_status");
        } else {
            source = readLine("/sys/class/misc/mst701/device/lock");
        }
        // } else {
        // source = readLine("/sys/class/misc/mst701/device/lock");
        // }

        if (source != null && source.equals("1")) {
			/*if (!checkCamera0IfFacing && Util.isPX5()) {
				try {
					android.hardware.Camera.CameraInfo mCameraInfo = new android.hardware.Camera.CameraInfo();
					android.hardware.Camera.getCameraInfo(0, mCameraInfo);
					Log.w("ParkBrake", "camera_surfaceview "
							+ mCameraInfo.facing);
					if (mCameraInfo.facing == 1) {
						return 0;
					}
					checkCamera0IfFacing = true;
				} catch (RuntimeException e) {
					Log.w("ParkBrake", "reverse camera_surfaceview 0"
							+ " maybe doesn't exist");
					return 0;
				}
			}*/
            return 1;
        }

        return 0;

    }

    // private int isSignal() {
    //
    // String source ;
    //
    // if(Util.isPX5()){
    // source = readLine("/sys/class/ak/source/cvbs_status");
    // } else {
    // source = readLine("/sys/class/misc/mst701/device/lock");
    // }
    //
    // if (source != null && source.equals("1")) {
    // return 1;
    // }
    //
    // return 0;
    //
    // }
    private String readLine(String path) {

        File file = new File(path);

        String source = null;
        if (file.exists()) {
            BufferedReader buf;

            try {
                FileReader fr = new FileReader(file);
                buf = new BufferedReader(fr);
                source = buf.readLine();
                buf.close();
                fr.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return source;
    }

    private void removeAllHandlerMsg() {
        mHandler.removeMessages(HIDE_EMPTY);
        mHandler.removeMessages(MSG_RESTART_CAMERA_FAIL);
        mHandler.removeMessages(MSG_CHECK_SIGNAL);
    }

    @Override
    public void onPause() {
        if (mPause) {
            return;
        }
        super.onPause();
        mHandler.removeMessages(START_RADAR_UI);
        removeAllHandlerMsg();
        // mPause = true;

        mSignal = -1;
        mPreSignal = -1;
        mEmptyView.setVisibility(View.VISIBLE);

        stopPreview();

        closeCamera();
        // finish();

        // mRadarUI.onPause();

        stopCheckSignal();

        unregisterListener();

        if (mTrackParamterDialog != null) {
            mTrackParamterDialog.hide();
        }
        // Log.d(TAG, "<<<onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.isGLCamera()) {
            showBlackEx(true, 300);
        }
        mHandler.removeMessages(START_RADAR_UI);
        mCameraIndex = 1;
        Log.d(TAG, "onResume");
        mSignalView.setVisibility(View.GONE);
        startCheckSignal(false);
        // mRadarUI.onResume();
        // mPause = false;
        mEmptyView.setVisibility(View.VISIBLE);

        if (checkCamera0IfFacing || isCamera0Facing0()) {
            if (!mPreviewing && !mStartPreviewFail) {
                try {
                    startPreview();
                } catch (Exception e) {
                    return;
                }
            }
        }

        if (RadarManager.isShow) {
            // RadarManager.stop();
            mHandler.sendEmptyMessageDelayed(START_RADAR_UI, 400);
            // RadarManager.start(context);
        }
        // if (mDelayStartCameraTime != 0) {
        // mHandler.sendEmptyMessageDelayed(MSG_RESTART_CAMERA_FAIL,
        // mDelayStartCameraTime);
        // } else {
        // mHandler.sendEmptyMessage(MSG_RESTART_CAMERA_FAIL);
        // }
        showCameraSwitchIcon();
    }

    public void onDestroy() {
        // if (mThis == this) {
        // mThis = null;
        // } else {
        // Log.d("allen", "!!!!!!!!!!!!!!");
        // }

        // Log.d(TAG, "onDestroy");
        Canbox.removeHandler("Reverse");
        // mRadarUI.onDestroy();
        super.onDestroy();
        // Log.d(TAG, "<<<onDestroy");
    }

    private void doSleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private final static int MSG_RESTART_CAMERA_FAIL = 10000;
    private final static int MSG_SWITCH_CAMER_P90 = 10001;
    private int mCamerFailTime = 0;

    private final static int HIDE_EMPTY = 1;

    private final static int START_RADAR_UI = 6;
    private static final int MSG_REMOVE_BLACK = 15;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    // if (mThis != null) {
                    // mThis.finish();
                    // }
                    break;
                case HIDE_EMPTY:
                    hideEmpty();
                    break;
                case MSG_RESTART_CAMERA_FAIL:
                    restartPreview();
                    break;
                case MSG_CHECK_SIGNAL:
                    startCheckSignal(true);
                    break;

                case MSG_REMOVE_BLACK:
                    showBlack(false);
                    break;
                case MSG_SWITCH_CAMER_P90:
                    doSwitchToFrontCamera(msg.arg1);
                    break;
                case START_RADAR_UI:
                    if (RadarManager.isShow && mContext != null) {
                        RadarManager.stop();
                        RadarManager.start(mContext);
                        Handler handler = Canbox.getHandler(RadarManager.TAG);
                        if (null != handler) {
                            handler.sendMessage(handler.obtainMessage(Canbox.CANBOX_RADAR_BACK));
                        }
                    }
                    break;
            }
        }
    };

    private Handler mHandlerCanbox = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Canbox.CANBOX_RADAR_FRONT: {
                    // showRadarOSD();
                }
                break;
                case Canbox.CANBOX_RADAR_BACK: {
                    // showRadarOSD();
                }
                break;
                case Canbox.CANBOX_STEER_ANGLE: {
                    // showLocusOSD();
                    // Log.e("", ""+msg.arg1);
                    // msg.arg1 = msg.arg1/10;
                    if (mDyncTrackExist == 1) {
                        if (mBackTrackView != null) {
                            mBackTrackView.setVisibility(View.VISIBLE);
                            if (msg.arg2 == 0) {
                                mBackTrackView.doTrack(msg.arg1);
                            } else {
                                mBackTrackView.doTrack(msg.arg1 * 1.0f / msg.arg2);
                            }
                            mBackTrackView.invalidate();
                        }
                    }
                }
                break;
                case Canbox.CANBOX_RADAR_STATUS: {
                    // byte[] status = (byte[]) msg.obj;
                    // showRadarStatus(status);
                }
                break;

                case Canbox.CANBOX_NISSIAN_UI_DATA:
                    try {
                        if (msg.obj != null) {
                            showNissianUI((byte[]) (msg.obj));
                        }
                    } catch (Exception e) {

                    }
                    break;
                case Canbox.CANBOX_DACIA_UI_DATA:
                    showDaciaUI(msg.arg1);
                    break;
                case Canbox.CANBOX_NISSIAN_REQUEST_INFO:
                    if (!mPause) {
                        byte[] buf = new byte[]{(byte) 0x90, 0x02, (byte) 0x94, 0x0};
                        CarUtil.sendDataToCanbox(buf);
                        mHandlerCanbox.sendEmptyMessageDelayed(Canbox.CANBOX_NISSIAN_REQUEST_INFO, 1000);
                    }

                case Canbox.CANBOX_HY_UI_DATA:
                    try {
                        if (msg.obj != null) {
                            showHYUI((byte[]) (msg.obj));
                        }
                    } catch (Exception e) {

                    }
                    break;
                case Canbox.CANBOX_MAZDA_RAISE_UI_DATA:
                    showMazdaUI(msg.arg1);
                    break;
                case Canbox.CANBOX_SUBARU_UI_DATA:
                    try {
                        showSabaruUI(msg.arg1);
                    } catch (Exception e) {

                    }
                    break;
                case Canbox.CANBOX_HY_REQUEST_INFO:
                    if (!mPause) {
                        byte[] buf = new byte[]{(byte) 0x90, 0x01, (byte) 0x50};
                        CarUtil.sendDataToCanbox(buf);
                        mHandlerCanbox.sendEmptyMessageDelayed(Canbox.CANBOX_HY_REQUEST_INFO, 1000);
                    }
                    break;
                default:
                    mReverseUICanbox.doMsg(msg.what, msg.arg1, msg.arg2);
                    break;
            }
        }
    };

    private static final String SAVE_DATA = "com.my.carapp.SAVE_DATA";

    private void saveData(String s, int v) {
        SharedPreferences.Editor sharedata = mContext.getSharedPreferences(SAVE_DATA, 0).edit();
        sharedata.putInt(s, v);
        sharedata.commit();
    }

    private int getData(String s) {

        SharedPreferences sharedata = mContext.getSharedPreferences(SAVE_DATA, 0);
        return sharedata.getInt(s, 0);
    }

    // for ba ktrack
    BackTrackView mBackTrackView;
    int mStaticTrackExist = 0;
    int mDyncTrackExist = 1;

    public void initBackTrack() {

        try {
            mStaticTrackExist = Settings.Global.getInt(mContext.getContentResolver(), SystemConfig.REVERSE_STATIC_TRACK);
        } catch (SettingNotFoundException snfe) {

        }
        try {
            mDyncTrackExist = Settings.Global.getInt(mContext.getContentResolver(), SystemConfig.REVERSE_DYNC_TRACK);
        } catch (SettingNotFoundException snfe) {

        }

        // int i = Settings.Global.getInt(mContext.getContentResolver(),
        // SystemConfig.GPS_AUTO_UPDATE_TIME);

        mBackTrackView = (BackTrackView) mMainView.findViewById(R.id.backtrack_view);
        // if (mBackTrackView != null) {
        // if (mDyncTrackExist == 0) {
        mBackTrackView.setVisibility(View.GONE);
        // } else {
        // mBackTrackView.setVisibility(View.VISIBLE);
        // }
        // }
        View v = mMainView.findViewById(R.id.backstatic_view);

        if (v != null) {
            if (mStaticTrackExist == 0) {
                v.setVisibility(View.GONE);
            } else {
                if (isSignal() == 1) {
                    v.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private BroadcastReceiver mReceiver = null;

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

                    if (action.equals(MyCmd.BROADCAST_CMD_FROM_CARUI_CAMERA)) {
                        doCamName(intent.getStringExtra(MyCmd.EXTRA_COMMON_DATA2), intent.getIntExtra(MyCmd.EXTRA_COMMON_DATA, 0));
                    }

                }
            };
            IntentFilter iFilter = new IntentFilter();

            iFilter.addAction(MyCmd.BROADCAST_CMD_FROM_CARUI_CAMERA);

            mContext.registerReceiver(mReceiver, iFilter);
        }
    }

    private void doCamName(String s, int index) {
        if (s == null) {
            return;
        }
        int id;
        switch (index) {
            case 1:
                id = R.id.cam1;
                break;
            case 2:
                id = R.id.cam2;
                break;
            case 3:
                id = R.id.cam3;
                break;
            case 4:
                id = R.id.cam4;
                break;
            default:
                return;
        }
        ((TextView) mMainView.findViewById(id)).setText(s);

    }

    private void doSwitchToFrontCamera(int source) {
        Log.d(TAG, ">>doSwitchToFrontCamera:" + source);

        if (!mPause) {
            if (Util.isRKSystem()) {
                Util.setFileValue("/sys/class/ak/source/cam_ch", source);
            } else {
                Util.setFileValue("/sys/class/misc/mst701/device/source", source);
            }

            try {
                startPreview();
            } catch (Exception e) {
                return;
            }

            Log.d(TAG, "<<switchToFrontCamera");

            mHandler.sendEmptyMessageDelayed(MSG_CHECK_SIGNAL, 100);
        }
    }

    public void switchToFrontCamera(int source) {
//		int source = MyCmd.CAMERA_SOURCE_FRONT_CAMERA;
//		showBlackEx(true, 300);

        Log.d(TAG, ">>switchToFrontCamera");
        stopCheckSignal();
        noSignalShowText(1);
        mSignal = -1;
        mPreSignal = -1;
        closeCamera();
        showBlackEx(true, 150);
        mHandler.removeMessages(MSG_SWITCH_CAMER_P90);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SWITCH_CAMER_P90, source, 0), 150);
//		if (Util.isPX5()) {
//			Util.setFileValue("/sys/class/ak/source/cam_ch", source);
//		} else {
//			Util.setFileValue("/sys/class/misc/mst701/device/source",
//					source);
//		}
//		
//		try {
//			startPreview();
//		} catch (Exception e) {
//			return;
//		}
//		
//		Log.d(TAG, "<<switchToFrontCamera");
//
//		mHandler.sendEmptyMessageDelayed(MSG_CHECK_SIGNAL, 100);

        mCameraIndex = source;
        showCameraSwitchIcon();
    }

    private void initNissianUI() {
        View v = mMainView.findViewById(R.id.layout_canbus_nissian);
        if (v != null) {
            if (MachineConfig.VALUE_CANBOX_NISSAN2013.equals(CarUtil.getCanboxType()) || MachineConfig.VALUE_CANBOX_NISSAN_RAISE.equals(CarUtil.getCanboxType())) {
                v.setVisibility(View.VISIBLE);

                mHandlerCanbox.sendEmptyMessageDelayed(Canbox.CANBOX_NISSIAN_REQUEST_INFO, 1);

                mOnClickListener = new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        byte[] buf = new byte[]{(byte) 0x83, 0x02, (byte) 0x41, 0x1};
                        int id = arg0.getId();
                        if (id == R.id.btn_up) {
                            buf[2] = 0x48;
                        } else if (id == R.id.btn1) {
                            buf[2] = 0x4f;
                        } else if (id == R.id.btn_left) {
                            buf[2] = 0x4a;
                        } else if (id == R.id.btn_right) {
                            buf[2] = 0x4b;
                        } else if (id == R.id.btn2) {
                            buf[2] = 0x4e;
                        } else if (id == R.id.btn_down) {
                            buf[2] = 0x49;
                        } else if (id == R.id.canceal) {
                            buf[2] = 0x44;
                        } else if (id == R.id.start) {
                            buf[2] = 0x45;
                        } else if (id == R.id.back1) {
                            buf[2] = 0x46;
                        } else if (id == R.id.vertical1) {
                            buf[2] = 0x42;
                        } else if (id == R.id.lateral) {
                            buf[2] = 0x43;
                        } else if (id == R.id.imgpa) {
                            buf[2] = 0x41;
                        } else if (id == R.id.triangle) {
                            buf[2] = 0x47;
                        }

                        CarUtil.sendDataToCanbox(buf);
                    }
                };

                int[] BUTTON_ON_CLICK_NISSAN = new int[]{R.id.btn_up, R.id.btn1, R.id.btn_left, R.id.btn_right, R.id.btn2, R.id.btn_down, R.id.canceal, R.id.start, R.id.back1, R.id.vertical1, R.id.lateral, R.id.imgpa, R.id.triangle};

                for (int i : BUTTON_ON_CLICK_NISSAN) {
                    v = mMainView.findViewById(i);
                    if (v != null) {
                        v.setOnClickListener(mOnClickListener);
                    }
                }

                showNissianUI(new byte[]{0, 0, 0, 0});
            } else {
                v.setVisibility(View.GONE);
            }
        }
    }

    private OnClickListener mOnClickListener;

    private void setViewVisible(int id, int visibility) {
        View v = mMainView.findViewById(id);
        if (v != null) {
            v.setVisibility(visibility);
        }
    }

    private void setViewEnable(int id, boolean b) {
        View v = mMainView.findViewById(id);
        if (v != null) {
            v.setEnabled(b);
        }
    }

    private void setViewSelected(int id, boolean b) {
        View v = mMainView.findViewById(id);
        if (v != null) {
            v.setSelected(b);
        }
    }

    private void showNissianUI(byte[] data) {
        setViewVisible(R.id.border_bg, View.GONE);
        setViewVisible(R.id.btn_up, View.GONE);
        setViewVisible(R.id.btn1, View.GONE);
        setViewVisible(R.id.btn_left, View.GONE);
        setViewVisible(R.id.btn_middle, View.GONE);
        setViewVisible(R.id.btn_right, View.GONE);
        setViewVisible(R.id.btn2, View.GONE);
        setViewVisible(R.id.btn_down, View.GONE);
        setViewVisible(R.id.canceal, View.GONE);
        setViewVisible(R.id.start, View.GONE);
        setViewVisible(R.id.back1, View.GONE);
        setViewVisible(R.id.vertical1, View.GONE);
        setViewVisible(R.id.lateral, View.GONE);
        setViewVisible(R.id.imgpa, View.GONE);
        setViewVisible(R.id.triangle, View.GONE);
        setViewVisible(R.id.camera, View.GONE);

        if (data[0] != 0) {
            setViewVisible(R.id.camera, View.VISIBLE);
        }

        switch (data[0]) {
            case 1:
                setViewVisible(R.id.imgpa, View.VISIBLE);
                break;
            case 2:
                setViewVisible(R.id.canceal, View.VISIBLE);
                setViewVisible(R.id.vertical1, View.VISIBLE);
                break;
            case 3:

                setViewVisible(R.id.canceal, View.VISIBLE);
                setViewVisible(R.id.start, View.VISIBLE);
                setViewVisible(R.id.triangle, View.VISIBLE);
                setViewVisible(R.id.lateral, View.VISIBLE);
                break;
            case 4:

                setViewVisible(R.id.canceal, View.VISIBLE);
                setViewVisible(R.id.start, View.VISIBLE);
                setViewVisible(R.id.back1, View.VISIBLE);
                setViewVisible(R.id.btn_up, View.VISIBLE);
                setViewVisible(R.id.btn1, View.VISIBLE);
                setViewVisible(R.id.btn_left, View.VISIBLE);
                setViewVisible(R.id.btn_middle, View.VISIBLE);
                setViewVisible(R.id.btn_right, View.VISIBLE);
                setViewVisible(R.id.btn2, View.VISIBLE);
                setViewVisible(R.id.btn_down, View.VISIBLE);
                break;
            case 5:
                setViewVisible(R.id.canceal, View.VISIBLE);
                break;
        }

        setViewEnable(R.id.btn_up, (data[1] & 0x80) == 0);
        setViewEnable(R.id.btn_down, (data[1] & 0x40) == 0);
        setViewEnable(R.id.btn_left, (data[1] & 0x20) == 0);
        setViewEnable(R.id.btn_right, (data[1] & 0x10) == 0);
        setViewEnable(R.id.btn1, (data[1] & 0x4) == 0);
        setViewEnable(R.id.btn2, (data[1] & 0x8) == 0);

        if (data[2] != 0) {
            ((ImageView) mMainView.findViewById(R.id.camera)).setImageResource(R.drawable.can37_bcamera);
        } else {
            ((ImageView) mMainView.findViewById(R.id.camera)).setImageResource(R.drawable.can37_fcamera);
        }

        String s = "";
        if (data[2] != 0) {
            int string_id = 0;
            switch (data[2]) {
                case 0x1:
                case 0x2:
                case 0x3:
                case 0x4:
                case 0x5:
                case 0x6:
                case 0x7:
                case 0x8:
                case 0x9:
                case 0xa:
                case 0xb:
                case 0xc:
                case 0xd:
                case 0xe:
                    string_id = R.string.can37info_01 + data[2] - 1;
                    break;

                case 0x12:
                case 0x13:
                case 0x14:
                case 0x15:
                case 0x16:
                case 0x17:
                case 0x18:
                case 0x19:
                case 0x1a:
                case 0x1b:
                case 0x1c:
                case 0x1d:
                    data[2] -= 0x12;
                    string_id = R.string.can37info_12 + data[2];
                    break;

                case 0x22:
                case 0x23:
                case 0x24:
                case 0x25:

                    data[2] -= 0x22;
                    string_id = R.string.can37info_22 + data[2];
                    break;

                case 0x27:
                case 0x28:
                    data[2] -= 0x27;
                    string_id = R.string.can37info_27 + data[2];
                    break;
                case 0x42:
                    string_id = R.string.can37info_42;
                    break;
                case 0x43:
                    string_id = R.string.can37info_43;
                    break;
                case 0x45:
                    string_id = R.string.can37info_45;
                    break;
                case 0x4c:
                    string_id = R.string.can37info_4c;
                    break;
                case 0x4d:
                    string_id = R.string.can37info_4d;
                    break;
                case 0x4e:
                    string_id = R.string.can37info_4e;
                    break;
                case 0x4f:
                    string_id = R.string.can37info_4f;
                    break;

            }
            try {
                s = mContext.getString(string_id);
                setViewVisible(R.id.border_bg, View.VISIBLE);
                ((TextView) mMainView.findViewById(R.id.txt_info)).setText(s);
            } catch (Exception e) {

            }
        }

    }

    private boolean mShowHYSetting = false;
    private byte mHYParam = 0;

    private void showHYUI(byte[] data) {
        setViewVisible(R.id.layout_canbus_hy, View.GONE);
        setViewVisible(R.id.can15_view_back, View.GONE);
        setViewVisible(R.id.can15_view_front, View.GONE);
        setViewVisible(R.id.full_view, View.GONE);
        setViewVisible(R.id.can15_settings_view, mShowHYSetting ? View.VISIBLE : View.GONE);

        int t = ((data[0] & 0xc0) >> 6);
        int v = data[0] & 0xf;
        if (t == 1) {
            if (v <= 4) {
                setViewVisible(R.id.layout_canbus_hy, View.VISIBLE);
                if (v == 2) {
                    setViewVisible(R.id.can15_view_back, View.VISIBLE);
                } else {
                    setViewVisible(R.id.can15_view_back, View.VISIBLE);
                    setViewVisible(R.id.full_view, View.VISIBLE);
                }
            }
        } else if (t == 2) {
            if (v > 4) {
                setViewVisible(R.id.layout_canbus_hy, View.VISIBLE);
                if (v == 6) {
                    setViewVisible(R.id.can15_view_front, View.VISIBLE);
                } else {
                    setViewVisible(R.id.can15_view_front, View.VISIBLE);
                    setViewVisible(R.id.full_view, View.VISIBLE);
                }
            }
        }

        try {
            mHYParam = data[1];
            View view = mMainView.findViewById(R.id.can15_guidelines_settings);
            if ((data[1] & 0x80) == 0) {
                ((RadioButton) view).setChecked(false);
            } else {
                ((RadioButton) view).setChecked(true);
            }

            view = mMainView.findViewById(R.id.can15_warning_settings);
            if ((data[1] & 0x40) == 0) {
                ((RadioButton) view).setChecked(false);
            } else {
                ((RadioButton) view).setChecked(true);
            }

            int type;

            // type = ((data[1]&0x18)>>3);
            type = ((data[1] & 0x3));
            view = mMainView.findViewById(R.id.can15_front_all_settings);
            if (type == 0) {
                ((RadioButton) view).setChecked(true);
            } else {
                ((RadioButton) view).setChecked(false);
            }

            view = mMainView.findViewById(R.id.can15_front_only_settings);
            if (type == 1) {
                ((RadioButton) view).setChecked(true);
            } else {
                ((RadioButton) view).setChecked(false);
            }

            view = mMainView.findViewById(R.id.can15_front_left_settings);
            if (type == 2) {
                ((RadioButton) view).setChecked(true);
            } else {
                ((RadioButton) view).setChecked(false);
            }

            view = mMainView.findViewById(R.id.can15_front_right_settings);
            if (type == 3) {
                ((RadioButton) view).setChecked(true);
            } else {
                ((RadioButton) view).setChecked(false);
            }

            type = ((data[1] & 0x18) >> 3);

            view = mMainView.findViewById(R.id.can15_back_all_settings);
            if (type == 0) {
                ((RadioButton) view).setChecked(true);
            } else {
                ((RadioButton) view).setChecked(false);
            }

            view = mMainView.findViewById(R.id.can15_back_only_settings);
            if (type == 1) {
                ((RadioButton) view).setChecked(true);
            } else {
                ((RadioButton) view).setChecked(false);
            }

            view = mMainView.findViewById(R.id.can15_back_left_settings);
            if (type == 2) {
                ((RadioButton) view).setChecked(true);
            } else {
                ((RadioButton) view).setChecked(false);
            }

            view = mMainView.findViewById(R.id.can15_back_right_settings);
            if (type == 3) {
                ((RadioButton) view).setChecked(true);
            } else {
                ((RadioButton) view).setChecked(false);
            }

        } catch (Exception e) {

        }

    }

    private void showHYSetting(int id) {
        View v = mMainView.findViewById(R.id.layout_canbus_hy);
        if (v != null) {
            if (id == -1) {
                mShowHYSetting = false;
                setViewVisible(R.id.can15_settings_view, View.GONE);
            } else {
                setViewVisible(R.id.can15_guidelines_settings, View.GONE);
                setViewVisible(R.id.can15_warning_settings, View.GONE);
                setViewVisible(R.id.can15_front_settings, View.GONE);
                setViewVisible(R.id.can15_back_settings, View.GONE);

                switch (id) {
                    case 1:
                        setViewVisible(R.id.can15_guidelines_settings, View.VISIBLE);
                        break;
                    case 2:
                        setViewVisible(R.id.can15_warning_settings, View.VISIBLE);
                        break;
                    case 3:
                        setViewVisible(R.id.can15_front_settings, View.VISIBLE);
                        break;
                    case 4:
                        setViewVisible(R.id.can15_back_settings, View.VISIBLE);
                        break;
                    default:
                        setViewVisible(R.id.can15_guidelines_settings, View.VISIBLE);
                        break;
                }
                mShowHYSetting = true;
                setViewVisible(R.id.can15_settings_view, View.VISIBLE);
            }
        }
    }

    private void initHYUI() {
        View v = mMainView.findViewById(R.id.layout_canbus_hy);
        if (v != null) {
            if (MachineConfig.VALUE_CANBOX_HY.equals(CarUtil.getCanboxType()) || MachineConfig.VALUE_CANBOX_HY_RAISE.equals(CarUtil.getCanboxType())) {
                byte[] buf = new byte[]{(byte) 0x90, 0x01, 0x50};
                if (MachineConfig.VALUE_CANBOX_HY_RAISE.equals(CarUtil.getCanboxType())) {
                    buf = new byte[]{0x06, (byte) 0x90, 0x40, 0};
                }

                CarUtil.sendDataToCanbox(buf);

                mOnClickListener = new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        byte[] buf = new byte[]{(byte) 0x84, 0x02, 0, 0};
                        int id = arg0.getId();
                        if (id == R.id.can15_all_b) {
                            buf[2] = 0x1;
                            buf[3] = 0x1;
                        } else if (id == R.id.can15_back) {
                            buf[2] = 0x1;
                            buf[3] = 0x2;
                        } else if (id == R.id.can15_left_back) {
                            buf[2] = 0x1;
                            buf[3] = 0x3;
                        } else if (id == R.id.can15_right_back) {
                            buf[2] = 0x1;
                            buf[3] = 0x4;
                        } else if (id == R.id.can15_all_f) {
                            buf[2] = 0x1;
                            buf[3] = 0x5;
                        } else if (id == R.id.can15_front) {
                            buf[2] = 0x1;
                            buf[3] = 0x6;
                        } else if (id == R.id.can15_left_front) {
                            buf[2] = 0x1;
                            buf[3] = 0x7;
                        } else if (id == R.id.can15_right_front) {
                            buf[2] = 0x1;
                            buf[3] = 0x8;
                        } else if (id == R.id.can15_guidelines_settings) {
                            buf[2] = 0x2;
                            if ((mHYParam & 0x80) == 0) {
                                buf[3] = 1;
                            } else {
                                buf[3] = 0;
                            }
                        } else if (id == R.id.can15_warning_settings) {
                            buf[2] = 0x3;
                            if ((mHYParam & 0x40) == 0) {
                                buf[3] = 1;
                            } else {
                                buf[3] = 0;
                            }
                        } else if (id == R.id.can15_front_all_settings) {
                            buf[2] = 0x4;
                            buf[3] = 0x0;
                        } else if (id == R.id.can15_front_only_settings) {
                            buf[2] = 0x4;
                            buf[3] = 0x1;
                        } else if (id == R.id.can15_front_left_settings) {
                            buf[2] = 0x4;
                            buf[3] = 0x2;
                        } else if (id == R.id.can15_front_right_settings) {
                            buf[2] = 0x4;
                            buf[3] = 0x3;
                        } else if (id == R.id.can15_back_all_settings) {
                            buf[2] = 0x5;
                            buf[3] = 0x0;
                        } else if (id == R.id.can15_back_only_settings) {
                            buf[2] = 0x5;
                            buf[3] = 0x1;
                        } else if (id == R.id.can15_back_left_settings) {
                            buf[2] = 0x5;
                            buf[3] = 0x2;
                        } else if (id == R.id.can15_back_right_settings) {
                            buf[2] = 0x5;
                            buf[3] = 0x3;
                        } else if (id == R.id.settings_b || id == R.id.settings_f) {
                            showHYSetting(0);
                        } else if (id == R.id.can15_exit_settings) {
                            showHYSetting(-1);
                        } else if (id == R.id.can15_guidelines) {
                            showHYSetting(1);
                        } else if (id == R.id.can15_warning) {
                            showHYSetting(2);
                        } else if (id == R.id.can15_frontview) {
                            showHYSetting(3);
                        } else if (id == R.id.can15_rearview) {
                            showHYSetting(4);
                        }
                        if (buf[2] != 0) {
                            if (MachineConfig.VALUE_CANBOX_HY_RAISE.equals(CarUtil.getCanboxType())) {
                                byte[] buf2 = null;
                                switch (buf[2]) {
                                    case 1:
                                        buf2 = new byte[]{0x5, (byte) 0x84, 0};
                                        switch (buf[3]) {
                                            case 1:
                                                buf2[2] = 6;
                                                break;
                                            case 2:
                                                buf2[2] = 5;
                                                break;
                                            case 3:
                                                buf2[2] = 8;
                                                break;
                                            case 4:
                                                buf2[2] = 9;
                                                break;
                                            case 5:
                                                buf2[2] = 1;
                                                break;
                                            case 6:
                                                buf2[2] = 2;
                                                break;
                                            case 7:
                                                buf2[2] = 3;
                                                break;
                                            case 8:
                                                buf2[2] = 4;
                                                break;
                                        }
                                        break;
                                    case 0x2:
                                        buf2 = new byte[]{0x5, (byte) 0x85, 1, buf[3]};
                                        break;
                                    case 0x3:
                                        buf2 = new byte[]{0x5, (byte) 0x85, 2, buf[3]};
                                        break;
                                    case 0x4:
                                        buf2 = new byte[]{0x5, (byte) 0x85, 3, buf[3]};
                                        break;
                                    case 0x5:
                                        buf2 = new byte[]{0x5, (byte) 0x85, 4, buf[3]};
                                        break;
                                }
                                if (buf2 != null) {
                                    CarUtil.sendDataToCanbox(buf2);
                                }
                            } else {
                                CarUtil.sendDataToCanbox(buf);
                            }

                        }
                    }
                };

                int[] BUTTON_ON_CLICK_NISSAN = new int[]{R.id.can15_all_b, R.id.can15_back, R.id.can15_left_back, R.id.can15_right_back, R.id.settings_b, R.id.can15_all_f, R.id.can15_front, R.id.can15_left_front, R.id.can15_right_front, R.id.settings_f, R.id.can15_exit_settings, R.id.can15_guidelines, R.id.can15_warning, R.id.can15_frontview, R.id.can15_rearview, R.id.can15_front_all_settings, R.id.can15_front_only_settings, R.id.can15_front_left_settings, R.id.can15_front_right_settings, R.id.can15_back_all_settings, R.id.can15_back_only_settings, R.id.can15_back_left_settings, R.id.can15_back_right_settings, R.id.can15_guidelines_settings, R.id.can15_warning_settings,

                };

                for (int i : BUTTON_ON_CLICK_NISSAN) {
                    v = mMainView.findViewById(i);
                    if (v != null) {
                        v.setOnClickListener(mOnClickListener);
                    }
                }

                showHYUI(new byte[]{0, 0, 0, 0});

            }
        }

    }

    private void initHondaUI() {
        View v = mMainView.findViewById(R.id.layout_honda_da_raise);
        if (v != null) {
            boolean show = false;

            if (CarUtil.getProIndex() == 52) { //for pro > v3
                show = true;
            } else if (MachineConfig.VALUE_CANBOX_HONDA_RAISE.equals(CarUtil.getCanboxType())) {
                if (CarUtil.getCarType() != 2) {
                    show = true;
                }
            }

            if (show) {
                v.setVisibility(View.VISIBLE);

                mOnClickListener = new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        byte[] buf = new byte[]{(byte) 0xc6, 0x02, (byte) 0x40, 0x0};
                        int id = arg0.getId();
                        if (id == R.id.honda_cam_type1) {
                            buf[3] = 0;
                        } else if (id == R.id.honda_cam_type2) {
                            buf[3] = 1;
                        } else if (id == R.id.honda_cam_type3) {
                            buf[3] = 2;
                        } else if (id == R.id.honda_cam_type4) {
                            buf[3] = 3;
                        }

                        CarUtil.sendDataToCanbox(buf);
                    }
                };

                int[] BUTTON_ON_CLICK_NISSAN = new int[]{R.id.honda_cam_type1, R.id.honda_cam_type2, R.id.honda_cam_type3, R.id.honda_cam_type4,};

                for (int i : BUTTON_ON_CLICK_NISSAN) {
                    v = mMainView.findViewById(i);
                    if (v != null) {
                        v.setOnClickListener(mOnClickListener);
                    }
                }
            }
        }
    }


    private void setDaciaSelectButton(int id) {
        setViewSelected(R.id.dacia_cam_type1, false);
        setViewSelected(R.id.dacia_cam_type2, false);
        setViewSelected(R.id.dacia_cam_type3, false);
        setViewSelected(R.id.dacia_cam_type4, false);
        if (id != 0) {
            setViewSelected(id, true);
        }
    }

    private void initDaciaUI() {
        if (MachineConfig.VALUE_CANBOX_DACIA_SIMPLE.equals(CarUtil.getCanboxType()) || 36 == CarUtil.getProIndex() || 147 == CarUtil.getProIndex()) {
            if ((CarUtil.m360UI & 0xff00) != 0) {
                showDaciaUI(CarUtil.m360UI & 0xff);
            }
        }
    }

    private void showDaciaUI(int status) {
        View v = mMainView.findViewById(R.id.dacia_360);
        if (v != null) {
            int id = 0;
            v.setVisibility(View.VISIBLE);
            switch (status) {
                case 1:
                    id = R.id.dacia_cam_type1;
                    break;
                case 2:
                    id = R.id.dacia_cam_type2;
                    break;
                case 3:
                    id = R.id.dacia_cam_type3;
                    break;
                case 4:
                    id = R.id.dacia_cam_type4;
                    break;
            }
            if (id != 0) {
                setDaciaSelectButton(id);
            }
            if (mOnClickListener == null) {
                setViewVisible(R.id.switch_camera, View.GONE);
                setViewVisible(R.id.switch_camera_mirror, View.GONE);
                mOnClickListener = new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        byte[] buf = new byte[]{(byte) 0xc6, 0x03, (byte) 0x4, 0x0, 0x0};
                        int arg0Id = arg0.getId();
                        if (arg0Id == R.id.dacia_cam_type1) {
                            buf[3] = 1;
                        } else if (arg0Id == R.id.dacia_cam_type2) {
                            buf[3] = 2;
                        } else if (arg0Id == R.id.dacia_cam_type3) {
                            buf[3] = 3;
                        } else if (arg0Id == R.id.dacia_cam_type4) {
                            buf[3] = 4;
                        }

                        if (147 == CarUtil.getProIndex()) {

                            byte arg = buf[3];
                            buf = new byte[]{0x2, (byte) 0xf2, 0x10, 0x0};
                            switch (arg) {
                                case 1:
                                    buf[3] = 8;
                                    break;
                                case 2:
                                    buf[3] = 7;
                                    break;
                                case 3:
                                    buf[3] = 5;
                                    break;
                                case 4:
                                    buf[3] = 6;
                                    break;
                            }
                        }

                        setDaciaSelectButton(arg0.getId());
                        CarUtil.sendDataToCanbox(buf);
                    }
                };

                int[] BUTTON_ON_CLICK_NISSAN = new int[]{R.id.dacia_cam_type1, R.id.dacia_cam_type2, R.id.dacia_cam_type3, R.id.dacia_cam_type4,};

                for (int i : BUTTON_ON_CLICK_NISSAN) {
                    v = mMainView.findViewById(i);
                    if (v != null) {
                        v.setOnClickListener(mOnClickListener);
                    }
                }
            }
        }

    }

    private void showSabaruUI(int data) {
        if ((data & 0xc) != 0) {
            setViewVisible(R.id.layout_canbus_subaru_simple, View.VISIBLE);
            if ((data & 0x4) != 0) {
                setViewVisible(R.id.subaru_left, View.VISIBLE);
            } else {
                setViewVisible(R.id.subaru_left, View.GONE);
            }

            if ((data & 0x8) != 0) {

                setViewVisible(R.id.subaru_right, View.VISIBLE);
            } else {
                setViewVisible(R.id.subaru_right, View.GONE);
            }
        } else {
            setViewVisible(R.id.layout_canbus_subaru_simple, View.GONE);
        }
    }

    private byte mMazdaUI = 0;

    private void showMazdaUI(int data) {
        if (mOnClickListener == null) {
            mOnClickListener = new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    byte[] buf = new byte[]{(byte) 0xc6, 0x02, (byte) 0x0, 0x0};
                    if (arg0.getId() == R.id.mazda_raise_mode) {
                        buf[2] = (byte) 0xca;
                        mMazdaUI++;
                        if (mMazdaUI < 4 || mMazdaUI > 0x10) {
                            mMazdaUI = 4;
                        }
                        buf[3] = mMazdaUI;
                    }
                    CarUtil.sendDataToCanbox(buf);
                }
            };
        }
        mMazdaUI = (byte) ((data & 0xff00) >> 8);
        if ((data & 0x1) != 0) {
            setViewVisible(R.id.layout_canbus_mazda_raise, View.VISIBLE);
            mMainView.findViewById(R.id.mazda_raise_mode).setOnClickListener(mOnClickListener);
        } else {
            setViewVisible(R.id.layout_canbus_mazda_raise, View.GONE);
        }
    }

    private void initMazdaRaiseUI() {
        if (MachineConfig.VALUE_CANBOX_MAZDA_RAISE.equals(CarUtil.getCanboxType())) {
            if (mOnClickListener == null) {
                mOnClickListener = new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        byte[] buf = new byte[]{(byte) 0x87, 0x02, (byte) 0x1, 0x1};
                        CarUtil.sendDataToCanbox(buf);
                    }
                };
            }
            if (CarUtil.getCarType2() == 1) {
                setViewVisible(R.id.layout_canbus_mazda_raise, View.VISIBLE);
                mMainView.findViewById(R.id.mazda_raise_mode).setOnClickListener(mOnClickListener);
            } else {
                setViewVisible(R.id.layout_canbus_mazda_raise, View.GONE);
            }
        }
    }
}
