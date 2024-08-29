package com.zhuchao.android.car.canbox;


import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.zhuchao.android.car.R;
import com.zhuchao.android.car.cartype.CarUtil;
import com.zhuchao.android.car.manager.OSProManager;

public class ReverseUICanbox {
    private final static int[][] IDS_VW = {{R.id.backligt_layout, 0}, {R.id.close, 0}, {R.id.vw_show, 0}, {R.id.vw_color, 0}, {R.id.vw_0, 0xc64600}, {R.id.vw_1, 0xc64601}, {R.id.vw_2, 0xc64602}, {R.id.vw_3, 0xc64603}, {R.id.vw_r, 0xc64604}, {R.id.vw_l, 0xc64605}, {R.id.vw_lr, 0xc64606}, {R.id.vw_radar_v_off, 0xc6ab00}, {R.id.vw_radar_v_on, 0xc6ab01},

    };
    private Canbox mCanbox;
    private View mMainView;
    private int mReverseID;
    private SeekBar mLevel;    private final OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            int id = arg0.getId();
            if (id == R.id.backligt_layout) {
                showBackLightControl(false);
            } else if (id == R.id.close) {
                if (OSProManager.mSimulationReverse == 0) {
                    setViewVisible(R.id.vw_show, 1);
                    setViewVisible(R.id.layout_canbus_vw_main, 0);
                } else {
                    OSProManager.simulationReverse((byte) 0);
                }
            } else if (id == R.id.vw_show) {
                setViewVisible(R.id.vw_show, 0);
                setViewVisible(R.id.layout_canbus_vw_main, 1);
            } else if (id == R.id.vw_color) {
                showBackLightControl(true);
            } else {
                int[][] ids = getIds();

                for (int[] ii : ids) {
                    if (ii[0] == id) {
                        mCanbox.sendReverseCmd(ii[1]);
                    }
                }
            }

        }
    };
    private SeekBar mLevelContrast;    private final IdConfig[] mIdConfig = {new IdConfig(46, R.id.layout_canbus_vw, IDS_VW, mOnClickListener)

    };
    private SeekBar mLevelSaturation;
    private TextView mTvBacklight;
    private TextView mTvConrast;
    private TextView mTvSaturation;

    private void showBackLightControl(boolean s) {
        setViewVisible(R.id.backligt_layout, s ? 1 : 0);
    }

    private int[][] getIds() {
        for (IdConfig config : mIdConfig) {
            if (config.mConfig == mReverseID) {
                return config.mIds;
            }
        }
        return null;
    }

    public void init(View v) {
        mCanbox = CarUtil.getCanboxInstance();

        if (mCanbox != null) {
            mReverseID = mCanbox.getReverseViewID();
            if (mReverseID != Integer.MAX_VALUE) {
                mMainView = v;
                initData(mReverseID);
                switch (mReverseID) {
                    case 13:
                    case 46:
                        initVW();
                        break;
                }
            }
        }

    }

    private void initVW() {
        updateView(0x40, 0);

        mTvBacklight = mMainView.findViewById(R.id.backlight_text);
        mTvConrast = mMainView.findViewById(R.id.contrast_text);
        mTvSaturation = mMainView.findViewById(R.id.saturation_text);

        mMainView.findViewById(R.id.contrast_layout).setVisibility(View.VISIBLE);
        mMainView.findViewById(R.id.saturation_layout).setVisibility(View.VISIBLE);

        mLevelContrast = mMainView.findViewById(R.id.contrast_level);
        mLevelContrast.setMax(40);
        mLevelContrast.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    progress += 30;
                    mCanbox.sendReverseCmd((0xc6 << 16 | 0x49 << 8 | (progress)));
                }
            }
        });

        mLevelSaturation = mMainView.findViewById(R.id.saturation_level);
        mLevelSaturation.setMax(40);
        mLevelSaturation.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    progress += 30;
                    mCanbox.sendReverseCmd((0xc6 << 16 | 0x48 << 8 | (progress)));
                }
            }
        });

        mLevel = mMainView.findViewById(R.id.level);
        mLevel.setMax(40);
        mLevel.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    progress += 30;
                    mCanbox.sendReverseCmd((0xc6 << 16 | 0x47 << 8 | (progress)));
                }
            }
        });
    }

    private void initData(int id) {
        for (IdConfig config : mIdConfig) {
            if (config.mConfig == id) {
                mMainView = mMainView.findViewById(config.mId);
                if (mMainView != null) {
                    for (int[] i : config.mIds) {
                        View v = mMainView.findViewById(i[0]);
                        if (v != null) {
                            v.setOnClickListener(mOnClickListener);
                        }
                    }
                }
            }
        }
    }

    private void setViewVisible(int id, int visible) {
        View v = mMainView.findViewById(id);
        if (v != null) {
            v.setVisibility(visible == 0 ? View.GONE : View.VISIBLE);
        }
    }

    private void setViewSrc(int id, int resId) {
        ImageView v = mMainView.findViewById(id);
        if (v != null) {
            v.setImageResource(resId);
        }
    }

    private void setViewSelect(int id, int sel) {
        View v = mMainView.findViewById(id);
        if (v != null) {
            if (sel == 1) {
                v.setAlpha(0.6f);
            } else {
                v.setAlpha(1f);
            }
        }
    }

    private void updateView(int arg1, int arg2) {
        switch (mReverseID) {
            case 13:
            case 46:
                updateVWView(arg1, arg2);
                break;
        }
    }

    private void updateVWView(int arg1, int arg2) {
        if (arg1 == 0x40) {
            if ((arg2 & 0x400) != 0 && ((arg2 & 0x70) != 0x70)) {
                mMainView.setVisibility(View.VISIBLE);
            } else {
                mMainView.setVisibility(View.GONE);
            }

            switch ((arg2 & 0x70) >> 4) {
                case 0:
                    setViewVisible(R.id.vw_0, 1);
                    setViewVisible(R.id.vw_1, 0);
                    setViewVisible(R.id.vw_2, 1);
                    setViewVisible(R.id.vw_3, 1);
                    setViewVisible(R.id.vw_l, 0);
                    setViewVisible(R.id.vw_r, 0);
                    setViewVisible(R.id.vw_lr, 0);

                    setViewSrc(R.id.vw_2, R.drawable.vw_22);
                    setViewSrc(R.id.vw_3, R.drawable.vw_32);
                    break;
                case 2:
                    setViewVisible(R.id.vw_0, 1);
                    setViewVisible(R.id.vw_1, 1);
                    setViewVisible(R.id.vw_2, 1);
                    setViewVisible(R.id.vw_3, 1);
                    setViewVisible(R.id.vw_l, 0);
                    setViewVisible(R.id.vw_r, 0);
                    setViewVisible(R.id.vw_lr, 0);
                    setViewSrc(R.id.vw_2, R.drawable.vw_2);
                    setViewSrc(R.id.vw_3, R.drawable.vw_3);
                    break;

                case 3:
                case 1:
                    setViewVisible(R.id.vw_0, 0);
                    setViewVisible(R.id.vw_1, 0);
                    setViewVisible(R.id.vw_2, 0);
                    setViewVisible(R.id.vw_3, 0);
                    setViewVisible(R.id.vw_l, 1);
                    setViewVisible(R.id.vw_r, 1);
                    setViewVisible(R.id.vw_lr, 1);
                    break;
            }
            setViewSelect(R.id.vw_0, 0);
            setViewSelect(R.id.vw_1, 0);
            setViewSelect(R.id.vw_2, 0);
            setViewSelect(R.id.vw_3, 0);
            setViewSelect(R.id.vw_l, 0);
            setViewSelect(R.id.vw_r, 0);
            setViewSelect(R.id.vw_lr, 0);
            switch ((arg2 & 0xf)) {
                case 0:
                    setViewSelect(R.id.vw_0, 1);
                    break;
                case 1:
                    setViewSelect(R.id.vw_1, 1);
                    break;
                case 2:
                    setViewSelect(R.id.vw_2, 1);
                    break;
                case 3:
                    setViewSelect(R.id.vw_3, 1);
                    break;
                case 4:
                    setViewSelect(R.id.vw_r, 1);
                    break;
                case 5:
                    setViewSelect(R.id.vw_l, 1);
                    break;
                case 6:
                    setViewSelect(R.id.vw_lr, 1);
                    break;
            }
        } else if (arg1 == 0xB0) {
            int b = arg2 & 0xff;
            int s = (arg2 & 0xff00) >> 8;
            int c = (arg2 & 0xff0000) >> 16;

            mTvBacklight.setText(String.valueOf(b));
            mTvBacklight.setVisibility(View.VISIBLE);
            mTvSaturation.setText(String.valueOf(s));
            mTvConrast.setText(String.valueOf(c));

            mLevel.setProgress(b - 30);
            mLevelSaturation.setProgress(s - 30);
            mLevelContrast.setProgress(c - 30);
        }
    }

    public void doMsg(int msg, int arg1, int arg2) {
        try {
            if (msg == Canbox.CANBOX_VW_RAISE_UI_DATA) {
                updateView(arg1, arg2);
            }
        } catch (Exception e) {

        }
    }




}
