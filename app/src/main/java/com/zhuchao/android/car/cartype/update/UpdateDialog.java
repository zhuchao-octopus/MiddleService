package com.zhuchao.android.car.cartype.update;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.zhuchao.android.car.R;
import com.zhuchao.android.car.canbox.CanService;
import com.zhuchao.android.car.canbox.Canbox;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.manager.McuManager;

import java.util.Objects;

public class UpdateDialog extends Dialog {

    TextView mTitle;
    TextView mMessage;
    McuManager mcu;

    public UpdateDialog(Context c) {
        super(c);
        startKeepAcc();

    }

    public void setTitle(String s) {
        mTitle.setText(s);
    }

    public void setMsg(String s) {
        mMessage.setText(s);
    }

    private final View.OnClickListener mOnClickDialogCancel = new View.OnClickListener() {
        public void onClick(View v) {
            stopKeepAcc();
            dismiss();
        }
    };

    private final Handler mHandler = new Handler(Objects.requireNonNull(Looper.myLooper())) {
        public void handleMessage(@NonNull Message msg) {
            startKeepAcc();
            super.handleMessage(msg);
        }
    };

    private void startKeepAcc() {
        mHandler.removeMessages(0);
        mHandler.sendEmptyMessageDelayed(0, 2000);
        if (mcu != null) {
            mcu.setKeepAcc(1);
        }


    }

    private void stopKeepAcc() {
        mHandler.removeMessages(0);
        if (mcu != null) {
            mcu.setKeepAcc(0);
        }

        if (mCanbox != null) {
            CanService.mCanbox = mCanbox;
            mCanbox = null;
        }
    }


    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        CarUtil.mIsUpdating = true;
        startKeepAcc();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        CarUtil.mIsUpdating = false;
        stopKeepAcc();
    }

    private Canbox mCanbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.canbox_update_msg);
        // ((TextView)
        // findViewById(R.id.alertTitle)).setText(R.string.update_canbox);
        mTitle = findViewById(R.id.alertTitle);
        mMessage = findViewById(R.id.message);
        findViewById(R.id.cancel).setOnClickListener(mOnClickDialogCancel);
        getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ERROR));
        mcu = McuManager.getInstance();

        if (CanService.mCanbox != null) {
            mCanbox = CanService.mCanbox;
            CanService.mCanbox = null;
        }
    }
}
