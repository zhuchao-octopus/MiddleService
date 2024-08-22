package com.zhuchao.android.car.manager.key;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.common.utils.decode.JavaDecode;

/**
 * 处理触摸按键，形成各种事件
 *
 * @author sky
 */
public class TouchKeyEvent {

    private static final String TAG = "TouchKeyEvent";

    // 处理长按事件
    private final static long LONG_PRESS_TIME = 5;// 50毫秒
    private final static long BEGIN_LONG_PRESS_TIME = 500;// 毫秒
    private final static long SECOND_2 = 1000;
    private boolean triggerSecond2 = false;
    private final static long SECOND_5 = 5000;
    private boolean triggerSecond5 = false;
    private final static long SECOND_8 = 6000;
    private boolean triggerSecond8 = false;
    private boolean triggerSlide = false;

    // 处理单击与双击事件
    private final static int MSG_CLICK = 0;

    // 记录基础数据
    private Action mCurrentAction = Action.UP;
    private int x, y, startX, startY, endX, endY, minVolidX, minVolidY, maxVolidX, maxVolidY, slideX, slideY;
    private long startTime, endTime, tmpTime;
    /**
     * 区域边长的一半
     */
    //	private final static int TouchKeyProcessor.HALF_SIDE = TouchKeyProcessor.TouchKeyProcessor.HALF_SIDE;

    public TouchKeyProcessor mTouchKeyProcessor;

    private enum Action {
        UP, // 抬起
        DOWN, // 压下
        MOVE, // 移动
    }

    private enum Event {
        /**
         * 单击
         */
        CLICK,
        /**
         * 长按,每LONG_PRESS_TIME毫秒发一次
         */
        LONG_PRESS, LONG_CLICK, SLIDE, // 划动
        QUICK_SLIDE,// 快速划动
        // /**长按5秒*/
        // LONG_CLICK_5S,
        // /**长按8秒*/
        LONG_CLICK_8S,
        // /**双击*/
        // DOUBLECLICK,
        // /**向上划动*/
        // SLIDER_UP,
        // /**向下划动*/
        // SLIDER_DOWN,
        // /**向左划动*/
        // SLIDER_LEFT,
        // /**向右划动*/
        // SLIDER_RIGHT
    }

    private int mStartTouch = START_TOUCH_INIT;
    private final static int START_TOUCH_INIT = 0;
    private final static int START_TOUCH_DOWN = 1;
    private final static int START_TOUCH_END_LONG_PRESS = 2;
    private final static int START_TOUCH_END_LONG_CLICK = 3;

    public TouchKeyEvent(Context c) {
        mTouchKeyProcessor = new TouchKeyProcessor(c);
    }

    public boolean isExistStudyData() {
        return mTouchKeyProcessor.isExistStudyData();
    }

    public void process(byte[] protocol) {
        x = JavaDecode.byteArrToInt(protocol, 2, 4, true);
        y = JavaDecode.byteArrToInt(protocol, 6, 4, true);
        boolean isDown = protocol[10] == 1;
        //		Log.d("allen", "x : " + x + " y : " + y + ", isDown:" + isDown);

        if (isDown) {
            if (mStartTouch == START_TOUCH_INIT) {
                mStartTouch = START_TOUCH_DOWN;
            }
            if (mStartTouch == START_TOUCH_DOWN) {
                processDown();
            } else if (mStartTouch == START_TOUCH_END_LONG_CLICK) {
                processDownFor8s();
            }
        } else {
            if (mStartTouch == START_TOUCH_END_LONG_PRESS) {
                stopSendLongPress();
            }
            mStartTouch = START_TOUCH_INIT;
            processUp();
        }
    }

