package com.zhuchao.android.car.cartype.update;

import android.util.Log;

import com.common.util.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.io.FileInputStream;

public class UpdateLuZheng extends Canbox {

    public UpdateLuZheng() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });
    }

    UpdateDialog mUpdateDialog;

    @Override
    public void startConnect() {
        mUpdateDialog = new UpdateDialog(mContext);
        mUpdateDialog.show();
        mUpdateDialog.setTitle("simple update");
        Util.doSleep(200);

        // TODO Auto-generated method stub
        // super.startConnect();
        //		byte[] data = new byte[] { (byte) 0xe0, 0xd, 0x1, 0x0, 0x0,
        //				(byte) 0x96, 0x00, 0x41, 0x42, 0x43, 0x44, 0x12, 0x34, 0x56,
        //				0x78 };
        mSendLen = -1;
        //		sendDataToCanbox(data, data.length);

        startUpdate();
    }

    public int getReturnType() {
        return 0;
    }

    private byte[] buf;
    private int mSendLen = -1;
    private int mPackageTotalNum = 0;
    private int mPackageSendNum = 0;
    private final static int PACKAGE_LEN = 128;

    private void startUpdate() {
        if (CarUtil.mUpdatFile != null) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(CarUtil.mUpdatFile);

                buf = new byte[fis.available()];

                //				Log.d("ffk", "!!!:"+buf.length+":"+(buf.length % PACKAGE_LEN));
                int n = fis.read(buf);
                mPackageTotalNum = (buf.length / PACKAGE_LEN) + ((buf.length % PACKAGE_LEN) == 0 ? 0 : 1);
                if (mPackageTotalNum > 962) {
                    //					mToast = Toast.makeText(mContext,
                    //							"fail !!!!!!!  file too big.", Toast.LENGTH_LONG);
                    //					mToast.show();

                    mUpdateDialog.setMsg("fail !!!!!!!  file too big");
                    return;
                }

                byte[] data = new byte[]{
                        (byte) 0xea, 0x2, (byte) ((mPackageTotalNum & 0xff00) >> 8), (byte) ((mPackageTotalNum & 0xff) >> 0)

                };
                mSendLen = 0;

                sendDataToCanbox(data, data.length);
                // mToast = Toast.makeText(mContext, "start",
                // Toast.LENGTH_LONG);
                // if(mToast!=null){
                // // mToast.setDuration(99900000);
                // }
                continuUpdate();
            } catch (Exception ce) {

            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Exception ce) {

                    }
                }
            }

        }
    }

    //	Toast mToast;

    private void continuUpdate() {
        try {
            Log.d("can_update", "continuUpdate:" + mPackageTotalNum);

            if (mPackageTotalNum <= mPackageSendNum) {
                return;
            }

            byte[] data = new byte[PACKAGE_LEN + 4];

            data[0] = (byte) 0xec;
            data[1] = (byte) (PACKAGE_LEN + 2);
            data[2] = (byte) ((mPackageSendNum & 0xff00) >> 8);
            data[3] = (byte) ((mPackageSendNum & 0xff) >> 0);

            for (int i = 0; i < PACKAGE_LEN; ++i) {
                int buf_len = i + mPackageSendNum * PACKAGE_LEN;
                if (buf_len < buf.length) {
                    data[4 + i] = buf[buf_len];
                } else {
                    data[4 + i] = 0;
                }
            }

            sendDataToCanbox(data, data.length);

            ++mPackageSendNum;

            int p = 0;
            p = mPackageSendNum * 100 / mPackageTotalNum;
            //			if (mToast != null) {
            //				mToast.cancel();
            //			}
            //			mToast = Toast
            //					.makeText(mContext, "update %" + p, Toast.LENGTH_LONG);
            //			mToast.show();

            mUpdateDialog.setMsg("update %" + p);
            //			Log.e("abc", mSendLen + ":" + buf.length + ":" + p);
        } catch (Exception e) {
            Log.e("abc", "continuUpdate!!!!!!!!!!err:");
        }

    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        switch (data[0]) {
            case (byte) 0xed:
                continuUpdate();
                break;
            case (byte) 0xeb:
                switch (data[2]) {
                    case (byte) 0x1:
                        continuUpdate();
                        break;
                    case 0x0:// stop
                        //				if (mToast != null) {
                        //					mToast.cancel();
                        //				}
                        //				mToast = Toast.makeText(mContext, "update file fail!!!!!!!",
                        //						Toast.LENGTH_LONG);
                        //				mToast.show();
                        mUpdateDialog.setMsg("update file fail");
                        break;
                    case 0x2:
                        //				if (mToast != null) {
                        //					mToast.cancel();
                        //				}
                        //				mToast = Toast.makeText(mContext, "update sucess!!!!!!!",
                        //						Toast.LENGTH_LONG);
                        //				mToast.show();

                        mUpdateDialog.setMsg("update file sucess");
                        break;
                }
                break;
        }
    }
}
