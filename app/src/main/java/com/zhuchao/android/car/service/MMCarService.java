package com.zhuchao.android.car.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.MessageEvent;
import com.zhuchao.android.fbase.MethodThreadMode;
import com.zhuchao.android.fbase.TAppProcessUtils;
import com.zhuchao.android.fbase.TCourierSubscribe;
import com.zhuchao.android.fbase.eventinterface.EventCourierInterface;
import com.zhuchao.android.session.Cabinet;

import java.util.Objects;

public class MMCarService extends Service {
    private static final String TAG = "MMCarService";
    public static MMCarService mThis;
    private final CarBinderProxy mCarBinderProxy = new CarBinderProxy();
    public MMCarService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mThis = this;
        MMLog.d(TAG, "MMCarService onCreate! " + TAppProcessUtils.getCurrentProcessNameAndId(this));
        Cabinet.getEventBus().registerEventObserver(this);
        registerUserEventReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Cabinet.getEventBus().unRegisterEventObserver(this);
        unregisterUserBroadcastListener();
        super.onDestroy();
        MMLog.d(TAG, TAG + " onDestroy!");
    }

    @Override
    public IBinder onBind(Intent intent) {
        /// TODO: Return the communication channel to the service.
        ///throw new UnsupportedOperationException("Not yet implemented");
        ///MMLog.d(TAG,intent.toString());
        return mCarBinderProxy;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerUserEventReceiver() {
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_HELLO);
        iFilter.addAction(MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_CAR_SERVICE);
        iFilter.addAction(MessageEvent.MESSAGE_EVENT_MACHINE_ACTION_CONFIG_UPDATE);
        iFilter.addAction(MessageEvent.MESSAGE_EVENT_LINK_Z);
        iFilter.addAction(MessageEvent.MESSAGE_EVENT_LINK_CARLETTER);

        registerReceiver(mUserEventReceiver, iFilter);
    }

    private final BroadcastReceiver mUserEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MMLog.d(TAG,"mUserEventReceiver action="+intent.getAction()+ " "+TAppProcessUtils.getCurrentProcessNameAndId(context));
            switch (Objects.requireNonNull(intent.getAction())) {

                case MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_CAR_SERVICE:
                case MessageEvent.MESSAGE_EVENT_LINK_Z:
                case MessageEvent.MESSAGE_EVENT_LINK_CARLETTER:
                case MessageEvent.MESSAGE_EVENT_MACHINE_ACTION_CONFIG_UPDATE:
                    break;
            }
        }
    };

    private void unregisterUserBroadcastListener() {
        try {
            unregisterReceiver(mUserEventReceiver);
        } finally {
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    @TCourierSubscribe(threadMode = MethodThreadMode.threadMode.BACKGROUND)
    public boolean onTCourierSubscribeEvent(EventCourierInterface courierInterface) {
        MMLog.d(TAG,courierInterface.toString());
        switch (courierInterface.getId()) {
            case MessageEvent.MESSAGE_EVENT_USB_MOUNTED:
            case MessageEvent.MESSAGE_EVENT_USB_VIDEO:
            case MessageEvent.MESSAGE_EVENT_LOCAL_VIDEO:
            case MessageEvent.MESSAGE_EVENT_USB_UNMOUNT:
                break;
        }
        return true;
    }

}