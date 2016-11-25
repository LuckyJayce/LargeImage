package com.example.lagerimage_test;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.shizhefei.view.largeimage.LargeImageView;


public class SingleDemoActivity extends FragmentActivity {
    private LargeImageView largeImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singledemo);

        largeImageView = (LargeImageView) findViewById(R.id.imageView);
        largeImageView.setImage(R.drawable.mvc);

//        try {
//            BitmapFactory.Options options =  new BitmapFactory.Options();
//            options.inSampleSize = 4;
//            Rect outPadding = new Rect();
//            Bitmap d = BitmapFactory.decodeStream(getAssets().open("mvc.png"),outPadding,options);
//
//            imageView.setImage(new InputStreamBitmapDecoderFactory(getAssets().open("mvc.png")), new BitmapDrawable(getResources(),d));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
