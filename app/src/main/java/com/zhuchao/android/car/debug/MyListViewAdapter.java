package com.zhuchao.android.car.debug;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.zhuchao.android.car.R;


public class MyListViewAdapter extends ArrayAdapter<String> {
	private final ArrayList<String> mList = new ArrayList<String>();

	private final int mLayout;
	private final Context mActivity;
	private final int mTextId;
	LayoutInflater mInflater;

	public MyListViewAdapter(Context context, int layout) {
		super(context, layout);

		mTextId = R.id.list_text;
		mLayout = layout;
		mActivity = context;
		mInflater = LayoutInflater.from(mActivity);
	}

	public int getCount() {
		if (mList == null)
			return 0;
		return mList.size();
	}
	
	public void addData(String s) {
		mList.add(s);
	}

	public static class ViewHolder {
		public TextView text;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (mList == null)
			return null;

		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(mLayout, null, false);
			viewHolder = new ViewHolder();
			viewHolder.text = convertView.findViewById(mTextId);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.text.setText(mList.get(position));
		return convertView;

	}

}
