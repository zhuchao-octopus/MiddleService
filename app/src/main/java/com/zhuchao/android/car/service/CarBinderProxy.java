package com.zhuchao.android.car.service;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.zhuchao.android.car.IMyCarAidlInterface;
import com.zhuchao.android.car.IMyCarAidlInterfaceListener;
import com.zhuchao.android.car.PEventCourier;
import com.zhuchao.android.fbase.MMLog;

public class CarBinderProxy extends IMyCarAidlInterface.Stub{
    private static final String TAG = "CarBinderProxy";
    private final RemoteCallbackList<IMyCarAidlInterfaceListener> mListenerList = new RemoteCallbackList<>();

    @Override
    public void registerListener(IMyCarAidlInterfaceListener iMyCarAidlInterfaceListener) throws RemoteException {
        mListenerList.register(iMyCarAidlInterfaceListener);
        int num = mListenerList.beginBroadcast();
        mListenerList.finishBroadcast();
        MMLog.d(TAG,"mListenerList.size="+num);
    }

    @Override
    public void unregisterListener(IMyCarAidlInterfaceListener iMyCarAidlInterfaceListener) throws RemoteException {
        mListenerList.unregister(iMyCarAidlInterfaceListener);
        int num = mListenerList.beginBroadcast();
        mListenerList.finishBroadcast();
        MMLog.d(TAG,"mListenerList.size="+num);
    }

    @Override
    public void sendMessage(PEventCourier pEventCourier) throws RemoteException {
        MMLog.d(TAG,pEventCourier.toStr());
        onNewMessageCallback(pEventCourier);
    }

    @Override
    public String getHelloData() {
        return "Hello this is aidl service aidl proxy!";
    }

    private void onNewMessageCallback(PEventCourier pEventCourier) {
        int num = mListenerList.beginBroadcast();
        for (int i = 0; i < num; ++i) {
            IMyCarAidlInterfaceListener listener = mListenerList.getBroadcastItem(i);
            try {
                listener.onMessageCarAidlInterface(pEventCourier);
            } catch (RemoteException e) {
                //throw new RuntimeException(e);
                MMLog.e(TAG, String.valueOf(e));
            }
        }
        mListenerList.finishBroadcast();
    }
}
