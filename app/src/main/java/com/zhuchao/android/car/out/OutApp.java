package com.zhuchao.android.car.out;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.common.util.MyCmd;
import com.zhuchao.android.car.service.MMCarService;
import com.zhuchao.android.car.service.MyCarService;
import com.zhuchao.android.fbase.MMLog;

public class OutApp extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MMLog.d("OutApp","OutApp.onCreate!");
        if (MyCarService.mThis == null) {
            Intent it = new Intent(this, MyCarService.class);
            startService(it);
        }
        //if (MMCarService.mThis == null)
        {
            Intent it = new Intent(this, MMCarService.class);
            startService(it);
        }
        ///else {
        ///   Intent it = new Intent(MyCmd.BROADCAST_START_BACKLIGHTSETTINGS);
        ///    it.putExtra(MyCmd.EXTRA_COMMON_DATA, 1);
        ///   sendBroadcast(it);
        ///}
        finish();
    }
}
