/*
Copyright 2015 shizhefei（LuckyJayce）
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
   http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.shizhefei.view.largeimage;

import java.io.InputStream;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;

import com.shizhefei.view.largeimage.ImageManager.DrawData;
import com.shizhefei.view.largeimage.ImageManager.OnImageLoadListenner;

public class LargeImageView extends UpdateView implements IPhotoView, OnImageLoadListenner {

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public LargeImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		imageManager = new ImageManager(context);
	}

	public LargeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		imageManager = new ImageManager(context);
	}

	public LargeImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		imageManager = new ImageManager(context);
	}

	public LargeImageView(Context context) {
		super(context);
		imageManager = new ImageManager(context);
	}

	public void setScale(float scale, float offsetX, float offsetY) {
		mScale.setScale(scale);
		mScale.setFromX((int) offsetX);
		mScale.setFromY((int) offsetY);
		notifyInvalidate();
	}

	public int getImageWidth() {
		if (imageManager != null) {
			return imageManager.getWidth();
		}
		return 0;
	}

	public int getImageHeight() {
		if (imageManager != null) {
			return imageManager.getHeight();
		}
		return 0;
	}

	public Scale getScale() {
		return mScale;
	}

	private Scale mScale = new Scale(1, 0, 0);
	private ImageManager imageManager;

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		imageManager.start(this);
	}

	@Override
	protected void onDetachedFromWindow() {
		imageManager.destroy();
		super.onDetachedFromWindow();
	}

	private Drawable drawable;

	public void setDefaulImage(Drawable drawable) {
		this.drawable = drawable;
	}

	public void setImage(String filePath) {
		mScale.setScale(1);
		mScale.fromX = 0;
		mScale.fromY = 0;
		imageManager.load(filePath);
	}

	public void setImage(InputStream inputStream) {
		mScale.setScale(1);
		mScale.fromX = 0;
		mScale.fromY = 0;
		imageManager.load(inputStream);
	}

	@Override
	protected void onUpdateWindow(Rect visiableRect) {
		preInvalidateTime = SystemClock.uptimeMillis();
		runnable = null;
		invalidate(getVisiableRect());
	}

	private volatile long preInvalidateTime;
	private volatile Runnable runnable;

	// 1000毫秒/60帧 = 16.6666秒 一帧 = 17秒 一帧
	private static final int LOOP_TIME = 17;

	private void notifyInvalidate() {
		// 避免更新太过频繁，设置最小LOOP_TIME毫秒的更新间隔

		// 和上次的间隔时间
		long deltaTime = SystemClock.uptimeMillis() - preInvalidateTime;
		if (runnable != null) {
			return;
		}
		if (deltaTime < LOOP_TIME) {
			LargeImageView.this.postDelayed(runnable = new Runnable() {

				@Override
				public void run() {
					preInvalidateTime = SystemClock.uptimeMillis();
					runnable = null;
					Log.d("eeee", "preInvalidateTime:" + preInvalidateTime);
					invalidate(getVisiableRect());
				}
			}, LOOP_TIME - deltaTime);
		} else {
			// 处于主线程执行invalidate操作，否则post到主线程上执行ui操作
			if (Looper.getMainLooper() == Looper.myLooper()) {
				preInvalidateTime = SystemClock.uptimeMillis();
				runnable = null;
				Log.d("eeee", "preInvalidateTime:" + preInvalidateTime);
				invalidate(getVisiableRect());
			} else {
				LargeImageView.this.post(runnable = new Runnable() {

					@Override
					public void run() {
						preInvalidateTime = SystemClock.uptimeMillis();
						runnable = null;
						Log.d("eeee", "preInvalidateTime:" + preInvalidateTime);
						invalidate(getVisiableRect());
					}
				});
			}
		}
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		if (getWidth() == 0) {
			return;
		}

		Log.d("countTime", "----------------- mScale.scale" + mScale.scale);

		long startTime = SystemClock.uptimeMillis();

		Rect visiableRect = getVisiableRect();

		Log.d("countTime", "getVisiableRect " + (SystemClock.uptimeMillis() - startTime));
		startTime = SystemClock.uptimeMillis();

		Log.d("cccc", "onDraw onUpdateWindow " + visiableRect);
		if (!imageManager.hasLoad()) {
			if (drawable != null) {
				int saveCount = canvas.save();
				drawable.draw(canvas);
				canvas.restoreToCount(saveCount);
			}
			return;
		}

		int saveCount = canvas.save();
//		canvas.clipRect(visiableRect);

		Log.d("countTime", "clipRect " + (SystemClock.uptimeMillis() - startTime));
		startTime = SystemClock.uptimeMillis();

		float width = mScale.scale * getWidth();
		int imgWidth = imageManager.getWidth();

		Log.d("countTime", "getWidth " + (SystemClock.uptimeMillis() - startTime));

		float imageScale = imgWidth / width;

		// 需要显示的图片的实际宽度。
		Rect imageRect = new Rect();
		imageRect.left = (int) Math.ceil((visiableRect.left + mScale.fromX) * imageScale);
		imageRect.top = (int) Math.ceil((visiableRect.top + mScale.fromY) * imageScale);
		imageRect.right = (int) Math.ceil((visiableRect.right + mScale.fromX) * imageScale);
		imageRect.bottom = (int) Math.ceil((visiableRect.bottom + mScale.fromY) * imageScale);

		Log.d("countTime", "imageScale " + (SystemClock.uptimeMillis() - startTime));
		startTime = SystemClock.uptimeMillis();

		List<DrawData> drawData = imageManager.getDrawData(imageScale, imageRect);

		Log.d("countTime", "getDrawData " + (SystemClock.uptimeMillis() - startTime));
		startTime = SystemClock.uptimeMillis();

		for (DrawData data : drawData) {
			Rect drawRect = data.imageRect;
			drawRect.left = (int) (drawRect.left / imageScale - mScale.fromX);
			drawRect.top = (int) (drawRect.top / imageScale - mScale.fromY);
			drawRect.right = (int) (Math.ceil(drawRect.right / imageScale) - mScale.fromX);
			drawRect.bottom = (int) (Math.ceil(drawRect.bottom / imageScale) - mScale.fromY);
			canvas.drawBitmap(data.bitmap, data.srcRect, drawRect, null);
		}

		Log.d("countTime", "draw " + (SystemClock.uptimeMillis() - startTime));
		startTime = SystemClock.uptimeMillis();

		canvas.restoreToCount(saveCount);
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	@Override
	public void onBlockImageLoadFinished() {
		notifyInvalidate();
	}

	@Override
	public void onImageLoadFinished(int imageWidth, int imageHeight) {
		notifyInvalidate();
	}
}
