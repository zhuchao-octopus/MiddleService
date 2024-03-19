package com.my.canbox;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RadarView2 extends ImageView {
	private byte[] mRadarDataFront = new byte[4];
	private byte[] mRadarDataBack = new byte[4];

	private int[] mRadarBackStartX = new int[4];
	private int[] mRadarBackStartY = new int[4];

	private int[] mRadarFrontStartX = new int[4];
	private int[] mRadarFrontStartY = new int[4];

	private final static int RADAR_BAR_NUM = 10;
	private final static int RADAR_BAR_INTERVAL = 10;
	private final static int RADAR_BAR_HEIGHT = 25;

	private Paint mPaint;

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int i;
		int r;
		// draw front
		for (i = 0; i < 4; ++i) {
			r = 3 - i;
			if (mRadarDataFront[r] <= Canbox.RADAR_DISTANCE_WANRING){
				mPaint.setColor(Color.RED);
			} else if (mRadarDataFront[r] <= Canbox.RADAR_DISTANCE_NORMAL){
				mPaint.setColor(Color.YELLOW);
			} else {
				mPaint.setColor(Color.GREEN);
			}
			
			switch (i) {
			case 0:
				for (int j = 0; j < mRadarDataFront[r]; ++j) {
					canvas.drawLine(mRadarFrontStartX[i] + RADAR_BAR_INTERVAL
							* (RADAR_BAR_NUM - j) + 3, mRadarBackStartY[i] - j,
							mRadarFrontStartX[i] + RADAR_BAR_INTERVAL
									* (RADAR_BAR_NUM - j), mRadarBackStartY[i]
									+ RADAR_BAR_HEIGHT - j, mPaint);
				}
				break;
			case 1:
			case 2:
				for (int j = 0; j < mRadarDataFront[r]; ++j) {
					canvas.drawLine(mRadarFrontStartX[i] + RADAR_BAR_INTERVAL
							* (RADAR_BAR_NUM - j), mRadarBackStartY[i],
							mRadarFrontStartX[i] + RADAR_BAR_INTERVAL
									* (RADAR_BAR_NUM - j), mRadarBackStartY[i]
									+ RADAR_BAR_HEIGHT, mPaint);
				}
				break;
			case 3:
				for (int j = 0; j < mRadarDataFront[r]; ++j) {
					canvas.drawLine(mRadarFrontStartX[i] + RADAR_BAR_INTERVAL
							* (RADAR_BAR_NUM - j), mRadarBackStartY[i] + j,
							mRadarFrontStartX[i] + RADAR_BAR_INTERVAL
									* (RADAR_BAR_NUM - j) + 3,
							mRadarBackStartY[i] + RADAR_BAR_HEIGHT + j, mPaint);
				}
				break;
			}
		}
		// draw back		
		for (i = 0; i < 4; ++i) {
			r = 3 - i;
			if (mRadarDataBack[r] <= Canbox.RADAR_DISTANCE_WANRING){
				mPaint.setColor(Color.RED);
			} else if (mRadarDataBack[r] <= Canbox.RADAR_DISTANCE_NORMAL){
				mPaint.setColor(Color.YELLOW);
			} else {
				mPaint.setColor(Color.GREEN);
			}
			
			switch (i) {
			case 0:
				for (int j = 0; j < mRadarDataBack[r]; ++j) {
					canvas.drawLine(mRadarBackStartX[i] + RADAR_BAR_INTERVAL
							* j - 3, mRadarBackStartY[i] - j,
							mRadarBackStartX[i] + RADAR_BAR_INTERVAL * j,
							mRadarBackStartY[i] + RADAR_BAR_HEIGHT - j, mPaint);
				}
				break;
			case 1:
			case 2:
				for (int j = 0; j < mRadarDataBack[r]; ++j) {
					canvas.drawLine(mRadarBackStartX[i] + RADAR_BAR_INTERVAL
							* j, mRadarBackStartY[i], mRadarBackStartX[i]
							+ RADAR_BAR_INTERVAL * j, mRadarBackStartY[i]
							+ RADAR_BAR_HEIGHT, mPaint);
				}
				break;
			case 3:
				for (int j = 0; j < mRadarDataBack[r]; ++j) {
					canvas.drawLine(mRadarBackStartX[i] + RADAR_BAR_INTERVAL
							* j, mRadarBackStartY[i] + j, mRadarBackStartX[i]
							+ RADAR_BAR_INTERVAL * j - 3, mRadarBackStartY[i]
							+ RADAR_BAR_HEIGHT + j, mPaint);
				}
				break;
			}

		}
	}

	public boolean setRadarData(byte[] radaData) {
		if (radaData.length < 8)
			return false;

		int i = 0;
		for (; i < 4; ++i) {
			mRadarDataBack[i] = radaData[i];
		}
		for (; i < 8; ++i) {
			mRadarDataFront[i - 4] = radaData[i];
		}

		return true;
	}

	private void initRadarData() {
		mPaint = new Paint();
		mPaint.setColor(Color.YELLOW);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(3);

		mRadarBackStartX[0] = 637;
		mRadarBackStartX[1] = 637;
		mRadarBackStartX[2] = 637;
		mRadarBackStartX[3] = 637;

		mRadarBackStartY[0] = 30;
		mRadarBackStartY[1] = 75;
		mRadarBackStartY[2] = 117;
		mRadarBackStartY[3] = 165;

		mRadarFrontStartX[0] = 20;
		mRadarFrontStartX[1] = 20;
		mRadarFrontStartX[2] = 20;
		mRadarFrontStartX[3] = 20;

		mRadarFrontStartY[0] = 30;
		mRadarFrontStartY[1] = 75;
		mRadarFrontStartY[2] = 117;
		mRadarFrontStartY[3] = 165;
		// tee
//		mRadarDataBack[0] = 10;
//		mRadarDataBack[3] = 10;
//		mRadarDataBack[1] = 10;
//		mRadarDataBack[2] = 10;
//
//		mRadarDataFront[0] = 10;
//		mRadarDataFront[1] = 5;
//		mRadarDataFront[2] = 5;
//		mRadarDataFront[3] = 10;
	}

	public RadarView2(Context context) {
		super(context);
		initRadarData();
	}

	public RadarView2(Context context, AttributeSet attrs) {

		super(context, attrs);
		initRadarData();
	}

	public RadarView2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initRadarData();
	}
};