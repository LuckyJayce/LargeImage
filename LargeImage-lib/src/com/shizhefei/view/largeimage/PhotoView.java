package com.shizhefei.view.largeimage;

import android.content.Context;
import android.util.AttributeSet;

import com.shizhefei.view.largeimage.PhotoViewAttacher.OnPhotoTapListener;

public class PhotoView extends LargeImageView {

	public PhotoView(Context context) {
		super(context);
		photoViewAttacher = new PhotoViewAttacher<PhotoView>(this);
	}

	public PhotoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		photoViewAttacher = new PhotoViewAttacher<PhotoView>(this);
	}

	public PhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		photoViewAttacher = new PhotoViewAttacher<PhotoView>(this);
	}

	public PhotoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		photoViewAttacher = new PhotoViewAttacher<PhotoView>(this);
	}

	private PhotoViewAttacher<PhotoView> photoViewAttacher;

	public void setOnPhotoTapListener(OnPhotoTapListener onPhotoTapListener) {
		photoViewAttacher.setOnPhotoTapListener(onPhotoTapListener);
	}

}
