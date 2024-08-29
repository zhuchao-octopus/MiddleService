package com.zhuchao.android.car.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.zhuchao.android.car.hardware.BackTrack;

public class BackTrackView extends View {

    private static final String TAG = "BackTrackView";
    private final static int PAINT_WIDTH = 4;
    private final static int UP_Y = 0;
    private final static double[] SAWTOOTH = new double[]{35, 35, 30, 20, 18, 15};
    private static double mAngle;
    private BackTrack mBackTrack;
    private Paint mPaint1;
    private Paint mPaint2;
    private Paint mPaint3;

    public BackTrackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mBackTrack = new BackTrack();
        mBackTrack.init();

        mPaint1 = new Paint();
        mPaint1.setColor(Color.GREEN);
        mPaint1.setStrokeWidth(PAINT_WIDTH);
        mPaint1.setAntiAlias(true);
        mPaint2 = new Paint();
        mPaint2.setColor(Color.YELLOW);
        mPaint2.setStrokeWidth(PAINT_WIDTH);
        mPaint2.setAntiAlias(true);
        mPaint3 = new Paint();
        mPaint3.setColor(Color.RED);
        mPaint3.setStrokeWidth(PAINT_WIDTH);
        mPaint3.setAntiAlias(true);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec - UP_Y);
    }

    // private final static int SAWTOOTH1 = 80;
    // private final static int SAWTOOTH2 = 40;
    // private final static int SAWTOOTH3 = 10;
    // private final static int SAWTOOTH4 = 10;
    // private final static int SAWTOOTH5 = 10;
    // private final static int SAWTOOTH6 = 10;

    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);


        Paint paint = mPaint1;

        //		canvas.drawText("for test", 100, 200, paint);
        //Log.e(TAG, "d System.currentTimeMillis()" + System.currentTimeMillis());

        int i;

        double startX = 0;
        double startY = 0;
        double l_x, l_y, r_x, r_y;
        double tanx;
        for (i = 0; i < BackTrack.show_l_num; i++) {
            if (mBackTrack.xy_l_screen_x[i] <= 0 || mBackTrack.xy_l_screen_y[i] <= 0) {
                continue;
            }

            if (i <= mBackTrack.sawtooth_l_point[0]) {
                paint = mPaint3;
            } else if (i >= mBackTrack.sawtooth_l_point[2] && i <= mBackTrack.sawtooth_l_point[3]) {
                paint = mPaint2;
            } else if (i >= mBackTrack.sawtooth_l_point[5] && i <= mBackTrack.sawtooth_l_point[6]) {
                paint = mPaint1;
            } else {
                startX = 0;
                startY = 0;
                continue;
            }

            if (startX != 0 && startY != 0) {
                canvas.drawLine((float) startX, (float) startY, (float) mBackTrack.xy_l_screen_x[i], (float) mBackTrack.xy_l_screen_y[i], paint);
            }
            startX = mBackTrack.xy_l_screen_x[i];
            startY = mBackTrack.xy_l_screen_y[i];

        }
        startX = 0;
        startY = 0;
        for (i = 0; i < BackTrack.show_r_num; i++) {
            if (mBackTrack.xy_r_screen_x[i] <= 0 || mBackTrack.xy_r_screen_y[i] <= 0) {
                continue;
            }

            if (i <= mBackTrack.sawtooth_r_point[0]) {
                paint = mPaint3;
            } else if (i >= mBackTrack.sawtooth_r_point[2] && i <= mBackTrack.sawtooth_r_point[3]) {
                paint = mPaint2;
            } else if (i >= mBackTrack.sawtooth_r_point[5] && i <= mBackTrack.sawtooth_r_point[6]) {
                paint = mPaint1;
            } else {
                startX = 0;
                startY = 0;
                continue;
            }

            if (startX != 0 && startY != 0) {
                canvas.drawLine((float) startX, (float) startY, (float) mBackTrack.xy_r_screen_x[i], (float) mBackTrack.xy_r_screen_y[i], paint);
            }
            startX = mBackTrack.xy_r_screen_x[i];
            startY = mBackTrack.xy_r_screen_y[i];

        }

        // 画锯齿
        double sawtooth;
        for (i = 0; i < BackTrack.SAWTOOTH_NUM; i++) // »­¾â³Ý
        {
			/*
			int l = mBackTrack.sawtooth_l_point[i];
			int r = mBackTrack.sawtooth_r_point[i];

			canvas.drawLine((float) mBackTrack.xy_l_screen_x[l],
					(float) mBackTrack.xy_l_screen_y[l],
					(float) mBackTrack.xy_r_screen_x[r],
					(float) mBackTrack.xy_r_screen_y[r], mPaint1);
*/
            // //

            if (i == 6) {

                int l = mBackTrack.sawtooth_l_point[i];
                int r = mBackTrack.sawtooth_r_point[i];

                canvas.drawLine((float) mBackTrack.xy_l_screen_x[l], (float) mBackTrack.xy_l_screen_y[l], (float) mBackTrack.xy_r_screen_x[r], (float) mBackTrack.xy_r_screen_y[r], mPaint1);

            } else {
                l_x = mBackTrack.xy_l_screen_x[mBackTrack.sawtooth_l_point[i]];
                l_y = mBackTrack.xy_l_screen_y[mBackTrack.sawtooth_l_point[i]];
                r_x = mBackTrack.xy_r_screen_x[mBackTrack.sawtooth_r_point[i]];
                r_y = mBackTrack.xy_r_screen_y[mBackTrack.sawtooth_r_point[i]];

                //Log.e(TAG, "l_y:" + l_y);

                tanx = (r_y - l_y) / (r_x - l_x);

                sawtooth = SAWTOOTH[i];

                if (i == 0 || i == 1) {
                    paint = mPaint3;
                } else if (i == 5) {
                    paint = mPaint1;
                } else {
                    paint = mPaint2;
                }

                if (i == 1 || i == 4) {
                    if (mAngle > 0) {
                        canvas.drawLine((float) (l_x - sawtooth), (float) (l_y - (tanx * sawtooth)), (float) (r_x + sawtooth), (float) (r_y + (tanx * sawtooth)), paint);
                    } else {
                        canvas.drawLine((float) (l_x + sawtooth), (float) (l_y + (tanx * sawtooth)), (float) (r_x - sawtooth), (float) (r_y - (tanx * sawtooth)), paint);
                    }

                } else {
                    if (mAngle > 0) {
                        canvas.drawLine((float) l_x, (float) l_y, (float) (l_x - sawtooth), (float) (l_y - (tanx * sawtooth)), paint);
                        canvas.drawLine((float) r_x, (float) r_y, (float) (r_x + sawtooth), (float) (r_y + (tanx * sawtooth)), paint);
                    } else {
                        canvas.drawLine((float) l_x, (float) l_y, (float) (l_x + sawtooth), (float) (l_y + (tanx * sawtooth)), paint);
                        canvas.drawLine((float) r_x, (float) r_y, (float) (r_x - sawtooth), (float) (r_y - (tanx * sawtooth)), paint);
                    }
                }
            }

        }

        //	Log.e(TAG, "d System.currentTimeMillis()" + System.currentTimeMillis());
    }

    public void doTrack(double angle) {
        if (angle < -30 || angle > 30) {
            return;
        }
        //-0.5~0.5 it show incorrect
        if (angle >= 0 && angle < 0.5) {
            angle = 0.5;
        } else if (angle < 0 && angle > -0.5) {
            angle = -0.5;
        }

        mAngle = angle;
        // Log.e(TAG, "System.currentTimeMillis()"+System.currentTimeMillis());
        mBackTrack.do_drack(angle);
        // Log.e(TAG, "System.currentTimeMillis()"+System.currentTimeMillis());
    }

    public int get_track_car_w() {
        return BackTrack.get_track_car_w();
    }

    public void set_track_car_w(int w) {
        BackTrack.set_track_car_w(w);
    }

    public int get_track_car_l() {
        return BackTrack.get_track_car_l();
    }

    public void set_track_car_l(int w) {
        BackTrack.set_track_car_l(w);
    }

    //	public int get_track_car_d()
    //	{
    //		return BackTrack.get_track_car_d();
    //	}
    public int get_track_camera_h() {
        return BackTrack.get_track_camera_h();
    }

    //	public void set_track_car_d(int w)
    //	{
    //		BackTrack.set_track_car_d(w);
    //	}
    public void set_track_camera_h(int w) {
        BackTrack.set_track_camera_h(w);
    }

    public double get_track_angle_2a() {
        return BackTrack.get_track_angle_2a();
    }

    public void set_track_angle_2a(double w) {
        BackTrack.set_track_angle_2a(w);
    }

    public double get_track_angle_b() {
        return BackTrack.get_track_angle_b();
    }

    public void set_track_angle_b(double w) {
        BackTrack.set_track_angle_b(w);
    }

    public void setParam() {

    }

    public void reFlash() {
        mBackTrack.do_drack(mAngle);
        invalidate();
    }

}
