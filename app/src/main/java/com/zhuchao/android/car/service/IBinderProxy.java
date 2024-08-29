package com.zhuchao.android.car.service;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.zhuchao.android.car.aidl.IMyAidlInterface;
import com.zhuchao.android.car.aidl.IMyAidlInterfaceListener;
import com.zhuchao.android.car.aidl.PEventCourier;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.session.Cabinet;

public class IBinderProxy extends IMyAidlInterface.Stub {
    private static final String TAG = "IBinderProxy";
    private final RemoteCallbackList<IMyAidlInterfaceListener> mListenerList = new RemoteCallbackList<>();
    private int mRemoteCallbackCount = 0;

    @Override
    public void registerListener(IMyAidlInterfaceListener iMyCarAidlInterfaceListener) throws RemoteException {
        mListenerList.register(iMyCarAidlInterfaceListener);
        ///int num = mListenerList.beginBroadcast();
        ///mListenerList.finishBroadcast();
        mRemoteCallbackCount++;
        ///MMLog.d(TAG, "mListenerList.size=" + num);
    }

    @Override
    public void unregisterListener(IMyAidlInterfaceListener iMyCarAidlInterfaceListener) throws RemoteException {
        mListenerList.unregister(iMyCarAidlInterfaceListener);
        mRemoteCallbackCount--;
        ///int num = mListenerList.beginBroadcast();
        ///mListenerList.finishBroadcast();
        ///MMLog.d(TAG, "mListenerList.size=" + num);
    }

    @Override
    public void sendMessage(PEventCourier pEventCourier) throws RemoteException {
        MMLog.d(TAG, pEventCourier.toStr());
        Cabinet.getEventBus().post(pEventCourier);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    public int getListenerCount() {
        return mRemoteCallbackCount;
    }

    public void notifyNewMessage(PEventCourier pEventCourier) {
        try {
            int num = mListenerList.beginBroadcast();
            for (int i = 0; i < num; ++i) {
                try {
                    IMyAidlInterfaceListener listener = mListenerList.getBroadcastItem(i);
                    listener.onMessageAidlInterface(pEventCourier);
                } catch (RemoteException ignored) {
                }
            }
        } finally {
            mListenerList.finishBroadcast();
        }
    }

}
