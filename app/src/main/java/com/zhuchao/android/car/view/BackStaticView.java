package com.zhuchao.android.car.view;

import com.zhuchao.android.car.hardware.BackTrack;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class BackStaticView extends View {

	private static final String TAG = "BackStaticView";

	public BackStaticView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private final static int PAINT_WIDTH = 6;

	private BackTrack mBackTrack;
	private Paint mPaint1;
	private Paint mPaint2;
	private Paint mPaint3;

	private double[] xy_l_screen_x;
	private double[] xy_l_screen_y;

	private double[] xy_r_screen_x;
	private double[] xy_r_screen_y;

	private int[] sawtooth_l_point;
	private int[] sawtooth_r_point;

	private void init() {
		mBackTrack = new BackTrack();
		mBackTrack.init();
		mBackTrack.do_drack(0.5);

		xy_l_screen_x = mBackTrack.xy_l_screen_x;
		xy_l_screen_y = mBackTrack.xy_l_screen_y;
		xy_r_screen_x = mBackTrack.xy_r_screen_x;
		xy_r_screen_y = mBackTrack.xy_r_screen_y;

		sawtooth_l_point = mBackTrack.sawtooth_l_point;
		sawtooth_r_point = mBackTrack.sawtooth_r_point;

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

	private final static int UP_Y = 0;

	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);

		Paint paint = mPaint1;

//		Log.e(TAG, "d System.currentTimeMillis()" + System.currentTimeMillis());

		int i;

		double startX = 0;
		double startY = 0;
		double l_x, l_y, r_x, r_y;
		double tanx;
		for (i = 0; i < BackTrack.show_l_num; i++) {
			if (xy_l_screen_x[i] <= 0 || xy_l_screen_y[i] <= 0) {
				continue;
			}

			if (i <= sawtooth_l_point[0]) {
				paint = mPaint3;
			} else if (i >= sawtooth_l_point[2] && i <= sawtooth_l_point[3]) {
				paint = mPaint2;
			} else if (i >= sawtooth_l_point[5] && i <= sawtooth_l_point[6]) {
				paint = mPaint1;
			} else {
				startX = 0;
				startY = 0;
				continue;
			}

			if (startX != 0 && startY != 0) {
				canvas.drawLine((float) startX, (float) startY,
						(float) xy_l_screen_x[i], (float) xy_l_screen_y[i],
						paint);
			}
			startX = xy_l_screen_x[i];
			startY = xy_l_screen_y[i];

		}
		startX = 0;
		startY = 0;
		for (i = 0; i < BackTrack.show_r_num; i++) {
			if (xy_r_screen_x[i] <= 0 || xy_r_screen_y[i] <= 0) {
				continue;
			}

			if (i <= sawtooth_r_point[0]) {
				paint = mPaint3;
			} else if (i >= sawtooth_r_point[2] && i <= sawtooth_r_point[3]) {
				paint = mPaint2;
			} else if (i >= sawtooth_r_point[5] && i <= sawtooth_r_point[6]) {
				paint = mPaint1;
			} else {
				startX = 0;
				startY = 0;
				continue;
			}

			if (startX != 0 && startY != 0) {
				canvas.drawLine((float) startX, (float) startY,
						(float) xy_r_screen_x[i], (float) xy_r_screen_y[i],
						paint);
			}
			startX = xy_r_screen_x[i];
			startY = xy_r_screen_y[i];

		}

		// 画锯齿
		double sawtooth;
		for (i = 0; i < BackTrack.SAWTOOTH_NUM; i++) // »­¾â³Ý
		{
			/*
			 * int l = sawtooth_l_point[i]; int r = sawtooth_r_point[i];
			 * 
			 * canvas.drawLine((float) xy_l_screen_x[l], (float)
			 * xy_l_screen_y[l], (float) xy_r_screen_x[r], (float)
			 * xy_r_screen_y[r], mPaint1);
			 */
			// //

			if (i == 6) {

				int l = sawtooth_l_point[i];
				int r = sawtooth_r_point[i];

				canvas.drawLine((float) xy_l_screen_x[l],
						(float) xy_l_screen_y[l], (float) xy_r_screen_x[r],
						(float) xy_r_screen_y[r], mPaint1);

			} else {
				l_x = xy_l_screen_x[sawtooth_l_point[i]];
				l_y = xy_l_screen_y[sawtooth_l_point[i]];
				r_x = xy_r_screen_x[sawtooth_r_point[i]];
				r_y = xy_r_screen_y[sawtooth_r_point[i]];

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
					// if (mAngle > 0) {
					// canvas.drawLine((float) (l_x - sawtooth),
					// (float) (l_y - (tanx * sawtooth)),
					// (float) (r_x + sawtooth),
					// (float) (r_y + (tanx * sawtooth)), paint);
					// } else {
					// canvas.drawLine((float) (l_x + sawtooth),
					// (float) (l_y + (tanx * sawtooth)),
					// (float) (r_x - sawtooth),
					// (float) (r_y - (tanx * sawtooth)), paint);
					// }

				} else {
					// Cif (mAngle > 0) {
					canvas.drawLine((float) l_x, (float) l_y,
							(float) (l_x - sawtooth),
							(float) (l_y - (tanx * sawtooth)), paint);
					canvas.drawLine((float) r_x, (float) r_y,
							(float) (r_x + sawtooth),
							(float) (r_y + (tanx * sawtooth)), paint);
					// } else {
					// canvas.drawLine((float) l_x, (float) l_y,
					// (float) (l_x + sawtooth),
					// (float) (l_y + (tanx * sawtooth)), paint);
					// canvas.drawLine((float) r_x, (float) r_y,
					// (float) (r_x - sawtooth),
					// (float) (r_y - (tanx * sawtooth)), paint);
					// }
				}
			}

		}

		Log.e(TAG, "d System.currentTimeMillis()" + System.currentTimeMillis());
	}

	public void reFlash(){
		mBackTrack.do_drack(0.5);
		invalidate();
	}
	
	private final static double[] SAWTOOTH = new double[] { 35, 35, 30, 20, 18,
			15 };
}
