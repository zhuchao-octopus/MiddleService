package com.zhuchao.android.car.canbox;


import android.content.Context;
import android.content.Intent;

import com.common.util.MyCmd;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.manager.McuManager;

public class OBDView {


    /*协议参考 比纳瑞科技OBD通用串口通信协议V104*/
    public static final int ID_ALL = 0xffff0000;
    /*ID: BIT 0~7 数据在协议中的偏移量
     * 0~15数据占据的字节数
     * 16~31 ID标识
     */
    public static final int ID_GEAR_BRAKE_SEAT_LIGHT = 0x10200;
    /*Bit7~5:保留
Bit4: 手刹0:松开1:拉起
Bit3~Bit0: 档位
P挡:0x00
R挡:0x01
N挡:0x02
D挡:0x03
2挡:0x04
1挡:0x05
     * */
    //public static final int ID_SEAT_LIGHT = 0x20101; //保留
	/*
	 *Bit7~Bit5:大灯信息
关闭: 0x00
示廓灯: 0x01
近光灯: 0x02
远光灯: 0x03
Bit4~Bit3:转向灯
转向灯灭:0x00
左转向灯:0x01
右转向灯:0x02
双闪灯:0x03
Bit2:驾驶位安全带0:拔下1:插上
Bit1~Bit0:保留 */
    public static final int ID_DOOR = 0x40102;
    /*这个跟欣朴大众的一致，在CarService统一UI那里转换过的。
     *
     * 0:左前
     * 1：右前
     * 2.左后
     * 3.右后
     * 4.后
     * 5。前
     * 6.天窗半 开
     * 7.开窗全开
     * */
    public static final int ID_RPM = 0x80203;
    public static final int ID_SPEED = 0x100205;
    public static final int ID_ENGINEER_TEMP = 0x200107;

    public static final int DATA_MAX = 10;

    private static final byte[] mData = new byte[DATA_MAX];

    private static void copyData(int id, byte[] data) {

        int pos = (id & 0xff);
        int num = ((id & 0xff00) >> 8);

        System.arraycopy(data, 0, mData, pos + 0, num);

    }

    public static void sendCanboxInfo(Context c, int id, byte[] buf) {
        if (GlobalDefinition.mScreenSaverStyle != 1) {
            return;
        }

        if (id != ID_ALL) {
            copyData(id, buf);
        }

        if (!mNeedData) {
            return;
        }

        if (c != null) {
            Intent i = new Intent(MyCmd.BROADCAST_SEND_FROM_CAN);
            i.putExtra(MyCmd.EXTRA_COMMON_CMD, id);
            i.putExtra(MyCmd.EXTRA_COMMON_DATA, buf);
            //			if (packageName != null){
            //				i.setPackage(packageName);
            //			}
            c.sendBroadcast(i);


        }
    }

    public static boolean mNeedData = false;

    public static void updateScreenSave(Context c, int data) {
        Canbox canbox = CarUtil.getCanboxInstance();
        if (data == 1) {
            mNeedData = true;
            sendCanboxInfo(c, ID_ALL, mData);
            McuManager mcu = McuManager.getInstance();
            if (mcu != null) {
                mcu.queryBattery();
            }
            if (canbox != null) {
                canbox.updateScreenSaveNeedData(true);
            }
        } else {
            mNeedData = false;
            if (canbox != null) {
                canbox.updateScreenSaveNeedData(false);
            }
        }
    }

}
