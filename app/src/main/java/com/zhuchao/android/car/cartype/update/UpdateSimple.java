package com.zhuchao.android.car.cartype.update;

import android.util.Log;
import android.widget.Toast;

import com.common.utils.Util;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.io.FileInputStream;

public class UpdateSimple extends Canbox {

    public UpdateSimple() {
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
        // TODO Auto-generated method stub
        // super.startConnect();

        mUpdateDialog = new UpdateDialog(mContext);
        mUpdateDialog.show();
        mUpdateDialog.setTitle("simple update");
        Util.doSleep(200);

        byte[] data = new byte[]{
                (byte) 0xe0, 0xd, 0x1, 0x0, 0x0, (byte) 0x96, 0x00, 0x41, 0x42, 0x43, 0x44, 0x12, 0x34, 0x56, 0x78
        };
        mSendLen = -1;
        sendDataToCanbox(data, data.length);

    }

    private byte[] buf;
    private int mSendLen = -1;
    public int getReturnType() {
        return 0;
    }
    private void startUpdate() {
        if (CarUtil.mUpdateFile != null)
        {
            try (FileInputStream fis = new FileInputStream(CarUtil.mUpdateFile)) {
                buf = new byte[fis.available()];
                int n = fis.read(buf);
                int crc = 0;

                for (byte b : buf) {
                    crc += b & 0xff;
                }

                byte[] data = new byte[]{(byte) 0xe0, 0x9, 0x2, (byte) ((buf.length & 0xff000000) >> 24), (byte) ((buf.length & 0xff0000) >> 16), (byte) ((buf.length & 0xff00) >> 8), (byte) ((buf.length & 0xff) >> 0), (byte) ((crc & 0xff000000) >> 24), (byte) ((crc & 0xff0000) >> 16), (byte) ((crc & 0xff00) >> 8), (byte) ((crc & 0xff) >> 0)};
                mSendLen = 0;
                sendDataToCanbox(data, data.length);
                // mToast = Toast.makeText(mContext, "start",
                // Toast.LENGTH_LONG);
                // if(mToast!=null){
                // // mToast.setDuration(99900000);
                // }
            } catch (Exception ignored) {
            }

        }
    }

    Toast mToast;

    private void continuUpdate() {
        try {
            if ((buf.length - mSendLen) <= 0) {
                return;
            }

            int len = buf.length - mSendLen;
            byte subcmd = 0x3;
            if (len > 64) {
                len = 64;
            } else {
                subcmd = 0x4;
            }
            byte[] data = new byte[len + 3];

            data[0] = (byte) 0xe0;
            data[1] = (byte) (len + 1);
            data[2] = subcmd;
            if (len >= 0) System.arraycopy(buf, 0 + mSendLen, data, 3, len);

            mSendLen += len;
            sendDataToCanbox(data, data.length);

            int p = 0;
            p = mSendLen * 100 / buf.length;
            //			if (mToast != null) {
            //
            //				mToast.cancel();
            //
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
        if (data[0] == (byte) 0xe1) {
            switch (data[2]) {
                case 0:// ready
                    if (mSendLen == -1) {
                        startUpdate();
                    } else {
                        //					continuUpdate();
                    }
                    break;
                case (byte) 0x81:
                    continuUpdate();
                    break;
                case 0x9:// stop
                    //				if (mToast != null) {
                    //					mToast.cancel();
                    //				}
                    //				mToast = Toast.makeText(mContext, "update fail!!!!!!!",
                    //						Toast.LENGTH_LONG);
                    //				mToast.show();

                    mUpdateDialog.setMsg("update fail!!!!!!!");
                    break;
                case 0xa:

                    mUpdateDialog.setMsg("update sucess!!!!!!!");
                    //				if (mToast != null) {
                    //					mToast.cancel();
                    //				}
                    //				mToast = Toast.makeText(mContext, "update sucess!!!!!!!",
                    //						Toast.LENGTH_LONG);
                    //				mToast.show();
                    break;
            }
        }
    }
}
