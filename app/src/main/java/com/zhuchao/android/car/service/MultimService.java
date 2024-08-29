package com.zhuchao.android.car.service;

import static android.media.session.PlaybackState.ACTION_SKIP_TO_NEXT;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.Rating;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.service.media.MediaBrowserService;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zhuchao.android.car.aidl.PEventCourier;
import com.zhuchao.android.fbase.DataID;
import com.zhuchao.android.fbase.EventCourier;
import com.zhuchao.android.fbase.FileUtils;
import com.zhuchao.android.fbase.MMLog;
import com.zhuchao.android.fbase.MessageEvent;
import com.zhuchao.android.fbase.MethodThreadMode;
import com.zhuchao.android.fbase.PlaybackEvent;
import com.zhuchao.android.fbase.PlayerStatusInfo;
import com.zhuchao.android.fbase.TAppProcessUtils;
import com.zhuchao.android.fbase.TCourierSubscribe;
import com.zhuchao.android.fbase.eventinterface.PlayerCallback;
import com.zhuchao.android.session.Cabinet;
import com.zhuchao.android.session.TPlayManager;
import com.zhuchao.android.video.OMedia;
import com.zhuchao.android.video.VideoList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class MultimService extends MediaBrowserService implements PlayerCallback {
    ///private int mPosition = -1;
    public static final String MEDIA_ID_ROOT = "_ROOT_";
    private static final String TAG = "MultimService";
    ///private VideoList mPlayBeanList = new VideoList();
    ///private Context mContext;
    @SuppressLint("StaticFieldLeak")
    private static TPlayManager tPlayManager = null;
    private final IBinderProxyMedia mIBinderProxyMedia = new IBinderProxyMedia();
    //private MediaPlayer mMediaPlayer;
    private final MediaSession.Callback mSessionCallback = new MediaSession.Callback() {
        @Override
        public void onPrepare() {
            super.onPrepare();
            MMLog.d(TAG, "MediaSessionCompat.Callback onPrepare");
        }

        @Override
        public void onPrepareFromMediaId(String mediaId, Bundle extras) {
            super.onPrepareFromMediaId(mediaId, extras);
            MMLog.d(TAG, "MediaSessionCompat.Callback onPrepareFromMediaId");
        }

        @Override
        public void onPrepareFromSearch(String query, Bundle extras) {
            super.onPrepareFromSearch(query, extras);
            MMLog.d(TAG, "MediaSessionCompat.Callback onPrepareFromSearch");
        }

        @Override
        public void onPrepareFromUri(Uri uri, Bundle extras) {
            super.onPrepareFromUri(uri, extras);
            MMLog.d(TAG, "MediaSessionCompat.Callback onPrepareFromUri");
        }

        @Override
        public void onPlay() {
            super.onPlay();
            //处理播放器 的播放逻辑 车载应用的话，别忘了处理音频焦点
            MMLog.d(TAG, "MediaSessionCompat.Callback onPlay");
        }

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
            super.onPlayFromSearch(query, extras);
            MMLog.d(TAG, "MediaSessionCompat.Callback onPlayFromSearch");
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
            MMLog.d(TAG, "MediaSessionCompat.Callback onPlayFromMediaId");
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);
            MMLog.d(TAG, "MediaSessionCompat.Callback onPlayFromUri");
        }

        @Override
        public void onPause() {
            super.onPause();
            MMLog.d(TAG, "MediaSessionCompat.Callback onPause");
        }

        @Override
        public void onStop() {
            super.onStop();
            MMLog.d(TAG, "MediaSessionCompat.Callback onStop");
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            MMLog.d(TAG, "MediaSessionCompat.Callback onSkipToPrevious");
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            MMLog.d(TAG, "MediaSessionCompat.Callback onSkipToNext");
        }

        @Override
        public void onSkipToQueueItem(long id) {
            MMLog.d(TAG, "MediaSessionCompat.Callback onSkipToQueueItem");
            super.onSkipToQueueItem(id);
        }

        @Override
        public void onFastForward() {
            super.onFastForward();
            MMLog.d(TAG, "MediaSessionCompat.Callback onFastForward");
        }

        @Override
        public void onRewind() {
            super.onRewind();
            MMLog.d(TAG, "MediaSessionCompat.Callback onRewind");
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            MMLog.d(TAG, "MediaSessionCompat.Callback onSeekTo");
        }

        @Override
        public void onSetRating(@NonNull Rating rating) {
            super.onSetRating(rating);
            MMLog.d(TAG, "MediaSessionCompat.Callback onSetRating");
        }

        @Override
        public void onSetPlaybackSpeed(float speed) {
            super.onSetPlaybackSpeed(speed);
            MMLog.d(TAG, "MediaSessionCompat.Callback onSetPlaybackSpeed");
        }

        @Override
        public boolean onMediaButtonEvent(@NonNull Intent mediaButtonEvent) {
            MMLog.d(TAG, "MediaSessionCompat.Callback onMediaButtonEvent " + mediaButtonEvent);
            return super.onMediaButtonEvent(mediaButtonEvent);
        }

        @Override
        public void onCustomAction(@NonNull String action, @Nullable Bundle extras) {
            super.onCustomAction(action, extras);
            MMLog.d(TAG, "MediaSessionCompat.Callback onCustomAction");
        }

        @Override
        public void onCommand(@NonNull String command, @Nullable Bundle args, @Nullable ResultReceiver cb) {
            super.onCommand(command, args, cb);
            MMLog.d(TAG, "MediaSessionCompat.Callback onCommand");
        }
    };
    private final BroadcastReceiver mUserEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MMLog.d(TAG, "mUserEventReceiver action=" + intent.getAction() + " " + TAppProcessUtils.getCurrentProcessNameAndId(context));

            switch (Objects.requireNonNull(intent.getAction())) {

                case MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_PLAY_PAUSE:
                    if (tPlayManager != null) tPlayManager.playPause();
                    break;
                case MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_PLAY:
                    if (tPlayManager != null) {
                        String musicName = intent.getStringExtra("file-name");
                        if (!FileUtils.EmptyString(musicName) && FileUtils.existFile(musicName)) tPlayManager.startPlay(musicName);
                        else tPlayManager.autoPlay();
                    }
                    break;
                case MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_PAUSE:
                    if (tPlayManager != null && tPlayManager.isPlaying()) tPlayManager.playPause();
                    break;
                case MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_NEXT:
                    if (tPlayManager != null) tPlayManager.playNext();
                    break;
                case MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_PREV:
                    if (tPlayManager != null) tPlayManager.playPre();
                    break;
                case MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_AUTO_PLAY:
                    if (tPlayManager != null) {
                        tPlayManager.setPlayOrder(DataID.PLAY_MANAGER_PLAY_ORDER2);//循环顺序播放
                        tPlayManager.setAutoPlaySource(DataID.SESSION_SOURCE_ALL);//自动播放源列表
                        tPlayManager.autoPlay();
                    }
                    break;
                case MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_DISABLE_AUTO_PLAY:
                    break;
            }
        }
    };
    public boolean isHaveAudioFocus = false;
    AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            MMLog.d(TAG, "onAudioFocusChange  focusChange=" + focusChange + ", before isHaveAudioFocus=" + isHaveAudioFocus);
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    isHaveAudioFocus = false;
                    mSessionCallback.onPause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    isHaveAudioFocus = false;
                    MMLog.d(TAG, " AUDIO_FOCUS_LOSS_TRANSIENT  ");
                    //handlePause(false);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // TODO: 2019-07-31   降低音量
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    isHaveAudioFocus = true;
                    mSessionCallback.onPlay();
                    break;
                case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                    ;
                    break;
                default:
                    break;
            }
        }
    };
    ///private boolean mAutoPlay = false;
    private MediaSession mMediaSessionCompat;
    ///private MediaBrowser mMediaBrowserCompat;
    ///private MediaController mMediaControllerCompat;
    private PlaybackState mPlaybackState;
    private AudioManager mAudioManager;
    private PlayerStatusInfo playerStatusInfo = null;

    /*
    private final MediaBrowser.SubscriptionCallback mBrowserSubscriptionCallback = new MediaBrowser.SubscriptionCallback() {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowser.MediaItem> children) {
            ///Log.e(TAG, "onChildrenLoaded------" + children);
            ///list.clear();
            //children 即为Service发送回来的媒体数据集合
            ///for (MediaBrowser.MediaItem item : children) {
            ///Log.e(TAG, (String) item.getDescription().getTitle());
            ///list.add(item);
            ///}
            ///demoAdapter.notifyDataSetChanged();
        }
    };*/
    /*
    private final MediaController.Callback mMediaControllerCompatCallback = new MediaController.Callback() {
        //蓝牙音乐信息变化之后在这里进行回调
        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            updatePlayState(state);
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onSessionEvent(@NonNull String event, @Nullable Bundle extras) {
            super.onSessionEvent(event, extras);
        }

        @Override
        public void onQueueChanged(@Nullable List<MediaSession.QueueItem> queue) {
            super.onQueueChanged(queue);
        }

        @Override
        public void onQueueTitleChanged(@Nullable CharSequence title) {
            super.onQueueTitleChanged(title);
        }

        @Override
        public void onExtrasChanged(@Nullable Bundle extras) {
            super.onExtrasChanged(extras);
        }

        @Override
        public void onAudioInfoChanged(MediaController.PlaybackInfo info) {
            super.onAudioInfoChanged(info);
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            updatePlayMetadata(metadata);
        }
    };*/
    /*
    private final MediaBrowser.ConnectionCallback mBrowserConnectionCallback = new MediaBrowser.ConnectionCallback() {
        @Override
        public void onConnected() {
            MMLog.d(TAG, "MediaBrowserCompat.ConnectionCallback onConnected!");
            if (mMediaBrowserCompat.isConnected()) {
                String mMediaId = mMediaBrowserCompat.getRoot();
                mMediaBrowserCompat.unsubscribe(mMediaId);
                mMediaBrowserCompat.subscribe(mMediaId, mBrowserSubscriptionCallback);

                mMediaControllerCompat = new MediaController(mContext, mMediaSessionCompat.getSessionToken());
                //注册蓝牙音乐信息状态监听
                mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback);
                if (mMediaControllerCompat.getMetadata() != null) {
                    updatePlayMetadata(mMediaControllerCompat.getMetadata());
                    updatePlayState(mMediaControllerCompat.getPlaybackState());
                }
            }
        }

        @Override
        public void onConnectionFailed() {
            MMLog.d(TAG, "onConnectionFailed！");
        }
    };*/
    /*
    private void updatePlayState(PlaybackState state) {
        if (state == null) {
            return;
        }
        switch (state.getState()) {
            case PlaybackState.STATE_NONE://无任何状态
                break;
            case PlaybackState.STATE_PAUSED:
            case PlaybackState.STATE_PLAYING:
            case PlaybackState.STATE_BUFFERING:
            case PlaybackState.STATE_CONNECTING:
            case PlaybackState.STATE_ERROR:
            case PlaybackState.STATE_FAST_FORWARDING:
            case PlaybackState.STATE_REWINDING:
            case PlaybackState.STATE_SKIPPING_TO_NEXT:
            case PlaybackState.STATE_SKIPPING_TO_PREVIOUS:
            case PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM:
            case PlaybackState.STATE_STOPPED:
                break;
        }
    }*/
    /*
    private void updatePlayMetadata(MediaMetadata metadata) {
        if (metadata == null) return;
        ///更新曲目信息
    }
    */
    public static ArrayList<MediaBrowser.MediaItem> transformPlayList(VideoList videoList) {
        ArrayList<MediaBrowser.MediaItem> mediaItems = new ArrayList<>();
        if (videoList != null) {
            for (HashMap.Entry<String, Object> m : videoList.getMap().entrySet()) {
                OMedia oMedia = (OMedia) m.getValue();
                MediaMetadata metadata = new MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_MEDIA_ID, oMedia.getPathName()).putString(MediaMetadata.METADATA_KEY_TITLE, oMedia.getName()).putString(MediaMetadata.METADATA_KEY_ARTIST, oMedia.getMovie().getArtist()).putLong(MediaMetadata.METADATA_KEY_DURATION, oMedia.getMovie().getDuration()).build();
                mediaItems.add(createMediaItem(metadata));
            }
        } else if (tPlayManager != null) {
            VideoList videoList1 = tPlayManager.getAllMedia();
            for (HashMap.Entry<String, Object> m : videoList1.getMap().entrySet()) {
                OMedia oMedia = (OMedia) m.getValue();
                MediaMetadata metadata = new MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_MEDIA_ID, oMedia.getPathName()).putString(MediaMetadata.METADATA_KEY_TITLE, oMedia.getName()).putString(MediaMetadata.METADATA_KEY_ARTIST, oMedia.getMovie().getArtist()).putLong(MediaMetadata.METADATA_KEY_DURATION, oMedia.getMovie().getDuration()).build();
                mediaItems.add(createMediaItem(metadata));
            }
        }
        return mediaItems;
    }

    private static MediaBrowser.MediaItem createMediaItem(MediaMetadata metadata) {
        return new MediaBrowser.MediaItem(metadata.getDescription(), MediaBrowser.MediaItem.FLAG_PLAYABLE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MMLog.d(TAG, TAG + " onCreate! " + TAppProcessUtils.getCurrentProcessNameAndId(this));
        Cabinet.getEventBus().registerEventObserver(this);
        ///mContext = this;
        mPlaybackState = new PlaybackState.Builder().setState(PlaybackState.STATE_NONE, 0, 1.0f).setActions(getAvailableActions(PlaybackState.STATE_NONE)).build();
        ///mPlaybackState = new PlaybackState.Builder().setState(PlaybackState.STATE_NONE,0,1.0f).build();

        mMediaSessionCompat = new MediaSession(this, TAG);
        mMediaSessionCompat.setCallback(mSessionCallback);
        mMediaSessionCompat.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSessionCompat.setPlaybackState(mPlaybackState);
        mMediaSessionCompat.setActive(true);
        /// 设置token后会触发MediaBrowserCompat.ConnectionCallback的回调方法
        /// 表示MediaBrowser与MediaBrowserService连接成功
        setSessionToken(mMediaSessionCompat.getSessionToken());
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        tPlayManager = TPlayManager.getInstance();
        tPlayManager.registerStatusListener(this);
        tPlayManager.initialMediaLibrary();///初始化媒体资源库
        ///tPlayManager.printAllEventListener();
        ///BlueToothManager.getBlueToothStatus();
        registerUserEventReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = null;
        if (intent != null) action = intent.getAction();
        ///MMLog.d(TAG, TAG + " onStartCommand action=" + action);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        ///return super.onBind(intent);
        return mIBinderProxyMedia;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaSessionCompat != null) {
            mMediaSessionCompat.release();
            mMediaSessionCompat = null;
            //mMediaControllerCompat.unregisterCallback(mMediaControllerCompatCallback);
        }
        Cabinet.getEventBus().unRegisterEventObserver(this);
        unregisterUserEventBroadcastListener();
        MMLog.d(TAG, TAG + " onDestroy!");
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        MMLog.d(TAG, TAG + " onGetRoot! " + clientPackageName + " clientUid=" + clientUid);
        return new BrowserRoot(MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowser.MediaItem>> result) {
        MMLog.d(TAG, TAG + " onLoadChildren!");
        // 将信息从当前线程中移除，允许后续调用sendResult方法
        result.detach();
        // 我们模拟获取数据的过程，真实情况应该是异步从网络或本地读取数据
        ArrayList<MediaBrowser.MediaItem> mediaItems = transformPlayList(null);
        // 向Browser发送 播放列表数据
        result.sendResult(mediaItems);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private int requestAudioFocus() {
        int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        isHaveAudioFocus = AudioManager.AUDIOFOCUS_REQUEST_GRANTED == result;
        ///if (isHaveAudioFocus) {
        ///    mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceive);
        ///}
        MMLog.d(TAG, "requestAudioFocus " + isHaveAudioFocus);
        return result;
    }

    private void autoAudioFocus() {
        int result = mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        isHaveAudioFocus = AudioManager.AUDIOFOCUS_REQUEST_GRANTED == result;
    }

    public long getAvailableActions(int state) {
        long actions = PlaybackState.ACTION_SKIP_TO_PREVIOUS | ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_REWIND | PlaybackState.ACTION_FAST_FORWARD;
        if (state == PlaybackState.STATE_PLAYING) {
            actions |= PlaybackState.ACTION_PAUSE;
        } else {
            actions |= PlaybackState.ACTION_PLAY;
        }
        return actions;
    }

    @Override
    public void onEventPlayerStatus(PlayerStatusInfo playerStatusInfo) {
        switch (playerStatusInfo.getEventType()) {
            case PlaybackEvent.Status_NothingIdle:
            case PlaybackEvent.Status_Stopped:
                mPlaybackState = new PlaybackState.Builder().setState(PlaybackState.STATE_STOPPED, 0, 1.0f).build();
                mMediaSessionCompat.setPlaybackState(mPlaybackState);
                break;
            case PlaybackEvent.Status_Error:
                mPlaybackState = new PlaybackState.Builder().setState(PlaybackState.STATE_ERROR, 0, 1.0f).build();
                mMediaSessionCompat.setPlaybackState(mPlaybackState);
                break;
            case PlaybackEvent.MediaChanged:
            case PlaybackEvent.Status_Opening:
            case PlaybackEvent.Status_Changed:
                break;
            case PlaybackEvent.Status_Buffering:
                mPlaybackState = new PlaybackState.Builder().setState(PlaybackState.STATE_BUFFERING, 0, 1.0f).build();
                mMediaSessionCompat.setPlaybackState(mPlaybackState);
                break;
            case PlaybackEvent.Status_Playing:
                mPlaybackState = new PlaybackState.Builder().setState(PlaybackState.STATE_PLAYING, 0, 1.0f).build();
                mMediaSessionCompat.setPlaybackState(mPlaybackState);
                break;
            case PlaybackEvent.Status_Paused:
                mPlaybackState = new PlaybackState.Builder().setState(PlaybackState.STATE_PAUSED, 0, 1.0f).build();
                mMediaSessionCompat.setPlaybackState(mPlaybackState);
                break;
            case PlaybackEvent.Status_Next:
                mPlaybackState = new PlaybackState.Builder().setState(PlaybackState.STATE_SKIPPING_TO_NEXT, 0, 1.0f).build();
                mMediaSessionCompat.setPlaybackState(mPlaybackState);
                break;
            case PlaybackEvent.Status_Prev:
                mPlaybackState = new PlaybackState.Builder().setState(PlaybackState.STATE_SKIPPING_TO_PREVIOUS, 0, 1.0f).build();
                mMediaSessionCompat.setPlaybackState(mPlaybackState);
                break;
            case PlaybackEvent.Status_Ended:
                ;
                break;
        }

        this.playerStatusInfo = playerStatusInfo;
        if (mIBinderProxyMedia.getListenerCount() > 0) {///通知远程客户端
            PEventCourier pEventCourier = new PEventCourier(this.getClass(), MessageEvent.MESSAGE_EVENT_OCTOPUS_PLAYING_STATUS);
            Cabinet.getEventBus().post(pEventCourier);
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerUserEventReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_PLAY_PAUSE);
        intentFilter.addAction(MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_PLAY);
        intentFilter.addAction(MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_PAUSE);
        intentFilter.addAction(MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_NEXT);
        intentFilter.addAction(MessageEvent.MESSAGE_EVENT_OCTOPUS_ACTION_PREV);
        registerReceiver(mUserEventReceiver, intentFilter);
    }

    private void unregisterUserEventBroadcastListener() {
        try {
            unregisterReceiver(mUserEventReceiver);
        } catch (Exception ignored) {
        }
    }

    @TCourierSubscribe(threadMode = MethodThreadMode.threadMode.BACKGROUND)
    public boolean onTCourierSubscribeEvent(EventCourier eventCourier) {
        ///MMLog.d(TAG, eventCourier.toStr());
        switch (eventCourier.getId()) {
            case MessageEvent.MESSAGE_EVENT_USB_EJECT:
                String subName = null;
                if (eventCourier.getObj() != null) {
                    Intent intent = (Intent) eventCourier.getObj();
                    Bundle bundle = intent.getExtras();
                    Uri data = intent.getData();
                    if (data != null) subName = data.getPath();
                    //MMLog.d(TAG,"url="+data.getPath());
                }
                if (tPlayManager != null && subName != null) {
                    if (tPlayManager.getPlayingMedia() != null) {
                        if (tPlayManager.getPlayingMedia().getPathName() != null) {
                            if (tPlayManager.isPlaying() && tPlayManager.getPlayingMedia().getPathName().contains(subName)) tPlayManager.stopIdle();
                        }
                    }
                }
                break;
        }
        return true;
    }

    ///ADIL 跨进程通信
    @TCourierSubscribe(threadMode = MethodThreadMode.threadMode.BACKGROUND)
    public boolean onTCourierSubscribeEventAidl(PEventCourier pEventCourier) {
        ///MMLog.d(TAG, pEventCourier.toStr());
        switch (pEventCourier.getId()) {

            case MessageEvent.MESSAGE_EVENT_OCTOPUS_PLAY_PAUSE:
                if (tPlayManager != null) tPlayManager.playPause();
                break;
            case MessageEvent.MESSAGE_EVENT_OCTOPUS_PLAY:
                if (tPlayManager != null) tPlayManager.autoPlay();
                break;
            case MessageEvent.MESSAGE_EVENT_OCTOPUS_PAUSE:
                if (tPlayManager != null && tPlayManager.isPlaying()) tPlayManager.playPause();
                break;
            case MessageEvent.MESSAGE_EVENT_OCTOPUS_NEXT:
                if (tPlayManager != null) tPlayManager.playNext();
                break;
            case MessageEvent.MESSAGE_EVENT_OCTOPUS_PREV:
                if (tPlayManager != null) tPlayManager.playPre();
                break;

            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //通知AIDL远程客户端，媒体库状态
            case MessageEvent.MESSAGE_EVENT_LOCAL_VIDEO:
            case MessageEvent.MESSAGE_EVENT_USB_VIDEO:
            case MessageEvent.MESSAGE_EVENT_SD_VIDEO:
            case MessageEvent.MESSAGE_EVENT_LOCAL_AUDIO:
            case MessageEvent.MESSAGE_EVENT_USB_AUDIO:
            case MessageEvent.MESSAGE_EVENT_SD_AUDIO:
                PEventCourier pEventCourier1 = new PEventCourier(this.getClass(), pEventCourier.getId());
                mIBinderProxyMedia.notifyNewMessage(pEventCourier1);//通知AIDL远程客户端
                ///if (mAutoPlay && tPlayManager != null) tPlayManager.autoPlay();
                break;

            case MessageEvent.MESSAGE_EVENT_OCTOPUS_PLAYING_STATUS:
                if (this.playerStatusInfo != null) mIBinderProxyMedia.notifyPlayerStatus(this.playerStatusInfo);
                break;
        }
        return true;
    }
}
