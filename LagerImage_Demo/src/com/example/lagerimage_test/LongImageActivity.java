package com.example.lagerimage_test;

import java.io.IOException;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;

import com.shizhefei.view.largeimage.LargeImageView;

public class LongImageActivity extends Activity {
	public static final int REQUESTCODE_SELECTIMAGE = 666;
	private LargeImageView imageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_longimage);

		findViewById(R.id.button1).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					// 选择照片的时候也一样，我们用Action为Intent.ACTION_GET_CONTENT，
					// 有些人使用其他的Action但我发现在有些机子中会出问题，所以优先选择这个
					Intent intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(intent, REQUESTCODE_SELECTIMAGE);
				} catch (ActivityNotFoundException e) {
					e.printStackTrace();
				}
			}
		});

		imageView = (LargeImageView) findViewById(R.id.imageView);
		try {
			imageView.setImage(getAssets().open("ccc.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		// imageView.setScale(2.0f, imageView.getImageWidth(),
		// imageView.getImageHeight());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUESTCODE_SELECTIMAGE && resultCode == RESULT_OK) {
			if (data != null) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String picturePath = cursor.getString(columnIndex);
				imageView.setImage(picturePath);
			}
		}

	}
}
