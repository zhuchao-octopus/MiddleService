package com.zhuchao.android.car.out;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.common.util.MyCmd;
import com.zhuchao.android.car.service.MyCarService;

public class OutApp extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (MyCarService.mThis == null) {
            Intent it = new Intent(this, MyCarService.class);
            startService(it);
        } else {
            Intent it = new Intent(MyCmd.BROADCAST_START_BACKLIGHTSETTINGS);
            // it.putExtra(MyCmd.EXTRA_COMMON_DATA, 1);
            sendBroadcast(it);
        }
        finish();
    }
}