    private void processUp() {
        if (mCurrentAction == Action.UP) {
            // do nothing
        } else {
            // LOG.print("---up---");
            mCurrentAction = Action.UP;
            endX = x;
            endY = y;
            tmpTime = System.currentTimeMillis() - startTime;

            slideX = Math.abs(endX - startX);
            slideY = Math.abs(endY - startY);
            if (slideX > 2 * TouchKeyProcessor.HALF_SIDE || slideY > 2 * TouchKeyProcessor.HALF_SIDE) {
                if (tmpTime < 500) {
                    doKeyResult(Event.QUICK_SLIDE);
                    return;
                }
            }
            long seconde2 = SECOND_2;
            if (mTouchKeyProcessor.isNeedLongPress(endX, endY)) {
                seconde2 = BEGIN_LONG_PRESS_TIME;
            }
            if (tmpTime < seconde2 && isVolidPoint()) {// 点击事件形成
                doKeyResult(Event.CLICK);
            }
        }
    }

    private void processDown() {
        //		Log.d(TAG, "---down---");
        if (mCurrentAction == Action.DOWN) {
            mCurrentAction = Action.MOVE;
        } else if (mCurrentAction == Action.MOVE) {
            // LOG.print("---move---");
            endX = x;
            endY = y;
            tmpTime = System.currentTimeMillis() - startTime;
            if (mTouchKeyProcessor.isNeedLongPress(endX, endY)) {
                if (tmpTime > BEGIN_LONG_PRESS_TIME) {
                    if (isVolidPoint() && mStartTouch != START_TOUCH_END_LONG_PRESS) {
                        mStartTouch = START_TOUCH_END_LONG_PRESS;
                        doKeyResult(Event.LONG_PRESS);
                    }
                }
            } else {
                if (tmpTime > SECOND_2 && !triggerSecond2) {// 长按事件形成
                    // LOG.print("---2s---");
                    triggerSecond2 = true;
                    if (isVolidPoint()) {// 判断长按是否有效
                        mStartTouch = START_TOUCH_END_LONG_CLICK;
                        doKeyResult(Event.LONG_CLICK);
                        return;
                    }
                }
            }


            // 划动的距离
            slideX = Math.abs(endX - startX);
            slideY = Math.abs(endY - startY);
            if (slideX > 2 * TouchKeyProcessor.HALF_SIDE || slideY > 2 * TouchKeyProcessor.HALF_SIDE) {
                if (tmpTime > 500) {
                    triggerSlide = true;
                    doKeyResult(Event.SLIDE);
                }
            }

        } else {
            mCurrentAction = Action.DOWN;
            startX = x;
            startY = y;
            minVolidX = startX - TouchKeyProcessor.HALF_SIDE;
            minVolidY = startY - TouchKeyProcessor.HALF_SIDE;
            maxVolidX = startX + TouchKeyProcessor.HALF_SIDE;
            maxVolidY = startY + TouchKeyProcessor.HALF_SIDE;
            triggerSecond2 = false;
            triggerSecond5 = false;
            triggerSecond8 = false;
            triggerSlide = false;
            startTime = System.currentTimeMillis();
            // if(haveFirstClick){//如果已经触发了一次点击，判断看是否是双击
            // if(startTime - firstClickTime < DOUBLE_CLICK_SPLIT){//有可能会形成双击事件
            // mHandle.removeMessages(MSG_CLICK);//取消上报单击事件
            // }
            // }
        }
    }

