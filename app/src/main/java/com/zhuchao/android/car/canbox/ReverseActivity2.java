package com.zhuchao.android.car.canbox;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.common.util.BroadcastUtil;
import com.common.util.MyCmd;
import com.zhuchao.android.car.R;
import com.zhuchao.android.fbase.MMLog;

public class ReverseActivity2 extends Activity {

    ReverseUI mRadioUI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.back);
        mRadioUI = ReverseUI.getInstance(this, findViewById(R.id.screen1_main), 0);
        if(mRadioUI != null)
           mRadioUI.onCreate();
        MMLog.d("ReverseActivity2", "onCreate tag="+findViewById(R.id.screen1_main).getTag());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRadioUI != null) mRadioUI.onResume();
    }

    protected void onPause() {
        super.onPause();
        if (mRadioUI != null) mRadioUI.onPause();

    }

    protected void onDestroy() {
        super.onDestroy();
        if (mRadioUI != null) mRadioUI.onDestroy();

        BroadcastUtil.sendToCarServiceSetSource(this, MyCmd.SOURCE_MX51);
    }

}
