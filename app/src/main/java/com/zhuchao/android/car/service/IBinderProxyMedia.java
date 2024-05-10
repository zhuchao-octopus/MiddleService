package com.zhuchao.android.car.service;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.zhuchao.android.car.aidl.IMyAidlInterfaceListener;
import com.zhuchao.android.car.aidl.IMyMediaAidlInterface;
import com.zhuchao.android.car.aidl.PEventCourier;
import com.zhuchao.android.car.aidl.PMovie;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.MessageEvent;
import com.zhuchao.android.fbase.PlayerStatusInfo;
import com.zhuchao.android.fbase.ThreadUtils;
import com.zhuchao.android.session.Cabinet;
import com.zhuchao.android.video.Movie;
import com.zhuchao.android.video.OMedia;

import java.util.ArrayList;
import java.util.List;

public class IBinderProxyMedia extends IMyMediaAidlInterface.Stub {
    private static final String TAG = "IBinderProxyMedia";
    private final RemoteCallbackList<IMyAidlInterfaceListener> mListenerList = new RemoteCallbackList<>();
    private int mRemoteCallbackCount = 0;

    @Override
    public void registerListener(IMyAidlInterfaceListener iMyCarAidlInterfaceListener) {
        mListenerList.register(iMyCarAidlInterfaceListener);
        ///int num = mListenerList.beginBroadcast();
        ///mListenerList.finishBroadcast();
        mRemoteCallbackCount++;
        ///MMLog.d(TAG, "mListenerList.size=" + num);
        ThreadUtils.runThread(new Runnable() {
            @Override
            public void run() {
                PEventCourier pEventCourier1 = new PEventCourier(this.getClass(), MessageEvent.MESSAGE_EVENT_OCTOPUS_AIDL_START_REGISTER);
                notifyNewMessage(pEventCourier1);//通知AIDL远程客户端
            }
        });
    }

    @Override
    public void unregisterListener(IMyAidlInterfaceListener iMyCarAidlInterfaceListener) {
        mListenerList.unregister(iMyCarAidlInterfaceListener);
        mRemoteCallbackCount--;
        ///int num = mListenerList.beginBroadcast();
        ///mListenerList.finishBroadcast();
        ///MMLog.d(TAG, "mListenerList.size=" + num);
    }

    @Override
    public void sendMessage(PEventCourier pEventCourier) throws RemoteException {
        MMLog.d(TAG, pEventCourier.toStr());
        ///Cabinet.getEventBus().post(pEventCourier);
    }

    @Override
    public void pausePlay() {
        if (Cabinet.getPlayManager().isPlaying()) Cabinet.getPlayManager().playPause();
    }

    @Override
    public void playPause() {
        Cabinet.getPlayManager().playPause();
    }

    @Override
    public void playNext() {
        Cabinet.getPlayManager().playNext();
    }

    @Override
    public void playPrev() {
        Cabinet.getPlayManager().playPre();
    }

    @Override
    public void playStop() {
        Cabinet.getPlayManager().stopPlay();
    }

    @Override
    public void playStopFree() {
        Cabinet.getPlayManager().stopPlay();
    }

    @Override
    public void playStopFreeFree() {
        Cabinet.getPlayManager().stopPlay();
    }

    @Override
    public void startPlay(String fileName) {
        Cabinet.getPlayManager().startPlay(fileName);
    }

    @Override
    public boolean isPlaying() {
        return Cabinet.getPlayManager().isPlaying();
    }

    @Override
    public List<PMovie> getMediaList(int MsgID) {//获取媒体库媒体信息
        List<Movie> movies = new ArrayList<>();
        List<PMovie> pMovies = new ArrayList<>();
        if (Cabinet.getPlayManager().getMediaManager() != null) {
            switch (MsgID) {
                case MessageEvent.MESSAGE_EVENT_LOCAL_VIDEO:
                    movies = Cabinet.getPlayManager().getMediaManager().getLocalVideoSession().getVideoList().toList();
                    return transformToPMovie(movies);
                case MessageEvent.MESSAGE_EVENT_USB_VIDEO:
                    movies = Cabinet.getPlayManager().getMediaManager().getUSBVideoSession().getVideoList().toList();
                    return transformToPMovie(movies);
                case MessageEvent.MESSAGE_EVENT_SD_VIDEO:
                    movies = Cabinet.getPlayManager().getMediaManager().getSDVideoSession().getVideoList().toList();
                    return transformToPMovie(movies);
                case MessageEvent.MESSAGE_EVENT_LOCAL_AUDIO:
                    movies = Cabinet.getPlayManager().getMediaManager().getLocalAudioSession().getVideoList().toList();
                    return transformToPMovie(movies);
                case MessageEvent.MESSAGE_EVENT_USB_AUDIO:
                    movies = Cabinet.getPlayManager().getMediaManager().getUSBAudioSession().getVideoList().toList();
                    return transformToPMovie(movies);
                case MessageEvent.MESSAGE_EVENT_SD_AUDIO:
                    movies = Cabinet.getPlayManager().getMediaManager().getSDAudioSession().getVideoList().toList();
                    return transformToPMovie(movies);
            }
        }
        return pMovies;
    }

    private List<PMovie> transformToPMovie(List<Movie> list) {
        List<PMovie> pMovies = new ArrayList<>();
        for (Movie movie : list) {
            if (movie != null) pMovies.add(new PMovie(movie));
            else MMLog.log(TAG, "Movie object is null!");
        }
        return pMovies;
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

    public void notifyPlayerStatus(PlayerStatusInfo playerStatusInfo) {
        try {
            int num = mListenerList.beginBroadcast();
            for (int i = 0; i < num; ++i) {
                IMyAidlInterfaceListener listener = mListenerList.getBroadcastItem(i);
                if (playerStatusInfo.getObj() != null) {
                    OMedia oMedia = (OMedia) playerStatusInfo.getObj();
                    listener.onMessageMusice(playerStatusInfo.getEventType(), playerStatusInfo.getEventType(), playerStatusInfo.getTimeChanged(), playerStatusInfo.getLength(), oMedia.getPathName());
                }
            }
        } catch (RemoteException e) {
            MMLog.d(TAG, String.valueOf(e));
        } finally {
            mListenerList.finishBroadcast();
        }
    }
}
