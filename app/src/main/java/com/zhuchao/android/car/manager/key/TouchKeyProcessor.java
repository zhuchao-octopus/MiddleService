package com.zhuchao.android.car.manager.key;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;

import com.common.utils.MachineConfig;
import com.common.utils.MyCmd;
import com.common.utils.Util;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.manager.McuManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TouchKeyProcessor {

    private final static String TAG = "TouchKeyProcessor";

    private final Context mContext;

    private IKeyCallback mKeyCallback;

    /**
     * 是否正在学习触摸按键
     */
    private boolean isTouchKeyStudy = false;
    /**
     * 当前学习的触摸按键键值
     */
    private int mCurrentStudyTouchKeycode;

    /**
     * 已经学习的触摸按键
     */
    // private byte[] mAlreadyStudyTouchKeys = new byte[20];
    // private List<Byte> mAlreadyStudyTouchKeys = new ArrayList<Byte>();

    static class Keys {
        public int mKey;
        public Rect mRt;

        public Keys(int key, Rect rt) {
            mKey = key;
            mRt = rt;
        }
    }

    public boolean isExistStudyData() {
        return mKeyMappingMap.size() > 0;
    }

    private final ArrayList<Keys> mKeyMappingMap = new ArrayList<Keys>();
    private final ArrayList<Keys> mKeyMappingMapForStudy = new ArrayList<Keys>();

    public final static int HALF_MIN_SIDE = 10;
    public final static int HALF_MAX_SIDE = 20;

    public static int HALF_SIDE = 10;
    public static int HALF_CURRENT_SIDE = HALF_MIN_SIDE;

    private final static int LONG_CLICK_MOVE_4K = 1024 * 4;

    private final static String TOUCH_KEY_MAPPING_FILE = MachineConfig.VENDOR_DIR + ".touch_key_mapping";
    // private final static String DEFAULT_TOUCH_KEY_MAPPING_FILE =
    // "touch_key_mapping.cfg";
    private final File mMappingFile;
    private final McuManager mMcuManager;

    public TouchKeyProcessor(Context c) {
        Util.sudoExec("chmod:666:" + TOUCH_KEY_MAPPING_FILE);
        mMappingFile = new File(TOUCH_KEY_MAPPING_FILE);
        mContext = c;
        initKeyMappingMap();
        mMcuManager = McuManager.getInstance(null);

    }

    Rect mRectStudy;
    int mPreIndex = -1;

    int mPreClickX;
    int mPreClickY;

    public void onClick(int x, int y) {
        // LOG.print("---onClick---isTouchKeyStudy = " + isTouchKeyStudy);
        if (isTouchKeyStudy) {
            Integer keycode = getMappingKeycodeStudy(x, y);

            Rect rt = null;
            if (keycode == 0) {
                mRectStudy = new Rect(x - HALF_SIDE, y - HALF_SIDE, x + HALF_SIDE, y + HALF_SIDE);

                rt = mRectStudy;

                mPreIndex = -1;
            } else {
                mPreIndex = getIndexKeycodeStudy(x, y);
                // if (mPreIndex == index) {
                mRectStudy = new Rect(x - HALF_SIDE, y - HALF_SIDE, x + HALF_SIDE, y + HALF_SIDE);

                mPreClickX = x;
                mPreClickY = y;
                rt = mRectStudy;

                // } else {
                // mPreIndex = index;
                rt = mKeyMappingMapForStudy.get(mPreIndex).mRt;
                // }
            }
            sendStudyMsg(MyCmd.Cmd.TOUCH_STUDY_KEY, keycode, mPreIndex, rt);
            // mPreIndex = keycode;
        } else {

            Integer keycode = getMappingKeycode(x, y);
            int key = (keycode & 0xff);
            if (key != 0) {
                Util.setBeepToMcu();
                if (mMcuManager.mPowerOffFate) {
                    int longKey = ((keycode & 0xff00) >> 8);
                    if (longKey == MyCmd.Keycode.POWER) {
                        key = longKey;
                    }
                }
                mMcuManager.doKey(key);
            }
        }
    }


    public void onLongClick(int x, int y) {// 长按的时候，统一加4k
        // LOG.print("---onLongClick---");
        if (isTouchKeyStudy) {
            Rect rect = new Rect(x - HALF_SIDE + LONG_CLICK_MOVE_4K, y - HALF_SIDE + LONG_CLICK_MOVE_4K, x + HALF_SIDE + LONG_CLICK_MOVE_4K, y + HALF_SIDE + LONG_CLICK_MOVE_4K);
            if (mCurrentStudyTouchKeycode != MyCmd.Keycode.NONE) {
                // mKeyMappingMap.put(mCurrentStudyTouchKeycode, rect);
                // if (mKeyCallback != null) {
                // mKeyCallback
                // .onGetTouchStudyKeycode(mCurrentStudyTouchKeycode);
                // }
                // TODO 通知已经学习
                notifyAlreadyStudyTouchKeys();
            }
        } else {
            Integer keycode = getMappingKeycode(x, y);
            if ((keycode & 0xff00) != 0) {
                Util.setBeepToMcu();
                mMcuManager.doKey(((keycode & 0xff00) >> 8));
            } else {
                int key = (keycode & 0xff);
                if (key == MyCmd.Keycode.IXB_360_DISPLAY) {
                    Util.setBeepToMcu();
                    key = MyCmd.Keycode.DARK;
                    mMcuManager.doKey(key);
                }
            }
        }
    }

    public void onLongClick8s(int x, int y) {// 长按的时候，统一加4k
        // LOG.print("---onLongClick---");
        if (isTouchKeyStudy) {
            Rect rect = new Rect(x - HALF_SIDE + LONG_CLICK_MOVE_4K, y - HALF_SIDE + LONG_CLICK_MOVE_4K, x + HALF_SIDE + LONG_CLICK_MOVE_4K, y + HALF_SIDE + LONG_CLICK_MOVE_4K);
            if (mCurrentStudyTouchKeycode != MyCmd.Keycode.NONE) {
                // mKeyMappingMap.put(mCurrentStudyTouchKeycode, rect);
                // if (mKeyCallback != null) {
                // mKeyCallback
                // .onGetTouchStudyKeycode(mCurrentStudyTouchKeycode);
                // }
                // TODO 通知已经学习
                notifyAlreadyStudyTouchKeys();
            }
        } else {
            Integer keycode = getMappingKeycode(x, y);

            int key = (keycode & 0xff);
            if (key == MyCmd.Keycode.EJECT) {
                mMcuManager.doKey(MyCmd.Keycode.DVD_FORCE_EJECT);
            }

        }
    }

    public boolean isNeedLongPress(int x, int y) {
        if (!isTouchKeyStudy) {
            Integer keycode = getMappingKeycode(x, y);
            return (keycode & 0xff) == MyCmd.Keycode.VOLUME_DOWN || (keycode & 0xff) == MyCmd.Keycode.VOLUME_UP || ((keycode & 0xff00) >> 8) == MyCmd.Keycode.VOLUME_DOWN || ((keycode & 0xff00) >> 8) == MyCmd.Keycode.VOLUME_UP;
        }
        return false;
    }

    public void onLongPress(int x, int y) {
        // LOG.print("---onLongPress---");
        if (isTouchKeyStudy) {

        } else {

            Integer keycode = getMappingKeycode(x, y);
            if ((keycode & 0xff) == MyCmd.Keycode.VOLUME_DOWN || (keycode & 0xff) == MyCmd.Keycode.VOLUME_UP) {
                mMcuManager.doKey(((keycode & 0xff)));
            } else if (((keycode & 0xff00) >> 8) == MyCmd.Keycode.VOLUME_DOWN || ((keycode & 0xff00) >> 8) == MyCmd.Keycode.VOLUME_UP) {
                mMcuManager.doKey(((keycode & 0xff00) >> 8));
            }

            // mMcuManager.doKey((byte) ((keycode & 0xff00) >> 8));

            // if (keycode != MyCmd.Keycode.NONE) {
            // // TODO 做相应按键处理
            //			switch (keycode) {
            //			case (int)MyCmd.Keycode.VOLUME_DOWN:
            //				setVolume(false);
            //				break;
            //			case MyCmd.Keycode.VOLUME_UP:
            //				setVolume(true);
            //				break;
            //			}

        }
    }


    public void onSendFixKey(byte key1, boolean longClick) {

        //		if(GlobalDef.mTouchKeyType == 0){
        //			if(!longClick && MyCmd.Keycode.POWER == key && !mMcuManager.mPowerOffFate){
        //				key = MyCmd.Keycode.MUTE;
        //			}
        //		}
        int key = (key1 & 0xff);

        Log.d(TAG, GlobalDefinition.mTouchKeyType + ":onSendFixKey:" + key);

        if (key == MyCmd.Keycode.MULT_MUTE_AND_POWER) {
            if (!longClick && !mMcuManager.mPowerOffFate) {
                key = MyCmd.Keycode.MUTE;
            } else {
                key = MyCmd.Keycode.POWER;
            }
        } else if (key == MyCmd.Keycode.MULT_BACK_AND_HOME) {
            if (!longClick && !mMcuManager.mPowerOffFate) {
                key = MyCmd.Keycode.BACK;
            } else {
                key = MyCmd.Keycode.HOME;
            }
        } else if (key == MyCmd.Keycode.IXB_360_DISPLAY) {
            if (longClick) {
                key = MyCmd.Keycode.DARK;
            }
        }

        mMcuManager.doKey(key & 0xff);
    }

    public void onSlide(int startX, int startY, int endX, int endY) {
        // 这里假设触摸区域只可能在上下左，不兼容在右边
        // LOG.print("---------------onSlide");
    }

    public void onQueckSlide(int startX, int startY, int endX, int endY, long time) {
        // 这里假设触摸区域只可能在上下左，不兼容在右边
        processVolume(startX, startY, endX, endY, time);// 声音处理

    }

    private void processVolume(int startX, int startY, int endX, int endY, long time) {
        // if (mContext != null) {
        // BroadcastUtil.showVolumeBar(mContext, true);
        // }
        if (time / COEFFICIENT == 0) return;
        int slideX = endX - startX;
        int slideY = endY - startY;
        int absX = Math.abs(slideX);
        int absY = Math.abs(slideY);
        long speed = 0;// 速度
        if (absX > absY) {// 左右
            speed = absX / (time / COEFFICIENT);
            // 向右
            // 向左
            asyncSetVolume((int) speed, slideX < 0);
        } else {// 上下
            speed = absY / (time / COEFFICIENT);
            // 向上
            // 向下
            asyncSetVolume((int) speed, slideY < 0);
        }
    }

    /**
     * 调节系数，系数越大，速度越大
     */
    private final static int COEFFICIENT = 10;
    private final static int DURATION_TIME = 1000;
    private final static int MAX_TIMES = 20;

    /**
     * 异步设置时间
     */
    private void asyncSetVolume(final int speed, final boolean isUp) {
        if (speed == 0) {
            return;
        }
        final int sleepTime = DURATION_TIME / speed;
        // LOG.print("speed = " + speed + ", allTimes " + sleepTime +
        // ", isUp = " + isUp);
        new Thread(new Runnable() {

            @Override
            public void run() {
                int times = 0;
                while (true) {
                    try {
                        Thread.sleep(sleepTime);
                        //						setVolume(isUp);
                        times++;
                        if (times >= MAX_TIMES || times >= speed) {
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    public void setTouchCurrentStudyKeycode(int keycode) {
        // LOG.print("---setTouchCurrentStudyKeycode---keycode = " + keycode);
        if (isTouchKeyStudy) {
            if (mRectStudy != null) {

                Keys k;

                if (mPreIndex == -1) {// new
                    k = new Keys(keycode, mRectStudy);
                    mKeyMappingMapForStudy.add(k);
                } else {
                    k = mKeyMappingMapForStudy.get(mPreIndex);
                    k.mKey = keycode;
                    k.mRt = mRectStudy;
                }

                mPreIndex = -1;
                // removeMappingKeycodeStudy(this.mPreClickX, mPreClickY);
                //
                // mKeyMappingMapForStudy.put(keycode, mRectStudy);
            }
        }
        // else {
        // mCurrentStudyTouchKeycode = MyCmd.Keycode.NONE;
        // }
    }

    public void setTouchCurrentMode(boolean isStudy) {
        // LOG.print("---setTouchCurrentMode---isStudy = " + isStudy);
        isTouchKeyStudy = isStudy;

        if (isStudy) {
            HALF_SIDE = HALF_MIN_SIDE;
            mKeyMappingMapForStudy.clear();

            mKeyMappingMapForStudy.addAll(mKeyMappingMap);

            mRectStudy = null;
            mPreIndex = -1;

            mPreClickX = 0;
            mPreClickY = 0;

            sendStudyMsg(MyCmd.Cmd.TOUCH_STUDY_START);
        } else {

            HALF_SIDE = HALF_CURRENT_SIDE;
            sendStudyMsg(MyCmd.Cmd.TOUCH_STUDY_END);

        }
    }

    private boolean is2Rect(Rect r1, Rect r2) {
        int nMaxLeft = 0;
        int nMaxTop = 0;
        int nMinRight = 0;
        int nMinBottom = 0;

        // 计算两矩形可能的相交矩形的边界
        nMaxLeft = r1.left >= r2.left ? r1.left : r2.left;
        nMaxTop = r1.top >= r2.top ? r1.top : r2.top;
        nMinRight = (r1.left + r1.width()) <= (r2.left + r2.width()) ? (r1.left + r1.width()) : (r2.left + r2.width());
        nMinBottom = (r1.top + r1.height()) <= (r2.top + r2.height()) ? (r1.top + r1.height()) : (r2.top + r2.height());
        // 判断是否相交
        if (nMaxLeft > nMinRight || nMaxTop > nMinBottom) {
            return false;
        } else {
            Log.d(TAG, r1 + "::" + r2);
            return true;
        }
    }

    private void findTheFixHalfSide() {
        //		int half_side = HALF_MIN_SIDE;

        ArrayList<Keys> keymap = new ArrayList<Keys>();

        keymap.addAll(mKeyMappingMap);
        int j;
        int i;
        for (i = 0; i < keymap.size(); ++i) {
            Keys k = keymap.get(i);
            k.mRt.left -= HALF_MIN_SIDE;
            k.mRt.top -= HALF_MIN_SIDE;
            k.mRt.right += HALF_MIN_SIDE;
            k.mRt.bottom += HALF_MIN_SIDE;

        }
        for (i = 0; i < keymap.size(); ++i) {
            Keys k = keymap.get(i);
            for (j = i + 1; j < keymap.size(); ++j) {
                if (is2Rect(k.mRt, keymap.get(j).mRt)) {
                    break;
                }
            }
            if (j < keymap.size()) {
                break;
            }
        }

        if (i < keymap.size()) {

            //			Log.d(TAG, "!!!!!!!!!!1");
            HALF_CURRENT_SIDE = HALF_MIN_SIDE;
        } else {
            mKeyMappingMap.clear();
            mKeyMappingMap.addAll(keymap);

            //			Log.d(TAG, "!!!!!!!!!!2");
            HALF_CURRENT_SIDE = HALF_MAX_SIDE;
        }

        HALF_SIDE = HALF_CURRENT_SIDE;
    }

    public void saveStudyKey() {
        mKeyMappingMap.clear();
        mKeyMappingMap.addAll(mKeyMappingMapForStudy);

        findTheFixHalfSide();
        saveMapping();

        Util.sudoExecNoCheck("sync");
    }

    public void setTouchClearStudyKeycode(byte keycode) {
        // LOG.print("---setTouchClearStudyKeycode---keycode = " + keycode);
        if (keycode == MyCmd.Keycode.NONE) {// 清除所有
            mKeyMappingMapForStudy.clear();
        } else {// 清除某个
            if (mKeyMappingMapForStudy.get(keycode) != null) {
                mKeyMappingMapForStudy.remove(keycode);
            }
        }
        // notifyAlreadyStudyTouchKeys();
        // saveMapping();
    }

    public void queryTouchAlreadyStudyKeys() {
        // LOG.print("---queryTouchAlreadyStudyKeys---");
        notifyAlreadyStudyTouchKeys();
    }

    private void notifyAlreadyStudyTouchKeys() {
        // Integer[] alreadyStudyKeys = new Integer[mKeyMappingMap.size()];
        // int i = 0;
        // for (Integer keycode : mKeyMappingMap.keySet()) {
        // alreadyStudyKeys[i] = keycode;
        // i++;
        // }
        // if (mKeyCallback != null) {
        // mKeyCallback.onGetAlreadyStudyTouchKeys(alreadyStudyKeys);
        // }

    }

    private Integer getMappingKeycode(int x, int y) {

        for (int i = 0; i < mKeyMappingMap.size(); ++i) {

            Keys k = mKeyMappingMap.get(i);
            if (k.mRt.contains(x, y)) {
                return k.mKey;
            }
        }
        return 0;

    }

    // private boolean removeMappingKeycodeStudy(int x, int y) {
    // for (Integer keycode : mKeyMappingMapForStudy.keySet()) {
    // if (mKeyMappingMapForStudy.get(keycode).contains(x, y)) {
    // mKeyMappingMapForStudy.remove(keycode);
    // return true;
    // }
    // }
    // return false;
    // }

    private Integer getMappingKeycodeStudy(int x, int y) {

        for (int i = 0; i < mKeyMappingMapForStudy.size(); ++i) {

            Keys k = mKeyMappingMapForStudy.get(i);
            if (k.mRt.contains(x, y)) {
                return k.mKey;
            }
        }
        return 0;
    }

    private Integer getIndexKeycodeStudy(int x, int y) {

        for (int i = 0; i < mKeyMappingMapForStudy.size(); ++i) {

            Keys k = mKeyMappingMapForStudy.get(i);
            if (k.mRt.contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    private void saveMapping() {
        if (mKeyMappingMap == null) return;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mKeyMappingMap.size(); ++i) {

            Keys k = mKeyMappingMap.get(i);
            sb.append(k.mKey);
            sb.append(",");
            sb.append(k.mRt.centerX());
            sb.append(",");
            sb.append(k.mRt.centerY());
            sb.append("\n");
        }

        if (mMappingFile.exists()) {
            mMappingFile.delete();
        }
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            mMappingFile.createNewFile();

            //FileUtils.setPermissions(TOUCH_KEY_MAPPING_FILE, FileUtils.S_IRWXU	| FileUtils.S_IRWXG | FileUtils.S_IRWXO, -1, -1);

            fw = new FileWriter(mMappingFile);
            bw = new BufferedWriter(fw);
            bw.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initKeyMappingMap() {
        if (!mMappingFile.exists()) {
            loadDefaultMapping();
            return;
        }
        FileReader fr = null;

        BufferedReader br = null;
        try {
            mMappingFile.createNewFile();
            fr = new FileReader(mMappingFile);
            br = new BufferedReader(fr);
            String str = br.readLine();
            Integer keycode = (int) MyCmd.Keycode.NONE;
            int x = 0, y = 0;
            while (str != null) {
                if (str.contains(",")) {
                    String[] bean = str.split(",");
                    if (bean.length >= 3) {
                        keycode = Integer.parseInt(bean[0]);
                        x = Integer.parseInt(bean[1]);
                        y = Integer.parseInt(bean[2]);
                        Rect rect = new Rect(x - HALF_SIDE, y - HALF_SIDE, x + HALF_SIDE, y + HALF_SIDE);
                        // mKeyMappingMap.put(keycode, rect);

                        mKeyMappingMap.add(new Keys(keycode, rect));
                    }
                }
                str = br.readLine();
            }

            findTheFixHalfSide();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void loadDefaultMapping() {
        // int[][] defaultMapping = { { MyCmd.Keycode.HOME, 1074, 70 }, { 12,
        // 1088, 277 },
        // { 37, 1100, 174 }, { 36, 1103, 103 }, { 13, 1091, 225 } };
        // int[] tmp = null;
        // int x = 0, y = 0;
        // byte keycode = MyCmd.Keycode.NONE;
        // for (int i = 0; i < defaultMapping.length; i++) {
        // tmp = defaultMapping[i];
        // keycode = (byte) tmp[0];
        // x = tmp[1];
        // y = tmp[2];
        // Rect rect = new Rect(x - HALF_SIDE, y - HALF_SIDE, x + HALF_SIDE, y
        // + HALF_SIDE);
        // mKeyMappingMap.put(keycode, rect);
        // }
    }

    private void sendStudyMsg(int cmd, int data, int data2, Rect rect) {
        Intent it = new Intent(MyCmd.BROADCAST_RETURN_TOUCH_STUDY);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
        it.putExtra(MyCmd.EXTRA_COMMON_DATA, data);
        if (rect != null) {
            it.putExtra(MyCmd.EXTRA_COMMON_DATA2, rect);
        }
        it.putExtra(MyCmd.EXTRA_COMMON_DATA3, data2);
        mContext.sendBroadcast(it);
    }

    private void sendStudyMsg(int cmd) {
        Intent it = new Intent(MyCmd.BROADCAST_RETURN_TOUCH_STUDY);
        it.putExtra(MyCmd.EXTRA_COMMON_CMD, cmd);
        mContext.sendBroadcast(it);
    }
}
