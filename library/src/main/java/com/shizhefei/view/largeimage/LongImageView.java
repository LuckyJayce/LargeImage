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
