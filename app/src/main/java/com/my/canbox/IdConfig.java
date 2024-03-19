package com.my.canbox;

import android.view.View.OnClickListener;

public class IdConfig {
	public int mConfig;
	public int mId;
	public int[][] mIds;
	public OnClickListener mOnClickListener;

	public IdConfig(int c, int id, int[][] ids, OnClickListener listenter) {
		mId = id;
		mConfig = c;
		mIds = ids;
		mOnClickListener = listenter;
	}

}