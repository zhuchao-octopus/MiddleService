package com.zhuchao.android.car.canbox;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RadarView1 extends ImageView {

    private final static int RADAR_BAR_NUM = 10;
    private final static int RADAR_BAR_INTERVAL = 4;
    // private int[] mRadarFrontStartYEx = new int[2];
    private final static int RADAR_BAR_HEIGHT = 18;
    private final byte[] mRadarDataFrontEx = new byte[2];
    private final int[] mRadarFrontStartXEx = new int[2];
    private final byte[] mRadarDataFront = new byte[4];
    private final byte[] mRadarDataBack = new byte[4];
    private final int[] mRadarColorFront = new int[4];
    private final int[] mRadarColorBack = new int[4];
    private final int[] mRadarBackStartX = new int[4];
    private final int[] mRadarBackStartY = new int[4];
    private final int[] mRadarFrontStartX = new int[4];
    private final int[] mRadarFrontStartY = new int[4];
    private final byte[] mRadarDataLeft = new byte[4];
    private final byte[] mRadarDataRight = new byte[4];
    private final int[] mRadarColorLeft = new int[4];
    private final int[] mRadarColorRight = new int[4];
    private final int[] mRadarLeftRightStartY = new int[4];
    private boolean mLeftRightRadar = true; // golf7
    private int mRadarLeftStartX;// = new int[4];
    private int mRadarRightStartX;// = new int[4];
    private Paint mPaint;
    private boolean mUseCustomColor = false;
    private int mRadarNum = -1;

    public RadarView1(Context context) {
        super(context);
        initRadarData();
    }

    public RadarView1(Context context, AttributeSet attrs) {

        super(context, attrs);
        initRadarData();
    }

    public RadarView1(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initRadarData();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int i;
        // draw front
        for (i = 0; i < 4; ++i) {
            if (!mUseCustomColor) {
                if (mRadarDataFront[i] <= Canbox.RADAR_DISTANCE_WANRING) {
                    mPaint.setColor(Color.RED);
                } else if (mRadarDataFront[i] <= Canbox.RADAR_DISTANCE_NORMAL) {
                    mPaint.setColor(Color.YELLOW);
                } else {
                    mPaint.setColor(Color.GREEN);
                }
            } else {
                mPaint.setColor(mRadarColorFront[i]);
            }

            switch (i) {
                case 0:
                case 1:
                case 2:
                case 3:
                    for (int j = 0; j < mRadarDataFront[i]; ++j) {
                        canvas.drawLine(mRadarFrontStartX[i], mRadarFrontStartY[i] + RADAR_BAR_INTERVAL * (RADAR_BAR_NUM - j), mRadarFrontStartX[i] + RADAR_BAR_HEIGHT, mRadarFrontStartY[i] + RADAR_BAR_INTERVAL * (RADAR_BAR_NUM - j), mPaint);
                    }
                    break;
            }
        }
        // draw back
        for (i = 0; i < 4; ++i) {
            if (!mUseCustomColor) {
                if (mRadarDataBack[i] <= Canbox.RADAR_DISTANCE_WANRING) {
                    mPaint.setColor(Color.RED);
                } else if (mRadarDataBack[i] <= Canbox.RADAR_DISTANCE_NORMAL) {
                    mPaint.setColor(Color.YELLOW);
                } else {
                    mPaint.setColor(Color.GREEN);
                }
            } else {
                mPaint.setColor(mRadarColorBack[i]);
            }

            switch (i) {
                case 0:
                case 1:
                case 2:
                case 3:
                    for (int j = 0; j < mRadarDataBack[i]; ++j) {
                        canvas.drawLine(mRadarBackStartX[i], mRadarBackStartY[i] + RADAR_BAR_INTERVAL * j, mRadarBackStartX[i] + RADAR_BAR_HEIGHT, mRadarBackStartY[i] + RADAR_BAR_INTERVAL * j, mPaint);
                    }
                    break;
            }
        }

        // draw front ex
        int exInterVal = RADAR_BAR_INTERVAL;// -1;
        int yStart = mRadarFrontStartY[0];// +10;

        for (i = 0; i < 2; ++i) {
            if (mRadarDataFrontEx[i] <= Canbox.RADAR_DISTANCE_WANRING) {
                mPaint.setColor(Color.RED);
            } else if (mRadarDataFrontEx[i] <= Canbox.RADAR_DISTANCE_NORMAL) {
                mPaint.setColor(Color.YELLOW);
            } else {
                mPaint.setColor(Color.GREEN);
            }

            for (int j = 0; j < mRadarDataFrontEx[i]; ++j) {
                if (j > 9) {
                    break;
                }
                canvas.drawLine(mRadarFrontStartXEx[i], yStart + exInterVal * (RADAR_BAR_NUM - j), mRadarFrontStartXEx[i] + RADAR_BAR_HEIGHT, yStart + exInterVal * (RADAR_BAR_NUM - j), mPaint);
            }

        }

        exInterVal = RADAR_BAR_INTERVAL - 1;
        int barNum = 6;
        if (mLeftRightRadar) {
            for (i = 0; i < 4; ++i) {
                if (!mUseCustomColor) {
                    if (mRadarDataRight[i] <= Canbox.RADAR_DISTANCE_WANRING) {
                        mPaint.setColor(Color.RED);
                    } else if (mRadarDataRight[i] <= Canbox.RADAR_DISTANCE_NORMAL) {
                        mPaint.setColor(Color.YELLOW);
                    } else {
                        mPaint.setColor(Color.GREEN);
                    }
                } else {
                    mPaint.setColor(mRadarColorRight[i]);
                }

                switch (i) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        for (int j = 0; j < mRadarDataRight[i]; ++j) {
                            canvas.drawLine(mRadarRightStartX + exInterVal * j, mRadarLeftRightStartY[i], mRadarRightStartX + exInterVal * j, mRadarLeftRightStartY[i] + RADAR_BAR_HEIGHT,

                                    mPaint);
                        }
                        break;
                }
            }

            for (i = 0; i < 4; ++i) {
                if (!mUseCustomColor) {
                    if (mRadarDataLeft[i] <= Canbox.RADAR_DISTANCE_WANRING) {
                        mPaint.setColor(Color.RED);
                    } else if (mRadarDataLeft[i] <= Canbox.RADAR_DISTANCE_NORMAL) {
                        mPaint.setColor(Color.YELLOW);
                    } else {
                        mPaint.setColor(Color.GREEN);
                    }
                } else {
                    mPaint.setColor(mRadarColorLeft[i]);
                }

                switch (i) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:

                        for (int j = 0; j < mRadarDataLeft[i]; ++j) {
                            if (j > barNum) {
                                break;
                            }
                            canvas.drawLine(mRadarLeftStartX + exInterVal * (barNum - j), mRadarLeftRightStartY[i], mRadarLeftStartX + exInterVal * (barNum - j), mRadarLeftRightStartY[i] + RADAR_BAR_HEIGHT,

                                    mPaint);
                        }

                        break;
                }
            }
        }
    }

    public boolean setRadarData(byte[] radaData) {
        if (radaData.length < 8) return false;

        int i = 0;
        for (; i < 4; ++i) {
            mRadarDataBack[i] = radaData[i];
        }
        for (; i < 8; ++i) {
            mRadarDataFront[i - 4] = radaData[i];
        }

        return true;
    }

    public boolean setRadarDataFrontEx(byte[] radaData) {
        if (radaData.length < 2) return false;

        int i = 0;
        for (; i < 2; ++i) {
            mRadarDataFrontEx[i] = radaData[i];
        }
        return true;
    }

    public boolean setRadarColor(int[] radaData) {
        if (radaData == null) {
            mUseCustomColor = false;
        } else {
            mUseCustomColor = true;

            if (radaData.length < 8) return false;

            int i = 0;
            for (; i < 4; ++i) {
                mRadarColorBack[i] = radaData[i];
            }
            for (; i < 8; ++i) {
                mRadarColorFront[i - 4] = radaData[i];
            }
        }

        return true;
    }

    public boolean setRadarLeftColor(int[] radaData) {
        if (radaData == null) {
            mUseCustomColor = false;
        } else {
            mUseCustomColor = true;

            int i = 0;
            for (; i < radaData.length; ++i) {
                mRadarColorLeft[i] = radaData[i];
            }
        }

        return true;
    }

    public boolean setRadarLeft(byte[] radaData) {
        if (radaData.length < 4) return false;

        int i = 0;
        mLeftRightRadar = true;
        for (; i < radaData.length; ++i) {
            mRadarDataLeft[i] = radaData[i];
        }
        return true;
    }

    public boolean setRadarRightColor(int[] radaData) {
        if (radaData == null) {
            mUseCustomColor = false;
        } else {
            mUseCustomColor = true;

            int i = 0;
            for (; i < radaData.length; ++i) {
                mRadarColorRight[i] = radaData[i];
            }
        }

        return true;
    }

    public boolean setRadarRight(byte[] radaData) {
        if (radaData.length < 4) return false;

        int i = 0;
        mLeftRightRadar = true;
        for (; i < radaData.length; ++i) {
            mRadarDataRight[i] = radaData[i];
        }
        return true;
    }

    public void setRadarNum(int num) { //dafault is 4
        if (mRadarNum == num) {
            return;
        }
        mRadarNum = num;
        if (num == 3) {
            mRadarBackStartX[0] = 47;
            mRadarBackStartX[1] = 80;
            mRadarBackStartX[2] = 80;
            mRadarBackStartX[3] = 112;

            mRadarBackStartY[0] = 340;
            mRadarBackStartY[1] = 340;
            mRadarBackStartY[2] = 340;
            mRadarBackStartY[3] = 340;

            mRadarFrontStartX[0] = 47;
            mRadarFrontStartX[1] = 80;
            mRadarFrontStartX[2] = 80;
            mRadarFrontStartX[3] = 112;

            mRadarFrontStartY[0] = 26;
            mRadarFrontStartY[1] = 26;
            mRadarFrontStartY[2] = 26;
            mRadarFrontStartY[3] = 26;
        } else {
            mRadarBackStartX[0] = 42;
            mRadarBackStartX[1] = 67;
            mRadarBackStartX[2] = 92;
            mRadarBackStartX[3] = 117;

            mRadarBackStartY[0] = 340;
            mRadarBackStartY[1] = 340;
            mRadarBackStartY[2] = 340;
            mRadarBackStartY[3] = 340;

            mRadarFrontStartX[0] = 42;
            mRadarFrontStartX[1] = 67;
            mRadarFrontStartX[2] = 92;
            mRadarFrontStartX[3] = 117;

            mRadarFrontStartY[0] = 26;
            mRadarFrontStartY[1] = 26;
            mRadarFrontStartY[2] = 26;
            mRadarFrontStartY[3] = 26;
        }
    }

    private void initRadarData() {
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(2);

        setRadarNum(0);

        mRadarFrontStartXEx[0] = 19;
        mRadarFrontStartXEx[1] = 140;

        mRadarLeftRightStartY[0] = 120;
        mRadarLeftRightStartY[1] = 170;
        mRadarLeftRightStartY[2] = 220;
        mRadarLeftRightStartY[3] = 270;

        mRadarLeftStartX = 13;
        mRadarRightStartX = 145;

        // test
        // mRadarDataBack[0] = 10;
        // mRadarDataBack[3] = 10;
        // mRadarDataBack[1] = 10;
        // mRadarDataBack[2] = 10;
        //
        // mRadarDataFront[0] = 10;
        // mRadarDataFront[1] = 5;
        // mRadarDataFront[2] = 5;
        // mRadarDataFront[3] = 10;
        //
        //
        //
        // mRadarDataLeft[0] = 1;
        // mRadarDataLeft[1] = 2;
        // mRadarDataLeft[2] = 4;
        // mRadarDataLeft[3] = 6;
        //
        // mRadarDataRight[0] = 6;
        // mRadarDataRight[1] = 6;
        // mRadarDataRight[2] = 3;
        // mRadarDataRight[3] = 4;
    }
}