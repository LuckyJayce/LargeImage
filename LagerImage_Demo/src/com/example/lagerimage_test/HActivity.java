package com.example.lagerimage_test;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;

import com.shizhefei.view.largeimage.LargeImageView;

public class HActivity extends Activity {
	private LargeImageView imageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_h);


		imageView = (LargeImageView) findViewById(R.id.imageView);
		try {
			imageView.setImage(getAssets().open("aaa.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
