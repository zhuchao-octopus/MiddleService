package com.my.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;
import com.my.out.R;

public class LedView extends View {

	private final static int []DRAWABLE_ID = {R.drawable.a,
		R.drawable.b,
		R.drawable.c,
		R.drawable.d,
		R.drawable.e,
		R.drawable.f,
		R.drawable.g,
		R.drawable.h,
		R.drawable.i,
		R.drawable.j,
		R.drawable.k,
		R.drawable.l,
		R.drawable.m,
		R.drawable.n,
		R.drawable.o,
		R.drawable.p,
		R.drawable.q,
		R.drawable.r,
		R.drawable.s,};
	
	private Context mContext;
	public LedView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LedView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		mContext = context;
	}
	
	public void updateView(int show) {
		LayerDrawable background = null;

		if (show != 0) {
			int num = 0;
			for (int i = 0; i < DRAWABLE_ID.length; ++i) {
				if ((show & (0x1 << i)) != 0) {
					num++;
				}
			}
			if (num != 0) {
				Drawable[] d = new Drawable[num];
				int j = 0;
				for (int i = 0; i < DRAWABLE_ID.length; ++i) {
					if ((show & (0x1 << i)) != 0) {
						d[j] = mContext.getDrawable(DRAWABLE_ID[i]);
						++j;
					}
				}
				background = new LayerDrawable(d);
			}
		}
		setBackground(background);
	}
}
