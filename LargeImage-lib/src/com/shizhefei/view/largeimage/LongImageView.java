package com.shizhefei.view.largeimage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;

public class LongImageView extends LargeImageView {

	public LongImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public LongImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public LongImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LongImageView(Context context) {
		super(context);
	}

	@Override
	public void onImageLoadFinished(final int imageWidth, final int imageHeight) {
		super.onImageLoadFinished(imageWidth, imageHeight);
		post(new Runnable() {
			@Override
			public void run() {
				setLayout(imageWidth, imageHeight);
			}
		});
	}

	private void setLayout(int imageWidth, int imageHeight) {
		int layoutWidth = getWidth();
		LayoutParams layoutParams = getLayoutParams();
		layoutParams.height = (int) (1.0f * imageHeight * layoutWidth / imageWidth);
		setLayoutParams(layoutParams);
	}
}
