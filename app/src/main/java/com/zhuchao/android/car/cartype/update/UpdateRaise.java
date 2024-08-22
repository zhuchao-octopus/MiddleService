package com.zhuchao.android.car.cartype.update;

import android.util.Log;
import android.widget.Toast;

import com.common.utils.Util;
import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;

import java.io.FileInputStream;

public class UpdateRaise extends Canbox {

    public UpdateRaise() {
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x01, 0x2, 0x3, 0x0, 0x0
        });
        sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[]{
                0x05, 0x02, 0x0, 0x0, 0x0, 0x1
        });
    }

    @Override
    public void startConnect() {
        // TODO Auto-generated method stub
        // super.startConnect();
        mUpdateDialog = new UpdateDialog(mContext);
        mUpdateDialog.show();
        Util.doSleep(200);
        byte[] data = new byte[]{
                (byte) 0xD9, (byte) 0x05, (byte) 0x01, (byte) 0x5E, (byte) 0x6A, (byte) 0xEA, (byte) 0x87
        };
        mSendLen = -1;
        sendDataToCanbox(data, data.length);


    }

    public int getReturnType() {
        return 0;
    }

    private byte[] buf;
    private int mSendLen = -1;
    UpdateDialog mUpdateDialog;

    private void startUpdate() {
        if (CarUtil.mUpdateFile != null) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(CarUtil.mUpdateFile);

                buf = new byte[fis.available()];

                int n = fis.read(buf);

                byte[] data = new byte[]{(byte) 0xD9, (byte) 0x01, (byte) 0x81};
                sendDataToCanbox(data, data.length);

                //				int crc = 0;
                //				for (int i = 0; i < buf.length; ++i) {
                //					crc += (int) (buf[i] & 0xff);
                //				}
                //
                //				byte[] data = new byte[] { (byte) 0xe0, 0x9, 0x2,
                //						(byte) ((buf.length & 0xff000000) >> 24),
                //						(byte) ((buf.length & 0xff0000) >> 16),
                //						(byte) ((buf.length & 0xff00) >> 8),
                //						(byte) ((buf.length & 0xff) >> 0),
                //
                //						(byte) ((crc & 0xff000000) >> 24),
                //						(byte) ((crc & 0xff0000) >> 16),
                //						(byte) ((crc & 0xff00) >> 8),
                //						(byte) ((crc & 0xff) >> 0)
                //
                //				};
                //				mSendLen = 0;
                //				sendDataToCanbox(data, data.length);
                // mToast = Toast.makeText(mContext, "start",
                // Toast.LENGTH_LONG);
                // if(mToast!=null){
                // // mToast.setDuration(99900000);
                // }


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

    Toast mToast;

    private void continuUpdate(int len, int pos) {
        try {
            if ((buf.length - mSendLen) <= 0) {
                return;
            }

            byte[] data = new byte[len + 6];

            data[0] = (byte) 0xd9;
            data[1] = (byte) (len + 4);
            data[2] = (byte) 0x83;
            data[3] = (byte) ((pos & 0xff0000) >> 16);
            data[4] = (byte) ((pos & 0xff00) >> 8);
            data[5] = (byte) ((pos & 0xff) >> 0);
            if (len >= 0) System.arraycopy(buf, pos + 0, data, 6, len);

            sendDataToCanbox(data, data.length);

            // Log.e("abc", mSendLen + ":" + buf.length + ":" + p);
        } catch (Exception e) {
            Log.e("abc", "continuUpdate!!!!!!!!!!err:");
        }

    }

    @Override
    public void parseCanboxData(byte[] data, int len) {
        // TODO Auto-generated method stub
        if (data[0] == (byte) 0x69) {
            switch (data[2]) {
                case 1:
                    if (data[3] == (byte) 0x5e && data[4] == (byte) 0x6a && data[5] == (byte) 0xea && data[6] == (byte) 0x87) {
                        startUpdate();
                        mUpdateDialog.setTitle("raise " + mContext.getString(R.string.updateing_canbox));
                    }
                    break;
                case (byte) 0x2:
                    continuUpdate(data[3] & 0xff, ((data[4] & 0xff) << 16) | ((data[5] & 0xff) << 8) | ((data[6] & 0xff) << 0));
                    break;
                case (byte) 0x81:
                    mUpdateDialog.setTitle(mContext.getString(R.string.updateing_canbox));
                    break;
                case (byte) 0x82:// stop
                    mUpdateDialog.setTitle(mContext.getString(R.string.update_canbox_noused));
                    break;
                case (byte) 0x83:// stop
                    mUpdateDialog.setMsg("%" + (data[3] & 0xff));
                    break;
                case (byte) 0x84:// stop
                    mUpdateDialog.setMsg(mContext.getString(R.string.update_canbox_fail) + (String.format("%02d_%02d_%02d", (data[3] & 0xff), (data[4] & 0xff), (data[5] & 0xff))));
                    break;
                case (byte) 0x85:// stop
                    mUpdateDialog.setMsg(mContext.getString(R.string.update_canbox_success));
                    break;
                case 0xa:
                    if (mToast != null) {
                        mToast.cancel();
                    }
                    mToast = Toast.makeText(mContext, "update sucess!!!!!!!", Toast.LENGTH_LONG);
                    mToast.show();
                    break;
            }
        }
    }
}
