package com.my.cartype.update;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.my.GlobalDef;
import com.common.util.Util;
import com.my.canbox.Canbox;
import com.my.canbox.RadarManager;
import com.my.cartype.CarUtil;
import com.my.manager.McuManager;
import com.my.out.R;

public class UpdateHiWorld extends Canbox {

	public UpdateHiWorld() {
		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x01, 0x2, 0x3,
				0x0, 0x0 });
		// sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x02, 0x2, 0x0,
		// 0x3, 0x2 });

		sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x10, 0x1 });
	}

	public byte sum(byte[] data, int len) {
		byte sum = 0;
		for (int i = 0; i < len; ++i) {
			sum += data[i];
		}
		sum = (byte) ((sum & 0xFF) - 1);
		return sum;
	}

	public void sendDataToCanbox(byte[] data, int len) {
		// canbox
		byte[] send = new byte[len + 4];
		send[0] = (byte) (len + 3);
		send[1] = (byte) 0x5a;
		send[2] = (byte) 0xa5;
		send[len + 3] = sum(data, len);
		byteArrayCopy(send, data, 3, 0, len);
		sendCmd(CANBOX_WRITE_COMMON_DATA, 0, send);
		Util.doSleep(50);
	}

	public void sendDataToCanbox2(byte[] data, int len) {
		// canbox
		byte[] send = new byte[len + 4];
		send[0] = (byte) (len + 3);
		send[1] = (byte) 0xaa;
		send[2] = (byte) 0x55;
		send[len + 3] = sum(data, len);
		byteArrayCopy(send, data, 3, 0, len);
		sendCmd(CANBOX_WRITE_COMMON_DATA, 0, send);
		Util.doSleep(50);
	}

	UpdateDialog mUpdateDialog;

	@Override
	public void startConnect() {
		// TODO Auto-generated method stub
		// super.startConnect();
		mUpdateDialog = new UpdateDialog(mContext);
		mUpdateDialog.show();
		Util.doSleep(200);

		startUpdate();
		// byte[] data = new byte[] { 0x2, (byte) 0xe0, 0x0, 0x0 };
		// sendDataToCanbox(data, data.length);
		// Util.doSleep(50);
		// data = new byte[] { 0x2, (byte) 0xe1, 0x0, 0x0};
		// sendDataToCanbox(data, data.length);
		// Util.doSleep(50);
		// data = new byte[] { 0x2, (byte) 0xe0, 0x0, 0x0};
		// sendDataToCanbox2(data, data.length);
		// Util.doSleep(50);
		// data = new byte[] { 0x2, (byte) 0xe1, 0x0, 0x0};
		// sendDataToCanbox2(data, data.length);

		mSendLen = -1;
		// startUpdate();
	}

	private byte[] buf;
	private int mSendLen = -1;
	private int mPackageTotalNum = 0;
	private int mPackageSendNum = 0;
	private final static int PACKAGE_LEN = 136;
	public int mType = 0;
	private void startUpdate() {
		if (CarUtil.mUpdatFile != null) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(CarUtil.mUpdatFile);

				buf = new byte[fis.available()];

				Log.d("ffk", "!!!:" + buf.length + ":"
						+ (buf.length % PACKAGE_LEN));
				int n = fis.read(buf);
				mPackageTotalNum = (buf.length / PACKAGE_LEN)
						+ ((buf.length % PACKAGE_LEN) == 0 ? 0 : 1);
				if (mPackageTotalNum > 962) {
					// mToast = Toast.makeText(mContext,
					// "fail !!!!!!!  file too big.", Toast.LENGTH_LONG);
					// mToast.show();

					mUpdateDialog.setMsg("fail !!!!!!!  file too big");
					return;
				}
				Log.d("ccfk", "startUpdate:" + mPackageTotalNum);
				// sendCmd(CANBOX_WRITE_MCU_DATA, 0, new byte[] { 0x05, 0x10,
				// 0x1 });
				// Util.doSleep(50);
				 byte[] data = new byte[] { 0x2, (byte) 0xe0, 0x0, 0x0 };
				 if (mType == 0){
					 sendDataToCanbox(data, data.length);
				 } else {
					 sendDataToCanbox2(data, data.length);					 
				 }
//				 sendDataToCanbox2(data, data.length);

//				byte[] data2 = new byte[] { 15, 0x5a, (byte) 0xa5, 0x2, (byte) 0xe0, 0x0, 0x0, (byte) 0xe1,
//						(byte) 0xaa, (byte) 0x55, 0x2, (byte) 0xe0, 0x0, 0x0, (byte) 0xe1 };
//				sendCmd(CANBOX_WRITE_COMMON_DATA, 0, data2);

				// sendDataToCanbox2(data, data.length);

				// byte[] data = new byte[] { (byte) 0xea, 0x2,
				// (byte) ((mPackageTotalNum & 0xff00) >> 8),
				// (byte) ((mPackageTotalNum & 0xff) >> 0)
				//
				// };
				// mSendLen = 0;
				//
				// sendDataToCanbox(data, data.length);
				// // mToast = Toast.makeText(mContext, "start",
				// // Toast.LENGTH_LONG);
				// // if(mToast!=null){
				// // // mToast.setDuration(99900000);
				// // }
				// continuUpdate();
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

	// Toast mToast;
	byte[] data = new byte[PACKAGE_LEN + 1];

	private void continuUpdate() {
		try {
			Log.d("ddk", mPackageSendNum + "continuUpdate:" + mPackageTotalNum);

			if (mPackageTotalNum <= mPackageSendNum) {
				return;
			}
			data[0] = (byte) PACKAGE_LEN;
			for (int i = 0; i < PACKAGE_LEN; ++i) {
				int buf_len = i + mPackageSendNum * PACKAGE_LEN;
				data[i + 1] = buf[buf_len];
			}

			// sendDataToCanbox(data, data.length);
			sendCmd(CANBOX_WRITE_COMMON_DATA, 0, data);
			++mPackageSendNum;

			int p = 0;
			p = mPackageSendNum * 100 / mPackageTotalNum;
			// if (mToast != null) {
			// mToast.cancel();
			// }
			// mToast = Toast
			// .makeText(mContext, "update %" + p, Toast.LENGTH_LONG);
			// mToast.show();

			mUpdateDialog.setMsg("update %" + p);

			// Log.e("abc", mSendLen + ":" + buf.length + ":" + p);
		} catch (Exception e) {
			Log.e("abc", "continuUpdate!!!!!!!!!!err:");
		}

	}

	public int getReturnType() {
		return 0xff;
	}

	private int updateTag = 0;

	@Override
	public void parseCanboxData(byte[] data, int len) {
		// TODO Auto-generated method stub

		Log.d("abcd", updateTag + "is finish!!!!!!" + mPackageSendNum);
		if (updateTag == -1 || mUpdateDialog == null
				|| !mUpdateDialog.isShowing()) {
			return;
		}
		// Log.d("ccfk1",
		// mUpdateDialog.isShowing()+"parseCanboxData:" +
		// Util.byte2HexStr(data));
		switch (data[2]) {
		case 0x55:// U 升级准备 85
			Log.d("ccfk", "parseCanboxData!!!!!!!!!!!!!!: 0x55");
			byte[] send = new byte[] { 0x4, 0x19, (byte) 0x78, 0x02, 0x17 };
			sendCmd(CANBOX_WRITE_COMMON_DATA, 0, send);
			mPackageSendNum = 0;
			break;
		case 0x52:// R 升级出错
			Log.d("ccfk", "error!!!");
			mPackageSendNum = mPackageTotalNum;
			// if (mToast != null) {
			// mToast.cancel();
			// }
			// mToast = Toast.makeText(mContext, "update file fail!!!!!!!",
			// Toast.LENGTH_LONG);
			// mToast.show();

			mUpdateDialog.setMsg("update file fail");
			break;
		case 0x42:
			// 忽略单独发B的情况
			// Log.d("kkfb", "parseCanboxData!!!!!!!!!!!!!!: 0x42");
			mPackageSendNum = 0;
			// Util.doSleep(5000);
			break;
		case 0x53:// S 下一帧 83
			continuUpdate();
			break;
		case 0x45:// E 升级结束 69
			Log.d("abcd", (mPackageTotalNum) + "is finish!!!!!!"
					+ mPackageSendNum);
			if (mPackageSendNum == (mPackageTotalNum)) { // finish
				Log.d("abcd", "finish!!!!!!");
				updateTag = -1;
				mUpdateDialog.setMsg("update file sucess");
			}
			// Log.d("ccfk", "end!!!");
			// if (mToast != null) {
			// mToast.cancel();
			// }
			// mToast = Toast.makeText(mContext, "update sucess!!!!!!!",
			// Toast.LENGTH_LONG);
			// mToast.show();
			break;
		}
	}

}
