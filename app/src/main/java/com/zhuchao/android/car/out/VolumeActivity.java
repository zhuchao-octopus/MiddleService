package com.zhuchao.android.car.out;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.common.utils.MyCmd;

public class VolumeActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent it = new Intent(MyCmd.BROADCAST_START_VOLUMESETTINGS);
        it.setPackage(getPackageName());
        sendBroadcast(it);
        finish();
    }
}
