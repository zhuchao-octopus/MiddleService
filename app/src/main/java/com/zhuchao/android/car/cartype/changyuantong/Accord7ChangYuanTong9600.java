package com.zhuchao.android.car.cartype.changyuantong;

import android.os.Handler;

import com.common.utils.MyCmd;
import com.zhuchao.android.car.GlobalDefinition;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;


public class Accord7ChangYuanTong9600 extends Canbox {

    byte[] mAirData = new byte[8];
    private int mTempOutDoor = CarUtil.INVALID_OUT_DOOR_TEMP;

    public Accord7ChangYuanTong9600() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x01, 0x0, 0x3, 0x0, 0x0});
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{0x05, 0x02, 0x5, 0x3, 0x5, 0x0});
    }

    private void parseACInfo(byte[] data, int len) {

        byte[] airData = new byte[8];
        if ((data[0] & 0xff) == 0x40) {
            data[0] = (byte) 0xff;
        } else if (data[0] == 0x24) {
            data[0] = 0x0;
        }

        if ((data[1] & 0xff) == 0x40) {
            data[1] = (byte) 0xff;
        } else if (data[1] == 0x24) {
            data[1] = 0x0;
        }

        if ((data[3] & 0x7) == 1) {
            data[3] = (byte) 0x40;
        } else if ((data[3] & 0x7) == 2) {
            data[3] = (byte) 0x60;
        } else if ((data[3] & 0x7) == 3) {
            data[3] = (byte) 0x20;
        } else if ((data[3] & 0x7) == 4) {
            data[3] = (byte) 0xA0;
        } else {
            data[3] = 0;
        }

        airData[0] = (byte) (((data[4] & 0x1) << 3) | (((data[4] & 0x2) << 5)) | (((data[4] & 0x8) << 2)) | (((data[4] & 0x10) >> 2)) | (((data[4] & 0x20) >> 4)) | (((data[4] & 0x40) >> 6)));

        airData[1] = (byte) ((data[2] & 0x7) | data[3]);

        airData[2] = data[0];
        airData[3] = data[1];
        airData[4] = (byte) (((data[4] & 0x4) << 5));


        boolean airControl = false;
        if (airData[0] != mAirData[0] || airData[1] != mAirData[1] || airData[2] != mAirData[2] || airData[3] != mAirData[3] || airData[4] != mAirData[4]) {
            airControl = true;

            mAirData = airData;
        }


        Handler handler = getHandler("CanService");
        if (null != handler) {
            if (airControl) {
                handler.sendMessage(handler.obtainMessage(CANBOX_RETURN_AIR, airData));
            }
        }

        if ((data[4] & 0x80) != 0) {
            int temp = (data[5] & 0xff);
            updateOutDoorTemp(temp);


        } else {
            GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, "");
        }

    }

    public void updateOutDoorTemp(int temp) {

        if (temp == CarUtil.INVALID_OUT_DOOR_TEMP) {
            if (mTempOutDoor != CarUtil.INVALID_OUT_DOOR_TEMP) {
                temp = mTempOutDoor;
            } else {
                return;
            }
        }
        mTempOutDoor = temp;
        String unit = mContext.getResources().getString(R.string.temp_unic_centigrade);
        if (CarUtil.mTempUnit == 2) {
            temp = (int) ((temp) * 1.8f + 32);
            unit = mContext.getResources().getString(R.string.temp_unic_fahrenheit);
        }

        String s = "";

        if (temp >= 0 && temp <= 40) {
            s += temp + unit;
        } else if (temp >= 200 && temp <= 255) {
            temp = -(255 - temp);
            s += temp + unit;
        }
        GlobalDefinition.sendByCarServiceToSystemUI(mContext, "com.android.systemui", MyCmd.Cmd.SET_OUT_DOOR_TEMP, s);

    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub

        parseACInfo(data, len);


    }

}
