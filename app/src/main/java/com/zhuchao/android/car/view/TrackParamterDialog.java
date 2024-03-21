package com.zhuchao.android.car.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.zhuchao.android.car.hardware.BackTrack;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;


public class TrackParamterDialog extends Dialog {
    public final static int MYDIALOG_STYLE_OK_CANCEAL = 0;
    public final static int MYDIALOG_STYLE_OK = 1;
    public final static int MYDIALOG_STYLE_CANCEAL = 2;
    public final static int MYDIALOG_STYLE_SCAN_CANCEAL = 3;
    public final static int MYDIALOG_STYLE_SCAN = 4;
    public final static int MYDIALOG_STYLE_PASSWD = 5;


    public TrackParamterDialog(Context context) {
        super(context, R.style.dialog);
    }

    public TrackParamterDialog(Context context, View.OnClickListener ok) {
        super(context, R.style.dialog);
    }

    public void hide() {

        super.cancel();
    }

    private final View.OnClickListener mOnClickDialogCancel = new View.OnClickListener() {
        public void onClick(View v) {
            BackTrack.reset();
            hide();
        }
    };


    private final View.OnClickListener mOnClickDialogOK = new View.OnClickListener() {
        public void onClick(View v) {
            BackTrack.saveConfig(GlobalDefinition.getContext());
            hide();
        }
    };


    private void initText() {
        String s = "";
        int i;

        i = BackTrack.get_track_camera_h();
        s = String.valueOf((i - 800) / 10);
        ((TextView) findViewById(R.id.camera_heigt)).setText(s);


        i = BackTrack.get_screen_w();
        s = String.valueOf((i - 1024) / 10);
        ((TextView) findViewById(R.id.left_right)).setText(s);


        i = BackTrack.get_track_car_w();
        s = String.valueOf((i - 1800) / 10);
        ((TextView) findViewById(R.id.width)).setText(s);


        //		i = BackTrack.get_screen_w();
        //		i = (i-1024)/10;
        //		((TextView)findViewById(R.id.left_right)).setText(s);
    }

    private final View.OnClickListener mOnClickDialog = new View.OnClickListener() {
        public void onClick(View v) {

            int i = 0;
            int text_id;
            //			left_right
            int id = v.getId();
            if (id == R.id.camera_h_m) {
                i = BackTrack.get_track_camera_h() - 10;
                if (i < 600) {
                    return;
                }
                BackTrack.set_track_camera_h(i);
            } else if (id == R.id.camera_h_a) {
                i = BackTrack.get_track_camera_h() + 10;
                if (i > 1000) {
                    return;
                }
                BackTrack.set_track_camera_h(i);
            } else if (id == R.id.angle_b_m) {
                i = (int) BackTrack.get_track_angle_b() - 1;
                if (i <= 16) {
                    return;
                }
                BackTrack.set_track_angle_b(i);
            } else if (id == R.id.angle_b_a) {
                i = (int) BackTrack.get_track_angle_b() + 1;
                if (i >= 26) {
                    return;
                }
                BackTrack.set_track_angle_b(i);
            } else if (id == R.id.car_w_m) {
                i = BackTrack.get_track_car_w() - 10;
                if (i < 1600) {
                    return;
                }
                BackTrack.set_track_car_w(i);
            } else if (id == R.id.car_w_a) {
                i = BackTrack.get_track_car_w() + 10;
                if (i > 2000) {
                    return;
                }
                BackTrack.set_track_car_w(i);
            } else if (id == R.id.camera_lr_m) {
                i = BackTrack.get_screen_w() - 10;
                if (i < 924) {
                    return;
                }
                BackTrack.set_screen_w(i);
            } else if (id == R.id.camera_lr_a) {
                i = BackTrack.get_screen_w() + 10;
                if (i > 1124) {
                    return;
                }
                BackTrack.set_screen_w(i);
            } else if (id == R.id.reset) {
                BackTrack.reset();
            }
            initText();
            //			Log.d("eedf", ""+i);
            if (mBackStaticView != null) {
                mBackStaticView.reFlash();
            }

            if (mBackTrackView != null) {
                mBackTrackView.reFlash();
            }

        }
    };

    public void setBackStaticView(BackStaticView v) {
        mBackStaticView = v;
    }

    public void setBackTrackView(BackTrackView v) {
        mBackTrackView = v;
    }

    private BackStaticView mBackStaticView;
    private BackTrackView mBackTrackView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.my_dialog);

        setContentView(R.layout.track_parameter_dialog);

        findViewById(R.id.cancel).setOnClickListener(mOnClickDialogCancel);
        findViewById(R.id.camera_h_a).setOnClickListener(mOnClickDialog);
        findViewById(R.id.camera_h_m).setOnClickListener(mOnClickDialog);


        findViewById(R.id.camera_lr_m).setOnClickListener(mOnClickDialog);
        findViewById(R.id.camera_lr_a).setOnClickListener(mOnClickDialog);

        findViewById(R.id.car_w_m).setOnClickListener(mOnClickDialog);
        findViewById(R.id.car_w_a).setOnClickListener(mOnClickDialog);

        findViewById(R.id.angle_b_m).setOnClickListener(mOnClickDialog);
        findViewById(R.id.angle_b_a).setOnClickListener(mOnClickDialog);


        findViewById(R.id.reset).setOnClickListener(mOnClickDialog);
        findViewById(R.id.ok).setOnClickListener(mOnClickDialogOK);
        //
        // if (mOk != null) {
        // findViewById(R.id.num_ok).setOnClickListener(mOk);
        // }
        initText();
    }

}