    private void processDownFor8s() {
        // Log.d(TAG, "---down---");
        if (mCurrentAction == Action.MOVE) {
            // LOG.print("---move---");
            endX = x;
            endY = y;
            tmpTime = System.currentTimeMillis() - startTime;

            if (tmpTime > SECOND_8 && !triggerSecond8) {// 长按事件形成
                // LOG.print("---2s---");
                triggerSecond8 = true;
                if (isVolidPoint()) {// 判断长按是否有效
                    mStartTouch = START_TOUCH_END_LONG_CLICK;
                    doKeyResult(Event.LONG_CLICK_8S);
                }
            }
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    startSendLongPress(msg.arg1, msg.arg2);
                    break;
                case 1:
                    startSendLongPressFixKey(msg.arg1);
                    break;
            }
        }
    };

    private int mRollMaxTime = 0;
    private final static int ROLL_MAX_TIME = 60;
    private final static int LONG_PRESS_INT_TIME = 50;

    private void startSendLongPress(int x, int y) {
        //		Log.d("dd", "startSendLongPress"+mRollMaxTime);
        mRollMaxTime++;
        mTouchKeyProcessor.onLongPress(x, y);
        long l = SystemClock.uptimeMillis();
        if (mRollMaxTime < ROLL_MAX_TIME) {
            mHandler.sendMessageAtTime(mHandler.obtainMessage(0, x, y), LONG_PRESS_INT_TIME + l);
        }
    }

    private void startSendLongPressFixKey(int key) {
        // Log.d("dd", "startSendLongPress"+mRollMaxTime);
        mRollMaxTime++;

        mTouchKeyProcessor.onSendFixKey((byte) key, true);

        long l = SystemClock.uptimeMillis();
        if (mRollMaxTime < ROLL_MAX_TIME) {
            mHandler.sendMessageAtTime(mHandler.obtainMessage(1, key, 0), LONG_PRESS_INT_TIME + l);
        }

    }

    private void stopSendLongPress() {
        mHandler.removeMessages(0);
    }

    private void doKeyResult(Event event) {
        //		Log.d("allen", "event:" + event + "x:" + endX + "y:" + endY);
        if (mTouchKeyProcessor != null) {
            switch (event) {
                case CLICK:
                    //				showBeep();
                    mTouchKeyProcessor.onClick(endX, endY);
                    break;
                case LONG_PRESS:
                    mRollMaxTime = 0;
                    Util.setBeepToMcu();
                    startSendLongPress(endX, endY);
                    break;
                case LONG_CLICK:
                    //				showBeep();
                    mTouchKeyProcessor.onLongClick(endX, endY);
                    break;
                case LONG_CLICK_8S:
                    //				showBeep();
                    mTouchKeyProcessor.onLongClick8s(endX, endY);
                    break;
                case SLIDE:
                    mTouchKeyProcessor.onSlide(startX, startY, endX, endY);
                    break;
                case QUICK_SLIDE:
                    mTouchKeyProcessor.onQueckSlide(startX, startY, endX, endY, tmpTime);
                    break;
                default:
                    break;
            }
        }
    }

    private boolean isVolidPoint() {
        return endX > minVolidX && endY > minVolidY && endX < maxVolidX && endY < maxVolidY && !triggerSlide;
    }

    //touch fix key


    private byte mKey = 0;
    private long mStartTime = 0;
    private boolean mLongPress = false;

    private final static int LONG_PRESS_FOR_POWER = 3000;

    public byte processTouchFixKey(byte[] protocol) {
        byte key = 0;
        if (protocol.length > 2) {
            if (protocol[3] == 1) { // down
                if (mStartTime == 0) {
                    mStartTime = SystemClock.uptimeMillis();
                    mKey = protocol[2];
                } else {
                    long pressTime = SystemClock.uptimeMillis() - mStartTime;
                    if (pressTime > BEGIN_LONG_PRESS_TIME && !mLongPress) {
                        if (mKey == protocol[2]) {
                            key = protocol[2];
                            mLongPress = true;
                            mRollMaxTime = 0;
                            if (key == MyCmd.Keycode.VOLUME_DOWN || key == MyCmd.Keycode.VOLUME_UP) {
                                Util.setBeepToMcu();
                                startSendLongPressFixKey(key);
                            }
                        }
                    }

                    if (pressTime > LONG_PRESS_FOR_POWER) {
                        if (isSupportPowerKey(protocol[2])) {
                            key = protocol[2];
                            Util.setBeepToMcu();
                            mTouchKeyProcessor.onSendFixKey(key, mLongPress);
                            clear();
                            mStartTime = Integer.MAX_VALUE;
                        }
                    }
                }
            } else {
                if (mKey != 0 && mKey == protocol[2]) {
                    key = protocol[2];

                    Util.setBeepToMcu();

                    mTouchKeyProcessor.onSendFixKey(key, mLongPress);

                }
                clear();
            }
        }
        return key;
    }

    private boolean isSupportPowerKey(int key) {
        return key == MyCmd.Keycode.MULT_MUTE_AND_POWER;
    }

    private void clear() {
        mKey = 0;
        mStartTime = 0;
        if (mLongPress) {
            mRollMaxTime = 0;
            mLongPress = false;
            mHandler.removeMessages(1);
        }
    }
}
