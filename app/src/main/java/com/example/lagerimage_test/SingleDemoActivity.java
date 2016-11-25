package com.example.lagerimage_test;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.shizhefei.view.largeimage.LargeImageView;
import com.shizhefei.view.largeimage.factory.InputStreamBitmapDecoderFactory;

import java.io.IOException;
import java.io.InputStream;

import static com.example.lagerimage_test.R.id.imageView;


public class SingleDemoActivity extends FragmentActivity {
    private LargeImageView largeImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singledemo);

        largeImageView = (LargeImageView) findViewById(imageView);
        //        largeImageView.setImage(R.drawable.mvc);

        try {
            InputStream inputStream = getAssets().open("111.jpg");
            largeImageView.setImage(new InputStreamBitmapDecoderFactory(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
